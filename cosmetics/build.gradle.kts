plugins {
  java
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":scripting"))
  compileOnly(project(":menus"))
}