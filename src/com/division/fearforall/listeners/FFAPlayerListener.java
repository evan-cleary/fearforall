/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.listeners;

import static com.division.common.utils.LocationTools.toVector;
import com.division.fearforall.core.FearForAll;
import com.division.fearforall.events.PlayerDeathInArenaEvent.DeathCause;
import com.division.fearforall.events.MoveMethod;
import com.division.fearforall.events.*;
import com.division.fearforall.regions.HealRegion;
import com.division.fearforall.regions.Region;
import com.division.fearforall.regions.Selection;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 *
 * @author Evan
 */
public class FFAPlayerListener implements Listener {

    FearForAll FFA;

    public FFAPlayerListener(FearForAll instance) {
        this.FFA = instance;
        FFA.getServer().getPluginManager().registerEvents(this, FFA);

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent evt) {
        if (evt.getPlayer().hasPermission("fearforall.selection")) {
            if (evt.hasBlock()) {
                Block evtBlock = evt.getClickedBlock();
                ItemStack iih = evt.getItem();
                final int mat = Material.STICK.getId();
                if (iih != null) {
                    if (iih.getTypeId() == mat) {
                        if (evt.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            Selection.setP2(toVector(evtBlock));
                            evt.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Set Point 1: " + toVector(evtBlock));
                            evt.setCancelled(true);
                        }
                        if (evt.getAction() == Action.LEFT_CLICK_BLOCK) {
                            Selection.setP1(toVector(evtBlock));
                            evt.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Set Point 2: " + toVector(evtBlock));
                            evt.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMoveEvent(PlayerMoveEvent evt) {
        Location from = evt.getFrom();
        Location to = evt.getTo();
        if (evt.isCancelled()) {
            return;
        }
        Player evtPlayer = evt.getPlayer();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }
        if (FFA.hasActiveTimer(evtPlayer)) {
            FFA.getServer().getScheduler().cancelTask(FFA.getActiveTimer(evtPlayer));
            FFA.removeTimer(evtPlayer);
            evtPlayer.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Teleport has been cancelled.");
            return;
        }
        Region region = FFA.getRegion();
        HealRegion healRegion = FFA.getHealRegion();
        Vector pt = toVector(to);
        Vector pf = toVector(from);
        World world = evtPlayer.getWorld();
        if (healRegion != null) {
            if (healRegion.contains(world, pt) && !healRegion.contains(world, pf)) {
                evtPlayer.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " You have entered the heal region.");
            }
            if (!healRegion.contains(world, pt) && healRegion.contains(world, pf)) {
                evtPlayer.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " You have left the heal region.");
            }
        }
        if (region.contains(world, pt) && !region.contains(world, pf)) {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerEnteredArenaEvent(evtPlayer, pf, pt, MoveMethod.MOVED));
        } else if (!region.contains(world, pt) && region.contains(world, pf)) {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerLeftArenaEvent(evtPlayer, pf, pt, MoveMethod.MOVED));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent evt) {
        Location loc = evt.getPlayer().getLocation();
        World world = evt.getPlayer().getWorld();
        Vector pt = toVector(loc);
        Region region = FFA.getRegion();
        if (evt.getPlayer().hasPermission("fearforall.bypass")) {
            return;
        }
        if (region.contains(world, pt)) {
            if (!evt.getMessage().contains("ffa")) {
                evt.setCancelled(true);
                evt.getPlayer().sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " You cannot use commands in the arena.");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent evt) {
        Player p = evt.getEntity();
        World world = p.getWorld();
        Location loc = evt.getEntity().getLocation();
        Vector pt = toVector(loc);
        Region region = FFA.getRegion();
        if (region.contains(world, pt)) {
            evt.getDrops().clear();
            EntityDamageEvent ede = p.getLastDamageCause();
            if (ede instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) ede;
                if (edee.getDamager() instanceof Player) {
                    FFA.getServer().getPluginManager().callEvent(new PlayerKilledPlayerInArenaEvent(p, (Player) edee.getDamager()));
                } else {
                    FFA.getServer().getPluginManager().callEvent(new PlayerDeathInArenaEvent(p, DeathCause.ENVIRONMENT));
                }
            } else {
                FFA.getServer().getPluginManager().callEvent(new PlayerDeathInArenaEvent(p, DeathCause.ENVIRONMENT));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDrop(PlayerDropItemEvent evt) {
        Location loc = evt.getPlayer().getLocation();
        World world = evt.getPlayer().getWorld();
        Vector pt = toVector(loc);
        Region region = FFA.getRegion();
        if (region.contains(world, pt)) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " STOP TRYING TO DUPE NOOB. <3 Shake");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerBreak(BlockBreakEvent evt) {
        if (evt.getPlayer().hasPermission("fearforall.selection")) {
            if (evt.getPlayer().getItemInHand().getType() == Material.STICK) {
                evt.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        Player evtPlayer = evt.getPlayer();
        World world = evtPlayer.getWorld();
        Location loc = evt.getRespawnLocation();
        Vector rt = toVector(loc);
        Region region = FFA.getRegion();
        if (!region.contains(world, rt)) {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerLeftArenaEvent(evtPlayer, null, rt, MoveMethod.RESPAWNED));
        } else {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerEnteredArenaEvent(evtPlayer, null, rt, MoveMethod.RESPAWNED));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent evt) {
        if (!evt.isCancelled()) {
            Player evtPlayer = evt.getPlayer();
            World world = evtPlayer.getWorld();
            Location locTo = evt.getTo();
            Location locFrom = evt.getFrom();
            Region region = FFA.getRegion();
            Vector tTo = toVector(locTo);
            Vector tFrom = toVector(locFrom);
            if (!region.contains(world, tTo) && region.contains(world, tFrom)) {
                FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerLeftArenaEvent(evtPlayer, tTo, tFrom, MoveMethod.TELEPORTED));
            } else if (region.contains(world, tTo) && !region.contains(world, tFrom)) {
                FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerEnteredArenaEvent(evtPlayer, tTo, tFrom, MoveMethod.TELEPORTED));
            } else if (region.contains(world, tTo) && locFrom == null) {
                FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerEnteredArenaEvent(evtPlayer, tTo, tFrom, MoveMethod.TELEPORTED));
            }
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryEdit(InventoryClickEvent evt) {
        HumanEntity HE = evt.getWhoClicked();
        Player evtPlayer;
        if (HE instanceof Player) {
            evtPlayer = (Player) HE;
            if (evtPlayer.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (evtPlayer != null) {
                Location loc = evtPlayer.getLocation();
                World world = evtPlayer.getWorld();
                Vector pt = new Vector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                Region region = FFA.getRegion();
                if (region.contains(world, pt)) {
                    evt.setResult(Event.Result.DENY);
                    evtPlayer.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " You cannot modify your inventory.");
                }
                if (FFA.hasActiveTimer(evtPlayer)) {
                    evt.setResult(Event.Result.DENY);
                    evtPlayer.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " You cannot modify your inventory.");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerKick(PlayerKickEvent evt) {
        Player evtPlayer = evt.getPlayer();
        World world = evtPlayer.getWorld();
        Location loc = evtPlayer.getLocation();
        Vector pt = toVector(loc);
        Region region = FFA.getRegion();
        if (region.contains(world, pt)) {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerQuitInArenaEvent(evtPlayer));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent evt) {
        Player evtPlayer = evt.getPlayer();
        World world = evtPlayer.getWorld();
        Location loc = evtPlayer.getLocation();
        Vector pt = toVector(loc);
        Region region = FFA.getRegion();
        if (region.contains(world, pt)) {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerQuitInArenaEvent(evtPlayer));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent evt) {
        if (evt.isCancelled()) {
            return;
        }
        if (evt.getEntity() instanceof Player) {
            Player evtPlayer = (Player) evt.getEntity();
            if (FFA.hasActiveTimer(evtPlayer)) {
                FFA.getServer().getScheduler().cancelTask(FFA.getActiveTimer(evtPlayer));
                FFA.removeTimer(evtPlayer);
                evtPlayer.sendMessage(ChatColor.YELLOW + "[FearForAll]" + ChatColor.RED + " Teleport has been cancelled.");
                return;
            }
            Location loc = evtPlayer.getLocation();
            World world = evtPlayer.getWorld();
            Vector pt = toVector(loc);
            Region region = FFA.getRegion();
            if (region.contains(world, pt)) {
                if (evt instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent edee = (EntityDamageByEntityEvent) evt;
                    FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerDamageInArenaEvent(evtPlayer, edee.getDamager(), evt.getCause(), evt));
                } else {
                    FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerDamageInArenaEvent(evtPlayer, null, evt.getCause(), evt));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent evt) {
        if (FearForAll.getInstance().isUsingLeaderBoards()) {
            FFA.getDataInterface().createPlayerAccount(evt.getPlayer().getName());
        }
        Player evtPlayer = evt.getPlayer();
        World world = evtPlayer.getWorld();
        Location loc = evtPlayer.getLocation();
        Vector pt = toVector(loc);
        Region region = FFA.getRegion();
        if (!region.contains(world, pt)) {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerLeftArenaEvent(evtPlayer, null, pt, MoveMethod.JOINED));
        }
        if (region.contains(world, pt)) {
            FearForAll.getInstance().getServer().getPluginManager().callEvent(new PlayerEnteredArenaEvent(evtPlayer, null, pt, MoveMethod.JOINED));
        }
    }
}
