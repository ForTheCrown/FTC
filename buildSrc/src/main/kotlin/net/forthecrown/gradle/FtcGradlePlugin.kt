package net.forthecrown.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

const val MC_VERSION = "1.20.2"
const val API_VERSION = "1.20"
const val NMS_DEPENDENCY = "io.papermc.paper:dev-bundle:${MC_VERSION}-R0.1-SNAPSHOT"
const val NMS_CONFIG_NAME = "paperweightDevelopmentBundle"

const val GENERATE_CONFIG_DIR = "build/generated-sources/"
const val GENERATED_CONFIG_PATH = "$GENERATE_CONFIG_DIR/paper-plugin.yml"

const val CREATE_PLUGIN_YML = "createPluginYml"
const val FTC_GROUP = "ForTheCrown"

private const val BUILD_ALL_PLUGINS = "build-all-plugins"

class FtcGradlePlugin: Plugin<Project> {

  override fun apply(target: Project) {
    if (target == target.rootProject) {
      return
    }

    addGeneratedToSourceSet(target)

    val yml = FtcPaperYml(target.name, target.version.toString())
    val ftcExtension = FtcExtension(target)

    yml.authors {
      add("JulieWoolie") // :3
    }

    target.extensions.run {
      add("pluginYml", yml)
      add("ftc", ftcExtension)
    }

    createPluginYmlTask(target, yml, ftcExtension)
    createRootBuildTask(target, yml)
  }

  private fun createRootBuildTask(target: Project, yml: FtcPaperYml) {
    val task = target.task("buildAndCopyToRoot")
    task.group = FTC_GROUP
    task.description = "Builds the project, and then moves the jar file to the root build directory"

    task.dependsOn("build")

    task.doLast {
      val proj = it.project;
      val root = proj.rootProject;

      val projLibs = proj.buildDir.toPath().resolve("libs")
      val rootLibs = root.buildDir.toPath().resolve("libs")

      val jarName = "${yml.name}-${proj.version}"

      if (moveJar(projLibs, rootLibs, "$jarName-all.jar")) {
        return@doLast
      }

      moveJar(projLibs, rootLibs, "$jarName.jar")
    }
  }

  private fun moveJar(sourceDir: Path, destDir: Path, fileName: String): Boolean {
    val sourceFile = sourceDir.resolve(fileName)
    val destFile = destDir.resolve(fileName)

    if (!Files.exists(sourceFile)) {
      return false
    }

    if (!Files.exists(destDir)) {
      Files.createDirectories(destDir)
    }

    Files.copy(
        sourceFile,
        destFile,
        StandardCopyOption.COPY_ATTRIBUTES,
        StandardCopyOption.REPLACE_EXISTING
    )
    return true
  }

  private fun createPluginYmlTask(target: Project, yml: FtcPaperYml, ftcExtension: FtcExtension) {
    val task = target.task(CREATE_PLUGIN_YML)
    task.group = FTC_GROUP
    task.description = "Creates a paper-plugin.yml"

    task.doFirst {
      if (ftcExtension.skipDependency || !ftcExtension.apiFor.isNullOrEmpty()) {
        return@doFirst
      }

      if (ftcExtension.autoAddDependencies) {
        scanForProjectDependencies(it.project, yml)
      }

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
      dependencyName = "project:${proj.name}"
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