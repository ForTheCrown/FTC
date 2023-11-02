plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-Leaderboards"
  main = "net.forthecrown.leaderboards.LeaderboardPlugin"

  loadAfter {
    regular("project:vanilla-hook")
  }
}