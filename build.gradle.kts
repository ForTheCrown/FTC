import net.forthecrown.gradle.API_VERSION
import java.nio.file.Files
import java.nio.file.StandardCopyOption

plugins {
  java
  id("io.freefair.lombok") version "8.0.1"
  id("ftc_plugin")
}

version = "1.0.0-SNAPSHOT"
group = "net.forthecrown"

val grenadier      = "net.forthecrown:grenadier:2.1.1"
val grenadierAnnot = "net.forthecrown:grenadier-annotations:1.2.1"
val mathlib        = "org.spongepowered:math:2.1.0-SNAPSHOT"
val toml           = "org.tomlj:tomlj:1.1.0"
val tomlConfig     = "me.lucko.configurate:configurate-toml:4.1"
val configurate    = "org.spongepowered:configurate-core:4.1.2"
val apiVersion     = API_VERSION

repositories {
  mavenCentral()
}

task("build-all") {
  description = "Builds all modules"
  group = "build"

  subprojects {
    dependsOn(":${this.name}:build")
  }

  doLast {
    childProjects.values.forEach {
      val base = it.extensions.findByType(BasePluginExtension::class.java)!!
      val jarName = "libs/${base.archivesName.get()}-${it.version}.jar"

      val buildFile = it.file("${it.buildDir}/$jarName")
      val path = buildFile.toPath();
      val outPath = buildDir.toPath().resolve(jarName)

      if (Files.notExists(path)) {
        return@forEach
      }

      Files.copy(path, outPath, StandardCopyOption.REPLACE_EXISTING)
    }
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "io.freefair.lombok")

  if (name != "commons") {
    apply(plugin = "ftc_plugin")
  }

  group = rootProject.group
  version = rootProject.version

  repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://repo.papermc.io/repository/maven-public/")
  }

  dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20-R0.1-SNAPSHOT")

    compileOnly(grenadier)
    compileOnly(grenadierAnnot)
    compileOnly(mathlib)
    compileOnly(toml)

    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("com.mojang:datafixerupper:6.0.6")

    compileOnly(tomlConfig)
    compileOnly(configurate)
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
      filesMatching("runtime_libraries.json") {
        expand(
          "grenadier" to grenadier,
          "grenadierAnnotations" to grenadierAnnot,
          "toml" to toml,
          "mathLib" to mathlib,
          "configurate" to configurate,
          "tomlConfig" to tomlConfig
        )
      }
    }
  }
}