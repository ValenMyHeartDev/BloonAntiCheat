package com.bloon.anticheat;

/**
 * Datos temporales por jugador para acumular violaciones de TriggerBot.
 * Esta clase solo almacena el buffer; no implementa por sí sola detección.
 */
public final class PlayerData {
    private double triggerBotBuffer;

    public double getTriggerBotBuffer() {
        return triggerBotBuffer;
    }

    public void addTriggerBotViolation(double amount) {
        if (amount > 0) {
            triggerBotBuffer += amount;
        }
    }

    public void reduceTriggerBotBuffer(double amount) {
        if (amount > 0) {
            triggerBotBuffer = Math.max(0, triggerBotBuffer - amount);
        }
    }

    public void resetTriggerBotBuffer() {
        triggerBotBuffer = 0;
    }
}
