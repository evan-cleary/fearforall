package com.division.fearforall.events;

import static com.division.common.utils.LocationTools.toVector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Evan
 */
public class PlayerEnteredArenaEvent extends Event implements Cancellable {

    public static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location from;
    private Location to;
    private MoveMethod method;
    private boolean cancelled = false;

    public PlayerEnteredArenaEvent(Player p, Location from, Location to, final MoveMethod method) {
        this.player = p;
        this.from = from;
        this.to = to;
        this.method = method;
    }

    @Override
    public String toString() {
        if (!cancelled) {
            return "PEAE: " + player.getName() + " " + method.toString() + " from " + ChatColor.LIGHT_PURPLE + toVector(from) + ChatColor.WHITE + " to " + ChatColor.LIGHT_PURPLE + toVector(to) + ChatColor.WHITE + " and entered arena";
        } else {
            return "PEAE: Cancelled " + player.getName() + " " + method.toString() + " from " + ChatColor.LIGHT_PURPLE + toVector(from) + ChatColor.WHITE + " to " + ChatColor.LIGHT_PURPLE + toVector(to) + ChatColor.WHITE + " and prevented entering arena";

        }
    }

    public Player getPlayer() {
        return player;
    }

    
    public MoveMethod getMethod(){
        return method;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
