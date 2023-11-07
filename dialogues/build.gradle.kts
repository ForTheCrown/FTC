plugins {
  java
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":scripting"))
  compileOnly(project(":usables"))
}

pluginYml {
  name = "FTC-Dialogues"
  main = "net.forthecrown.dialogues.DialoguesPlugin"
}