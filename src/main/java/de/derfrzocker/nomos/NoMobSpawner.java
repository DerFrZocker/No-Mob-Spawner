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

package de.derfrzocker.nomos;

import de.derfrzocker.nomos.impl.DefaultWorldHandler;
import de.derfrzocker.nomos.impl.v1_16_R2.WorldHandler_v1_16_R2;
import de.derfrzocker.spigot.utils.Config;
import de.derfrzocker.spigot.utils.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class NoMobSpawner extends JavaPlugin {

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

    private final Map<String, SpawnerConfiguration> worldSpawnerConfigurations = new HashMap<>();

    @NotNull
    private SpawnerConfiguration defaultSpawnerConfiguration = new SpawnerConfiguration("dummy", new HashMap<>(), NoMobSpawnerAction.KEEP, null);

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // we don't use #getConfig(), since it loads the default config of the jar as default values
        Config config = new Config(new File(getDataFolder(), "config.yml"));


        ConfigurationSection configurations = config.getConfigurationSection("configurations");
        if (configurations != null) {
            readSpawnerConfigurations(configurations);
        }

        ConfigurationSection defaultConfiguration = config.getConfigurationSection("default-configuration");
        if (defaultConfiguration != null) {
            defaultSpawnerConfiguration = readConfiguration(defaultConfiguration);
        }

        Version current = Version.getCurrent();

        if (current == Version.v1_16_R2) {
            new WorldHandler_v1_16_R2(this);
        } else {
            new DefaultWorldHandler(this);
            getLogger().warning("You are running a not directly supported version!");
            getLogger().warning("The plugin should work anyway, but the performance is not as good as direct supported versions");
            getLogger().warning("Also only the action 'REPLACE' will function");
        }

        new Metrics(this, 8692);

    }

    /**
     * @param worldName of the world
     * @return the SpawnerConfiguration of the world, or the default one
     */
    @NotNull
    public SpawnerConfiguration getSpawnerConfiguration(String worldName) {
        return worldSpawnerConfigurations.getOrDefault(worldName, defaultSpawnerConfiguration);
    }

    private void readSpawnerConfigurations(ConfigurationSection configurationSection) {
        Set<String> configurationKeys = configurationSection.getKeys(false);

        for (String configurationKey : configurationKeys) {
            ConfigurationSection section = configurationSection.getConfigurationSection(configurationKey);

            // loading worlds
            Set<String> worlds = new HashSet<>();

            if (section.isList("active-in-world")) {
                List<String> list = section.getStringList("active-in-world");

                for (String world : list) {
                    if (!worlds.add(world)) {
                        getLogger().warning("The Configuration '" + configurationKey + "' contains the world '" + world + "' multiple times in the list 'active-in-world'");
                    }
                }
            } else {
                worlds.add(section.getString("active-in-world"));
            }

            SpawnerConfiguration configuration = readConfiguration(section);

            for (String world : worlds) {
                SpawnerConfiguration previous = worldSpawnerConfigurations.put(world, configuration);

                if (previous != null) {
                    getLogger().warning("There a multiple Configurations for the world '" + world + "'");
                    getLogger().warning("Previous Configuration: '" + previous + "'");
                    getLogger().warning("New Configuration: '" + configuration + "'");
                }
            }
        }

    }

    @NotNull
    private SpawnerConfiguration readConfiguration(ConfigurationSection section) {
        // loading entity types and values
        ConfigurationSection entitySection = section.getConfigurationSection("types");

        Map<EntityType, Boolean> entityTypes = new HashMap<>();

        if (entitySection != null) {
            Set<String> entityKeys = entitySection.getKeys(false);

            for (String entityKey : entityKeys) {
                try {
                    EntityType entityType = EntityType.valueOf(entityKey.toUpperCase());
                    entityTypes.put(entityType, entitySection.getBoolean(entityKey));
                } catch (IllegalArgumentException e) {
                    getLogger().warning("The Configuration '" + buildPath(section) + "' contains an unknown EntityType, ignoring it. Unknown EntityType: '" + entityKey + "'");
                }

            }
        }

        // loading action
        String actionString = section.getString("action");
        NoMobSpawnerAction noMobSpawnerAction;

        if (actionString == null) {
            getLogger().warning("The Configuration '" + buildPath(section) + "' does not contain an 'action', using default action '" + NoMobSpawnerAction.KEEP + "'");
            noMobSpawnerAction = NoMobSpawnerAction.KEEP;
        } else {
            try {
                noMobSpawnerAction = NoMobSpawnerAction.valueOf(actionString.toUpperCase());
            } catch (IllegalArgumentException e) {
                getLogger().warning("The Configuration '" + buildPath(section) + "' contains an unknown action, using default action '" + NoMobSpawnerAction.KEEP + "'. Unknown 'action': '" + actionString + "'");
                getLogger().warning("The allowed actions are: " + getAllowedAction());

                noMobSpawnerAction = NoMobSpawnerAction.KEEP;
            }
        }

        Material material = null;

        if (noMobSpawnerAction == NoMobSpawnerAction.REPLACE) {
            String materialString = section.getString("replace-material");

            if (materialString == null) {
                getLogger().warning("The Configuration '" + buildPath(section) + "' does not contain an 'replace-material', using default material '" + AIR + "'");
                getLogger().warning("A 'replace-material' must be present when using the action '" + NoMobSpawnerAction.REPLACE + "'");
                material = AIR;
            } else {
                try {
                    material = Material.valueOf(materialString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    getLogger().warning("The Configuration '" + buildPath(section) + "' contains an unknown replace material, using default replace material '" + AIR + "'. Unknown 'replace-material': '" + materialString + "'");

                    material = AIR;
                }
            }
        }

        return new SpawnerConfiguration(buildPath(section), entityTypes, noMobSpawnerAction, material);
    }

    private String getAllowedAction() {
        StringBuilder stringBuilder = new StringBuilder();

        boolean first = true;

        for (NoMobSpawnerAction action : NoMobSpawnerAction.values()) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(" ");
            }

            stringBuilder.append("'");
            stringBuilder.append(action);
            stringBuilder.append("'");
        }

        return stringBuilder.toString();
    }

    private String buildPath(ConfigurationSection configurationSection) {
        ConfigurationSection parent = configurationSection.getParent();
        StringBuilder builder = new StringBuilder(configurationSection.getName());

        while (parent != null) {
            builder.insert(0, '.');
            builder.insert(0, parent.getName());

            parent = parent.getParent();
        }

        if (builder.charAt(0) == '.') {
            builder.substring(1);
        }

        return builder.toString();
    }


}
