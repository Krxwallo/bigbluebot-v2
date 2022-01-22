package de.lookonthebrightsi.kord.commands

import de.lookonthebrightsi.config.discordConfig
import de.lookonthebrightsi.kord
import de.lookonthebrightsi.logger
import de.lookonthebrightsi.testGuild
import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.message.create.embed

data class Command(
    val name: String,
    val description: String,
    val builder: BaseInputChatBuilder.() -> Unit = {},
    val execution: suspend GuildChatInputCommandInteraction.() -> Unit = {}
)

suspend fun registerCommands() {
    kord.globalCommands.collect {
        it.delete()
    }
    if (discordConfig.testMode) kord.createGuildApplicationCommands(testGuild.id) {
        bbbCommands.forEach {
            input(it.name, it.description, it.builder)
            logger.info("Created '${it.name}' command.")
        }
    }
    else kord.createGlobalApplicationCommands {
        bbbCommands.forEach {
            input(it.name, it.description, it.builder)
            logger.debug("Created '${it.name}' command.")
        }
    }

    kord.on<InteractionCreateEvent> {
        val commandInteraction = interaction as? GuildChatInputCommandInteraction ?: return@on
        bbbCommands.forEach {
            if (it.name == commandInteraction.command.rootName) it.execution(commandInteraction)
        }
    }
}

suspend fun GuildChatInputCommandInteraction.hasPermission(permission: Permission): Boolean {
    if (permission !in member.getPermissions()) {
        acknowledgeEphemeral().followUp {
            embed {
                color = Color(255, 0, 0)
                title = "No Permission"
                description = "You need the **${permission::javaClass.name}** permission to execute this command."
            }
        }
        return false
    }
    return true
}
