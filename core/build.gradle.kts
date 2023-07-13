plugins {
  java
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
  implementation(project(":commons", "reobf"))
  implementation(project(":class-loader-tools"))
  compileOnly("net.luckperms:api:5.4")

  compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.6.4")
}

pluginYml {
  name = "FTC-Core"
  main = "net.forthecrown.core.CorePlugin"
  loader = "net.forthecrown.core.CoreLoader"

  depends {
    required("LuckPerms")
    required("FastAsyncWorldEdit")
  }
}

tasks {
  buildAndCopyToRoot {
    dependsOn(shadowJar)
  }
}