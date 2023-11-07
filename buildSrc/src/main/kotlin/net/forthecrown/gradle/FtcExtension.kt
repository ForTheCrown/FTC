package net.forthecrown.gradle

import org.gradle.api.Project

class FtcExtension(private val project: Project) {

  val apiVersion: String get() = MC_VERSION
  var apiFor: String? = null;
  var skipDependency: Boolean = false;
  var autoAddDependencies: Boolean = true;

  fun useVanilla() {
  }

  fun implementedBy(projectName: String) {
    apiFor = projectName;
  }
}