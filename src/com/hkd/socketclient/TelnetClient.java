package com.hkd.socketclient;


import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;

import static java.lang.Thread.sleep;


public class TelnetClient {
    private TelnetConnection client;
    private OutputStream outstream;
    private org.apache.commons.net.telnet.TelnetClient rawConnection;
    private InputStream instream;
    AtomicInteger guy;
    private LinkedList<Thread> threads = new LinkedList();
    private PipedInputStream spyReader;

    public TelnetClient(String ip, int port) throws IOException {
        client = new TelnetConnection(ip, port);
        client.connect();
        rawConnection = client.getConnection();
        outstream = client.getOutput();
        instream = client.getReader();
    }

    public void close() throws IOException {
        rawConnection.disconnect();
    }

    /**
     * @param cmd the string of message you want to send
     * @return true if message was sent successfully
     */
    public boolean sendCommand(String cmd) {
        if (client == null || !client.isConnected()) {
            return false;
        }
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(cmd.toUpperCase(Locale.ENGLISH));
        stringBuilder.append("\n\r");

        byte[] cmdbyte = stringBuilder.toString().getBytes();

        try {
            outstream.write(cmdbyte, 0, cmdbyte.length);
            outstream.flush();
            return true;
        } catch (Exception e1) {
            return false;
        }
    }

    //TODO implement a nice timeout
    public boolean sendUntilResponse(String cmd, int speed, final ResponseTest tester) throws InterruptedException, IOException {
        final boolean[] notSeen = {true};
        InputStreamReader a = spawnSpy();
        final BufferedReader stream = new BufferedReader(a);
        Runnable r = new Runnable() {
            @Override
            public void run(){
                String line;
                try {
                    while (true){
                        line = stream.readLine();
                        if(tester.test(line)) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                notSeen[0] = false;
            }
        };
        new Thread(r).start();
        while(notSeen[0]){
            sendCommand(cmd);
            sleep(speed);
        }
        return true;
    }

    public String getResponse(String cmd) throws IOException, InterruptedException {

        if (client == null || !client.isConnected()) {
            throw new IOException("Unable to send message to disconnected client");
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(cmd.toUpperCase(Locale.ENGLISH));
        stringBuilder.append("\n\r");

        byte[] cmdbyte = stringBuilder.toString().getBytes();

        InputStreamReader a = spawnSpy();
        BufferedReader buf = new BufferedReader(a);

        while(buf.ready())
            buf.read();
        outstream.write(cmdbyte, 0, cmdbyte.length);
        outstream.flush();

        String result;
        Boolean done = false;

        do {
            result = buf.readLine();
            if(result!=null)
                done = true;
        } while(!done);

        return result;
    }

    public String expectResponse(String cmd, String expected) throws IOException, InterruptedException, TimeoutException, ExecutionException {
        return expectResponse(cmd, expected, -1);
    }

    public String expectResponse(String cmd, String expected, int timeout) throws IOException, InterruptedException, TimeoutException, ExecutionException {

        if (client == null || !client.isConnected()) {
            throw new IOException("Unable to send message to disconnected client");
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(cmd.toUpperCase(Locale.ENGLISH));
        stringBuilder.append("\n\r");

        byte[] cmdbyte = stringBuilder.toString().getBytes();

        outstream.write(cmdbyte, 0, cmdbyte.length);
        outstream.flush();

        if (timeout == -1) {
            return readUntil(expected);
        } else {
            return readUntil(expected, timeout);
        }
    }

    public InputStreamReader spawnSpy() throws InterruptedException, IOException {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream();
        in.connect(out);
        if(spyReader!=null) {
            return spawnSpy(spyReader, out);
        } else {
            spyReader = in;
            return spawnSpy(instream, out);
        }
    }

    private InputStreamReader spawnSpy(InputStream in, PipedOutputStream pipeout) throws InterruptedException {
        return new InputStreamReader(new TeeInputStream(in,pipeout));
    }

    private String readUntil(String expected) throws InterruptedException, TimeoutException, ExecutionException {
        return readUntil(expected, -1);
    }

    private String readUntil(String expected, int timeout) throws InterruptedException, TimeoutException, ExecutionException {
        final ExecutorService service;
        final Future<String> result;
        if (timeout == -1) {
            service = Executors.newFixedThreadPool(1);
            result = service.submit(new ReadUntil(expected));
            return result.get(5, TimeUnit.SECONDS);
        } else {
            service = Executors.newFixedThreadPool(1);
            result = service.submit(new ReadUntil(expected));
            return result.get(timeout, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public boolean disconnect() {
        spyReader = null;
        return client.disconnect();
    }

    private class ReadUntil implements Callable<String> {
        String expected;

        @Override
        public String call() {
            try {
                InputStreamReader a = spawnSpy();
                BufferedReader stream = new BufferedReader(a);

                String line;
                while ((line = stream.readLine()) != null){
                    if(line.contains(expected))
                        break;
                }
                return line;
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public ReadUntil(String expect) {
            expected = expect;
        }
    }
}
