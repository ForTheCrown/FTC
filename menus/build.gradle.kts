plugins {
  java
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-Menus"
  main = "net.forthecrown.menu.internal.MenusPlugin"
}