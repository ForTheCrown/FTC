plugins {
  java
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":dungeons"))
  compileOnly(project(":scripting"))
  compileOnly(project(":menus"))
}

pluginYml {
  name = "FTC-ExtendedItems"
  main = "net.forthecrown.inventory.ItemsPlugin"
}