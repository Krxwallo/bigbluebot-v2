@file:Suppress("EXPERIMENTAL_API_USAGE")

import commands.registerCommands
import config.discordConfig
import config.loadConfigs
import config.saveConfigs
import config.secrets
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.any
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import web.startBrowser
import web.stopBrowser
import kotlin.concurrent.thread

val logger: Logger = LoggerFactory.getLogger("bigbluebot")

lateinit var kord: Kord
lateinit var testGuild: Guild
lateinit var debugChannel: TextChannel

suspend fun main() {
    loadConfigs()

    kord = Kord(secrets.token)
    testGuild = kord.getGuild(Snowflake(discordConfig.testGuild))!!
    debugChannel = testGuild.getChannelOf(Snowflake(discordConfig.debugChannel))

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
    }

    // Selenium
    thread { startBrowser() }

    logger.info("Logging in...")
    kord.login()
}

suspend fun shutdown() {
    logger.info("Shutting down...")
    saveConfigs()
    kord.shutdown()
    stopBrowser()
    logger.info("Bye")
}