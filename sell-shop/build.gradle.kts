plugins {
  java
}

repositories {
  mavenCentral()
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":menus"))
}

pluginYml {
  name = "FTC-SellShop"
  main = "net.forthecrown.sellshop.SellShopPlugin"
}