/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.engines;

import org.bukkit.event.Listener;

/**
 *
 * @author Evan
 */
public abstract class Engine implements Listener {

    public abstract String getName();

    public void runStartupChecks() throws EngineException {
    }
}
