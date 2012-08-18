/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.events;

import static com.division.common.utils.LocationTools.toVector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Evan
 */
public class PlayerLeftArenaEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Player player;
    private Location from;
    private Location to;
    private MoveMethod method;

    public PlayerLeftArenaEvent(final Player p, final Location from, final Location to, final MoveMethod method) {
        this.player = p;
        this.from = from;
        this.to = to;
        this.method = method;
    }

    @Override
    public String toString() {
        return "PLAE: " + player.getName() + " " + method.toString() + " from " + ChatColor.LIGHT_PURPLE + toVector(from) + ChatColor.WHITE + " to " + ChatColor.LIGHT_PURPLE + toVector(to) + ChatColor.WHITE + " and left arena";
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
