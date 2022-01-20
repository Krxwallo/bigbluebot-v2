@file:Suppress("EXPERIMENTAL_API_USAGE")

import commands.registerCommands
import config.*
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.any
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.channel.TopGuildMessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import web.conference
import web.startBrowser
import web.stopBrowser
import java.util.*
import kotlin.concurrent.timer

val logger: Logger = LoggerFactory.getLogger("bigbluebot")

lateinit var kord: Kord
lateinit var testGuild: Guild
lateinit var debugChannel: TextChannel
lateinit var messagesChannel: TopGuildMessageChannel
lateinit var updateStatusTask: Timer

suspend fun main() = runBlocking {
    loadConfigs()

    kord = Kord(secrets.token)
    testGuild = kord.getGuild(Snowflake(discordConfig.testGuild))!!
    debugChannel = testGuild.getChannelOf(Snowflake(discordConfig.debugChannel))
    messagesChannel = testGuild.getChannelOf(Snowflake(discordConfig.messagesChannel))

    registerCommands()

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

    // Selenium
    launch(Dispatchers.IO) { startBrowser() }

    logger.info("Logging in...")
    kord.login()
}

suspend fun guildMember(id: String) = testGuild.getMember(Snowflake(id))

private suspend fun linkedUsers(): String {
    val string = StringBuilder("Linked users: (${users.size})\n")
    users.forEach { (id, bbbName) ->
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
                    
                    Current conference: **$conference**
                    Users in conference: **${web.users.size}**
                    Talking users: **${web.users.filterValues { !it }.size}**
                    Muted users: **${web.users.filterValues { it }.size}**
                    
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

    if (messages.status == null) messages.status = messagesChannel.createEmbed { embed() }.id.toString()
    else {
        try {
            val msg = messagesChannel.getMessage(Snowflake(messages.status!!))
            msg.edit { embed { embed() } }
        }
        catch (e: Exception) {
            logger.warn("Status Message not found")
            messages.status = messagesChannel.createEmbed { embed() }.id.toString()
        }
    }
}

suspend fun shutdown() {
    logger.info("Shutting down...")
    updateStatusTask.cancel()
    updateStatus(true)
    saveConfigs()
    kord.shutdown()
    stopBrowser()
    logger.info("Bye")
}