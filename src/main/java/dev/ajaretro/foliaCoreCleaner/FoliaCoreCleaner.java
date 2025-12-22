package dev.ajaretro.foliaCoreCleaner;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;
import java.util.logging.Level;

public final class FoliaCoreCleaner extends JavaPlugin {

    private boolean hasCore = false;

    // Config Settings
    private List<String> blacklistedWorlds;
    private int spawnProtectionRadius;

    // Stats
    private long totalChunksDeleted = 0;
    private long totalBytesSaved = 0;
    private long sessionChunksDeleted = 0;
    private long sessionBytesSaved = 0;

    @Override
    public void onEnable() {
        // 1. Load Config & Stats
        saveDefaultConfig();
        loadSettings();

        // 2. Core Check
        if (getServer().getPluginManager().getPlugin("FoliaCore") != null) {
            this.hasCore = true;
            getLogger().info("Connected to Foliacore");
        } else {
            this.hasCore = false;
            getLogger().log(Level.WARNING, "It is better to use FoliaCore-Cleaner with Foliacore");
        }

        // 3. bStats
        int pluginId = 28479;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SingleLineChart("chunks_deleted", () -> (int) totalChunksDeleted));
        metrics.addCustomChart(new SingleLineChart("storage_saved_mb", () -> (int) (totalBytesSaved / 1024 / 1024)));

        // 4. Register Listeners
        getServer().getPluginManager().registerEvents(new ChunkMarkerListener(this), this);
        getServer().getPluginManager().registerEvents(new ChunkCleanerListener(this), this);

        getLogger().info("FoliaCore-Cleaner is running.");
    }

    @Override
    public void onDisable() {
        saveStats();

        double mbSaved = sessionBytesSaved / 1024.0 / 1024.0;
        getLogger().info("--------------------------------------------------");
        getLogger().info(" FoliaCore-Cleaner Session Report");
        getLogger().info(" Chunks Cleaned: " + sessionChunksDeleted);
        getLogger().info(" Storage Saved:  " + String.format("%.2f", mbSaved) + " MB");
        getLogger().info("--------------------------------------------------");
    }

    public void addDeletedChunkStats(long chunkSizeInBytes) {
        this.totalChunksDeleted++;
        this.totalBytesSaved += chunkSizeInBytes;
        this.sessionChunksDeleted++;
        this.sessionBytesSaved += chunkSizeInBytes;

        if (totalChunksDeleted % 100 == 0) saveStats();
    }

    private void loadSettings() {
        reloadConfig();
        this.totalChunksDeleted = getConfig().getLong("stats.total-chunks-deleted", 0);
        this.totalBytesSaved = getConfig().getLong("stats.total-bytes-saved", 0);

        this.blacklistedWorlds = getConfig().getStringList("blacklisted-worlds");
        this.spawnProtectionRadius = getConfig().getInt("spawn-protection-radius", 500);
    }

    private void saveStats() {
        getConfig().set("stats.total-chunks-deleted", totalChunksDeleted);
        getConfig().set("stats.total-bytes-saved", totalBytesSaved);
        saveConfig();
    }

    // Getters for the Listener to use
    public List<String> getBlacklistedWorlds() { return blacklistedWorlds; }
    public int getSpawnProtectionRadius() { return spawnProtectionRadius; }
}