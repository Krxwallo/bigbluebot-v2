@file:Suppress("unused")

package web

import config.Moodle

fun moodle(path: String) = "${config.moodle.moodleSite}/$path"

val Moodle.loginPage get() = moodle("login/index.php")