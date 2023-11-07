plugins {
  java
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-Structures"
  main = "net.forthecrown.structure.StructuresPlugin"
}