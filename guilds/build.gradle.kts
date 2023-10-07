plugins {
  java
}

repositories {
  mavenCentral()
  maven("https://nexus.scarsz.me/content/groups/public/")

  // Dynmap
  maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":menus"))
  compileOnly(project(":discord"))
  compileOnly(project(":waypoints"))
  compileOnly(project(":anti-grief"))
}

pluginYml {
  name = "FTC-Guilds"
  main = "net.forthecrown.guilds.GuildPlugin"
}