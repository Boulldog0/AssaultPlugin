package fr.Boulldogo.AssaultPlugin.Utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubVersion {

    private final JavaPlugin plugin;
    private final String currentVersion;
    private final String apiUrl;

    public GithubVersion(JavaPlugin plugin, String currentVersion, String apiUrl) {
        this.plugin = plugin;
        this.currentVersion = currentVersion;
        this.apiUrl = apiUrl;
    }

    public void checkVersion() {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            JSONObject json = (JSONObject) JSONValue.parse(reader);

            String latestVersion = (String) json.get("tag_name");

            if (!latestVersion.equals(currentVersion)) {
                plugin.getLogger().warning("New version of Assault Plugin is avaiable : " + latestVersion);
                plugin.getLogger().warning("Download it at : https://www.spigotmc.org/resources/assaultplugin.116864/");
                plugin.getConfig().set("latest-version", latestVersion);
                plugin.saveConfig();
            } else {
                plugin.getLogger().info("Plugin Assault is up to date (version " + currentVersion + ")");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
