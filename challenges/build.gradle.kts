plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":scripting"))
  compileOnly(project(":menus"))
  compileOnly(project(":sell-shop"))
}

pluginYml {
  name = "FTC-Challenges"
  main = "net.forthecrown.challenges.ChallengesPlugin"

  openClassLoader = true

  depends {
    optional("VotingPlugin")
    optional("project:shops")
  }
}