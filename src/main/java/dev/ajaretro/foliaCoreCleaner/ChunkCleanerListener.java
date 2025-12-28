package dev.ajaretro.foliaCoreCleaner;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.persistence.PersistentDataType;

public class ChunkCleanerListener implements Listener {

    private final FoliaCoreCleaner plugin;
    private final NamespacedKey MODIFIED_KEY;

    public ChunkCleanerListener(FoliaCoreCleaner plugin) {
        this.plugin = plugin;
        this.MODIFIED_KEY = new NamespacedKey(plugin, "is_modified");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        World world = chunk.getWorld();

        if (plugin.getBlacklistedWorlds().contains(world.getName())) {
            return;
        }

        Location spawn = world.getSpawnLocation();
        double chunkX = chunk.getX() * 16;
        double chunkZ = chunk.getZ() * 16;

        double distance = Math.sqrt(Math.pow(chunkX - spawn.getX(), 2) + Math.pow(chunkZ - spawn.getZ(), 2));

        if (distance < plugin.getSpawnProtectionRadius()) {
            return;
        }

        if (chunk.getPersistentDataContainer().has(MODIFIED_KEY, PersistentDataType.BYTE)) {
            return;
        }

        if (chunk.getInhabitedTime() > 200) {
            return;
        }

        plugin.addDeletedChunkStats(4096);
        event.setSaveChunk(false);
    }
}