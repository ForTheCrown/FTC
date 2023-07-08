package net.forthecrown.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension

const val API_VERSION = "1.20"
const val NMS_DEPENDENCY = "io.papermc.paper:dev-bundle:${API_VERSION}-R0.1-SNAPSHOT"
const val NMS_CONFIG_NAME = "paperweightDevelopmentBundle"

const val GENERATE_CONFIG_DIR = "build/generated-sources/"
const val GENERATED_CONFIG_PATH = "$GENERATE_CONFIG_DIR/paper-plugin.yml"

const val CREATE_PLUGIN_YML = "createPluginYml"

class FtcGradlePlugin: Plugin<Project> {

  override fun apply(target: Project) {
    addGeneratedToSourceSet(target)

    val yml = FtcPaperYml(target.name, target.version.toString())
    val ftcExtension = FtcExtension()

    yml.authors {
      add("JulieWoolie") // :3
    }

    target.extensions.run {
      add("pluginYml", yml)
      add("ftc", ftcExtension)
    }

    target.afterEvaluate {
      if (ftcExtension.useVanilla) {
        val dependencies = it.dependencies

        val dep = dependencies.create(NMS_DEPENDENCY)
        dependencies.add(NMS_CONFIG_NAME, dep)
      }
    }

    val task = target.task(CREATE_PLUGIN_YML)
    task.group = "ForTheCrown"
    task.description = "Creates a paper-plugin.yml"

    task.doFirst {
      scanForProjectDependencies(it.project, yml)
      setArchiveName(it.project, yml)
      createPluginYml(it)
    }

    target.tasks.findByName("compileJava")?.dependsOn(CREATE_PLUGIN_YML)
  }

  private fun setArchiveName(project: Project, yml: FtcPaperYml) {
    val base = project.extensions.getByType(BasePluginExtension::class.java)
    base.archivesName.set(yml.name)
  }

  private fun addGeneratedToSourceSet(project: Project) {
    val jPlugin = project.extensions.findByType(JavaPluginExtension::class.java)!!
    val sourceSets = jPlugin.sourceSets

    sourceSets.findByName("main")?.apply {
      this.resources {
        it.srcDir(GENERATE_CONFIG_DIR)
      }
    }
  }

  private fun scanForProjectDependencies(project: Project, yml: FtcPaperYml) {
    project.configurations.forEach { it ->
      it.allDependencies.stream()
        .filter { it is ProjectDependency }
        .map { it as ProjectDependency }

        .forEach {
          addDependsFromProject(yml, it)
        }
    }
  }

  private fun addDependsFromProject(yml: FtcPaperYml, it: ProjectDependency) {
    val proj = it.dependencyProject
    val projExt = proj.extensions.findByType(FtcPaperYml::class.java)

    val dependencyName: String

    if (projExt != null) {
      if (!projExt.name.startsWith("FTC")) {
        println("Found invalid plugin name! dependencyProject=${it.dependencyProject.name}")
      }

      dependencyName = "project:${proj.name}"
    } else if (proj.name == "commons") {
      dependencyName = "project:commons"
    } else {
      return
    }

    yml.depends {
      if (map.containsKey(dependencyName)) {
        return@depends
      }

      required(dependencyName)
    }
  }
}