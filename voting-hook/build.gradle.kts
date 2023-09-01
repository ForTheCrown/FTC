plugins {
  java
}

repositories {
  mavenCentral()

  // VotingPlugin
  maven("https://nexus.bencodez.com/repository/maven-public")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly("com.bencodez:votingplugin:6.11.3")
}

pluginYml {
  name = "FTC-VotingPluginHook"
  main = "net.forthecrown.voting.VotingPlugin"

  depends {
    required("VotingPlugin")
  }
}