package net.forthecrown.gradle

import org.gradle.api.Project

class FtcExtension(private val project: Project) {

  val apiVersion: String get() = API_VERSION

  fun useVanilla() {
  }
}