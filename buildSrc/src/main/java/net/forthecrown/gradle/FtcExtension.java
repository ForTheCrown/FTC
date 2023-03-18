package net.forthecrown.gradle;

import java.io.IOException;
import java.nio.file.Path;
import org.gradle.api.Project;

public class FtcExtension {
  private final BuildInfo info;
  private final Project project;

  private String jarBaseName;

  public FtcExtension(Project project, BuildInfo info) {
    this.project = project;
    this.info = info;
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

    Path resources = project.getProjectDir()
        .toPath()
        .resolve("src")
        .resolve("main")
        .resolve("resources");

    System.out.printf("resources='%s', jarPath='%s'\n", resources, jarPath);

    try {
      JarResourceSync.sync(resources, jarPath);
    } catch (Exception exc) {
      throw new RuntimeException(exc);
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