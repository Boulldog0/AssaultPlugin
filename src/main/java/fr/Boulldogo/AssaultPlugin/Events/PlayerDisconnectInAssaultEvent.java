package fr.Boulldogo.AssaultPlugin.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDisconnectInAssaultEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
    private Player player;
	
	public PlayerDisconnectInAssaultEvent(Player player) {
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
