package fr.Boulldogo.AssaultPlugin.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;

public class PlayerDisconnectInAssaultEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Faction playerSideFaction;
    private Faction enemySideFaction;
	
	public PlayerDisconnectInAssaultEvent(Player player, Faction playerSideFaction, Faction enemySideFaction) {
		this.player = player;
		this.playerSideFaction = playerSideFaction;
		this.enemySideFaction = enemySideFaction;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Faction getPlayerSideFaction() {
		return playerSideFaction;
	}
	
	public Faction getEnemySideFaction() {
		return enemySideFaction;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
