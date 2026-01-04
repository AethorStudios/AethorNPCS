package com.aethor.aethornpcs.api.dto;

import java.util.Objects;

/**
 * Represents a location in the game world.
 * This is a pure Java DTO that doesn't depend on Bukkit.
 */
public final class NpcLocation {

    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public NpcLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = Objects.requireNonNull(world, "world cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public NpcLocation(String world, double x, double y, double z) {
        this(world, x, y, z, 0.0f, 0.0f);
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NpcLocation that = (NpcLocation) o;
        return Double.compare(that.x, x) == 0 &&
               Double.compare(that.y, y) == 0 &&
               Double.compare(that.z, z) == 0 &&
               Float.compare(that.yaw, yaw) == 0 &&
               Float.compare(that.pitch, pitch) == 0 &&
               world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return String.format("NpcLocation{world=%s, x=%.2f, y=%.2f, z=%.2f, yaw=%.1f, pitch=%.1f}",
                world, x, y, z, yaw, pitch);
    }
}
