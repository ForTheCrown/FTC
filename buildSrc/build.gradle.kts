plugins {
  java
  `java-gradle-plugin`
  kotlin("jvm") version "1.8.22"
}

repositories {
  mavenCentral()

}

dependencies {

}

gradlePlugin {
  plugins {
    register("ftc_plugin") {
      id = "ftc_plugin"
      implementationClass = "net.forthecrown.gradle.FtcGradlePlugin"
    }
  }
}