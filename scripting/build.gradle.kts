plugins {
  `java-library`
}

val rhinoDependency = "org.mozilla:rhino:1.7.14"

dependencies {
  api(rhinoDependency)

  compileOnly(project(":commons"))
}

ftc {
  implementedBy("scripting-impl")
}