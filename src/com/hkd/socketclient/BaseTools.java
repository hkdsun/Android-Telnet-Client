package com.hkd.socketclient;

public class BaseTools {
    public static int getIntOrElse(String val, int e) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            // Log exception.
            return e;
        }
    }
}
