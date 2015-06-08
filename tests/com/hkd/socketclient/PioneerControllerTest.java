package com.hkd.socketclient;

import org.junit.Test;

import static org.junit.Assert.*;
import static java.lang.Thread.sleep;

public class PioneerControllerTest {

    @Test
    public void testGetVolume() throws Exception {
        TelnetClient c = new TelnetClient("192.168.0.105", 8102);
        PioneerController p = new PioneerController(c);
        sleep(500);
        sleep(500);
        sleep(500);
        p.changeVolume(10);
        sleep(10000);
        assertEquals(10, p.getVolume());
    }

    @Test
    public void testPioneerIsOn() throws Exception {
        TelnetClient c = new TelnetClient("192.168.0.105", 8102);
        PioneerController p = new PioneerController(c);
        c.sendCommand("PO");
        sleep(500);
        sleep(500);
        sleep(500);
        sleep(500);
        assertEquals(p.pioneerIsOn(),true);
    }

    @Test
    public void testChangeVolume() throws Exception {
        PioneerController p = new PioneerController(new TelnetClient("192.168.0.105", 8102));

        sleep(5000);
    }

    @Test
    public void testTogglePower() throws Exception {
        PioneerController p = new PioneerController(new TelnetClient("192.168.0.105", 8102));

    }

    @Test
    public void testPlay() throws Exception {
        PioneerController p = new PioneerController(new TelnetClient("192.168.0.105", 8102));

    }

    @Test
    public void testStop() throws Exception {
        PioneerController p = new PioneerController(new TelnetClient("192.168.0.105", 8102));

    }

    @Test
    public void testInput() throws Exception {
        PioneerController p = new PioneerController(new TelnetClient("192.168.0.105", 8102));

    }
}