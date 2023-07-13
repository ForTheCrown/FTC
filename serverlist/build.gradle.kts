

dependencies {
  compileOnly(project(":commons"))
  compileOnly(project(":scripting"))
}

pluginYml {
  name = "FTC-ServerList"
  main = "net.forthecrown.serverlist.ServerlistPlugin"
}