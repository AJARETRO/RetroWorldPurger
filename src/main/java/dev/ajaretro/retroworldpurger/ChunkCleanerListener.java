/*
 * Copyright (c) 2026 AJA_RETRO (https://ajaretro.dev). All Rights Reserved.
 * 
 * This source code and compiled binaries are the intellectual property of the author.
 * Redistribution, modification, or derivative works are strictly prohibited under the
 * terms of the Source-Available License.
 */

package dev.ajaretro.retroworldpurger;

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

    private final RetroWorldPurger plugin;
    private final NamespacedKey MODIFIED_KEY;

    public ChunkCleanerListener(RetroWorldPurger plugin) {
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
        double chunkX = chunk.getX() * 16.0;
        double chunkZ = chunk.getZ() * 16.0;

        // Optimize distance check by comparing squared distances directly (saves Math.sqrt cpu cycles)
        double dx = chunkX - spawn.getX();
        double dz = chunkZ - spawn.getZ();
        double distanceSquared = (dx * dx) + (dz * dz);
        double spawnRadius = plugin.getSpawnProtectionRadius();

        if (distanceSquared < (spawnRadius * spawnRadius)) {
            return;
        }

        if (chunk.getPersistentDataContainer().has(MODIFIED_KEY, PersistentDataType.BYTE)) {
            return;
        }

        if (chunk.getInhabitedTime() > plugin.getMinInhabitedTimeTicks()) {
            return;
        }

        // Flag the chunk save state to false so it is purged/discarded upon unload
        plugin.addDeletedChunkStats(4096);
        event.setSaveChunk(false);
    }
}