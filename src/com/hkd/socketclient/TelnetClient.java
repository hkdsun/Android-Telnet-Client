package com.hkd.socketclient;


import java.io.*;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;


public class TelnetClient {
    private TelnetConnection client;
    private OutputStream outstream;
    private org.apache.commons.net.telnet.TelnetClient connection;
    private BufferedInputStream instream;

    public TelnetClient(String ip, int port) throws IOException {
        client = new TelnetConnection(ip,port);
        client.connect();
        connection = client.getConnection();
        outstream = client.getOutput();
        instream = client.getReader();
    }

    public void close() throws IOException {
        connection.disconnect();
        return;
    }

    /**
     *
     * @param cmd the string of message you want to send
     * @return true if message was sent successfully
     */
    public boolean sendCommand(String cmd){
		if(client==null || !client.isConnected()){
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

    public String getResponse(String cmd,  int line) throws IOException, InterruptedException {

        if(client==null || !client.isConnected()){
            throw new IOException("Unable to send message to disconnected client");
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(cmd.toUpperCase(Locale.ENGLISH));
        stringBuilder.append("\n\r");

        byte[] cmdbyte = stringBuilder.toString().getBytes();

        BufferedReader buf = spawnSpy();
        buf = new BufferedReader(new InputStreamReader(new BufferedInputStream(instream)));
        outstream.write(cmdbyte, 0, cmdbyte.length);
        outstream.flush();

        String res = null;
        for (int i = 0; i < line+1; i++) {
            res = buf.readLine();
        }
        buf.close();
        return res;
    }

    public BufferedReader spawnSpy() throws InterruptedException {
        PipedInputStream pipe = new PipedInputStream();
        Thread t = new Thread(new ReaderThread(pipe));
        t.start();
        synchronized (t){
            t.wait();
        }
        return new BufferedReader(new InputStreamReader(pipe));
    }

    public class ReaderThread implements Runnable {
        PipedInputStream pipe;
        @Override
        public void run() {
            synchronized (this){
                PipedOutputStream spy = new PipedOutputStream();
                connection.registerSpyStream(spy);
                try {
                    spy.connect(pipe);
                    this.notifyAll();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public ReaderThread(PipedInputStream stream){
            pipe = stream;
        }
    }
}
