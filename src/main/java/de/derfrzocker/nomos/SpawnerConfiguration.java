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

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SpawnerConfiguration {

    @Nullable
    private final String configurationSectionPath;
    @NotNull
    private final Map<EntityType, Boolean> types;
    @NotNull
    private final NoMobSpawnerAction action;
    @Nullable
    private final Material replaceMaterial;

    public SpawnerConfiguration(@Nullable String configurationSectionPath, @NotNull Map<EntityType, Boolean> types, @NotNull NoMobSpawnerAction action, @Nullable Material replaceMaterial) {
        this.configurationSectionPath = configurationSectionPath;
        this.types = types;
        this.action = action;
        this.replaceMaterial = replaceMaterial;
    }

    @NotNull
    public Map<EntityType, Boolean> getTypes() {
        return types;
    }

    @NotNull
    public NoMobSpawnerAction getAction() {
        return action;
    }

    @Nullable
    public Material getReplaceMaterial() {
        return replaceMaterial;
    }

}
