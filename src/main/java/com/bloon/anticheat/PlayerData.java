package com.bloon.anticheat;

import java.util.UUID;

public final class PlayerData {
    private final UUID uuid;
    private double triggerBotBuffer;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public double getTriggerBuffer() {
        return triggerBotBuffer;
    }

    public void addTriggerBuffer(double amount) {
        if (amount > 0) {
            triggerBotBuffer += amount;
        }
    }

    public void reduceTriggerBuffer(double amount) {
        if (amount > 0) {
            triggerBotBuffer = Math.max(0, triggerBotBuffer - amount);
        }
    }

    public void resetBuffer() {
        triggerBotBuffer = 0;
    }
}
