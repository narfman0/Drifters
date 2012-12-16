package com.blastedstudios.drifters.server.network;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Collection;
import java.util.logging.Logger;

import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.network.Generated.GunShot;
import com.blastedstudios.drifters.network.Generated.NetAccount;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.network.Generated.NetBeingCreateFailed;
import com.blastedstudios.drifters.network.Generated.NetBeingList;
import com.blastedstudios.drifters.network.Generated.PlayerReward;
import com.blastedstudios.drifters.network.Generated.ReloadRequest;
import com.blastedstudios.drifters.network.Generated.ShotDamage;
import com.blastedstudios.drifters.network.Generated.WeaponLocker;
import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;
import com.blastedstudios.drifters.world.GunFactory;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.Message;

public class ClientSocketThread extends Thread implements EventListener{
	private static Logger logger = Logger.getLogger(ClientSocketThread.class.getCanonicalName());
	private static final EventEnum[] INTERESTING_EVENTS = { 
		EventEnum.CHARACTER_POSITION_SERVER, EventEnum.CHARACTER_RELOAD_SUCCESS, 
		EventEnum.CHARACTER_REWARD, EventEnum.CHARACTER_SHOT, EventEnum.GUN_SHOT,
		EventEnum.WORLD_WEAPON_LOCKER_ADDED, EventEnum.WORLD_WEAPON_LOCKER_REMOVED};
	private static int clientSocketThreadCount = 0;
	private Socket socket;
	private CodedOutputStream outToClient;
	private CodedInputStream inFromClient;
	private Server server;
	private NetAccount account;

	public ClientSocketThread(Socket clientSocket, Server server){
		super("ClientSocket-"+clientSocketThreadCount++);
		this.socket = clientSocket;
		this.server = server;
		logger.warning("client accepted " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
		try {
			outToClient = CodedOutputStream.newInstance(clientSocket.getOutputStream());
			inFromClient = CodedInputStream.newInstance(clientSocket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(EventEnum event : INTERESTING_EVENTS)
			EventManager.addListener(event, this);
	}

	@Override public void run(){
		while(true){
			if(!socket.isConnected() || socket.isClosed()){
				logger.warning("Client closed, disconnecting");
				break;
			}
			try{
				EventEnum type = EventEnum.fromInteger(inFromClient.readUInt32());
				byte[] buffer = inFromClient.readRawBytes(inFromClient.readInt32());
				handleMessage(type, buffer);
			}catch(Exception e){
				logger.severe("Socket faulty with message " + e.getMessage() + ", closing");
				try {
					socket.close();
				} catch (IOException e1) {}
				EventManager.removeListener(EventEnum.CHARACTER_POSITION_SERVER, this);
			}
		}
	}
	
	private void handleMessage(EventEnum type, byte[] buffer) 
	throws IOException, ClassNotFoundException{
		switch(type){
		case ACCOUNT_RETRIEVE_REQUEST:{
			NetAccount receivedAccount = NetAccount.parseFrom(buffer);
			account = server.accountThread.getAccount(receivedAccount.getEmail(), receivedAccount.getPassword());
			if(account != null)
				sendMessage(EventEnum.ACCOUNT_RETRIEVE_RESPONSE, account);
			logger.info("Account retrieved: " + receivedAccount.getEmail());
			break;
		}case CHARACTER_CREATE_REQUEST:{
			NetBeing.Builder netBeing = NetBeing.newBuilder(NetBeing.parseFrom(buffer)); 
			String failMessage = null;
			for(NetBeing existingBeing : server.accountThread.accounts.get(netBeing.getAccount()).getBeingsList())
				if(existingBeing.getName().equals(netBeing.getName())){
					failMessage = "Name already taken";
					logger.info("Name already taken for " + netBeing.getName());
				}
			if(failMessage == null){
				float maxHP = Properties.getInt("character.class." + netBeing.getBeingType().name().toLowerCase() + ".maxhp");
				netBeing.setMaxHp(maxHP);
				netBeing.setHp(maxHP);
				netBeing.addGuns(GunFactory.getGlock());
				switch(netBeing.getBeingType()){
				case ASSAULT:
					netBeing.addGuns(GunFactory.getAk47());
					break;
				case DEMO:
					netBeing.addGuns(GunFactory.getUZI());
					break;
				case SNIPER:
					netBeing.addGuns(GunFactory.getPSG1());
					break;
				case SPY:
					netBeing.addGuns(GunFactory.getWaltherPPK());
					break;
				case TANK:
					netBeing.addGuns(GunFactory.getBenelliM4());
					break;
				}
				netBeing.setCurrentGun(0);
				netBeing.setCash(0);
				netBeing.setLevel(1);
				netBeing.setXp(0);
				NetAccount previous = server.accountThread.accounts.remove(netBeing.getAccount());
				NetAccount.Builder account = NetAccount.newBuilder(previous);
				account.addBeings(netBeing);
				NetAccount newAccount = account.build();
				server.accountThread.accounts.put(account.getEmail(), newAccount);
				server.accountThread.saveAccount(newAccount);
				logger.info("Being " + netBeing.getName() + " created for acct " + netBeing.getAccount());
				sendMessage(EventEnum.CHARACTER_CREATE_SUCCESS, netBeing.build());
			}else{
				NetBeingCreateFailed.Builder failedMessage = NetBeingCreateFailed.newBuilder();
				failedMessage.setReason(failMessage);
				sendMessage(EventEnum.CHARACTER_CREATE_FAILED, failedMessage.build());
			}
			break;
		}case CHARACTER_GUN_BUY_REQUEST:{
			Gun gun = Gun.parseFrom(buffer);
			for(int i=0; i<account.getBeingsCount(); i++){
				Being being = server.world.getBeing(account.getBeings(i).getName());
				if(being != null && being.buy(gun)){
					logger.info("Being " + being.getName() + " successfully bought " + 
							gun.getName() + " for $" + gun.getCost());
					EventManager.sendEvent(EventEnum.CHARACTER_GUN_BUY_RESPONSE, gun);
				}
			}
			break;
		}case CHARACTER_POSITION_CLIENT:{
			EventManager.sendEvent(EventEnum.CHARACTER_POSITION_CLIENT, NetBeing.parseFrom(buffer));
			break;
		}case CHARACTER_CHOSEN_INITIATE:{
			NetBeing netBeing = NetBeing.parseFrom(buffer);
			NetAccount account = server.accountThread.accounts.get(netBeing.getAccount());
			for(NetBeing being : account.getBeingsList())
				if(being.getName().equals(netBeing.getName())){
					EventManager.sendEvent(EventEnum.CHARACTER_CHOSEN_INITIATE, netBeing);
					sendMessage(EventEnum.CHARACTER_CHOSEN_COMPLETE);
					
					for(WeaponLocker locker : server.world.getWeaponLockers())
						sendMessage(EventEnum.WORLD_WEAPON_LOCKER_ADDED, locker);
				}
			break;
		}case CHARACTER_RELOAD_REQUEST:
			ReloadRequest request = ReloadRequest.parseFrom(buffer);
			EventManager.sendEvent(EventEnum.CHARACTER_RELOAD_REQUEST, request.getBeing());
			break;
		case GUN_SHOT_REQUEST:{
			GunShot gunshot = GunShot.parseFrom(buffer);
			EventManager.sendEvent(EventEnum.GUN_SHOT_REQUEST, gunshot);
			break;
		}case LOGOUT_INITIATE:{
			disconnect();
		}case UNDEFINED:
			logger.severe("Undefined message received");
			break;
		default:
			break;
		}
	}
	
	private void sendMessage(EventEnum type, Message... send){
		try{
			synchronized (outToClient) {
				outToClient.writeUInt32NoTag(type.ordinal());
				for(Object object : send){
					Method serializedSizeMeth = object.getClass().getMethod("getSerializedSize");
					int size = (Integer) serializedSizeMeth.invoke(object);
					outToClient.writeInt32NoTag(size);
					Method writeToMeth = object.getClass().getMethod("writeTo", CodedOutputStream.class);
					writeToMeth.invoke(object, outToClient);
				}
				if(send.length == 0)
					outToClient.writeInt32NoTag(0);
				outToClient.flush();
			}
		}catch(Exception e){
			logger.severe("Failed to create message of type " + type + ", closing connection" );
			e.printStackTrace();
			disconnect();
		}
	}

	@Override public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case CHARACTER_POSITION_SERVER:{
			@SuppressWarnings("unchecked")
			Collection<Being> beings = (Collection<Being>) data[0];
			NetBeingList.Builder netBeingList = NetBeingList.newBuilder();
			for(Being being : beings){
				NetBeing.Builder beingBuilder = NetBeing.newBuilder();
				beingBuilder.setName(being.getName());
				if(being.getAccount() != null)
					beingBuilder.setAccount(being.getAccount().getEmail());
				beingBuilder.setBeingType(being.getType());
				beingBuilder.setFactionType(being.getFactionType());
				beingBuilder.setPosX(being.getPosition().x);
				beingBuilder.setPosY(being.getPosition().y);
				beingBuilder.setVelX(being.getVelocity().x);
				beingBuilder.setVelY(being.getVelocity().y);
				beingBuilder.setMaxHp(being.getMaxHP());
				beingBuilder.setHp(being.getHp());
				beingBuilder.setCurrentGun(being.getCurrentGun());
				beingBuilder.setCash(being.getCash());
				beingBuilder.addAllGuns(being.getGuns());
				netBeingList.addBeings(beingBuilder.build());
			}
			sendMessage(EventEnum.CHARACTER_POSITION_SERVER, netBeingList.build());
			break;
		}case CHARACTER_RELOAD_SUCCESS:{
			String beingName = (String) data[0];
			ReloadRequest.Builder reloadRequest = ReloadRequest.newBuilder();
			reloadRequest.setBeing(beingName);
			sendMessage(EventEnum.CHARACTER_RELOAD_SUCCESS, reloadRequest.build());
			break;
		}case CHARACTER_REWARD:{
			sendMessage(EventEnum.CHARACTER_REWARD, (PlayerReward) data[0]);
			break;
		}case CHARACTER_SHOT:{
			sendMessage(EventEnum.CHARACTER_SHOT, (ShotDamage)data[0]);
			break;
		}case GUN_SHOT:{
			sendMessage(EventEnum.GUN_SHOT, (GunShot) data[0]);
			break;
		}case WORLD_WEAPON_LOCKER_ADDED:{
			sendMessage(EventEnum.WORLD_WEAPON_LOCKER_ADDED, (WeaponLocker) data[0]);
			break;
		}case WORLD_WEAPON_LOCKER_REMOVED:{
			sendMessage(EventEnum.WORLD_WEAPON_LOCKER_REMOVED, (WeaponLocker) data[0]);
			break;
		}default:
			//sendMessage(event, (Message) data[0]);
			break;
		}
	}
	
	private void disconnect(){
		for(EventEnum event : INTERESTING_EVENTS)
			EventManager.removeListener(event, this);
		try {
			socket.close();
		} catch (IOException e) {}
		if(account != null){
			server.accountThread.saveAccount(account);
			EventManager.sendEvent(EventEnum.LOGOUT_COMPLETE, account);
		}
	}
}
