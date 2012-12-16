package com.blastedstudios.drifters.client;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.network.Generated.GunShot;
import com.blastedstudios.drifters.network.Generated.NetAccount;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.network.Generated.FactionType;
import com.blastedstudios.drifters.network.Generated.PlayerReward;
import com.blastedstudios.drifters.network.Generated.ShotDamage;
import com.blastedstudios.drifters.network.Generated.NetBeing.BeingType;
import com.blastedstudios.drifters.network.Generated.NetBeingCreateFailed;
import com.blastedstudios.drifters.network.Generated.NetBeingList;
import com.blastedstudios.drifters.network.Generated.ReloadRequest;
import com.blastedstudios.drifters.network.Generated.WeaponLocker;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

public class Network implements EventListener{
	private static Logger logger = Logger.getLogger(Network.class.getCanonicalName());
	private static final EventEnum[] INTERESTING_EVENTS = new EventEnum[]{
		EventEnum.ACCOUNT_RETRIEVE_REQUEST, EventEnum.CHARACTER_POSITION_CLIENT,
		EventEnum.CHARACTER_CHOSEN_INITIATE, EventEnum.CHARACTER_CHOSEN_COMPLETE,
		EventEnum.CHARACTER_CREATE_REQUEST, EventEnum.CHARACTER_GUN_BUY_REQUEST,
		EventEnum.CHARACTER_RELOAD_REQUEST,
		EventEnum.GUN_SHOT_REQUEST, EventEnum.LOGOUT_INITIATE
	};
	private Socket socket;
	private Timer networkThread;
	private CodedOutputStream outToServer;
	private CodedInputStream inFromServer;
	
	public Network(){
		for(EventEnum event : INTERESTING_EVENTS)
			EventManager.addListener(event, this);
	}

	@Override public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case ACCOUNT_RETRIEVE_REQUEST:{
			connect();
			NetAccount.Builder acct = NetAccount.newBuilder();
			acct.setEmail((String) data[0]);
			acct.setPassword((String) data[1]);
			sendMessage(EventEnum.ACCOUNT_RETRIEVE_REQUEST, acct.build());
			break;
		}case CHARACTER_CREATE_REQUEST:{
			NetBeing.Builder beingBuilder = NetBeing.newBuilder();
			beingBuilder.setAccount((String) data[0]);
			beingBuilder.setName((String) data[1]);
			beingBuilder.setBeingType((BeingType) data[2]);
			beingBuilder.setFactionType((FactionType) data[3]);
			sendMessage(EventEnum.CHARACTER_CREATE_REQUEST, beingBuilder.build());
			break;
		}case CHARACTER_POSITION_CLIENT:{
			Being being = (Being) data[0];
			NetBeing.Builder beingBuilder = NetBeing.newBuilder();
			beingBuilder.setName(being.getName());
			beingBuilder.setPosX(being.getPosition().x);
			beingBuilder.setPosY(being.getPosition().y);
			beingBuilder.setVelX(being.getVelocity().x);
			beingBuilder.setVelY(being.getVelocity().y);
			beingBuilder.setHp(being.getHp());
			beingBuilder.setCurrentGun(being.getCurrentGun());
			beingBuilder.setCash(being.getCash());
			sendMessage(EventEnum.CHARACTER_POSITION_CLIENT, beingBuilder.build());
			break;
		}case CHARACTER_CHOSEN_INITIATE:
			sendMessage(EventEnum.CHARACTER_CHOSEN_INITIATE, (NetBeing)data[0]);
			break;
		case CHARACTER_GUN_BUY_REQUEST:
			sendMessage(EventEnum.CHARACTER_GUN_BUY_REQUEST, (Gun)data[0]);
			break;
		case CHARACTER_RELOAD_REQUEST:
			ReloadRequest.Builder reload = ReloadRequest.newBuilder();
			reload.setBeing((String) data[0]);
			sendMessage(EventEnum.CHARACTER_RELOAD_REQUEST, reload.build());
			break;
		case GUN_SHOT_REQUEST:
			sendMessage(EventEnum.GUN_SHOT_REQUEST, (GunShot)data[0]);
			break;
		case LOGOUT_INITIATE:{
			sendMessage(EventEnum.LOGOUT_INITIATE);
			disconnect();
			EventManager.sendEvent(EventEnum.LOGOUT_COMPLETE);
			break;
		}
		default:
			//sendMessage(event, (Message)data[0]);
			break;
		}
	}

	private void handleMessageReceived(EventEnum type, byte[] buffer) throws IOException{
		switch(type){
		case ACCOUNT_RETRIEVE_RESPONSE:
			logger.info("Login success");
			NetAccount netAccount = NetAccount.parseFrom(buffer);
			EventManager.sendEvent(EventEnum.ACCOUNT_RETRIEVE_RESPONSE, netAccount);
			break;
		case CHARACTER_CREATE_FAILED:
			logger.info("Character create failed");
			String reason = NetBeingCreateFailed.parseFrom(buffer).getReason();
			EventManager.sendEvent(EventEnum.CHARACTER_CREATE_FAILED, reason);
			break;
		case CHARACTER_CREATE_SUCCESS:
			NetBeing netBeing = NetBeing.parseFrom(buffer);
			logger.info("Character create success for " + netBeing.getName());
			EventManager.sendEvent(EventEnum.CHARACTER_CREATE_SUCCESS, netBeing);
			break;
		case CHARACTER_CHOSEN_COMPLETE:
			EventManager.sendEvent(EventEnum.CHARACTER_CHOSEN_COMPLETE);
			break;
		case CHARACTER_GUN_BUY_RESPONSE:
			EventManager.sendEvent(EventEnum.CHARACTER_GUN_BUY_RESPONSE, Gun.parseFrom(buffer));
			break;
		case CHARACTER_POSITION_SERVER:
			EventManager.sendEvent(EventEnum.CHARACTER_POSITION_SERVER, NetBeingList.parseFrom(buffer));
			break;
		case CHARACTER_RELOAD_SUCCESS:
			EventManager.sendEvent(EventEnum.CHARACTER_RELOAD_SUCCESS, ReloadRequest.parseFrom(buffer));
			break;
		case CHARACTER_REWARD:
			EventManager.sendEvent(EventEnum.CHARACTER_REWARD, PlayerReward.parseFrom(buffer));
			break;
		case CHARACTER_SHOT:
			EventManager.sendEvent(EventEnum.CHARACTER_SHOT, ShotDamage.parseFrom(buffer));
			break;
		case GUN_SHOT:
			EventManager.sendEvent(EventEnum.GUN_SHOT, GunShot.parseFrom(buffer));
			break;
		case WORLD_WEAPON_LOCKER_ADDED:
			EventManager.sendEvent(EventEnum.WORLD_WEAPON_LOCKER_ADDED, WeaponLocker.parseFrom(buffer));
			break;
		case WORLD_WEAPON_LOCKER_REMOVED:
			EventManager.sendEvent(EventEnum.WORLD_WEAPON_LOCKER_REMOVED, WeaponLocker.parseFrom(buffer));
			break;
		default:
			break;
		}
	}
	
	private void sendMessage(EventEnum type, Message... send){
		try {
			outToServer.writeUInt32NoTag(type.ordinal());
			for(Object object : send){
				Method serializedSizeMeth = object.getClass().getMethod("getSerializedSize");
				int size = (Integer) serializedSizeMeth.invoke(object);
				outToServer.writeInt32NoTag(size);
				Method writeToMeth = object.getClass().getMethod("writeTo", CodedOutputStream.class);
				writeToMeth.invoke(object, outToServer);
			}
			if(send.length == 0)
				outToServer.writeInt32NoTag(0);
			outToServer.flush();
		} catch (Exception e) {
			logger.severe("Socket faulty with message " + e.getMessage() + ", closing");
			try {
				socket.close();
			} catch (Exception e1) {}
			EventManager.sendEvent(EventEnum.LOGOUT_COMPLETE);
		}
	}

	private void disconnect(){
		if(socket != null)
			try {
				socket.close();
				logger.info("Logged out successfully");
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	private void connect(){
		try {
			socket = new Socket(Properties.get("network.host"), Properties.getInt("network.port"));
			logger.info("Connected to server successfully");
			outToServer = CodedOutputStream.newInstance(socket.getOutputStream());
			inFromServer = CodedInputStream.newInstance(socket.getInputStream());
			networkThread = new Timer("Network", true);
			networkThread.schedule(new TimerTask() {
				@Override public void run() {
					try {
						while(socket.getInputStream().available() > 0){
							EventEnum type = EventEnum.fromInteger(inFromServer.readUInt32());
							byte[] buffer =  inFromServer.readRawBytes(inFromServer.readInt32());
							handleMessageReceived(type, buffer);
						}
					} catch (IOException e) {
						if(socket.isClosed()){
							logger.info("Socket is closed, shutting down read thread");
							cancel();
						}else
							e.printStackTrace();
					}
				}
			}, 0, 10);
		} catch (Exception e) {
			logger.info("Failed to connect to server " + Properties.get("network.host") + 
					" on port " + Properties.getInt("network.port"));
			e.printStackTrace();
		}
	}
}
