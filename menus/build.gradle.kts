plugins {
  java
}

repositories {

}

dependencies {
  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-MenuService"
  main = "net.forthecrown.menu.internal.MenusPlugin"
}