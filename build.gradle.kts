plugins {
  java
  id("io.freefair.lombok") version "8.0.1"
}

val grenadier      = "net.forthecrown:grenadier:2.0.9"
val grenadierAnnot = "net.forthecrown:grenadier-annotations:1.1.1"
val mathlib        = "org.spongepowered:math:2.1.0-SNAPSHOT"
val toml           = "org.tomlj:tomlj:1.1.0"
val apiVersion     = "1.20"

repositories {
  mavenCentral()
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "io.freefair.lombok")

  group = rootProject.group
  version = rootProject.version

  repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://repo.papermc.io/repository/maven-public/")
  }

  dependencies {
    compileOnly("io.papermc.paper:paper-api:1.19.4-R0.1-SNAPSHOT")

    compileOnly(grenadier)
    compileOnly(grenadierAnnot)
    compileOnly(mathlib)
    compileOnly(toml)

    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("com.mojang:datafixerupper:6.0.6")
  }

  java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
  }

  tasks {
    test {
      useJUnitPlatform()
    }

    javadoc {
      options.encoding = Charsets.UTF_8.name()
    }

    compileJava {
      options.encoding = Charsets.UTF_8.name()
      options.release.set(17)
    }

    processResources {
      filesMatching(listOf("plugin.yml", "paper-plugin.yml")) {
        expand(
          "version" to version,
          "api_version" to apiVersion,
        )
      }
    }
  }
}