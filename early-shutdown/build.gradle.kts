dependencies {
  compileOnly(project(":commons"))
}

val thisProj = project;

pluginYml {
  main = "net.forthecrown.earlyshutdown.EarlyShutdownPlugin"
  name = "FTC-EarlyShutdown"

  loadAfter {
    val root = rootProject;
    root.subprojects {
      if (this.equals(thisProj)) {
        return@subprojects
      }

      regular("project:${this.name}")
    }
  }
}