package com.hkd.socketclient;


import java.io.*;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class TelnetClient {
    private TelnetConnection client;
    private OutputStream outstream;
    private org.apache.commons.net.telnet.TelnetClient connection;
    private BufferedInputStream instream;

    public TelnetClient(String ip, int port) throws IOException {
        client = new TelnetConnection(ip,port);
        connection = client.getConnection();
        outstream = client.getOutput();
        instream = client.getReader();
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

    public String getResponse(String cmd,  TimeUnit timeout) throws IOException {
        if(client==null || !client.isConnected()){
            throw new IOException("Unable to send message to disconnected client");
        }

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(cmd.toUpperCase(Locale.ENGLISH));
        stringBuilder.append("\n\r");

        byte[] cmdbyte = stringBuilder.toString().getBytes();

        FilterInputStream inspy;

        connection.registerSpyStream(spy);

        // TODO

        outstream.write(cmdbyte, 0, cmdbyte.length);
        outstream.flush();



        return "hello world";
    }
}
