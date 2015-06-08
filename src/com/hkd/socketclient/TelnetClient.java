package com.hkd.socketclient;


import java.io.*;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;

import static java.lang.Thread.sleep;


public class TelnetClient {
    private TelnetConnection client;
    private OutputStream outstream;
    private org.apache.commons.net.telnet.TelnetClient rawConnection;
    private BufferedInputStream instream;
    AtomicInteger guy;

    public TelnetClient(String ip, int port) throws IOException {
        client = new TelnetConnection(ip, port);
        client.connect();
        rawConnection = client.getConnection();
        outstream = client.getOutput();
        instream = client.getReader();
        guy = new AtomicInteger();
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
        final BufferedReader stream = new BufferedReader(spawnSpy());
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
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
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

        BufferedReader buf = new BufferedReader(spawnSpy());
        while(buf.ready())
            buf.read();
        outstream.write(cmdbyte, 0, cmdbyte.length);
        outstream.flush();
        String result = buf.readLine();
        while(result.length() < 1 || result.contains(cmd))
            result = buf.readLine();
        buf.close();

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

        BufferedReader buf = new BufferedReader(spawnSpy());
        outstream.write(cmdbyte, 0, cmdbyte.length);
        outstream.flush();

        if (timeout == -1) {
            return readUntil(expected);
        } else {
            return readUntil(expected, timeout);
        }
    }

    public InputStreamReader spawnSpy() throws InterruptedException, IOException {
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        in.connect(out);
        return spawnSpy(in,out);
    }

    private InputStreamReader spawnSpy(final PipedInputStream pipein, final PipedOutputStream pipeout) throws InterruptedException {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    IOUtils.copy(instream, pipeout);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(r).start();

        return new InputStreamReader(pipein);
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
        return client.disconnect();
    }

    private class ReadUntil implements Callable<String> {
        String expected;

        @Override
        public String call() {
            try {
                final InputStreamReader reader = spawnSpy();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            IOUtils.copy(reader,System.out);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                BufferedReader stream = new BufferedReader(spawnSpy());

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
