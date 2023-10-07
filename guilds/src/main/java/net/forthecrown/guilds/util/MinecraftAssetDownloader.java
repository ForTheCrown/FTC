package net.forthecrown.guilds.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.Getter;
import net.forthecrown.Loggers;
import net.forthecrown.utils.io.PathUtil;
import net.forthecrown.utils.io.Results;
import org.slf4j.Logger;

/**
 * Wiki page that I got this info from: <a href="https://wiki.vg/Game_files">https://wiki.vg/Game_files</a>
 */
@Getter
public class MinecraftAssetDownloader {

  private static final Logger LOGGER = Loggers.getLogger();

  private static final String VERSION_MANIFEST
      = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

  private static final String URL_PREFIX
      = "https://resources.download.minecraft.net/";

  private final Path directory;

  private String gameVersion;

  private int downloadedFiles = 0;
  private int failedDownloads = 0;

  public MinecraftAssetDownloader(String gameVersion, Path directory) {
    this.gameVersion = gameVersion;
    this.directory = directory;
  }

  public static void download(Path dir, String version) {
    new MinecraftAssetDownloader(version, dir).run();
  }

  public void run() {
    Optional<URL> assetIndexResult = getVersionInfoUrl()
        .flatMap(this::getClientJarUrl)
        .resultOrPartial(LOGGER::error);

    if (assetIndexResult.isEmpty()) {
      return;
    }

    URL clientJarUrl = assetIndexResult.get();
    LOGGER.info("Beginning download of MC assets for version {}", gameVersion);

    try {
      downloadClientJar(clientJarUrl);
    } catch (IOException exc) {
      LOGGER.error("Failed to vanilla assets", exc);
      return;
    }

    LOGGER.info(
        "Finished downloading MC assets, "
            + "downloaded {} files, "
            + "failed to download {} files",

        downloadedFiles, failedDownloads
    );
  }

  private void extractClientAssets(byte[] clientJar) {

  }

  private DataResult<URL> getVersionInfoUrl() {
    JsonObject obj;

    try {
      URL manifestUrl = new URL(VERSION_MANIFEST);
      obj = downloadJson(manifestUrl).getAsJsonObject();
    } catch (IOException exc) {
      return Results.error(
          "IO error downloading from URL: '%s', message: '%s'",
          VERSION_MANIFEST, exc.getMessage()
      );
    }

    switch (gameVersion.toLowerCase()) {
      case "latest":
      case "latest.release":
        gameVersion = obj.getAsJsonObject("latest").get("release").getAsString();
        break;

      case "snapshot":
      case "latest.snapshot":
        gameVersion = obj.getAsJsonObject("latest").get("snapshot").getAsString();
        break;
    }

    JsonArray versionArray = obj.getAsJsonArray("versions");

    for (var e: versionArray) {
      JsonObject versionObj = e.getAsJsonObject();
      String id = versionObj.get("id").getAsString();

      if (!id.equalsIgnoreCase(gameVersion)) {
        continue;
      }

      String versionInfo = versionObj.get("url").getAsString();

      try {
        return Results.success(new URL(versionInfo));
      } catch (MalformedURLException exc) {
        return Results.error(
            "Final result URL error, invalid url: '%s'",
            versionInfo
        );
      }
    }

    return Results.error("Invalid version: '%s'", gameVersion);
  }

  private DataResult<URL> getClientJarUrl(URL versionInfo) {
    JsonObject json;

    try {
      json = downloadJson(versionInfo).getAsJsonObject();
    } catch (IOException exc) {
      return Results.error(
          "Error downloading version info JSON from url '%s', error: '%s'",
          versionInfo, exc.getMessage()
      );
    }

    return new Dynamic<>(JsonOps.INSTANCE, json)
        .get("downloads")
        .get("client")
        .get("url")
        .flatMap(Dynamic::asString)
        .flatMap(string -> {
          try {
            return Results.success(new URL(string));
          } catch (IOException exc) {
            return Results.error("Error getting asset index URL, url='%s', error='%s'",
                string, exc.getMessage()
            );
          }
        });
  }

  private void downloadAssets(JsonObject objects) {
    final int totalAssets = objects.size();

    for (var e: objects.entrySet()) {
      String hash = e.getValue().getAsJsonObject().get("hash").getAsString();
      String first2Chars = hash.substring(0, 2);

      String fileUrl = URL_PREFIX + "/" + first2Chars + "/" + hash;

      try {
        downloadFile(e.getKey(), fileUrl);
        downloadedFiles++;

        LOGGER.debug("Downloaded asset {}", e.getKey());

        if (downloadedFiles % 100 == 0) {
          LOGGER.info("MC asset download progress {}/{}",
              downloadedFiles, totalAssets
          );
        }
      } catch (IOException exc) {
        failedDownloads++;

        LOGGER.error("Failed to download asset {}", e.getKey(), exc);
      }
    }
  }

  private void downloadFile(String name, String stringUrl) throws IOException {
    URL url = new URL(stringUrl);
    Path destination = directory.resolve(name);

    PathUtil.ensureParentExists(destination);

    try (var stream = url.openStream()) {
      var out = Files.newOutputStream(destination);

      stream.transferTo(out);
      out.close();
    }
  }

  private void downloadClientJar(URL url) throws IOException {
    var input = url.openStream();
    ZipInputStream zip = new ZipInputStream(input);

    ZipEntry entry;

    while ((entry = zip.getNextEntry()) != null) {
      if (!entry.getName().startsWith("assets") || entry.isDirectory()) {
        continue;
      }

      Path output = directory.resolve(entry.getName());

      try {
        PathUtil.ensureParentExists(output);
        Files.copy(zip, output);
        downloadedFiles++;

        LOGGER.debug("Extracted asset file '{}'", entry.getName());
      } catch (IOException exc) {
        LOGGER.error("Failed to extract asset file '{}'", entry.getName());
        failedDownloads++;
      }
    }

    zip.closeEntry();
    zip.close();
  }

  private JsonElement downloadJson(URL url) throws IOException {
    InputStreamReader reader = new InputStreamReader(url.openStream());
    JsonElement element = JsonParser.parseReader(reader);
    reader.close();
    return element;
  }
}