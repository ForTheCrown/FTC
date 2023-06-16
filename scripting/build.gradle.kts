plugins {
  `java-library`
}

repositories {

}

dependencies {
  api("org.mozilla:rhino:1.7.14")

  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-ScriptEngine"
  main = "net.forthecrown.scripts.ScriptingPlugin"
}