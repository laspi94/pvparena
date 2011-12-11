/*
 * player manager class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.4.0 - mayor rewrite, improved help
 */

package net.slipcor.pvparena.managers;

import java.util.HashMap;
import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.slipcor.pvparena.PAPlayer;

public class PlayerManager {
	// bets placed mapped to value: BetterName:BetName => Amount
	public HashMap<String, Double> paPlayersBetAmount = new HashMap<String, Double>();

	private HashMap<String, PAPlayer> players = new HashMap<String, PAPlayer>();
	private HashMap<String, Integer> kills = new HashMap<String, Integer>();
	private HashMap<String, Integer> deaths = new HashMap<String, Integer>();
	private DebugManager db = new DebugManager();

	public String getTeamStringList(HashMap<String, String> paTeams) {
		String result = "";
		for (PAPlayer p : players.values()) {
			if (p.getTeam() != null) {

				if (!result.equals(""))
					result += ", ";
				result += ChatColor.valueOf(paTeams.get(p.getTeam()))
						+ p.getName() + ChatColor.WHITE;
			}
		}
		return result;
	}

	public HashMap<String, String> getPlayerTeamMap() {
		HashMap<String, String> result = new HashMap<String, String>();
		for (PAPlayer p : players.values()) {
			if (p.getTeam() != null) {
				result.put(p.getName(), p.getTeam());
			}
		}
		return result;
	}

	/*
	 * returns "are the team counts equal?"
	 */
	public boolean checkEven() {
		HashMap<String, Integer> counts = new HashMap<String, Integer>();

		// count each team members
		for (PAPlayer p : players.values()) {
			if (p.getTeam() != null) {
				if (!counts.containsKey(p.getTeam())) {
					counts.put(p.getTeam(), 1);
				} else {
					int i = counts.get(p.getTeam());
					counts.put(p.getTeam(), i);
				}
			}
		}

		if (counts.size() < 1)
			return false; // noone there => not even

		int temp = -1;
		for (int i : counts.values()) {
			if (temp == -1) {
				temp = i;
				continue;
			}
			if (temp != i)
				return false; // different count => not even
		}
		return true; // every team as the same player count!
	}

	public int countPlayersInTeams() {
		int result = 0;
		for (PAPlayer p : players.values()) {
			if (p.getTeam() != null) {
				result++;
			}
		}
		return result;
	}

	public boolean existsPlayer(Player pPlayer) {
		return players.containsKey(pPlayer.getName());
	}

	public boolean ready() {
		if (countPlayersInTeams() < 1) {
			return false;
		}
		for (PAPlayer p : players.values()) {
			if (p.getTeam() != null) {
				if (p.getFightClass() == null) {
					// player not ready!
					return false;
				}
			}
		}
		return true;
	}

	public void reset() {
		for (PAPlayer p : players.values()) {
			p.setTeam(null);
			p.setClass(null);
			p.setLives((byte) 0);
			p.setSignLocation(null);
			paPlayersBetAmount.clear();
		}
	}

	/*
	 * tell every fighting player
	 */
	public void tellEveryone(String msg) {
		db.i("@all: " + msg);
		for (PAPlayer p : players.values()) {
			p.getPlayer().sendMessage(
					ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
		}
	}

	/*
	 * tell every fighting player except given player
	 */
	public void tellEveryoneExcept(Player player, String msg) {
		db.i("@all/" + player.getName() + ": " + msg);
		for (PAPlayer p : players.values()) {
			if (p.getPlayer().equals(player))
				continue;
			p.getPlayer().sendMessage(
					ChatColor.YELLOW + "[PVP Arena] " + ChatColor.WHITE + msg);
		}
	}

	public HashSet<PAPlayer> getPlayers() {
		return (HashSet<PAPlayer>) players.values();
	}

	public String getClass(Player player) {
		return players.get(player.getName()).getFightClass();
	}

	public void setClass(Player player, String s) {
		players.get(player.getName()).setClass(s);
	}

	public int getDeaths(String s) {
		return deaths.get(s);
	}

	public int getKills(String s) {
		return kills.get(s);
	}

	public void addKill(String sKiller) {
		if (kills.get(sKiller) != null) {
			kills.put(sKiller, kills.get(sKiller) + 1);
		} else {
			kills.put(sKiller, 1);
		}
	}

	public void addDeath(String sKilled) {
		if (deaths.get(sKilled) != null) {
			deaths.put(sKilled, deaths.get(sKilled) + 1);
		} else {
			deaths.put(sKilled, 1);
		}
	}

	public byte getLives(Player player) {
		return players.get(player.getName()).getLives();
	}

	public void setLives(Player player, byte l) {
		players.get(player.getName()).setLives(l);
	}

	public void setRespawn(Player player, boolean s) {
		players.get(player.getName()).setRespawn(s);
	}

	public String getRespawn(Player player) {
		return players.get(player.getName()).getRespawn();
	}

	public Location getSignLocation(Player player) {
		return players.get(player.getName()).getSignLocation();
	}

	public void setSignLocation(Player player, Location l) {
		players.get(player.getName()).setSignLocation(l);
	}

	public String getTeam(Player player) {
		return players.get(player.getName()).getTeam();
	}

	public void setTeam(Player player, String s) {
		players.get(player.getName()).setTeam(s);
	}

	public void setTelePass(Player player, boolean b) {
		players.get(player.getName()).setTelePass(b);
	}

	public boolean getTelePass(Player player) {
		return players.get(player.getName()).getTelePass();
	}
}
