package fr.Boulldogo.AssaultPlugin.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.Main;
import fr.Boulldogo.AssaultPlugin.Commands.AssaultCommand;

public class InteractListener implements Listener {

    private final Main plugin;

    public InteractListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        Player player = e.getPlayer();
        ItemStack stack = e.getItem();
        Block block = e.getClickedBlock();

        if(stack != null) {
            Material material = stack.getType();
			String id = String.valueOf(material);
            if(!player.hasPermission("assault.bypass-restricted.items_interact")) {
                if(plugin.getConfig().getStringList("interaction-item-restricted-in-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.restricted-item-in-assault")));
                        }
                    }
                }

                if(plugin.getConfig().getStringList("interaction-item-restricted-outside-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(!isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.restricted-item-outside-assault")));
                        }
                    }
                }
            }
        }

        if(block != null) {
            Material material = block.getType();
			String id = String.valueOf(material);
            if(!player.hasPermission("assault.bypass-restricted.block_interact")) {
                if(plugin.getConfig().getStringList("interaction-block-restricted-in-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.restricted-block-in-assault")));
                        }
                    }
                }

                if(plugin.getConfig().getStringList("interaction-block-restricted-outside-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(!isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.restricted-block-outside-assault")));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        Block block = e.getBlock();
        Player player = e.getPlayer();
        if(block != null) {
            Material material = block.getType();
			String id = String.valueOf(material);
            if(!player.hasPermission("assault.bypass-restricted.block-place")) {
                if(plugin.getConfig().getStringList("block-place-restricted-in-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.block-place-in-assault")));
                        }
                    }
                }

                if(plugin.getConfig().getStringList("block-place-restricted-outside-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(!isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.block-place-outside-assault")));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        String prefix = plugin.getConfig().getBoolean("use-prefix") ? translateString(plugin.getConfig().getString("prefix")) : "";
        Block block = e.getBlock();
        Player player = e.getPlayer();
        if(block != null) {
            Material material = block.getType();
			String id = String.valueOf(material);
            if(!player.hasPermission("assault.bypass-restricted.block-break")) { 
                if(plugin.getConfig().getStringList("block-break-restricted-in-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.block-break-in-assault")));
                        }
                    }
                }

                if(plugin.getConfig().getStringList("block-break-restricted-outside-assault").contains(id)) {
                    Faction faction = FPlayers.getInstance().getByPlayer(player).getFaction();
                    if(!faction.isWilderness()) {
                        if(!isInAssault(faction)) {
                            e.setCancelled(true);
                            player.sendMessage(prefix + translateString(plugin.getConfig().getString("messages.block-break-outside-assault")));
                        }
                    }
                }
            }
        }
    }

    public boolean isInAssault(Faction faction) {
        return AssaultCommand.attackAssaultList.contains(faction)
                || AssaultCommand.defenseAssaultList.contains(faction)
                || AssaultCommand.attackJoinList.contains(faction)
                || AssaultCommand.defenseJoinList.contains(faction);
    }

    public String translateString(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
