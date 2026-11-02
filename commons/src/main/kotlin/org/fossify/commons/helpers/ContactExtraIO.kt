package org.fossify.commons.helpers

import android.content.ContentProviderOperation
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import android.telephony.PhoneNumberUtils
import android.util.Base64
import org.fossify.commons.models.contacts.Contact
import org.fossify.commons.models.contacts.ContactExtraData
import org.fossify.commons.models.contacts.DataRow
import org.fossify.commons.models.contacts.Relation
import org.fossify.commons.models.contacts.SipAddress

/**
 * Fork (sk11): reads and writes Contact.extra (ContactExtraData) for system contacts — everything a
 * provider raw contact holds that the Fossify model has no field for. ContactsHelper calls [read] when it
 * loads one contact (and [readAll] for an export), and [write] after every insert and update, so a
 * contact keeps all of it through an edit, a move between System and Private, and a vCard round trip.
 *
 * What the model already writes (MODEL_COLUMNS) always comes from the model, so an edit wins; every
 * other column of those rows is patched back from the verbatim rows kept in extra.rows.
 */
object ContactExtraIO {
    private const val BLOB_PREFIX = "b64:"

    private val DATA_COLUMNS = arrayOf(
        Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7, Data.DATA8,
        Data.DATA9, Data.DATA10, Data.DATA11, Data.DATA12, Data.DATA13, Data.DATA14, Data.DATA15,
    )

    private val NAME = CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
    private val ORG = CommonDataKinds.Organization.CONTENT_ITEM_TYPE
    private val RELATION = CommonDataKinds.Relation.CONTENT_ITEM_TYPE
    private val SIP = CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE

    // Carried elsewhere: the photo by the model, group memberships by the groups list.
    private val NOT_KEPT = setOf(CommonDataKinds.Photo.CONTENT_ITEM_TYPE, CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)

    // Kinds the model holds exactly one of; further rows are kept verbatim and written back in full.
    private val SINGLE_KINDS = setOf(CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE, ORG)

    // Columns the model (plus the first-class fields of ContactExtraData) owns, per kind.
    private val MODEL_COLUMNS: Map<String, Set<String>> = mapOf(
        NAME to setOf(Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7, Data.DATA8, Data.DATA9, Data.DATA10, Data.DATA11),
        CommonDataKinds.Phone.CONTENT_ITEM_TYPE to setOf(Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.IS_PRIMARY),
        CommonDataKinds.Email.CONTENT_ITEM_TYPE to setOf(Data.DATA1, Data.DATA2, Data.DATA3),
        CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE to setOf(
            Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7, Data.DATA8, Data.DATA9, Data.DATA10,
        ),
        CommonDataKinds.Im.CONTENT_ITEM_TYPE to setOf(Data.DATA1, Data.DATA5, Data.DATA6),
        CommonDataKinds.Event.CONTENT_ITEM_TYPE to setOf(Data.DATA1, Data.DATA2, Data.DATA3),
        CommonDataKinds.Website.CONTENT_ITEM_TYPE to setOf(Data.DATA1),
        // company, title, department, job description, symbol, phonetic name, office location
        ORG to setOf(Data.DATA1, Data.DATA4, Data.DATA5, Data.DATA6, Data.DATA7, Data.DATA8, Data.DATA9),
        CommonDataKinds.Nickname.CONTENT_ITEM_TYPE to setOf(Data.DATA1),
        CommonDataKinds.Note.CONTENT_ITEM_TYPE to setOf(Data.DATA1),
        RELATION to setOf(Data.DATA1, Data.DATA2, Data.DATA3),
        SIP to setOf(Data.DATA1, Data.DATA2, Data.DATA3),
    )

    // ---- read ---------------------------------------------------------------------------------------

    /** The extra data of one raw contact. Blocking. */
    fun read(context: Context, rawId: Int): ContactExtraData = readAll(context, setOf(rawId))[rawId] ?: ContactExtraData(loaded = true)

    /** The extra data of these raw contacts (null = all), keyed by raw id. One pass over Data. Blocking. */
    fun readAll(context: Context, rawIds: Set<Int>?): Map<Int, ContactExtraData> {
        val result = HashMap<Int, ContactExtraData>()
        val perKind = HashMap<Int, HashMap<String, Int>>()
        val columns = arrayOf(Data.RAW_CONTACT_ID, Data.MIMETYPE, Data.IS_PRIMARY, Data.IS_SUPER_PRIMARY) + DATA_COLUMNS
        val (selection, args) = idSelection(Data.RAW_CONTACT_ID, rawIds)
        try {
            context.contentResolver.query(Data.CONTENT_URI, columns, selection, args, "${Data._ID} ASC")?.use { cursor ->
                while (cursor.moveToNext()) {
                    val rawId = cursor.getInt(0)
                    val mimetype = cursor.getString(1) ?: continue
                    if (mimetype in NOT_KEPT) continue
                    val counts = perKind.getOrPut(rawId) { HashMap() }
                    val index = counts[mimetype] ?: 0
                    counts[mimetype] = index + 1

                    val values = HashMap<String, String>()
                    for (i in 2 until columns.size) {
                        readValue(cursor, i)?.let { values[columns[i]] = it }
                    }
                    absorb(result.getOrPut(rawId) { ContactExtraData(loaded = true) }, mimetype, index, values)
                }
            }
            val (rcSelection, rcArgs) = idSelection(RawContacts._ID, rawIds)
            context.contentResolver.query(
                RawContacts.CONTENT_URI, arrayOf(RawContacts._ID, RawContacts.SEND_TO_VOICEMAIL), rcSelection, rcArgs, null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    if (cursor.getInt(1) != 0) {
                        result.getOrPut(cursor.getInt(0)) { ContactExtraData(loaded = true) }.sendToVoicemail = true
                    }
                }
            }
        } catch (ignored: Exception) {
        }
        return result
    }

    private fun idSelection(column: String, ids: Set<Int>?): Pair<String?, Array<String>?> =
        if (ids == null) null to null else "$column IN (${ids.joinToString(",") { "?" }})" to ids.map { it.toString() }.toTypedArray()

    private fun absorb(extra: ContactExtraData, mimetype: String, index: Int, values: Map<String, String>) {
        when {
            mimetype == NAME && index == 0 -> {
                extra.phoneticGiven = values[CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME].orEmpty()
                extra.phoneticMiddle = values[CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME].orEmpty()
                extra.phoneticFamily = values[CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME].orEmpty()
            }

            mimetype == RELATION -> extra.relations.add(
                Relation(values[Data.DATA1].orEmpty(), values[Data.DATA2]?.toIntOrNull() ?: 0, values[Data.DATA3].orEmpty())
            )

            mimetype == SIP -> extra.sipAddresses.add(
                SipAddress(values[Data.DATA1].orEmpty(), values[Data.DATA2]?.toIntOrNull() ?: 0, values[Data.DATA3].orEmpty())
            )

            mimetype == ORG && index == 0 -> {
                extra.department = values[CommonDataKinds.Organization.DEPARTMENT].orEmpty()
                extra.jobDescription = values[CommonDataKinds.Organization.JOB_DESCRIPTION].orEmpty()
                extra.symbol = values[CommonDataKinds.Organization.SYMBOL].orEmpty()
                extra.companyPhonetic = values[CommonDataKinds.Organization.PHONETIC_NAME].orEmpty()
                extra.officeLocation = values[CommonDataKinds.Organization.OFFICE_LOCATION].orEmpty()
            }
        }

        val modelColumns = MODEL_COLUMNS[mimetype]
        val keepWhole = modelColumns == null || (mimetype in SINGLE_KINDS && index > 0)
        val kept = if (keepWhole) values else values.filter { (column, value) -> column !in modelColumns!! && value.isMeaningful() }
        // A modelled row is kept only for what it has beyond the model — usually nothing at all.
        if (keepWhole || kept.isNotEmpty()) {
            val data1 = values[Data.DATA1]
            val withKey = if (!keepWhole && data1 != null) kept + (Data.DATA1 to data1) else kept
            extra.rows.add(DataRow(mimetype, index, withKey))
        }
    }

    private fun String.isMeaningful() = isNotEmpty() && this != "0"

    private fun readValue(cursor: Cursor, i: Int): String? = when (cursor.getType(i)) {
        Cursor.FIELD_TYPE_NULL -> null
        Cursor.FIELD_TYPE_BLOB -> BLOB_PREFIX + Base64.encodeToString(cursor.getBlob(i), Base64.NO_WRAP)
        else -> cursor.getString(i)
    }

    private fun ContentValues.putStored(column: String, value: String) {
        if (value.startsWith(BLOB_PREFIX)) put(column, Base64.decode(value.removePrefix(BLOB_PREFIX), Base64.NO_WRAP)) else put(column, value)
    }

    // ---- write --------------------------------------------------------------------------------------

    /**
     * Write [contact]'s extra onto its raw contact [rawId], after commons has written the model fields.
     * [isNewRawContact]: a fresh insert also gets the verbatim rows of unmodelled kinds; on an update those
     * rows were never touched and are still there. Blocking; a value that will not write is skipped.
     */
    fun write(context: Context, rawId: Int, contact: Contact, isNewRawContact: Boolean) {
        val extra = contact.extra
        val ops = ArrayList<ContentProviderOperation>()
        val inserted = readRows(context, rawId)
        val id = rawId.toString()

        // Reading on the name row.
        inserted.firstOrNull { it.mimetype == NAME }?.let { name ->
            ops.add(
                ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection("${Data._ID} = ?", arrayOf(name.id.toString()))
                    .withValue(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, extra.phoneticGiven)
                    .withValue(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, extra.phoneticMiddle)
                    .withValue(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, extra.phoneticFamily)
                    .build()
            )
        }

        // Organization details on the organization row — creating one when only the details are filled.
        val orgRow = inserted.firstOrNull { it.mimetype == ORG }
        if (orgRow != null || extra.hasOrganizationExtra()) {
            val builder = if (orgRow != null) {
                ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection("${Data._ID} = ?", arrayOf(orgRow.id.toString()))
            } else {
                ContentProviderOperation.newInsert(Data.CONTENT_URI).withValue(Data.RAW_CONTACT_ID, rawId).withValue(Data.MIMETYPE, ORG)
            }
            ops.add(
                builder.withValue(CommonDataKinds.Organization.DEPARTMENT, extra.department)
                    .withValue(CommonDataKinds.Organization.JOB_DESCRIPTION, extra.jobDescription)
                    .withValue(CommonDataKinds.Organization.SYMBOL, extra.symbol)
                    .withValue(CommonDataKinds.Organization.PHONETIC_NAME, extra.companyPhonetic)
                    .withValue(CommonDataKinds.Organization.OFFICE_LOCATION, extra.officeLocation)
                    .build()
            )
        }

        // Event labels (the model writes date and type; a custom type needs its label).
        contact.events.filter { it.label.isNotEmpty() }.forEach { event ->
            inserted.firstOrNull { it.mimetype == CommonDataKinds.Event.CONTENT_ITEM_TYPE && it.data1 == event.value && it.data2 == event.type.toString() }
                ?.let { row ->
                    ops.add(
                        ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection("${Data._ID} = ?", arrayOf(row.id.toString()))
                            .withValue(CommonDataKinds.Event.LABEL, event.label).build()
                    )
                }
        }

        // Relations and SIP addresses: replaced as a whole.
        for ((mimetype, rows) in listOf(
            RELATION to extra.relations.map { Triple(it.name, it.type, it.label) },
            SIP to extra.sipAddresses.map { Triple(it.value, it.type, it.label) },
        )) {
            ops.add(
                ContentProviderOperation.newDelete(Data.CONTENT_URI)
                    .withSelection("${Data.RAW_CONTACT_ID} = ? AND ${Data.MIMETYPE} = ?", arrayOf(id, mimetype)).build()
            )
            rows.filter { it.first.isNotEmpty() }.forEach { (value, type, label) ->
                ops.add(
                    ContentProviderOperation.newInsert(Data.CONTENT_URI).withValue(Data.RAW_CONTACT_ID, rawId).withValue(Data.MIMETYPE, mimetype)
                        .withValue(Data.DATA1, value).withValue(Data.DATA2, type).withValue(Data.DATA3, label).build()
                )
            }
        }

        // Verbatim rows.
        val used = HashSet<Long>()
        extra.rows.forEach { row ->
            val modelColumns = MODEL_COLUMNS[row.mimetype]
            when {
                modelColumns == null -> if (isNewRawContact) ops.add(insertRow(rawId, row))
                // Further organizations / nicknames / notes: commons rewrote the kind with only the first.
                row.mimetype in SINGLE_KINDS && row.index > 0 -> ops.add(insertRow(rawId, row))
                row.mimetype == RELATION || row.mimetype == SIP -> Unit
                else -> {
                    val target = match(inserted, row, used) ?: return@forEach
                    used.add(target.id)
                    val values = ContentValues()
                    row.values.forEach { (column, value) -> if (column !in modelColumns) values.putStored(column, value) }
                    if (values.size() > 0) {
                        ops.add(
                            ContentProviderOperation.newUpdate(Data.CONTENT_URI).withSelection("${Data._ID} = ?", arrayOf(target.id.toString()))
                                .withValues(values).build()
                        )
                    }
                }
            }
        }

        ops.add(
            ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI).withSelection("${RawContacts._ID} = ?", arrayOf(id))
                .withValue(RawContacts.SEND_TO_VOICEMAIL, if (extra.sendToVoicemail) 1 else 0).build()
        )

        ops.forEach { op ->
            try {
                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, arrayListOf(op))
            } catch (ignored: Exception) {
            }
        }
    }

    private fun insertRow(rawId: Int, row: DataRow): ContentProviderOperation {
        val values = ContentValues()
        row.values.forEach { (column, value) -> values.putStored(column, value) }
        values.put(Data.RAW_CONTACT_ID, rawId)
        values.put(Data.MIMETYPE, row.mimetype)
        return ContentProviderOperation.newInsert(Data.CONTENT_URI).withValues(values).build()
    }

    private class Row(val id: Long, val mimetype: String, val data1: String, val data2: String)

    private fun readRows(context: Context, rawId: Int): List<Row> {
        val rows = ArrayList<Row>()
        try {
            context.contentResolver.query(
                Data.CONTENT_URI, arrayOf(Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2),
                "${Data.RAW_CONTACT_ID} = ?", arrayOf(rawId.toString()), "${Data._ID} ASC"
            )?.use { c ->
                while (c.moveToNext()) rows.add(Row(c.getLong(0), c.getString(1).orEmpty(), c.getString(2).orEmpty(), c.getString(3).orEmpty()))
            }
        } catch (ignored: Exception) {
        }
        return rows
    }

    // The row the model wrote that a kept row describes: the only one of a single kind, else the one with
    // the same value. A value removed in the meantime finds nothing, and stays removed.
    private fun match(inserted: List<Row>, row: DataRow, used: Set<Long>): Row? {
        val candidates = inserted.filter { it.mimetype == row.mimetype && it.id !in used }
        if (row.mimetype in SINGLE_KINDS || row.mimetype == NAME) return candidates.firstOrNull()
        val value = row.values[Data.DATA1].orEmpty()
        return if (row.mimetype == CommonDataKinds.Phone.CONTENT_ITEM_TYPE) {
            candidates.firstOrNull { it.data1 == value || PhoneNumberUtils.compare(it.data1, value) }
        } else {
            candidates.firstOrNull { it.data1 == value }
        }
    }
}
