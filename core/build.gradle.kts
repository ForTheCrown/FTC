plugins {
  java
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {

}

dependencies {
  implementation(project(":commons"))
  compileOnly("net.luckperms:api:5.4")
}

pluginYml {
  name = "FTC-Core"
  main = "net.forthecrown.core.CorePlugin"
  loader = "net.forthecrown.core.CoreLoader"

  depends {
    required("LuckPerms")
  }
}