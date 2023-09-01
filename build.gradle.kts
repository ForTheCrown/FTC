import net.forthecrown.gradle.FtcExtension
import net.forthecrown.gradle.MC_VERSION

plugins {
  java
  id("io.freefair.lombok") version "8.0.1"
  id("ftc_plugin")
}

version = "1.0.0-SNAPSHOT"
group = "net.forthecrown"

val grenadier      = "net.forthecrown:grenadier:2.1.3"
val grenadierAnnot = "net.forthecrown:grenadier-annotations:1.2.2"
val mathlib        = "org.spongepowered:math:2.1.0-SNAPSHOT"
val toml           = "org.tomlj:tomlj:1.1.0"
val configurate    = "org.spongepowered:configurate-core:4.1.2"
val apiVersion     = MC_VERSION

repositories {
  mavenCentral()
}

val buildAll = task("build-all-plugins") {
  group = "build"
  description = "Builds all plugin modules"
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "io.freefair.lombok")
  apply(plugin = "ftc_plugin")

  group = rootProject.group
  version = rootProject.version

  afterEvaluate {
    val ftcExtension = this.extensions.findByType(FtcExtension::class.java) ?: return@afterEvaluate

    if (ftcExtension.skipDependency || !ftcExtension.apiFor.isNullOrEmpty()) {
      return@afterEvaluate
    }

    buildAll.dependsOn(":${this.name}:buildAndCopyToRoot")
  }

  repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://repo.papermc.io/repository/maven-public/")
  }

  dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("com.google.guava:guava:32.1.2-jre")
    testImplementation("io.papermc.paper:paper-api:${apiVersion}-R0.1-SNAPSHOT")

    compileOnly("io.papermc.paper:paper-api:${apiVersion}-R0.1-SNAPSHOT")

    compileOnly(grenadier)
    compileOnly(grenadierAnnot)
    compileOnly(mathlib)
    compileOnly(toml)

    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("com.mojang:datafixerupper:6.0.6")

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
      options.compilerArgs.add("-Xmaxerrs")
      options.compilerArgs.add("3000")
    }

    processResources {
      if (this.project.name != "core") {
        return@processResources
      }

      filesMatching("runtime_libraries.json") {
        expand(
          "grenadier" to grenadier,
          "grenadierAnnotations" to grenadierAnnot,
          "toml" to toml,
          "mathLib" to mathlib,
          "configurate" to configurate,
        )
      }
    }
  }
}