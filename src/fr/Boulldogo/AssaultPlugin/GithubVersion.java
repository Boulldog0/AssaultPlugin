package fr.Boulldogo.AssaultPlugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubVersion implements Listener {

    private static final String GITHUB_API_URL = "https://api.github.com/repos/{OWNER}/{REPO}/releases/latest";
    private final Plugin plugin;
    private final String currentVersion;
    private final String latestVersionUrl;

    public GithubVersion(Plugin plugin, String owner, String repo) {
        this.plugin = plugin;
        this.currentVersion = plugin.getDescription().getVersion();
        this.latestVersionUrl = GITHUB_API_URL.replace("{OWNER}", owner).replace("{REPO}", repo);
    }

    public void checkForUpdates() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(latestVersionUrl).openConnection();
                    connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }

                    reader.close();
                    String response = responseBuilder.toString();
                    String latestVersion = parseLatestVersion(response).replace("v", "");

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        plugin.getLogger().warning("A new version of the plugin is available: " + latestVersion + " (You are running " + currentVersion + ")");
                        plugin.getLogger().warning("Download link : https://api.github.com/repos/Boulldogo/AssaultPlugin/releases/latest");
                    } else {
                        plugin.getLogger().info("You are running the latest version of the plugin AssaultPlugin.");
                    }

                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to check for updates for AssaultPlugin: " + e.getMessage());
                }
            }
        });
    }

    private String parseLatestVersion(String json) {
        String[] lines = json.split(",");
        for (String line : lines) {
            if (line.trim().startsWith("\"tag_name\"")) {
                return line.split(":")[1].replace("\"", "").trim();
            }
        }
        return null;
    }

    private boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int latestPart = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? Integer.parseInt(currentParts[i]) : 0;

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }

        return false;
    }
    
    public boolean isCurrentVersionUpToDate() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(latestVersionUrl).openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }

            reader.close();
            String response = responseBuilder.toString();
            String latestVersion = parseLatestVersion(response).replace("v", "");

            return currentVersion.equals(latestVersion);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to check for updates for AssaultPlugin: " + e.getMessage());
            return false;
        }
    }

    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
    	Player player = e.getPlayer();
		
		if(player.hasPermission("assault.updates")) {
			if(!isCurrentVersionUpToDate()) {
				player.sendMessage(ChatColor.RED + "A new version of plugin AssaultPlugin is available !");
				player.sendMessage(ChatColor.RED + "Download it at : https://api.github.com/repos/Boulldogo/AssaultPlugin/releases/latest");
			}
		}
    }


}


