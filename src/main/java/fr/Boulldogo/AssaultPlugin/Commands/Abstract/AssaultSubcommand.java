package fr.Boulldogo.AssaultPlugin.Commands.Abstract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;

public abstract class AssaultSubcommand {
	
	public String prefix = AssaultPlugin.getInstance().getConfig().getBoolean("use-prefix") ? translateString(AssaultPlugin.getInstance().getConfig().getString("prefix")) : "";
	private String name;
	private String permission;
	private Player sender;
	private LinkedList<String> requiredArguments = new LinkedList<>();
	private Map<String, String> optionalArguments = new HashMap<>();
	
	private List<String> givenArguments = new ArrayList<>();
	
	public AssaultSubcommand setName(String name) {
		this.name = name;
		return this;
	}
	
	public AssaultSubcommand setPermission(String permission) {
		this.permission = permission;
		return this;
	}
	
	public AssaultSubcommand addRequiredArgument(String argument) {
		this.requiredArguments.add(argument);
		return this;
	}
	
	public AssaultSubcommand addOptionalArgument(String argument, String desc) {
		this.optionalArguments.put(argument, desc);
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean playerCanExecute(Player player) {
		return player.hasPermission("assault." + permission);
	}
	
	public LinkedList<String> getRequiredArguments() {
		return requiredArguments;
	}
	
	public Map<String, String> getOptionalArguments() {
		return optionalArguments;
	}
	
	public AssaultPlugin getPlugin() {
		return AssaultPlugin.getInstance();
	}
	
	public String translateString(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	
	public void setSender(Player player) {
		this.sender = player;
	}
	
	public void addArgument(String arg) {
		givenArguments.add(arg);
	}
	
	public boolean isArgSet(int idx) {
		return idx >= 0 && idx < givenArguments.size();
	}
	
	public String getArgAsString(int idx) {
		if(!isArgSet(idx + 1)) return null;
		return (String) givenArguments.get(idx + 1);
	}
	
	public int getArgAsInt(int idx) {
		if(!isArgSet(idx + 1)) return 0;
		try {
			return Integer.parseInt(givenArguments.get(idx + 1));
		} catch(Exception e) {
			sender.sendMessage(ChatColor.RED + "Argument at position " + (idx + 1) + " is invalid. Required : Integer.");
			return -1;
		}
	}
	
	public Faction getArgAsFaction(int idx) {
		if(!isArgSet(idx + 1)) return Factions.getInstance().getWilderness();
		if(givenArguments.get(idx + 1) instanceof String) {
			return Factions.getInstance().getBestTagMatch((String) givenArguments.get(idx + 1));
		}
		return Factions.getInstance().getWilderness();
	}
	
	public void resetCommand() {
		this.sender = null;
		this.givenArguments.clear();
	}
	
	public abstract void execute(Player player);
}
