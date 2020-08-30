/*
 * MIT License
 *
 * Copyright (c) 2020 Marvin (DerFrZocker)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.derfrzocker.nomos.impl;

import de.derfrzocker.nomos.NoMobSpawner;
import de.derfrzocker.nomos.SpawnerConfiguration;
import de.derfrzocker.nomos.WorldHandler;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.generator.BlockPopulator;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DefaultWorldHandler extends BlockPopulator implements WorldHandler, Listener {

    @NotNull
    private static final Material AIR;

    // older version of Minecraft don't have 'CAVE_AIR'
    static {
        Material temp;

        try {
            temp = Material.valueOf("CAVE_AIR");
        } catch (IllegalArgumentException e) {
            temp = Material.AIR;
        }

        AIR = temp;
    }

    @NotNull
    private final NoMobSpawner noMobSpawner;

    public DefaultWorldHandler(@NotNull NoMobSpawner noMobSpawner) {
        Validate.notNull(noMobSpawner, "NoMobSpawner cannot be null");

        this.noMobSpawner = noMobSpawner;

        Bukkit.getPluginManager().registerEvents(this, noMobSpawner);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onWorldLoad(WorldInitEvent event) {
        event.getWorld().getPopulators().add(this);
    }

    @Override
    public void populate(@NotNull World world, @NotNull Random random, @NotNull Chunk source) {
        BlockState[] tileEntities = source.getTileEntities();

        if (tileEntities.length == 0) {
            return;
        }

        SpawnerConfiguration configuration = noMobSpawner.getSpawnerConfiguration(world.getName());

        // The DefaultWorldHandler only supports replace action
        Material replaceMaterial = configuration.getReplaceMaterial();

        if (replaceMaterial == null) {
            replaceMaterial = AIR;
        }

        for (BlockState tileEntity : tileEntities) {
            if (tileEntity instanceof CreatureSpawner) {
                CreatureSpawner spawner = (CreatureSpawner) tileEntity;

                boolean spawn = configuration.getTypes().getOrDefault(spawner.getSpawnedType(), true);

                if (!spawn) {
                    tileEntity.getBlock().setType(replaceMaterial, true);
                }

            }
        }
    }

}
