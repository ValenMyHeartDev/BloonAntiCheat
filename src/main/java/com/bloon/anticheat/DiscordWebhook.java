package com.bloon.anticheat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    public static void sendAlert(String webhookUrl, String playerName, String checkName, int vl) {
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("TU_WEBHOOK")) return;

        // Ejecutar de forma asíncrona para no congelar el servidor al realizar la petición web
        new Thread(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "json/application; charset=UTF-8");
                conn.setRequestProperty("User-Agent", "BloonAC-Bot");
                conn.setDoOutput(true);

                String jsonPayload = "{"
                        + "\"content\": null,"
                        + "\"embeds\": [{"
                        + "\"title\": \"🚨 Alerta de Seguridad - BloonAC\","
                        + "\"color\": 15158332,"
                        + "\"fields\": ["
                        + "{\"name\": \"Jugador\", \"value\": \"" + playerName + "\", \"inline\": true},"
                        + "{\"name\": \"Módulo\", \"value\": \"" + checkName + "\", \"inline\": true},"
                        + "{\"name\": \"Nivel (VL)\", \"value\": \"" + vl + "\", \"inline\": true}"
                        + "],"
                        + "\"footer\": {\"text\": \"BloonAC - Sistema de Protección Avanzada\"}"
                        + "}]"
                        + "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                conn.getResponseCode();
                conn.disconnect();
            } catch (Exception ignored) {}
        }).start();
    }
}
