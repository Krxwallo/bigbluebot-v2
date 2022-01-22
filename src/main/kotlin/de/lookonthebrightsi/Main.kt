@file:Suppress("EXPERIMENTAL_API_USAGE")

package de.lookonthebrightsi

import de.lookonthebrightsi.config.discordConfig
import de.lookonthebrightsi.config.loadConfigs
import de.lookonthebrightsi.config.saveConfigs
import de.lookonthebrightsi.config.secrets
import de.lookonthebrightsi.kord.commands.registerCommands
import de.lookonthebrightsi.kord.loginKord
import de.lookonthebrightsi.kord.resetDiscordUsers
import de.lookonthebrightsi.kord.updateStatus
import de.lookonthebrightsi.web.startBrowser
import de.lookonthebrightsi.web.stopBrowser
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.getChannelOf
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.TopGuildMessageChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

val logger: Logger = LoggerFactory.getLogger("bigbluebot")

lateinit var kord: Kord
lateinit var testGuild: Guild
lateinit var debugChannel: TopGuildMessageChannel
lateinit var statusChannel: TopGuildMessageChannel
lateinit var conferenceChatChannel: TopGuildMessageChannel
lateinit var updateStatusTask: Timer

suspend fun main() = coroutineScope {
    loadConfigs()

    kord = Kord(secrets.token)
    testGuild = kord.getGuild(Snowflake(discordConfig.testGuild))!!
    debugChannel = testGuild.getChannelOf(Snowflake(discordConfig.debugChannel))
    statusChannel = testGuild.getChannelOf(Snowflake(discordConfig.statusChannel))
    conferenceChatChannel = testGuild.getChannelOf(Snowflake(discordConfig.conferenceChatChannel))

    registerCommands()

    // Selenium
    launch(Dispatchers.IO) { startBrowser() }

    logger.info("Logging in...")
    loginKord()
}

suspend fun shutdown() {
    logger.info("Shutting down...")
    updateStatusTask.cancel()
    updateStatus(true)
    saveConfigs()
    stopBrowser()
    resetDiscordUsers() // Unmute discord ppl
    kord.shutdown()
    logger.info("Bye")
}