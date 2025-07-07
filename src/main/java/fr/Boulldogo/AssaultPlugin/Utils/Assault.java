package fr.Boulldogo.AssaultPlugin.Utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.massivecraft.factions.Faction;

import fr.Boulldogo.AssaultPlugin.Utils.AssaultManager.AssaultSide;

public class Assault {
	
	public Faction belligerentAttackFaction;
	public Faction belligerentDefenseFaction;
	public List<Faction> attackJoins;
	public List<Faction> defenseJoins;
	public List<Faction> waitingAttackJoins;
	public List<Faction> waitingDefenseJoins;
	public int attackScore;
	public int defenseScore;
	public int minutesRemaining;
	public int secondsRemaining;
	public String formattedStartTime;
	public Timestamp realStartTime;
	
	public Map<Player, AtomicInteger> attackTaggedPlayers;
	public Map<Player, AtomicInteger> defenseTaggedPlayers;
	
	public Map<String, AtomicInteger> attackDisconnectedPlayers;
	public Map<String, AtomicInteger> defenseDisconnectedPlayers;
	
	public CapturableZone zone;
	public AssaultSide zoneSide;
	public Location zoneWaypoint;
	
	@SuppressWarnings("deprecation")
	public Assault(Faction attackFac, Faction defenseFac) {
		this.belligerentAttackFaction = attackFac;
		this.belligerentDefenseFaction = defenseFac;
		this.attackJoins = new ArrayList<>();
		this.defenseJoins = new ArrayList<>();
		this.waitingAttackJoins = new ArrayList<>();
		this.waitingDefenseJoins = new ArrayList<>();
		this.realStartTime = Timestamp.from(Instant.now());
		this.formattedStartTime = (realStartTime.getHours() < 10 ? ("0" + realStartTime.getHours()) : realStartTime.getHours()) + ":" + (realStartTime.getMinutes() < 10 ? ("0" + realStartTime.getMinutes()) : realStartTime.getMinutes());	
		this.attackTaggedPlayers = new HashMap<>();
		this.defenseTaggedPlayers = new HashMap<>();
		this.attackDisconnectedPlayers = new HashMap<>();
		this.defenseDisconnectedPlayers = new HashMap<>();
	}
	
	public void changeZone(CapturableZone zone) {
		this.zone = zone;
	}
	
	public List<Player> getAllPlayers() {
		List<Player> allPlayers = new ArrayList<>();
		
        allPlayers.addAll(belligerentAttackFaction.getOnlinePlayers());
        allPlayers.addAll(belligerentDefenseFaction.getOnlinePlayers());
        attackJoins.forEach(f -> {
        	allPlayers.addAll(f.getOnlinePlayers());
        });
        defenseJoins.forEach(f -> {
        	allPlayers.addAll(f.getOnlinePlayers());
        });
        return allPlayers;
	}
	
	public List<Faction> getAllSide(AssaultSide side) {
		List<Faction> facs = new ArrayList<>();
		if(side.equals(AssaultSide.ATTACK)) {
			facs.add(belligerentAttackFaction);
			facs.addAll(attackJoins);
		} else {
			facs.add(belligerentDefenseFaction);
			facs.addAll(defenseJoins);
		}
		return facs;
	}
}
