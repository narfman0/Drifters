package com.blastedstudios.drifters.server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Timer;
import java.util.TimerTask;

import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.util.Properties;

public class ServerSocketThread {
	private Timer timer;
	private ServerSocket serverSocket;

	public ServerSocketThread(final Server server) {
		try {
			serverSocket = new ServerSocket(Properties.getInt("network.port"));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		timer = new Timer("Network", true);
		timer.schedule(new TimerTask() {
			@Override public void run() {
				try{
					new ClientSocketThread(serverSocket.accept(), server).start();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}, 0, Properties.getInt("network.client.connect.frequency"));
	}
}
