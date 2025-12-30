package com.dkprojects.dkklan.managers;

import com.dkprojects.dkklan.DkKlan;
import com.dkprojects.dkklan.war.War;
import com.dkprojects.dkklan.war.WarType;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordManager {
    private final DkKlan plugin;
    private String webhookUrl;

    public DiscordManager(DkKlan plugin) {
        this.plugin = plugin;
        this.webhookUrl = plugin.getConfig().getString("discord.webhook_url", "");
    }

    public void sendWarStart(War war) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String format = war.getType().name();
                    com.dkprojects.dkklan.war.MatchSeries series = plugin.getWarManager().getActiveSeries(war.getClanA());
                    if (series != null) {
                        format = "BO" + series.getBestOf();
                    }

                    String json = String.format(
                        "{\"content\": null, \"embeds\": [{" +
                        "\"title\": \"🏆 KLANSAVAŞI BAŞLADI\"," +
                        "\"description\": \"**%s** vs **%s**\\n\\n**Format:** %s | %dv%d\\n**Arena:** %s\"," +
                        "\"color\": 16753920" +
                        "}]}",
                        war.getClanA(), war.getClanB(),
                        format,
                        war.getTeamA().getPlayers().size(), war.getTeamB().getPlayers().size(),
                        war.getArena().getName()
                    );
                    sendPayload(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public void sendWarEnd(War war, String winner, String reason, String mvp) {
        if (webhookUrl == null || webhookUrl.isEmpty()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String score = war.getScoreA() + "-" + war.getScoreB(); // Match score
                    String titleSuffix = "";
                    
                    com.dkprojects.dkklan.war.MatchSeries series = plugin.getWarManager().getActiveSeries(war.getClanA());
                    if (series != null) {
                        score = series.getWinsA() + "-" + series.getWinsB(); // Series score
                        titleSuffix = " (" + score + ")";
                    }

                    String json = String.format(
                        "{\"content\": null, \"embeds\": [{" +
                        "\"title\": \"🎉 KAZANAN: %s%s\"," +
                        "\"description\": \"**%s** vs **%s**\\n\\n**Skor:** %s\\n**Sebep:** %s\\n**MVP:** %s\"," +
                        "\"color\": 65280" +
                        "}]}",
                        winner,
                        titleSuffix,
                        war.getClanA(), war.getClanB(),
                        score,
                        reason,
                        mvp != null ? mvp : "Yok"
                    );
                    sendPayload(json);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void sendPayload(String jsonPayload) throws Exception {
        URL url = new URL(webhookUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        conn.getResponseCode(); // Trigger request
        conn.disconnect();
    }
}
