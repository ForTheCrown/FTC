import java.util.Date

plugins {
  java

  id("io.papermc.paperweight.userdev") version "1.5.3"
  id("com.github.johnrengelman.shadow") version "8.1.0"

  id("ftc_plugin")
}

val minecraftVersion = "1.19.4"
val baseJarName = "ForTheCrown"
ftc.jarBaseName = baseJarName

val grenadier = "net.forthecrown:grenadier:2.0.1"
val nashorn = "org.openjdk.nashorn:nashorn-core:15.4"

group = "net.forthecrown"
version = "${minecraftVersion}-${ftc.buildId}-${if (ftc.isDebugBuild) "DEBUG" else "RELEASE"}"

repositories {
  mavenCentral()

  // Brigadier
  maven("https://libraries.minecraft.net")

  // Math library
  maven("https://repo.spongepowered.org/repository/maven-public")

  // Dynmap
  maven("https://mvn.lumine.io/repository/maven-public/")

  // WorldGuard
  maven("https://maven.enginehub.org/repo/")

  // DiscordSRV
  maven("https://nexus.scarsz.me/content/groups/public/")

  // VotingPlugin and GSit
  maven("https://jitpack.io")

  // AncientGates
  maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")

  // Lombok
  compileOnly("org.projectlombok:lombok:1.18.26")
  annotationProcessor("org.projectlombok:lombok:1.18.26")

  // FTC libraries
  compileOnly(grenadier)
  testImplementation(grenadier)

  // Misc libraries
  compileOnly("org.tomlj:tomlj:1.1.0")
  compileOnly(nashorn)
  compileOnly("org.apache.commons:commons-text:1.10.0")
  implementation("org.spongepowered:math:2.1.0-SNAPSHOT")

  // Plugin dependencies
  compileOnly("us.dynmap:DynmapCoreAPI:3.4")
  compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit:2.5.2")
  compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
  compileOnly("com.discordsrv:discordsrv:1.26.0")
  compileOnly("net.luckperms:api:5.4")
  compileOnly("org.mcteam.ancientgates:ancientgates:2.6")
  compileOnly("com.github.BenCodez:VotingPlugin:6.11.2")
  compileOnly("com.github.Gecolay.GSit:core:1.3.7")

  // Minecraft, Bukkit and Paper
  paperweight.paperDevBundle("${minecraftVersion}-R0.1-SNAPSHOT")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

base {
  this.archivesName.set(baseJarName)
}

tasks {
  jar {
    archiveBaseName.set(baseJarName)
  }

  test {
    useJUnitPlatform()
  }

  build {
    doLast {
      if (!ftc.isDebugBuild) {
        ftc.incrementBuildId()
      }

      ftc.syncJarAttributes()
    }
  }

  assemble {
    dependsOn(reobfJar)
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name()
    options.release.set(17)

    options.compilerArgs.add("-Xlint:all")
    options.compilerArgs.add("-Xmaxwarns")
    options.compilerArgs.add("300")
  }

  processResources {
    filteringCharset = Charsets.UTF_8.name()

    filesMatching("plugin.yml") {
      val date = Date()

      expand(
        "version"    to version,
        "nashorn"    to nashorn,
        "grenadier"  to grenadier,
        "buildID"    to ftc.buildId,
        "debugbuild" to ftc.isDebugBuild,
        "buildDate"  to date.toString()
      )
    }

    val copyDetails: ArrayList<FileCopyDetails> = ArrayList()
    eachFile {
      copyDetails.add(this)
    }

    doLast {
      copyDetails.forEach {
        val target = File(destinationDir, it.path)
        target.setLastModified(it.lastModified)
      }
    }
  }

  reobfJar {
    doLast {
      ftc.syncJarAttributes()
    }
  }
}