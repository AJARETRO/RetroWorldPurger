package dev.ajaretro.foliaCoreCleaner;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public final class FoliaCoreCleaner extends JavaPlugin {

    private boolean hasCore = false;
    private List<String> blacklistedWorlds;
    private int spawnProtectionRadius;

    private final AtomicLong totalChunksDeleted = new AtomicLong(0);
    private final AtomicLong totalBytesSaved = new AtomicLong(0);
    private final AtomicLong sessionChunksDeleted = new AtomicLong(0);
    private final AtomicLong sessionBytesSaved = new AtomicLong(0);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSettings();

        boolean isFolia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
        } catch (ClassNotFoundException ignored) {
            isFolia = false;
        }

        if (getServer().getPluginManager().getPlugin("FoliaCore") != null) {
            this.hasCore = true;
            getLogger().info("Connected to Foliacore");
        } else {
            this.hasCore = false;
            if (isFolia) {
                getLogger().log(Level.WARNING, "It is better to use FoliaCore-Cleaner with Foliacore");
            }
        }

        int pluginId = 28479;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SingleLineChart("chunks_deleted", () -> (int) totalChunksDeleted.get()));
        metrics.addCustomChart(new SingleLineChart("storage_saved_mb", () -> (int) (totalBytesSaved.get() / 1024 / 1024)));

        getServer().getPluginManager().registerEvents(new ChunkMarkerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkCleanerListener(this), this);

        getLogger().info("FoliaCore-Cleaner is running.");
    }

    @Override
    public void onDisable() {
        saveStats();

        double mbSaved = sessionBytesSaved.get() / 1024.0 / 1024.0;
        getLogger().info("--------------------------------------------------");
        getLogger().info(" FoliaCore-Cleaner Session Report");
        getLogger().info(" Chunks Cleaned: " + sessionChunksDeleted.get());
        getLogger().info(" Storage Saved:  " + String.format("%.2f", mbSaved) + " MB");
        getLogger().info("--------------------------------------------------");
    }

    public void addDeletedChunkStats(long chunkSizeInBytes) {
        this.totalChunksDeleted.incrementAndGet();
        this.totalBytesSaved.addAndGet(chunkSizeInBytes);
        this.sessionChunksDeleted.incrementAndGet();
        this.sessionBytesSaved.addAndGet(chunkSizeInBytes);

        if (totalChunksDeleted.get() % 100 == 0) saveStats();
    }

    private void loadSettings() {
        reloadConfig();
        this.totalChunksDeleted.set(getConfig().getLong("stats.total-chunks-deleted", 0));
        this.totalBytesSaved.set(getConfig().getLong("stats.total-bytes-saved", 0));

        this.blacklistedWorlds = getConfig().getStringList("blacklisted-worlds");
        this.spawnProtectionRadius = getConfig().getInt("spawn-protection-radius", 500);
    }

    private void saveStats() {
        getConfig().set("stats.total-chunks-deleted", totalChunksDeleted.get());
        getConfig().set("stats.total-bytes-saved", totalBytesSaved.get());
        saveConfig();
    }

    public List<String> getBlacklistedWorlds() { return blacklistedWorlds; }
    public int getSpawnProtectionRadius() { return spawnProtectionRadius; }
}