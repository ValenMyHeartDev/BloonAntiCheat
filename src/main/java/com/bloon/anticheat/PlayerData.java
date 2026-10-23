package com.bloon.anticheat;

import java.util.UUID;

/**
 * Datos temporales por jugador para acumular violaciones de TriggerBot.
 */
public final class PlayerData {
    private final UUID uuid;
    private double triggerBotBuffer;

    // Constructor que requiere Main.java
    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    // --- Métodos compatibles con TriggerCheck.java ---

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

    // --- Alias de conveniencia por si se usan los nombres largos ---

    public double getTriggerBotBuffer() {
        return getTriggerBuffer();
    }

    public void addTriggerBotViolation(double amount) {
        addTriggerBuffer(amount);
    }

    public void reduceTriggerBotBuffer(double amount) {
        reduceTriggerBuffer(amount);
    }

    public void resetTriggerBotBuffer() {
        resetBuffer();
    }
}
