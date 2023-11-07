plugins {
  java
}

repositories {
  mavenCentral()

  // VotingPlugin
  maven("https://nexus.bencodez.com/repository/maven-public")
  maven("https://nexuslite.gcnt.net/repos/other/")
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

  loadAfter {
    regular("project:leaderboards")
  }
}