package fr.Boulldogo.AssaultPlugin.Events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.massivecraft.factions.Faction;

public class AssaultWinEvent extends Event {
	
    private static final HandlerList handlers = new HandlerList();
	private Faction winnerFaction;
	private int winnerScore;
	private int eloPointsEarned;
	
	public AssaultWinEvent(Faction winnerFaction, int winnerScore, int eloPointsEarned) {
		this.winnerFaction = winnerFaction;
		this.winnerScore = winnerScore;
		this.eloPointsEarned = eloPointsEarned;
	}
	
	public Faction getwinnerFaction() {
		return winnerFaction;
	}
	
	public int getWinnerScore() {
		return winnerScore;
	}
	
	public int getEloPointsEarned() {
		return eloPointsEarned;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
