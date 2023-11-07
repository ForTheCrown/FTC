plugins {
  java
  id("com.github.johnrengelman.shadow") version "8.1.1"
}

val rhinoDependency = "org.mozilla:rhino:1.7.14"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":scripting"))
  implementation(project(":class-loader-tools"))
  compileOnly(project(":commons"))
}

pluginYml {
  name = "FTC-Scripting"
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

  buildAndCopyToRoot {
    dependsOn(shadowJar)
  }
}