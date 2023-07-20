dependencies {
  compileOnly(project(":commons"))
}

pluginYml {
  main = "net.forthecrown.earlyshutdown.EarlyShutdownPlugin"
  name = "FTC-EarlyShutdown"

  loadAfter {
    val root = rootProject
    root.subprojects {
      regular("project:${this.name}")
    }
  }
}