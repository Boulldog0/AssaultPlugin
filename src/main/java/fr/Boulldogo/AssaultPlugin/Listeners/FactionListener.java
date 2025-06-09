package fr.Boulldogo.AssaultPlugin.Listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.event.FactionRelationWishEvent;
import com.massivecraft.factions.struct.Relation;

import fr.Boulldogo.AssaultPlugin.AssaultPlugin;
import net.md_5.bungee.api.ChatColor;

public class FactionListener implements Listener {
	
	private final AssaultPlugin plugin;
	
	public FactionListener(AssaultPlugin plugin) {
		this.plugin = plugin;
	}
	
    @EventHandler
    public void onRelationChange(FactionRelationWishEvent e) {
        Faction faction = e.getFaction();
        Faction targetFaction = e.getTargetFaction();
        Relation relation = e.getCurrentRelation();
        Relation targetRelation = e.getTargetRelation();

        if(plugin.getConfig().getBoolean("disable-neutral-without-confirmation")) {
            if(relation == Relation.ENEMY && targetRelation == Relation.NEUTRAL) {
                String targetTag = targetFaction.getTag();
                String factionTag = faction.getTag();

                if(plugin.getConfig().contains("pending-neutral." + targetTag + "." + factionTag)) {
                    plugin.getConfig().set("pending-neutral." + targetTag + "." + factionTag, null);
                    plugin.getConfig().set("pending-neutral." + factionTag + "." + targetTag, null);
                    plugin.saveConfig();
                } else {
                    e.setCancelled(true);
                    plugin.getConfig().set("pending-neutral." + factionTag + "." + targetTag, true);
                    plugin.saveConfig();
                    faction.getOnlinePlayers().forEach(member -> member.sendMessage(translateString(plugin.getConfig().getString("messages.neutral_request_pending_faction")
                    		.replace("%f", targetTag))));
                    
                    targetFaction.getOnlinePlayers().forEach(member -> member.sendMessage(translateString(plugin.getConfig().getString("messages.neutral_request_pending_target_faction")
                    		.replace("%f", factionTag))));
                }
            }
        }
    }
    
    public String translateString(String s) {
    	return ChatColor.translateAlternateColorCodes('&', s);
    }
}
