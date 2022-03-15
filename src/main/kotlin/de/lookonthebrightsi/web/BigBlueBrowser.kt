package de.lookonthebrightsi.web

import de.lookonthebrightsi.config.secrets
import de.lookonthebrightsi.kord.resetDiscordUsers
import de.lookonthebrightsi.kord.update
import de.lookonthebrightsi.logger
import de.lookonthebrightsi.web.entities.Conference
import de.lookonthebrightsi.web.entities.sendToDiscord
import de.lookonthebrightsi.web.entities.toChatMessage
import io.github.bonigarcia.wdm.WebDriverManager
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchSessionException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import java.util.*

lateinit var driver: FirefoxDriver
lateinit var task: Timer
var courses = hashMapOf<String, String>() // name to href
var conference: Conference? = null // Currently in no conference

private suspend fun clearTabs() {
    driver.windowHandles.forEachIndexed { index, s ->
        if (index != 0) driver.switchTo().window(s).close()
    }
    conference = null
    driver.switchTo().window(driver.windowHandles.first())

    resetDiscordUsers() // Unmute discord ppl
}

private suspend fun login() {
    driver.get(de.lookonthebrightsi.config.moodle.loginPage)

    delay(1000)

    driver.findElement(By.id("username")).sendKeys(secrets.moodleUser)
    driver.findElement(By.id("password")).sendKeys(secrets.moodlePassword)
    driver.findElement(By.id("loginbtn")).click()
    driver.findElements(By.cssSelector("a.list-group-item")).filter { "course" in it.getDomAttribute("href") }.forEach {
        courses += it.text to it.getDomAttribute("href")
    }
}

suspend fun startBrowser() = coroutineScope {
    logger.info("Starting firefox browser")
    WebDriverManager.firefoxdriver().setup()
    driver = FirefoxDriver(FirefoxOptions().apply {
        setHeadless(false)
    })
    login()

    while (true) {
        try {
            // Update conference stuff etc
            val webElements = driver.findElements(By.cssSelector("div[class^=\"userItemContents\"]"))
            if (webElements.size == 1) {
                if (conference?.users?.isNotEmpty() == true) conference!!.users.clear()
            } else {
                conference?.users?.let { bbbUsers ->
                    webElements.forEach {
                        val name = it.findElement(By.cssSelector("span[class^=\"userNameMain\"]")).text.trimEnd()
                        val avatarClasses =
                            it.findElement(By.cssSelector("div[class^=\"avatar\"]")).getDomAttribute("class")
                        val muted =
                            "noVoice" in avatarClasses || "listenOnly" in avatarClasses || "muted" in avatarClasses
                        if ("(" !in name) {
                            // Assign muted value
                            if (name !in bbbUsers) bbbUsers.update(name, muted)
                            else if (bbbUsers[name] != muted) bbbUsers.update(name, muted)
                            // Don't update when the status didn't change
                        }
                    }
                }
                conference?.lastMsg?.let { lastMsg ->
                    val messageElements = driver.findElements(By.cssSelector("p[class^=\"message\"]"))
                    var found = false
                    messageElements.forEach { msg ->
                        if (found) msg.toChatMessage().apply { conference?.lastMsg = this }.takeUnless { it == lastMsg }?.sendToDiscord()
                        else if (msg.toChatMessage() == lastMsg) found = true
                    }
                } ?: run {
                    conference?.lastMsg = driver.findElements(By.cssSelector("p[class^=\"message\"]")).lastOrNull()?.toChatMessage()
                }
            }
            delay(100)
        }
        catch (e: WebDriverException) {
            logger.warn("Driver exception: ${e.message}")
            if (e is NoSuchSessionException) break
        }
    }
}

/** @return null when no error occurred / the error string */
suspend fun joinConference(name: String): String? {
    clearTabs()
    conference = null
    if (name !in courses) return "Course not found"
    val course = courses[name]
    driver.get(course)
    val conferences = driver.findElements(By.className("bigbluebuttonbn"))
    if (conferences.isEmpty()) return "No bigbluebutton conference found in $name"
    if (conferences.size != 1) logger.warn("Found multiple conferences in $name")
    conferences[0].findElement(By.tagName("a")).click()
    driver.findElement(By.id("join_button_input")).click()
    conference = Conference(name)
    driver.switchTo().window(driver.windowHandles.elementAt(1)) // Switch to conference screen
    return null
}

/** @return old conference string when conference was left / null when there was no conference to leave */
suspend fun leaveConference(): Conference? = conference?.apply {
    clearTabs()
}

fun stopBrowser() {
    conference = null
    driver.quit()
}