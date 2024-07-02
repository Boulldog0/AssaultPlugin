package fr.Boulldogo.AssaultPlugin.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;

public class AssaultStartEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
	private Faction attackFaction;
	private Faction defenseFaction;
	private Player whoStartAssault;
	
	public AssaultStartEvent(Faction attackFaction, Faction defenseFaction, Player whoStartAssault) {
		this.attackFaction = attackFaction;
		this.defenseFaction = defenseFaction;
		this.whoStartAssault = whoStartAssault;
	}
	
	public Faction getAttackFaction() {
		return attackFaction;
	}
	
	public Faction getDefenseFaction() {
		return defenseFaction;
	}
	
	public Player getPlayerWhoStartAssault() {
		return whoStartAssault;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
