/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.samistine.emptyplotfinder;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 *
 * @author Samuel
 */
public class ChunkUtils {

    public FoundChunks findChunksInArea(World world, int bottomX, int topX, int bottomZ, int topZ) {
        List<Chunk> chunks = new LinkedList<>();

        int minChunkX = (int) Math.floor((double) bottomX / 16);
        int maxChunkX = (int) Math.floor((double) topX / 16);
        int minChunkZ = (int) Math.floor((double) bottomZ / 16);
        int maxChunkZ = (int) Math.floor((double) topZ / 16);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                Chunk chunk = world.getChunkAt(cx, cz);
                chunks.add(chunk);
                //System.out.println("bottomX=" + bottomX + ", topX=" + topX + ", bottomZ=" + bottomZ + ", topZ=" + topZ);
            }
        }

        List<Chunk> completeChunks = new LinkedList<>();
        List<Chunk> partialChunks = new LinkedList<>();

        for (Chunk chunk : chunks) {
            if (isChunkInsideOrAtParameters(chunk, bottomX, topX, bottomZ, topZ)) {
                completeChunks.add(chunk);
            } else {
                partialChunks.add(chunk);
            }
        }

        return new FoundChunks(completeChunks, partialChunks);
    }

    public boolean isChunkInsideOrAtParameters(Chunk chunk, int bottomX, int topX, int bottomZ, int topZ) {
        Block b1 = chunk.getBlock(0, 0, 0);
        Block b2 = chunk.getBlock(0, 0, 15);
        Block b3 = chunk.getBlock(15, 0, 0);
        Block b4 = chunk.getBlock(15, 0, 15);
        return isBlockInsideOrAtParameters(b1, bottomX, topX, bottomZ, topZ)
                && isBlockInsideOrAtParameters(b2, bottomX, topX, bottomZ, topZ)
                && isBlockInsideOrAtParameters(b3, bottomX, topX, bottomZ, topZ)
                && isBlockInsideOrAtParameters(b4, bottomX, topX, bottomZ, topZ);
    }

    public boolean isBlockInsideOrAtParameters(Block block, int bottomX, int topX, int bottomZ, int topZ) {
        return block.getX() >= bottomX
                && block.getX() <= topX
                && block.getZ() >= bottomZ
                && block.getZ() <= topZ;
    }

    public boolean isLocationInsideOrAtParameters(Location loc, int bottomX, int topX, int bottomZ, int topZ) {
        return loc.getBlockX() >= bottomX
                && loc.getBlockX() <= bottomZ
                && loc.getBlockZ() >= bottomZ
                && loc.getBlockZ() <= topZ;
    }

    public static class FoundChunks {

        @Override
        public String toString() {
            return "FoundChunks{" + "completeChunks=" + Arrays.toString(completeChunks.toArray()) + ", partialChunks=" + Arrays.toString(partialChunks.toArray()) + '}';
        }

        List<Chunk> completeChunks;
        List<Chunk> partialChunks;

        public FoundChunks(List<Chunk> completeChunks, List<Chunk> partialChunks) {
            this.completeChunks = completeChunks;
            this.partialChunks = partialChunks;
        }

        public List<Chunk> getCompleteChunks() {
            return completeChunks;
        }

        public List<Chunk> getPartialChunks() {
            return partialChunks;
        }

    }

    public void highlightArea(FoundChunks chunks, int y) {
        for (Chunk c : chunks.completeChunks) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block b = c.getBlock(x, y, z);
                    b.setType(Material.LAPIS_BLOCK);
                }
            }
        }
        for (Chunk c : chunks.partialChunks) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    Block b = c.getBlock(x, y, z);
                    b.setType(Material.REDSTONE_BLOCK);
                }
            }
        }
    }

}
