/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.events;

import static com.division.common.utils.LocationTools.toVector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 *
 * @author Evan
 */
public class PlayerMovedInArenaEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location from;
    private Location to;
    private MoveMethod method;
    private PlayerEvent event;
    private boolean cancelled;

    public PlayerMovedInArenaEvent(final Player p, final Location from, final Location to, final MoveMethod method, final PlayerEvent event) {
        this.player = p;
        this.from = from;
        this.to = to;
        this.method = method;
        this.cancelled = false;
        this.event = event;
    }

    @Override
    public String toString() {
        if (!cancelled) {
            return "PMIAE: " + player.getName() + " move from " + ChatColor.LIGHT_PURPLE + toVector(from) + ChatColor.WHITE + " to " + ChatColor.LIGHT_PURPLE + toVector(to);
        } else {
            return "PMIAE: Cancelled " + player.getName() + " move from " + ChatColor.LIGHT_PURPLE + toVector(from) + ChatColor.WHITE + " to " + ChatColor.LIGHT_PURPLE + toVector(to);
        }
    }

    public Player getPlayer() {
        return player;
    }

    public MoveMethod getMethod() {
        return method;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public PlayerEvent getEvent() {
        return event;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum MoveMethod {

        MOVED,
        TELEPORTED,
        JOINED,
        RESPAWNED
    }
}
