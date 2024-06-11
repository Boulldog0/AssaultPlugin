package fr.Boulldogo.AssaultPlugin.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;

public class AssaultLooseEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
	private Faction looseFaction;
	private int looseScore;
	private int eloPointsRemoved;
	
	public AssaultLooseEvent(Faction looseFaction, int looseScore, int eloPointsRemoved) {
		this.looseFaction = looseFaction;
		this.looseScore = looseScore;
		this.eloPointsRemoved = eloPointsRemoved;
	}
	
	public Faction getlooseFaction() {
		return looseFaction;
	}
	
	public int getlooseScore() {
		return looseScore;
	}
	
	public int getEloPointsRemoved() {
		return eloPointsRemoved;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
