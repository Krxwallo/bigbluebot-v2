package de.lookonthebrightsi.kord

import de.lookonthebrightsi.*
import de.lookonthebrightsi.config.discordUsers
import de.lookonthebrightsi.config.messages
import de.lookonthebrightsi.web.conference
import de.lookonthebrightsi.web.startBrowser
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.common.exception.RequestException
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.reply
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.concurrent.timer

suspend fun loginKord() {
    bot.on<MessageCreateEvent> {
        if (message.mentionedUsers.firstOrNull { it.id == kord.selfId } != null)
            message.reply {
                content = "Hör auf mich zu pingen sonst bann ich dich"
            }
    }

    bot.on<ReadyEvent> {
        launch(Dispatchers.IO) { startBrowser() }

        updateStatusTask = timer(period = 3000L) {
            launch { updateStatus() }
        }

        kord.editPresence {
            listening("dem Lehrer")
        }
    }

    bot.start()
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

    if (messages.status == null) messages.status = statusChannel.createEmbed { embed() }.id.asString
    else {
        try {
            val msg = statusChannel.getMessage(Snowflake(messages.status!!))
            msg.edit { embed { embed() } }
        }
        catch (e: EntityNotFoundException) {
            logger.warn("Status Message not found")
            messages.status = statusChannel.createEmbed { embed() }.id.asString
        }
        catch (e: RequestException) {
            logger.warn("Error getting status message: ${e.stackTraceToString()}")
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