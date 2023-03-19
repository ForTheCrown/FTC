package net.forthecrown.gradle;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.gradle.api.Project;
import org.gradle.api.Rule;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSetContainer;

public class FtcExtension {
  private final BuildInfo info;
  private final Project project;

  private String jarBaseName;

  private final SourceSetContainer sourceSets;

  public FtcExtension(Project project, BuildInfo info) {
    this.project = project;
    this.info = info;

    this.sourceSets = project.getExtensions()
        .getByType(SourceSetContainer.class);
  }

  public void incrementBuildId() {
    setBuildId(getBuildId() + 1);
    info.writeSafe();
  }

  public void syncJarAttributes() {
    String jarName = getJarName();

    Path jarPath = project.getBuildDir()
        .toPath()
        .resolve("libs")
        .resolve(jarName);

    SourceDirectorySet resources = sourceSets.getByName("main").getResources();
    Set<File> files = resources.getSrcDirs();

    for (var f: files) {
      var resourceDir = f.toPath();
      System.out.printf("resources='%s', jarPath='%s'\n", resourceDir, jarPath);

      try {
        JarResourceSync.sync(resourceDir, jarPath);
      } catch (Exception exc) {
        throw new RuntimeException(exc);
      }
    }
  }

  public String getJarName() {
    return String.format("%s-%s.jar", jarBaseName, project.getVersion());
  }

  public Path getPath() {
    return info.getPath();
  }

  public int getBuildId() {
    return info.getBuildId();
  }

  public void setBuildId(int buildId) {
    info.setBuildId(buildId);
  }

  public boolean isDebugBuild() {
    return info.isDebugBuild();
  }

  public void setDebugBuild(boolean debugBuild) {
    info.setDebugBuild(debugBuild);
  }

  public String getTestServerPath() {
    return info.getTestServerPath();
  }

  public void setTestServerPath(String testServerPath) {
    info.setTestServerPath(testServerPath);
  }

  public String getServerLaunchFile() {
    return info.getServerLaunchFile();
  }

  public void setServerLaunchFile(String serverLaunchFile) {
    info.setServerLaunchFile(serverLaunchFile);
  }

  public String getJarBaseName() {
    return jarBaseName;
  }

  public void setJarBaseName(String jarBaseName) {
    this.jarBaseName = jarBaseName;
  }
}