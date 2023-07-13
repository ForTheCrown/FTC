plugins {
  `java-library`
}

repositories {
  maven("https://nexus.scarsz.me/content/groups/public/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":user-titles"))

  api("com.discordsrv:discordsrv:1.26.0")
  compileOnly("org.apache.logging.log4j:log4j-core:2.19.0")
}

pluginYml {
  name = "FTC-DiscordService"
  main = "net.forthecrown.discord.DiscordPlugin"

  depends {
    required("DiscordSRV")
    optional("project:user-titles")
  }
}