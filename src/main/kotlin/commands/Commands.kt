package commands

import dev.kord.core.entity.interaction.CommandInteraction
import dev.kord.rest.builder.interaction.ApplicationCommandCreateBuilder

data class Command(
    val name: String,
    val description: String,
    val builder: ApplicationCommandCreateBuilder.() -> Unit = {},
    val execution: (CommandInteraction) -> Unit = {}
)

val commands = listOf(
    Command("test", "A test command from the bigbluebot", {}) {
        println("TEST EXECUTION")
    }
)