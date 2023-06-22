package net.forthecrown.gradle

import org.gradle.api.Action

class FtcPaperYml {

  // Basic plugin details
  var version: String
  var main: String? = null
  var description: String? = null;
  var authors: MutableList<String>? = null;
  var prefix: String? = null;

  var name: String
  set(value) {
    if (prefix == null) {
      prefix = value.replace("FTC-", "")
    }

    field = value
  }

  // Class loading/bootstrap info
  var bootstrapper: String? = null
  var loader: String? = null
  var openClassLoader: Boolean = false;

  // Depedency and load order
  var load: PluginLoadOrder = PluginLoadOrder.POSTWORLD
  val loadBefore: LoadOrderList = LoadOrderList()
  val loadAfter: LoadOrderList = LoadOrderList()
  val depends: DependsList = DependsList()

  constructor(name: String, version: String) {
    this.name = name
    this.version = version
  }

  fun depends(act: DependsList.() -> Unit) {
    act(depends)
  }

  fun authors(act: MutableList<String>.() -> Unit) {
    if (authors == null) {
      authors = ArrayList();
    }

    act(authors!!);
  }

  fun loadBefore(act: LoadOrderList.() -> Unit) {
    act(loadBefore)
  }

  fun loadAfter(act: LoadOrderList.() -> Unit) {
    act(loadAfter)
  }
}

enum class PluginLoadOrder {
  STARTUP,
  POSTWORLD
}

class DependsList {
  val map: MutableMap<String, PluginDependency> = HashMap()

  fun optional(name: String, act: Action<PluginDependency>? = null) {
    val dep = PluginDependency(true)
    act?.execute(dep)
    map[name] = dep
  }

  fun required(name: String, act: Action<PluginDependency>? = null) {
    val dep = PluginDependency(false)
    act?.execute(dep)
    map[name] = dep
  }
}

data class PluginDependency(val optional: Boolean) {
  var bootstrap: Boolean = false
}

class LoadOrderList {
  val list: MutableMap<String, Boolean> = HashMap();

  fun bootstrap(name: String) {
    list[name] = true
  }

  fun regular(name: String) {
    list[name] = false;
  }
}