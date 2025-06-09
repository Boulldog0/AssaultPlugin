package fr.Boulldogo.AssaultPlugin.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager.AssaultSide;

public class KillInAssaultEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private int givenPoints;
    private AssaultSide side;
	
	public KillInAssaultEvent(Player player, int givenPoints, AssaultSide side) {
		this.player = player;
		this.givenPoints = givenPoints;
		this.side = side;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public int getGivenPoints() {
		return givenPoints;
	}
	
	public AssaultSide getSide() {
		return side;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
