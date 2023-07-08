plugins {
  `java-library`
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

val rhinoDependency = "org.mozilla:rhino:1.7.14"

dependencies {
  api(rhinoDependency)

  compileOnly(project(":commons"))
  implementation(project(":class-loader-tools"))
}

pluginYml {
  name = "FTC-ScriptEngine"
  main = "net.forthecrown.scripts.ScriptingPlugin"
  loader = "net.forthecrown.scripts.ScriptPluginLoader"
  openClassLoader = true
}

tasks {
  processResources {
    filesMatching("runtime_libraries.json") {
      expand("rhino" to rhinoDependency)
    }
  }

  shadowJar {
    dependencies {
      exclude(dependency(rhinoDependency))
    }
  }
}