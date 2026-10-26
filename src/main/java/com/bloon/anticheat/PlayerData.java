package com.bloon.anticheat;

import org.bukkit.Location;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private double triggerBuffer = 0.0;
    private Location lastSafeLocation;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getTriggerBuffer() {
        return triggerBuffer;
    }

    public void setTriggerBuffer(double triggerBuffer) {
        this.triggerBuffer = triggerBuffer;
    }

    public void addTriggerBuffer(double amount) {
        this.triggerBuffer += amount;
    }

    public Location getLastSafeLocation() {
        return lastSafeLocation;
    }

    public void setLastSafeLocation(Location lastSafeLocation) {
        this.lastSafeLocation = lastSafeLocation;
    }
}
