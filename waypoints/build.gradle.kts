plugins {
  java
}

repositories {
  mavenCentral()
  maven("https://mvn.lumine.io/repository/maven-public/")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":cosmetics"))
  compileOnly(project(":structures"))
  compileOnly(project(":anti-grief"))
}

pluginYml {
  name = "FTC-Waypoints"
  main = "net.forthecrown.waypoints.WaypointsPlugin"

  depends {
    required("dynmap")
  }
}