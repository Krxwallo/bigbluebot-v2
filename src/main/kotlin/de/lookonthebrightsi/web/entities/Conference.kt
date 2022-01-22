package de.lookonthebrightsi.web.entities

/**
 * Class that represents a BBB conference.
 * -> users
 * -> chat messages
 * -> etc.
 */
data class Conference(val name: String, val users: HashMap<String, Boolean> = hashMapOf(), var lastMsg: ChatMessage? = null)
