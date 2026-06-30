# 🧹 RetroWorldPurger

[![Official Website](https://img.shields.io/badge/Official-Website-red?style=for-the-badge&logo=googlechrome)](https://ajaretro.dev/retroworldpurger.html)
[![Modrinth Download](https://img.shields.io/badge/Modrinth-Download-00AD5C?style=for-the-badge&logo=modrinth)](https://modrinth.com/plugin/retroworldpurger)
[![GitHub](https://img.shields.io/badge/GitHub-Repository-222222?style=for-the-badge&logo=github)](https://github.com/AJARETRO/RetroWorldPurger)
[![Hangar](https://img.shields.io/badge/Hangar-Release-005f73?style=for-the-badge&logo=papermc)](https://hangar.papermc.io/AJA_RETRO/RetroWorldPurger)

**A lightweight, high-performance chunk purger and storage optimizer designed for Spigot, Paper, Folia, and all downstream forks.**

---

## ⚡ What is RetroWorldPurger?
Over time, Minecraft servers suffer from severe **region file storage bloat** because the server saves every chunk a player has ever generated or loaded, even if they just flew through it without touching a single block.

**RetroWorldPurger** solves this problem by monitoring player interactions. When a chunk unloads, the plugin checks if it remains unmodified. If no modifications have occurred and players didn't idle inside it, the plugin discards the chunk data so it doesn't get saved to disk. When a player returns to that area, the chunk generates fresh from the world seed.

---

## ✨ Features
* **Smart Chunk Purging:** Prevents empty or unmodified chunks from bloating region files on disk.
* **Comprehensive Modification Detection:**
  * **Direct Actions:** Block placing, block breaking, fires, and containers (chests, furnaces, etc.) opening.
  * **Pistons & Gravity:** Tracks blocks pushed/retracted by pistons across chunk boundaries and falling sand/gravel.
  * **Fluids & Flows:** Tracks water or lava flowing from a player-modified chunk into a clean chunk.
  * **Dispensers & Projectiles:** Detects items dispensed or arrows landing across borders.
  * **Explosions & Trees:** Flags all chunks affected by explosions (TNT, Creepers, Beds) or large tree sapling growth.
  * **Lightning Strikes:** Protects lightning-induced fires and skeleton horse traps from getting wiped.
* **Folia-Native & Highly Optimized:**
  * **Constant-Time O(1) Lookups:** Utilizes a pre-cached dynamic `EnumSet` for block material type checks instead of slow string operations.
  * **No Math Overheads:** Replaces heavy square-root calculations (`Math.sqrt`) with direct squared distance coordinates comparison.
  * **Early Exit Logic:** Checks chunk states first and terminates early if a chunk is already flagged, saving valuable region thread CPU cycles.
* **Configurable Player Residency Limit:** Define exactly how long a player must inhabit a chunk (in ticks) before the server saves it permanently.

---

## ⚙️ Configuration (`config.yml`)
```yaml
# Worlds that should be excluded from chunk cleaning
blacklisted-worlds:
  - "lobby"

# Distance from the world spawn (in blocks) where chunks are never purged
spawn-protection-radius: 500

# Minimum player presence (in ticks) inside a chunk before it gets saved permanently
# 200 ticks = 10 seconds.
min-inhabited-time-ticks: 200

# Automatically populated stats tracker
stats:
  total-chunks-deleted: 0
  total-bytes-saved: 0
```

---

## 📦 Compatibility & Requirements
* **Supported Platforms:** Spigot, PaperMC, Folia, Purpur, Pufferfish, and all their forks.
* **Supported Minecraft Versions:** Minecraft **1.19.4** through **1.26.3**.
* **Java Version:** **Java 17** or newer.

---

## 📊 bStats Telemetry
We use [bStats](https://bstats.org/plugin/bukkit/RetroWorldPurger/28479) (ID: 28479) to gather anonymous usage metrics. You can opt out at any time in the `plugins/bStats/config.yml` folder.

---

## ⚖️ License
**Copyright © 2026 AJA_RETRO (RetroWorldPurger). All Rights Reserved.**

Licensed under the **Source-Available License**. See [LICENSE.md](LICENSE.md) for full terms.
