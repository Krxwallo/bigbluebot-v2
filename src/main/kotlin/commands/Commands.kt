package commands

import dev.kord.core.behavior.interaction.followUp
import dev.kord.core.entity.interaction.GuildChatInputCommandInteraction
import dev.kord.core.entity.interaction.string
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import dev.kord.rest.builder.message.create.embed

data class Command(
    val name: String,
    val description: String,
    val builder: BaseInputChatBuilder.() -> Unit = {},
    val execution: suspend (GuildChatInputCommandInteraction) -> Unit = {}
)

val commands = listOf(
    Command("link", "Link your BigBlueButton username to BigBlueBot", {
        string("name", "The name you are using in BigBlueButton conferences") { required = true }
    }) {
        val name = it.command.options["name"]?.string()!!
        it.acknowledgeEphemeral().followUp {
            embed {
                title = "Successful Link"
                description = "You successfully linked the BigBlueButton name '$name'"
            }
        }
    }
)