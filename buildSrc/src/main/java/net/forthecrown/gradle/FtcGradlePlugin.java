package net.forthecrown.gradle;

import com.google.common.base.Strings;
import groovy.lang.Closure;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;

public class FtcGradlePlugin implements Plugin<Project> {

  static final String FTC_GROUP = "ftc";

  @Override
  public void apply(Project target) {
    final Path buildInfoPath = target.getProjectDir()
        .toPath()
        .resolve("build-info.json");

    BuildInfo info = new BuildInfo(buildInfoPath);
    try {
      info.read();
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }

    target.getExtensions().create("ftc", FtcExtension.class, target, info);

    var resetTask = target.task("resetBuildId");
    resetTask.setGroup(FTC_GROUP);
    resetTask.doFirst(task -> {
      info.setBuildId(1);
      info.writeSafe();
    });

    var incrementTask = target.task("incrementBuildId");
    incrementTask.setGroup(FTC_GROUP);
    incrementTask.doFirst(task -> {
      info.setBuildId(info.getBuildId() + 1);
      info.writeSafe();
    });

    var buildToTestServer = target.task("buildToTestServer");
    buildToTestServer.setGroup(FTC_GROUP);
    buildToTestServer.dependsOn("build");
    buildToTestServer.doLast(task -> {
      try {
        testServerLaunch(task, info);
      } catch (IOException exc) {
        throw new RuntimeException(exc);
      }
    });
  }

  private void testServerLaunch(Task task, BuildInfo info) throws IOException {
    String path = info.getTestServerPath();
    String launch = info.getServerLaunchFile();

    if (Strings.isNullOrEmpty(path)) {
      return;
    }

    Path serverPath = Path.of(path);
    Path pluginsDirectory = serverPath.resolve("plugins");

    var project = task.getProject();
    var ext = project.getExtensions().findByType(FtcExtension.class);

    var jarName = ext.getJarName();

    Files.copy(
        project.getBuildDir().toPath()
            .resolve("libs")
            .resolve(jarName),

        pluginsDirectory.resolve(jarName),

        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.COPY_ATTRIBUTES
    );

    if (Strings.isNullOrEmpty(launch)) {
      return;
    }

    Path launchFile = serverPath.resolve(launch);

    project.exec(execSpec -> {
      execSpec.setWorkingDir(serverPath.toFile());
      execSpec.commandLine("cmd", "/c", "start " + launch);
    });
  }
}