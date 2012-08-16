/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.division.fearforall.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Evan
 */
public class SHA1 {

    public static String getHash(int iterationNB, String phrase) {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha1.digest(phrase.getBytes());
            for (int i = 0; i < iterationNB; i++) {
                digest = sha1.digest(digest);
            }
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException ex) {
        }
        return null;
    }

    public static String bytesToHex(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;

    }
}
