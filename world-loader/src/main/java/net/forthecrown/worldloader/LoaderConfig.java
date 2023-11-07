package net.forthecrown.worldloader;

import net.forthecrown.worldloader.WorldLoaderService.LoadMode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class LoaderConfig {

  public boolean useChunkyWhenAvailable = true;

  public int maxChunksLoading = 25;

  public int logFrequency = 2000;

  public LoadMode defaultLodeMode;

  public int maxWorldSize = 50_000;
}
