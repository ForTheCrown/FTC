import net.forthecrown.gradle.MC_VERSION

plugins {
  java
  id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {

}

dependencies {
  paperweight.paperDevBundle("${MC_VERSION}-R0.1-SNAPSHOT")
}

ftc {
  implementedBy("core")
}

tasks {
  assemble {
    dependsOn(reobfJar)
  }
}