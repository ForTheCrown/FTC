plugins {
  java
}

repositories {
  maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":menus"))
  compileOnly(project(":discord"))
  compileOnly("com.discordsrv:discordsrv:1.26.0")
}

pluginYml {
  main = "net.forthecrown.antigrief.AntiGriefPlugin"
  name = "FTC-AntiGrief"

  depends {
    required("DiscordSRV")
  }
}