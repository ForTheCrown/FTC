plugins {
  `java-gradle-plugin`
}

repositories {
  mavenCentral()
}

dependencies {
  // Lombok
  compileOnly("org.projectlombok:lombok:1.18.26")
  annotationProcessor("org.projectlombok:lombok:1.18.26")

  implementation("com.google.code.gson:gson:2.10.1")
  implementation("com.google.guava:guava:31.1-jre")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

gradlePlugin {
  plugins {
    create("ftc_plugin") {
      id = "ftc_plugin"
      implementationClass = "net.forthecrown.gradle.FtcGradlePlugin"
    }
  }
}