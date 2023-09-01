plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":scripting"))
}

pluginYml {
  name = "FTC-Mail"
  main = "net.forthecrown.mail.MailPlugin"
}