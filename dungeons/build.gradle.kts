plugins {
  java
  id("io.papermc.paperweight.userdev") version "1.5.5"
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":structures"))

  paperweight.paperDevBundle("1.20-R0.1-SNAPSHOT")
}