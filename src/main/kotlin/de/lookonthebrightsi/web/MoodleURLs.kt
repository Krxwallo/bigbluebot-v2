@file:Suppress("unused")

package de.lookonthebrightsi.web

import de.lookonthebrightsi.config.Moodle

fun moodle(path: String) = "${de.lookonthebrightsi.config.moodle.moodleSite}/$path"

val Moodle.loginPage get() = moodle("login/index.php")