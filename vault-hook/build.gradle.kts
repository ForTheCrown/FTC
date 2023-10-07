plugins {
  java
}

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  compileOnly(project(":commons"))
  compileOnly("com.github.MilkBowl:VaultAPI:1.7")
}

pluginYml {
  name = "FTC-Vault-Hook"
  main = "net.forthecrown.vault.VaultPlugin"

  depends {
    optional("Vault")
  }
}