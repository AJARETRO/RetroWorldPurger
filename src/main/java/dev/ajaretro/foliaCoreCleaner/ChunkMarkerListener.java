package dev.ajaretro.foliaCoreCleaner;

import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkMarkerListener implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey MODIFIED_KEY;

    public ChunkMarkerListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.MODIFIED_KEY = new NamespacedKey(plugin, "is_modified");
    }

    private void markChunk(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        if (!pdc.has(MODIFIED_KEY, PersistentDataType.BYTE)) {
            pdc.set(MODIFIED_KEY, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        markChunk(event.getBlock().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        markChunk(event.getBlock().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getState() instanceof Container ||
                block.getType().name().contains("ANVIL") ||
                block.getType().name().contains("TABLE") ||
                block.getType().name().contains("DOOR")) {
            markChunk(block.getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        markChunk(event.getBlock().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            markChunk(block.getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLightning(LightningStrikeEvent event) {
        markChunk(event.getLightning().getChunk());
    }
}