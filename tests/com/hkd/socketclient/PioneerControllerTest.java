package com.hkd.socketclient;

import org.junit.Test;

import static org.junit.Assert.*;
import static java.lang.Thread.sleep;

public class PioneerControllerTest {

    @Test
    public void testGetVolume() throws Exception {
        PioneerController p = new PioneerController("192.168.0.105", 8102, new MainActivity());
        sleep(500);
        sleep(500);
        sleep(500);
        p.changeVolume(10);
        sleep(10000);
        assertEquals(10, p.getVolume());
        assertEquals(p.pioneerIsOn(), true);
    }

    @Test
    public void testPioneerIsOn() throws Exception {
        PioneerController p = new PioneerController("192.168.0.105", 8102, new MainActivity());
        p.sendCommand("PO");
        sleep(10000);
        assertEquals(p.pioneerIsOn(), true);
    }

    @Test
    public void testChangeVolume() throws Exception {
        PioneerController p = new PioneerController("192.168.0.105", 8102, new MainActivity());

        sleep(5000);
    }

    @Test
    public void testTogglePower() throws Exception {
        PioneerController p = new PioneerController("192.168.0.105", 8102, new MainActivity());

    }

    @Test
    public void testPlay() throws Exception {
        PioneerController p = new PioneerController("192.168.0.105", 8102, new MainActivity());

    }

    @Test
    public void testStop() throws Exception {
        PioneerController p = new PioneerController("192.168.0.105", 8102, new MainActivity());

    }

    @Test
    public void testInput() throws Exception {
        PioneerController p = new PioneerController("192.168.0.105", 8102, new MainActivity());

    }
}