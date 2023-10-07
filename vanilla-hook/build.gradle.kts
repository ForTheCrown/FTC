import net.forthecrown.gradle.DependencyLoad
import net.forthecrown.gradle.MC_VERSION

plugins {
  java
  id("io.papermc.paperweight.userdev") version "1.5.5"
}

version = MC_VERSION

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
  paperweight.paperDevBundle("${MC_VERSION}-R0.1-SNAPSHOT")
}

ftc {
  autoAddDependencies = false
}

pluginYml {
  name = "FTC-Vanilla-Hook"
  main = "net.forthecrown.vanilla.VanillaPlugin"

  loadBefore {
    regular("project:commons")
  }

  depends {
    required("project:commons") {
      joinClasspath = true
      load = DependencyLoad.AFTER
    }
  }
}

tasks {
  assemble {
    dependsOn(reobfJar)
  }
}