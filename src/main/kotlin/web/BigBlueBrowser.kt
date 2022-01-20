package web

import config.secrets
import io.github.bonigarcia.wdm.WebDriverManager
import logger
import org.openqa.selenium.By
import org.openqa.selenium.firefox.FirefoxDriver
import java.util.*
import kotlin.concurrent.timer

lateinit var driver: FirefoxDriver
lateinit var task: Timer
var courses = hashMapOf<String, String>() // name to href
var conference: String? = null // Currently in no conference
val users: HashMap<String, Boolean> = hashMapOf() // name to muted

private fun clearTabs() {
    driver.windowHandles.forEachIndexed { index, s ->
        if (index != 0) driver.switchTo().window(s).close()
    }
    conference = null
    driver.switchTo().window(driver.windowHandles.first())
}

private fun login() {
    driver.get(config.moodle.loginPage)
    driver.findElement(By.id("username")).sendKeys(secrets.moodleUser)
    driver.findElement(By.id("password")).sendKeys(secrets.moodlePassword)
    driver.findElement(By.id("loginbtn")).click()
    driver.findElements(By.cssSelector("a.list-group-item")).filter { "course" in it.getDomAttribute("href") }.forEach {
        courses += it.text to it.getDomAttribute("href")
    }
}

suspend fun startBrowser() {
    logger.info("Starting firefox browser")
    WebDriverManager.firefoxdriver().setup()
    driver = FirefoxDriver()
    login()

    task = timer(period = 100L) {
        // Update conference stuff etc
        val webElements = driver.findElements(By.cssSelector("div[class^=\"userItemContents\"]"))
        driver.findElements(By.className("icon_bbb_close")).forEach {
            // Close popup
            it.click()
        }
        if (webElements.size == 1) {
            if (users.isNotEmpty()) users.clear()
        }
        else {
            webElements.forEach {
                val name = it.findElement(By.cssSelector("span[class^=\"userNameMain\"]")).text
                val avatarClasses = it.findElement(By.cssSelector("div[class^=\"avatar\"]")).getDomAttribute("class")
                val muted = "noVoice" in avatarClasses || "listenOnly" in avatarClasses || "muted" in avatarClasses
                if ("(" !in it.text) {
                    // Assign muted value
                    if (it.text !in users) {
                        users[it.text] = muted

                    }
                    users[it.text] = muted

                    // TODO update mute on discord
                }
            }
        }
    }
}

/** @return null when no error occurred / the error string */
fun joinConference(name: String): String? {
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
    conference = name
    driver.switchTo().window(driver.windowHandles.elementAt(1)) // Switch to conference screen
    return null
}

/** @return old conference string when conference was left / null when there was no conference to leave */
fun leaveConference(): String? = conference?.apply { clearTabs() }

fun stopBrowser() {
    task.cancel()
    conference = null
    driver.quit()
}