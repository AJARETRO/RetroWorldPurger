/*
 * Copyright (c) 2026 AJA_RETRO (https://ajaretro.dev). All Rights Reserved.
 * 
 * This source code and compiled binaries are the intellectual property of the author.
 * Redistribution, modification, or derivative works are strictly prohibited under the
 * terms of the Source-Available License.
 */

package dev.ajaretro.retroworldpurger;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

public class ChunkMarkerListener implements Listener {

    private final JavaPlugin plugin;
    private final NamespacedKey MODIFIED_KEY;

    // Cache interactive materials for O(1) constant-time lookup
    private static final Set<Material> INTERACTIVE_MATERIALS = EnumSet.noneOf(Material.class);

    static {
        for (Material material : Material.values()) {
            String name = material.name();
            if (name.contains("ANVIL") ||
                    name.contains("TABLE") ||
                    name.contains("DOOR") ||
                    name.contains("PLATE") ||
                    name.contains("BUTTON") ||
                    name.contains("LEVER")) {
                INTERACTIVE_MATERIALS.add(material);
            }
        }
    }

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

    // --- 1. Direct Player Modifications ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        markChunk(event.getBlock().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        markChunk(event.getBlock().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIgnite(BlockIgniteEvent event) {
        markChunk(event.getBlock().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Check if the block is a container or belongs to the cached interactive types
        if (block.getState() instanceof Container || INTERACTIVE_MATERIALS.contains(block.getType())) {
            markChunk(block.getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        markChunk(event.getRightClicked().getLocation().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            markChunk(event.getEntity().getLocation().getChunk());
        }
    }

    // --- 2. Cross-Border Redstone & Automation ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            markChunk(block.getChunk());
            markChunk(block.getRelative(event.getDirection()).getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            markChunk(block.getChunk());
            markChunk(block.getRelative(event.getDirection()).getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        Chunk fromChunk = event.getBlock().getChunk();
        Chunk toChunk = event.getToBlock().getChunk();

        if (!fromChunk.equals(toChunk)) {
            // Fluid crosses chunk borders. If origin has modifications, propagate it.
            if (fromChunk.getPersistentDataContainer().has(MODIFIED_KEY, PersistentDataType.BYTE)) {
                markChunk(toChunk);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispense(BlockDispenseEvent event) {
        Block block = event.getBlock();
        if (block.getBlockData() instanceof Directional) {
            Directional directional = (Directional) block.getBlockData();
            Block targetBlock = block.getRelative(directional.getFacing());
            if (!block.getChunk().equals(targetBlock.getChunk())) {
                if (block.getChunk().getPersistentDataContainer().has(MODIFIED_KEY, PersistentDataType.BYTE)) {
                    markChunk(targetBlock.getChunk());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getHitBlock() != null) {
            markChunk(event.getHitBlock().getChunk());
        }
        if (event.getHitEntity() != null) {
            markChunk(event.getHitEntity().getLocation().getChunk());
        }
    }

    // --- 3. Explosions & Environmental Cascades ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            markChunk(block.getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            markChunk(block.getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent event) {
        for (org.bukkit.block.BlockState state : event.getBlocks()) {
            markChunk(state.getChunk());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLightning(LightningStrikeEvent event) {
        markChunk(event.getLightning().getChunk());
    }

    // --- 4. Physics & Entity Interactions ---

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        markChunk(event.getBlock().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        markChunk(event.getEntity().getLocation().getChunk());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        markChunk(event.getEntity().getLocation().getChunk());
    }
}