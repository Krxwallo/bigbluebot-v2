package de.lookonthebrightsi.web.entities

import de.lookonthebrightsi.conferenceChatChannel
import de.lookonthebrightsi.web.conference
import dev.kord.core.behavior.channel.createEmbed
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

data class ChatMessage(val sender: String, val content: String, val time: String)

suspend fun ChatMessage.sendToDiscord() {
    conferenceChatChannel.createEmbed {
        title = sender
        description = content
        footer { text = "At $time in ${conference?.name}" }
    }
}

fun WebElement.toChatMessage(): ChatMessage {
    val contentElement = findElement(By.xpath("./../.."))
    val name = contentElement.findElements(By.tagName("span"))[0].text
    val time = contentElement.findElement(By.tagName("time")).text
    return ChatMessage(name, text, time)
}