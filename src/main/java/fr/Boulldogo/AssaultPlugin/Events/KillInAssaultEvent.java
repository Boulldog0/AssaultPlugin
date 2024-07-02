package fr.Boulldogo.AssaultPlugin.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;

public class KillInAssaultEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private int givenPoints;
    private Faction playerSideFaction;
    private Faction enemySideFaction;
	
	public KillInAssaultEvent(Player player, int givenPoints, Faction playerSideFaction, Faction enemySideFaction) {
		this.player = player;
		this.givenPoints = givenPoints;
		this.playerSideFaction = playerSideFaction;
		this.enemySideFaction = enemySideFaction;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getGivenPoints() {
		return givenPoints;
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
