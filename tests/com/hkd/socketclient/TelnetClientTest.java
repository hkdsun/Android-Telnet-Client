package com.hkd.socketclient;

import org.junit.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class TelnetClientTest{

    @Test
    public void testSendCommand1() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);

        assertEquals(client.sendCommand("PF"),true);
        sleep(500);
        assertEquals(client.sendCommand("PO"),true);
        client.close();
    }

    @Test
    public void testGetResponse1() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);
        client.sendCommand("PF");
        sleep(500);
        assertEquals(client.getResponse("?PWR"), "PWR2");
        client.close();
    }

    @Test
    public void testExpectResponse() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);
        client.sendCommand("PF");
        sleep(500);
        assertEquals(client.expectResponse("PO", "VOL029"), "VOL029");
        client.close();
    }

    @Test(expected = TimeoutException.class)
    public void testExpectResponseException() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);
        client.sendCommand("PF");
        client.expectResponse("PO","VOL003");
        client.close();
    }

    @Test
    public void testSendUntilResponse() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);
        client.sendCommand("PO");
        client.sendUntilResponse("VD","VOL000", 500);
        client.sendUntilResponse("VU","VOL029", 500);
    }

}