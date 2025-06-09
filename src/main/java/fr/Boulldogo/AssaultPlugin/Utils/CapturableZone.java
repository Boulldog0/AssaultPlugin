package fr.Boulldogo.AssaultPlugin.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;

public class CapturableZone {
	
	public String prefix = AssaultPlugin.getInstance().getConfig().getBoolean("use-prefix") ? translateString(AssaultPlugin.getInstance().getConfig().getString("prefix")) : "";
	private Location loc;
	private Faction territory;
	private int radius;
	private int height;
	private Particle particles;
	private List<Player> insidePlayers;
	
	public CapturableZone(Location loc, Faction territory, int radius, int height, @Nullable Particle particles) {
		this.loc = loc;
		this.radius = radius;
		this.particles = Particle.FLAME;
		this.height = height;
		this.insidePlayers = new ArrayList<>();
		this.territory = territory;
	}
	
	public Location getLoc() {
		return loc;
	}
	
	public int getRadius() {
		return radius;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void addPlayer(Player player) {
		this.insidePlayers.add(player);
		player.sendMessage(prefix + translateString(AssaultPlugin.getInstance().getConfig().getString("messages.you-enter-in-capture-zone")));
	}
	
	public void removePlayer(Player player) {
		player.sendMessage(prefix + translateString(AssaultPlugin.getInstance().getConfig().getString("messages.you-leave-capture-zone")));
		this.insidePlayers.remove(player);
	}
	
	public boolean isPlayerInside(Player player) {
		return this.insidePlayers.contains(player);
	}
	
	public Faction getTerritory() {
		return territory;
	}
	
	public List<Player> getPlayers() {
		return insidePlayers;
	}

	public void renderZone() {
	    World world = loc.getWorld();
	    int x0 = loc.getBlockX() - radius;
	    int x1 = loc.getBlockX() + radius;
	    int z0 = loc.getBlockZ() - radius;
	    int z1 = loc.getBlockZ() + radius;
	    int y0 = loc.getBlockY();

	    for(int y = y0; y <= y0 + height; y++) {
	        for(int x = x0; x <= x1; x++) {
	            world.spawnParticle(particles, x, y, z0, 5, 0.1, 0.1, 0.1, 0.01, null);
	            world.spawnParticle(particles, x, y, z1, 5, 0.1, 0.1, 0.1, 0.01, null);
	        }

	        for(int z = z0; z <= z1; z++) {
	            world.spawnParticle(particles, x0, y, z, 5, 0.1, 0.1, 0.1, 0.01, null);
	            world.spawnParticle(particles, x1, y, z, 5, 0.1, 0.1, 0.1, 0.01, null);
	        }
	    }
	}
	
	public String translateString(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
}
