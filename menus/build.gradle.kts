plugins {
  java
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
}

base {
  archivesName.set("FTC-MenuService")
}

tasks {
}