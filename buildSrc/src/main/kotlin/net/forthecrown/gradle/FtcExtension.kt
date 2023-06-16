package net.forthecrown.gradle

class FtcExtension {

  var useVanilla: Boolean = false

  val apiVersion: String get() = API_VERSION
}