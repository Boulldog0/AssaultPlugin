package fr.Boulldogo.AssaultPlugin.Events;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;

public class AssaultStopEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
    private Faction winnerFaction;
    private Faction looserFaction;
    private List<Faction> winnerJoinFactions;
    private List<Faction> looserJoinFactions;
    private int winnerScore;
    private int looserScore;
	
	public AssaultStopEvent(Faction winnerFaction, Faction looserFaction, List<Faction> winnerJoinFactions, List<Faction> looserJoinFactions, int winnerScore, int looserScore) {
		this.winnerFaction = winnerFaction;
		this.looserFaction = looserFaction;
		this.winnerJoinFactions = winnerJoinFactions;
		this.looserJoinFactions = looserJoinFactions;
		this.winnerScore = winnerScore;
		this.looserScore = looserScore;
	}
	
	public Faction getWinnerFaction() {
		return winnerFaction;
	}

	public Faction getLooserFaction() {
		return looserFaction;
	}
	
	public List<Faction> getWinnerJoinFactions() {
		return winnerJoinFactions;
	}
	
	public List<Faction> getLooserJoinFactions() {
		return looserJoinFactions;
	}
	
	public int getWinnerScore() {
		return winnerScore;
	}
	
	public int getLooserScore() {
		return looserScore;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
