/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.engines;

import com.division.common.utils.ItemArmor;
import com.division.fearforall.core.FearForAll;
import com.division.fearforall.core.PlayerStorage;
import com.division.fearforall.crypto.SHA1;
import com.division.fearforall.events.PlayerDamageInArenaEvent;
import com.division.fearforall.events.PlayerEnteredArenaEvent;
import com.division.fearforall.events.PlayerMovedInArenaEvent;
import com.division.fearforall.events.PlayerMovedInArenaEvent.MoveMethod;
import com.division.fearforall.events.PlayerQuitInArenaEvent;
import com.massivecraft.factions.struct.Relation;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author Evan
 *
 */
@EngineInfo(author = "mastershake71",
version = "0.2.2DE",
depends = {"OfflineStorage"})
public class StorageEngine extends Engine {

    protected int aM;

    @Override
    public String getName() {
        return ("Storage");
    }
    private static ArrayList<PlayerStorage> massStorage = new ArrayList<PlayerStorage>();
    public ArrayList<Player> playersInArena = new ArrayList<Player>();
    private OfflineStorageEngine OSE = null;

    public StorageEngine() {
    }

    @Override
    public void runStartupChecks() throws EngineException {
        Engine eng = FearForAll.getInstance().getEngineManger().getEngine("OfflineStorage");
        if (eng != null) {
            if (eng instanceof OfflineStorageEngine) {
                this.OSE = (OfflineStorageEngine) eng;
            }
        }
        if (OSE == null) {
            throw new EngineException("Missing Dependency.");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDamagedInArena(PlayerDamageInArenaEvent evt) {
        Player victim = evt.getVictim();
        EntityDamageEvent ede = evt.getDamageEvent();
        EntityDamageByEntityEvent edee = null;
        if (ede instanceof EntityDamageByEntityEvent) {
            edee = (EntityDamageByEntityEvent) ede;
            Player attacker = null;
            if (edee.getDamager() instanceof Player) {
                attacker = (Player) edee.getDamager();
                getStorage(victim.getName()).setLastHit(attacker.getName());
                if (!evt.getDamageEvent().isCancelled()) {
                    Relation rel = FearForAll.getInstance().getRelationShip(victim, attacker);
                    if (rel.isAtLeast(Relation.ALLY)) {
                        attacker.damage(getArmorRedox(attacker, evt.getDamageEvent().getDamage()));
                        evt.getDamageEvent().setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuitInArena(PlayerQuitInArenaEvent evt) {
        Player evtPlayer = evt.getPlayer();
        if (hasStorage(evt.getPlayer())) {
            PlayerStorage pStorage = getStorage(evtPlayer.getName());
            if (evtPlayer.hasPotionEffect(PotionEffectType.SPEED)) {
                evtPlayer.removePotionEffect(PotionEffectType.SPEED);
            }
            if (!OSE.hasOfflineStorage(pStorage.getKey())) {
                OSE.covertPlayerStorage(pStorage);
            }
            playersInArena.remove(evtPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMovedInArena(PlayerMovedInArenaEvent evt) {
        Player evtPlayer = evt.getPlayer();
        if (evtPlayer.isDead() || hasStorage(evtPlayer)) {
            return;
        }
        PlayerEnteredArenaEvent event = new PlayerEnteredArenaEvent(evtPlayer, evt.getFrom(), evt.getTo(), evt.getMethod());
        FearForAll.getInstance().getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            evt.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMoveInArena(PlayerMovedInArenaEvent evt) {
        if (!evt.isCancelled()) {
            return;
        }
        if (evt.getMethod() == MoveMethod.TELEPORTED) {
            PlayerTeleportEvent event = (PlayerTeleportEvent) evt.getEvent();
            event.setCancelled(true);
        } else if (evt.getMethod() == MoveMethod.MOVED) {
            PlayerMoveEvent event = (PlayerMoveEvent) evt.getEvent();
            event.setCancelled(true);
        } else if (evt.getMethod() == MoveMethod.RESPAWNED) {
            PlayerRespawnEvent event = (PlayerRespawnEvent) evt.getEvent();
            event.setRespawnLocation(evt.getPlayer().getWorld().getSpawnLocation());
        } else if (evt.getMethod() == MoveMethod.JOINED) {
            PlayerJoinEvent event = (PlayerJoinEvent) evt.getEvent();
            event.getPlayer().teleport(evt.getPlayer().getWorld().getSpawnLocation());
        }

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerEnteredArena(PlayerEnteredArenaEvent evt) {
        if (checkAllowed(evt.getPlayer())) {
            addStorage(evt.getPlayer());
            playersInArena.add(evt.getPlayer());
        } else {
            evt.getPlayer().sendMessage(ChatColor.YELLOW + "[FearForAll] " + ChatColor.RED + "There are already 2 instances of your ip in the arena.");
            evt.setCancelled(true);
        }
    }

    public void addStorage(Player key) {
        PlayerStorage pStorage = new PlayerStorage(SHA1.getHash(20, key.getName()), key.getInventory());
        massStorage.add(pStorage);
    }

    public PlayerStorage getStorage(String rKey) {
        String key = SHA1.getHash(20, rKey);
        for (PlayerStorage ps : massStorage) {
            if (ps.getKey().equals(key)) {
                return ps;
            }
        }
        return null;
    }

    public void safeRestore(Player p) {
        if (!p.isDead()) {
            p.teleport(p.getWorld().getSpawnLocation());
        }
    }

    public boolean hasStorage(Player rkey) {
        if (getStorage(rkey.getName()) != null) {
            return true;
        }
        return false;
    }

    public void removeStorage(PlayerStorage pStorage) {
        massStorage.remove(pStorage);
    }

    public void saveAll() {
        for (PlayerStorage ps : massStorage) {
            createOfflineStorage(ps);
        }
    }

    public void createOfflineStorage(PlayerStorage pStorage) {
        if (pStorage != null) {
            if (!hasOfflineStorage(pStorage.getKey())) {
                System.out.println("[FearForAll] converting storage key: " + pStorage.getKey() + " to Offline Storage.");
                if (OSE.covertPlayerStorage(pStorage)) {
                    System.out.println("[FearForAll] storage key: " + pStorage.getKey() + " has been successfully converted.");
                } else {
                    System.out.println("[FearForAll] An error occured when converting storage key: " + pStorage.getKey());
                }
            }
        }
    }

    public boolean checkAllowed(Player p) {
        if (playersInArena.isEmpty()) {
            return true;
        }

        int ipCount = 0;
        String addr = p.getAddress().getHostName();
        Player[] players = playersInArena.toArray(new Player[0]);
        for (Player player : players) {
            if (player == p) {
                continue;
            }
            String addr2 = player.getAddress().getHostName();
            if (addr.equals(addr2)) {
                ipCount++;
                continue;
            }
        }
        if (ipCount >= 2) {
            return false;
        } else {
            return true;
        }
    }

    public boolean hasOfflineStorage(String key) {
        return OSE.hasOfflineStorage(key);
    }

    public int aO(Player p) {
        return this.l(p);
    }

    public int l(Player p) {
        int i = 0;
        ItemStack[] aitemstack = p.getInventory().getArmorContents();
        int j = aitemstack.length;

        for (int k = 0; k < j; ++k) {
            ItemStack itemstack = aitemstack[k];

            int l = ItemArmor.valueOf(itemstack.getType().name()).b();

            i += l;
        }
        return i;
    }

    public int getArmorRedox(Player p, int i) {
        int j = 25 - this.aO(p);
        int k = i * j;

        this.k(p, i);
        i = k / 25;

        return i;
    }

    public void k(Player p, int i) {
        i /= 4;
        if (i < 1) {
            i = 1;
        }
        ItemStack[] armor = p.getInventory().getArmorContents();

        for (int j = 0; j < armor.length; ++j) {
            if (armor[j] != null) {
                armor[j].setDurability((short) (armor[j].getDurability() - i));
                if (armor[j].getDurability() == 0) {
                    armor[j] = null;
                }
            }
        }
    }
}
