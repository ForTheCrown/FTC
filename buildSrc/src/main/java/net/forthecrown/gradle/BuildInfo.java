package net.forthecrown.gradle;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class BuildInfo {

  private static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .create();

  private static final String
      KEY_BUILD_ID = "build_id",
      KEY_DEBUG_BUILD = "debug_build",
      KEY_TEST_SERVER = "test_server",
      KEY_TS_PATH = "directory",
      KEY_TS_LAUNCH_FILE = "launch_file";

  private final Path path;

  private int buildId;
  private boolean debugBuild;

  private String testServerPath;
  private String serverLaunchFile;

  public BuildInfo(Path path) {
    this.path = path;
  }

  public static void modify(Path path, Consumer<BuildInfo> consumer)
      throws IOException
  {
    BuildInfo info = new BuildInfo(path);
    info.read();
    consumer.accept(info);
    info.write();
  }

  public static void modifySafe(Path path, Consumer<BuildInfo> consumer) {
    try {
      modify(path, consumer);
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  public void writeSafe() {
    try {
      write();
    } catch (IOException exc) {
      throw new RuntimeException(exc);
    }
  }

  public void write() throws IOException {
    JsonObject json = new JsonObject();
    JsonObject testServer = new JsonObject();

    if (!Strings.isNullOrEmpty(getTestServerPath())) {
      testServer.addProperty(KEY_TS_PATH, getTestServerPath());
    }

    if (!Strings.isNullOrEmpty(getServerLaunchFile())) {
      testServer.addProperty(KEY_TS_LAUNCH_FILE, getServerLaunchFile());
    }

    if (testServer.size() > 1) {
      json.add(KEY_TEST_SERVER, testServer);
    }

    json.addProperty(KEY_BUILD_ID, getBuildId());
    json.addProperty(KEY_DEBUG_BUILD, isDebugBuild());

    var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
    GSON.toJson(json, writer);
    writer.close();
  }

  public void read() throws IOException {
    var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
    JsonObject object = JsonParser.parseReader(reader).getAsJsonObject();

    if (object.has(KEY_TEST_SERVER)) {
      JsonObject ts = object.getAsJsonObject(KEY_TEST_SERVER);
      JsonElement tsPath = ts.get(KEY_TS_PATH);
      JsonElement tsLaunchFile = ts.get(KEY_TS_LAUNCH_FILE);

      if (tsPath != null) {
        setTestServerPath(tsPath.getAsString());
      } else {
        setTestServerPath(null);
      }

      if (tsLaunchFile != null) {
        setServerLaunchFile(tsLaunchFile.getAsString());
      } else {
        setServerLaunchFile(null);
      }
    } else {
      setTestServerPath(null);
      setServerLaunchFile(null);
    }

    buildId = object.get(KEY_BUILD_ID).getAsInt();
    debugBuild = object.get(KEY_DEBUG_BUILD).getAsBoolean();
  }

  public Path getPath() {
    return path;
  }

  public int getBuildId() {
    return buildId;
  }

  public void setBuildId(int buildId) {
    this.buildId = buildId;
  }

  public boolean isDebugBuild() {
    return debugBuild;
  }

  public void setDebugBuild(boolean debugBuild) {
    this.debugBuild = debugBuild;
  }

  public String getTestServerPath() {
    return testServerPath;
  }

  public void setTestServerPath(String testServerPath) {
    this.testServerPath = testServerPath;
  }

  public String getServerLaunchFile() {
    return serverLaunchFile;
  }

  public void setServerLaunchFile(String serverLaunchFile) {
    this.serverLaunchFile = serverLaunchFile;
  }
}