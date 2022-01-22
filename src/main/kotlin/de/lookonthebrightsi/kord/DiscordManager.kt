package de.lookonthebrightsi.kord

import de.lookonthebrightsi.*
import de.lookonthebrightsi.config.discordUsers
import de.lookonthebrightsi.config.messages
import de.lookonthebrightsi.web.conference
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.any
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.reply
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.launch
import kotlin.concurrent.timer

suspend fun loginKord() {
    kord.on<MessageCreateEvent> {
        if (message.mentionedUsers.any { it.id == kord.selfId })
            message.reply {
                content = "Hör auf mich zu pingen sonst bann ich dich"
            }
    }

    kord.on<ReadyEvent> {
        kord.editPresence {
            listening("dem Lehrer")
        }
        updateStatusTask = timer(period = 3000L) {
            launch { updateStatus() }
        }
    }

    kord.login()
}

suspend fun resetDiscordUsers() = discordUsers.forEach {
    testGuild.getMember(Snowflake(it.key)).edit {
        muted = false
        deafened = false
    }
}

suspend fun guildMember(id: String) = testGuild.getMember(Snowflake(id))

private suspend fun linkedUsers(): String {
    val string = StringBuilder("Linked users: (${discordUsers.size})\n")
    discordUsers.forEach { (id, bbbName) ->
        string.append("- ${guildMember(id).mention}: $bbbName\n")
    }
    return "$string-> Link your discord and BigBlueButton account with `/link`!"
}

suspend fun updateStatus(offline: Boolean = false) {
    suspend fun EmbedBuilder.embed() {
        color = if (offline) Color(255,0, 0) else Color(0, 255, 0)
        title = "BigBlueBot Status"
        description = if (!offline) """
                    ${linkedUsers()}
                    
                    Current conference: **${conference?.name ?: "None"}**
                    Users in conference: **${conference?.users?.size ?: 0}**
                    Talking users: **${conference?.users?.filterValues { !it }?.size ?: 0}**
                    Muted users: **${conference?.users?.filterValues { it }?.size ?: 0}**
                    
                    -------------------------------
                    
                    Developed by [Krxwallo](https://github.com/Krxwallo/) | [Source Code](https://github.com/Krxwallo/bigbluebot-v2)
                """.trimIndent() else """
                    **-- OFFLINE --**
                    
                    ${linkedUsers()}
                    
                    -------------------------------
                    
                    Developed by [Krxwallo](https://github.com/Krxwallo/) | [Source Code](https://github.com/Krxwallo/bigbluebot-v2)
                """.trimIndent()
        footer {
            text = "BigBlueBot v2 • This message gets updated every 3 seconds"
        }
    }

    if (messages.status == null) messages.status = statusChannel.createEmbed { embed() }.id.toString()
    else {
        try {
            val msg = statusChannel.getMessage(Snowflake(messages.status!!))
            msg.edit { embed { embed() } }
        }
        catch (e: Exception) {
            logger.warn("Status Message not found")
            messages.status = statusChannel.createEmbed { embed() }.id.toString()
        }
    }
}

suspend fun HashMap<String, Boolean>.update(name: String, mutedValue: Boolean) {
    this[name] = mutedValue
    // Maybe update discord mute status
    if (discordUsers.containsValue(name)) discordUsers.forEach {
        if (it.value == name) testGuild.getMember(Snowflake(it.key)).edit {
            muted = !mutedValue
        }
    }
}