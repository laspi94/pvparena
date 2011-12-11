/*
 * powerup effect class
 * 
 * author: slipcor
 * 
 * version: v0.4.0 - mayor rewrite, improved help
 * 
 * history:
 * 
 *     v0.3.10 - rewrite
 *     v0.3.9 - Permissions, rewrite
 *     v0.3.8 - BOSEconomy, rewrite
 *     v0.3.6 - CTF Arena
 *     v0.3.5 - Powerups!!
 */

package net.slipcor.pvparena.powerups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arenas.Arena;
import net.slipcor.pvparena.managers.ArenaManager;
import net.slipcor.pvparena.managers.DebugManager;
import net.slipcor.pvparena.managers.StatsManager;

public class PowerupEffect {
	protected boolean active = false;
	protected int uses = -1;
	protected int duration = -1;
	protected classes type = null;
	protected String mobtype = null;
	private double factor = 1.0;
	private double chance = 1.0;
	private int diff = 0;
	private List<String> items = new ArrayList<String>();
	private DebugManager db = new DebugManager();

	/*
	 * PowerupEffect classes
	 */
	public static enum classes {
		DMG_CAUSE, DMG_RECEIVE, DMG_REFLECT, FREEZE, HEAL, HEALTH, IGNITE, LIVES, PORTAL, REPAIR, SLIP, SPAWN_MOB, SPRINT, JUMP;
	}

	/*
	 * PowerupEffect instant classes (effects that activate when collecting)
	 */
	public static enum instants {
		FREEZE, HEALTH, LIVES, PORTAL, REPAIR, SLIP, SPAWN_MOB, SPRINT;
	}

	/*
	 * get the PowerupEffect class from name
	 */
	public static classes parseClass(String s) {
		for (classes c : classes.values()) {
			if (c.name().equalsIgnoreCase(s))
				return c;
		}
		return null;
	}

	/*
	 * PowerupEffect constructor
	 * 
	 * read a powerup effect and add it to the powerup
	 */
	public PowerupEffect(String eClass, HashMap<String, Object> puEffectVals) {
		db.i("adding effect " + eClass);
		this.type = parseClass(eClass);

		db.i("effect class is " + type.toString());
		for (Object evName : puEffectVals.keySet()) {
			if (evName.equals("uses")) {
				this.uses = (Integer) puEffectVals.get(evName);
				db.i("uses :" + String.valueOf(uses));
			} else if (evName.equals("duration")) {
				this.duration = (Integer) puEffectVals.get(evName);
				db.i("duration: " + String.valueOf(duration));
			} else if (evName.equals("factor")) {
				this.factor = (Double) puEffectVals.get(evName);
				db.i("factor: " + String.valueOf(factor));
			} else if (evName.equals("chance")) {
				this.chance = (Double) puEffectVals.get(evName);
				db.i("chance: " + String.valueOf(chance));
			} else if (evName.equals("diff")) {
				this.diff = (Integer) puEffectVals.get(evName);
				db.i("diff: " + String.valueOf(diff));
			} else if (evName.equals("items")) {
				this.items.add((String) puEffectVals.get(evName));
				db.i("items: " + items.toString());
			} else if (evName.equals("type")) {
				// mob type
				this.mobtype = (String) puEffectVals.get(evName);
				db.i("type: " + type.name());
			} else {
				db.w("undefined effect class value: " + evName);
			}
		}
	}

	/*
	 * initiate PowerupEffect
	 */
	public void init(Player player) {
		if (uses == 0)
			return;
		else if (uses > 0) {
			active = true;
			uses--;
		} else {
			active = true;
		}

		db.i("initiating - " + type.name());

		if (duration == 0) {
			active = false;
		}
		for (instants i : instants.values()) {
			if (this.type.toString().equals(i.toString())) {
				// type is instant. commit!
				commit(player);
			}
		}
	}

	/*
	 * commit PowerupEffect in combat
	 */
	public void commit(Player attacker, Player defender,
			EntityDamageByEntityEvent event) {
		db.i("committing entitydamagebyentityevent: " + this.type.name());
		if (this.type == classes.DMG_RECEIVE) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setDamage((int) Math.round(event.getDamage() * factor));
			} // else: chance fail :D
		} else if (this.type == classes.DMG_CAUSE) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setDamage((int) Math.round(event.getDamage() * factor));
			} // else: chance fail :D
		} else if (this.type == classes.DMG_REFLECT) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				EntityDamageByEntityEvent reflectEvent = new EntityDamageByEntityEvent(
						defender, attacker, event.getCause(),
						(int) Math.round(event.getDamage() * factor));
				PVPArena.instance.getEntityListener().onEntityDamageByEntity(
						reflectEvent);
			} // else: chance fail :D
		} else if (this.type == classes.IGNITE) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				defender.setFireTicks(20);
			} // else: chance fail :D
		} else {
			db.w("unexpected fight powerup effect: " + this.type.name());
		}
	}

	/*
	 * commit PowerupEffect on player
	 */
	public boolean commit(Player player) {

		db.i("committing " + this.type.name());
		Random r = new Random();
		if (r.nextFloat() <= chance) {
			if (this.type == classes.HEALTH) {
				if (diff > 0) {
					player.setHealth(player.getHealth() + diff);
				} else {
					player.setHealth((int) Math.round(player.getHealth()
							* factor));
				}
				return true;
			} else if (this.type == classes.LIVES) {
				byte lives = ArenaManager.getArenaByPlayer(player).playerManager
						.getLives(player);
				if (lives > 0)
					ArenaManager.getArenaByPlayer(player).playerManager
							.setLives(player, (byte) (lives + diff));
				else {
					Arena arena = ArenaManager.getArenaByPlayer(player);

					// pasted from onEntityDeath;

					String sTeam = arena.playerManager.getTeam(player);
					String color = arena.paTeams.get(sTeam);
					if (!color.equals("free")) {
						arena.playerManager.tellEveryone(PVPArena.lang.parse(
								"killed",
								ChatColor.valueOf(color) + player.getName()
										+ ChatColor.YELLOW));
					} else {
						arena.playerManager.tellEveryone(PVPArena.lang.parse(
								"killed", ChatColor.WHITE + player.getName()
										+ ChatColor.YELLOW));
					}
					StatsManager.addLoseStat(player, sTeam, arena);
					// needed so player does not get found when dead
					arena.playerManager.setTeam(player, null);
					arena.playerManager.setRespawn(player, true);

					arena.checkEndAndCommit();
				}

				return true;
			} else if (this.type == classes.PORTAL) {
				// player.set
				return true;
			} else if (this.type == classes.REPAIR) {
				for (String i : items) {
					ItemStack is = null;
					if (i.contains("HELM")) {
						is = player.getInventory().getHelmet();
					} else if ((i.contains("CHEST")) || (i.contains("PLATE"))) {
						is = player.getInventory().getHelmet();
					} else if (i.contains("LEGGINS")) {
						is = player.getInventory().getHelmet();
					} else if (i.contains("BOOTS")) {
						is = player.getInventory().getHelmet();
					} else if (i.contains("SWORD")) {
						is = player.getItemInHand();
					}
					if (is == null)
						continue;

					if (diff > 0) {
						if (is.getDurability() + diff > Byte.MAX_VALUE)
							is.setDurability(Byte.MAX_VALUE);
						else
							is.setDurability((short) (is.getDurability() + diff));
					}
				}
				return true;
			} else if (this.type == classes.SPAWN_MOB) {
				return true;
			} else if (this.type == classes.SPRINT) {
				player.setSprinting(true);
				return true;
			}
		}
		db.w("unexpected " + this.type.name());
		return false;
	}

	/*
	 * commit PowerupEffect on health gain
	 */
	public void commit(EntityRegainHealthEvent event) {
		db.i("committing entityregainhealthevent " + this.type.name());
		if (this.type == classes.HEAL) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setAmount((int) Math.round(event.getAmount() * factor));
				((Player) event.getEntity()).setSaturation(20);
				((Player) event.getEntity()).setFoodLevel(20);
			} // else: chance fail :D
		} else {
			db.w("unexpected fight heal effect: " + this.type.name());
		}
	}

	/*
	 * commit PowerupEffect on velocity event
	 */
	public void commit(PlayerVelocityEvent event) {
		db.i("committing velocityevent " + this.type.name());
		if (this.type == classes.HEAL) {
			Random r = new Random();
			if (r.nextFloat() <= chance) {
				event.setVelocity(event.getVelocity().multiply(factor));
			} // else: chance fail :D
		} else {
			db.w("unexpected jump effect: " + this.type.name());
		}
	}
}
