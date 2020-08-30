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

package de.derfrzocker.nomos.impl.v1_16_R2;

import net.minecraft.server.v1_16_R2.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RegionLimitWorldAccessOverrider extends RegionLimitedWorldAccess {

    private final Map<BlockPosition, IBlockData> tempStorage = new ConcurrentHashMap<>();
    private final Map<BlockPosition, TileEntity> tileEntitys = new ConcurrentHashMap<>();

    public RegionLimitWorldAccessOverrider(WorldServer worldserver, List<IChunkAccess> list) {
        super(worldserver, list);
    }

    public Map<BlockPosition, IBlockData> getTempStorage() {
        return tempStorage;
    }

    public Map<BlockPosition, TileEntity> getTileEntitys() {
        return tileEntitys;
    }

    @Override
    public IBlockData getType(BlockPosition blockposition) {
        IBlockData iBlockData = tempStorage.get(blockposition);

        if (iBlockData != null) {
            return iBlockData;
        }

        return super.getType(blockposition);
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPosition blockposition) {
        blockposition = blockposition.immutableCopy();

        if (!tempStorage.containsKey(blockposition)) {
            return super.getTileEntity(blockposition);
        }

        TileEntity tileEntity = tileEntitys.get(blockposition);

        if (tileEntity == null) {
            tileEntity = ((ITileEntity) tempStorage.get(blockposition).getBlock()).createTile(getMinecraftWorld());
            tileEntitys.put(blockposition, tileEntity);
        }

        return tileEntity;
    }

    @Override
    public boolean setTypeAndData(BlockPosition blockposition, IBlockData iblockdata, int i) {
        if (iblockdata.getBlock() == Blocks.SPAWNER) {
            tempStorage.put(blockposition.immutableCopy(), iblockdata);
            tileEntitys.remove(blockposition.immutableCopy());
            return true;
        }

        if (tempStorage.containsKey(blockposition.immutableCopy())) {
            tempStorage.remove(blockposition.immutableCopy());
            tileEntitys.remove(blockposition.immutableCopy());
        }

        return super.setTypeAndData(blockposition, iblockdata, i);
    }

}
