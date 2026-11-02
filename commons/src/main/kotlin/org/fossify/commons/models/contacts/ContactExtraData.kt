package org.fossify.commons.models.contacts

import kotlinx.serialization.Serializable

// Fork (sk11): everything a system contact can hold that the Fossify Contact model had no field for,
// carried as part of the contact itself — in Contact.extra, and in the private store's `extra` column —
// so a contact moves between System and Private, and through a vCard backup, without losing anything.

/** A relationship row (ContactsContract Relation): who, and how they are related. */
@Serializable
data class Relation(var name: String, var type: Int, var label: String = "")

/** An internet-call (SIP) address row (ContactsContract SipAddress). */
@Serializable
data class SipAddress(var value: String, var type: Int, var label: String = "")

/**
 * One provider data row kept verbatim: mimetype + its data1…data15 / is_primary / is_super_primary
 * columns as strings (a blob as "b64:<base64>"). Two uses:
 * - a row of a kind with no model field at all (Identity, another app's own kind…) — written back as is;
 * - the columns of a modelled kind's row that the model does not carry (an e-mail's primary flag, an
 *   IM's own type, a website's type, the second organization…) — patched back onto the matching row.
 * [index] is the row's position among the contact's rows of that mimetype.
 */
@Serializable
data class DataRow(var mimetype: String, var index: Int, var values: Map<String, String> = emptyMap())

@Serializable
data class ContactExtraData(
    // The reading (フリガナ) of the name parts.
    var phoneticGiven: String = "",
    var phoneticMiddle: String = "",
    var phoneticFamily: String = "",
    var relations: ArrayList<Relation> = arrayListOf(),
    var sipAddresses: ArrayList<SipAddress> = arrayListOf(),
    // The organization's columns beyond company and job title.
    var department: String = "",
    var jobDescription: String = "",
    var officeLocation: String = "",
    var companyPhonetic: String = "",
    var symbol: String = "",
    var sendToVoicemail: Boolean = false,
    var rows: ArrayList<DataRow> = arrayListOf(),
    // True once read from the provider (or built for a new contact). An update of a system contact loaded
    // without its extras (a list row) must not wipe what it never read — ContactsHelper checks this.
    var loaded: Boolean = false,
) {
    fun hasPhonetic() = phoneticGiven.isNotEmpty() || phoneticMiddle.isNotEmpty() || phoneticFamily.isNotEmpty()
    fun hasOrganizationExtra() =
        department.isNotEmpty() || jobDescription.isNotEmpty() || officeLocation.isNotEmpty() || companyPhonetic.isNotEmpty() || symbol.isNotEmpty()
}
