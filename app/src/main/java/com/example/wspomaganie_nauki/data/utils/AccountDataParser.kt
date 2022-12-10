package com.example.wspomaganie_nauki.data.utils

class AccountDataParser {

    companion object {
        private var accountEmail: String = ""

        fun getEmail() : String = accountEmail
        fun setEmail(newEmail: String) { accountEmail = newEmail }

        fun getDeckID(deckTitle: String) : String = "$accountEmail|$deckTitle"

        fun getEmailFromID(id: String) : String = id.substringBefore("|")

        fun getDeckTitleFromID(id: String) : String = id.substringAfter("|")
    }
}