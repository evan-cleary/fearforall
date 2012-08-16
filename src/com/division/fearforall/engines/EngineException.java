/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.engines;

/**
 *
 * @author Evan
 */
public class EngineException extends Exception {

    public EngineException(final String message) {
        super(message);
    }

    public EngineException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
