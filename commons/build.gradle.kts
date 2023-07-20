import net.forthecrown.gradle.API_VERSION

plugins {
  java
  id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {

}

dependencies {
  paperweight.paperDevBundle("${API_VERSION}-R0.1-SNAPSHOT")
}

tasks {
  assemble {
    dependsOn(reobfJar)
  }
}