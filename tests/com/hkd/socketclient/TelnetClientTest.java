package com.hkd.socketclient;

import org.junit.*;

import java.io.BufferedReader;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class TelnetClientTest{

    @Test
    public void testSendCommand1() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);

        assertEquals(client.sendCommand("PF"), true);
        assertEquals(client.sendCommand("PO"),true);
        client.close();
    }

    @Test
    public void testGetResponse1() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);

        assertEquals(client.getResponse("?VOL",1), "VOL003");
        client.close();
    }

    @Test
    public void testGetResponse2() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);
        client.sendCommand("PF");
        sleep(500);
        assertEquals(client.getResponse("PO",4), "VOL003");
        client.close();
    }

    @Test
    public void testSpawnSpy1() throws Exception {
        TelnetClient client = new TelnetClient("192.168.0.105", 8102);

        BufferedReader buf = client.spawnSpy();

        String[] cmds = {"PF","PO", "?VOL"};

        for (int i = 0; i < cmds.length; i++) {
            client.sendCommand(cmds[i]);
            for (String line = buf.readLine(); buf.ready(); line = buf.readLine()) {
                sleep(500);
                System.out.println(line);
            }
        }
        client.close();
    }
}