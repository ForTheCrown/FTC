package net.forthecrown.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
import net.forthecrown.core.logging.Loggers;
import net.forthecrown.utils.io.Results;
import net.forthecrown.utils.io.SerializationHelper;
import org.apache.logging.log4j.Logger;

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
    MinecraftAssetDownloader downloader
        = new MinecraftAssetDownloader(version, dir);

    downloader.run();
  }

  public void run() {
    Optional<URL> assetIndexResult = getVersionInfoUrl()
        .flatMap(this::getAssetIndex)
        .resultOrPartial(LOGGER::error);

    if (assetIndexResult.isEmpty()) {
      return;
    }

    URL assetIndex = assetIndexResult.get();
    JsonObject assetObjects;

    try {
      assetObjects = downloadJson(assetIndex)
          .getAsJsonObject()
          .getAsJsonObject("objects");
    } catch (IOException exc) {
      LOGGER.error("Error downloading asset index for version '{}'",
          gameVersion,
          exc
      );

      return;
    }

    LOGGER.info("Beginning asset download of MC assets for version {}",
        gameVersion
    );

    downloadAssets(assetObjects);

    LOGGER.info(
        "Finished downloading MC assets, "
            + "downloaded {} files, "
            + "failed to download {} files",

        downloadedFiles, failedDownloads
    );
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

    if (gameVersion.equalsIgnoreCase("latest")) {
      gameVersion = obj.getAsJsonObject("latest").get("release").getAsString();
    }

    JsonArray versionArray = obj.getAsJsonArray("version");

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

  private DataResult<URL> getAssetIndex(URL versionInfo) {
    JsonObject json;

    try {
      json = downloadJson(versionInfo).getAsJsonObject();
    } catch (IOException exc) {
      return Results.error(
          "Error downloading version info JSON from url '%s', error: '%s'",
          versionInfo, exc.getMessage()
      );
    }

    JsonObject assetIndex = json.getAsJsonObject("assetIndex");
    String url = assetIndex.get("url").getAsString();

    try {
      return Results.success(new URL(url));
    } catch (IOException exc) {
      return Results.error("Error getting asset index URL, url='%s', error='%s'",
          url, exc.getMessage()
      );
    }
  }

  private void downloadAssets(JsonObject objects) {
    for (var e: objects.entrySet()) {
      String hash = e.getValue().getAsJsonObject().get("hash").getAsString();
      String first2Chars = hash.substring(0, 2);

      String fileUrl = URL_PREFIX + "/" + first2Chars + "/" + hash;

      try {
        downloadFile(e.getKey(), fileUrl);
        downloadedFiles++;

        LOGGER.debug("Downloaded asset {}", e.getKey());
      } catch (IOException exc) {
        failedDownloads++;

        LOGGER.error("Failed to download asset {}", e.getKey(), exc);
      }
    }
  }

  private void downloadFile(String name, String stringUrl) throws IOException {
    URL url = new URL(stringUrl);
    Path destination = directory.resolve(name);

    SerializationHelper.ensureParentExists(destination);

    try (var stream = url.openStream()) {
      var out = Files.newOutputStream(destination);

      stream.transferTo(out);
      out.close();
    }
  }

  private JsonElement downloadJson(URL url) throws IOException {
    return JsonParser.parseReader(new InputStreamReader(url.openStream()));
  }
}