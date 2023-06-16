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

pluginYml {
  name = "FTC-Cosmetics"
  main = "net.forthecrown.cosmetics.CosmeticsPlugin"
}