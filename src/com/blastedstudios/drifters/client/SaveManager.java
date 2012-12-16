package com.blastedstudios.drifters.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.server.Server;
import com.blastedstudios.drifters.world.Being;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

/**
 * Used by client to save and load characters
 */
public class SaveManager {
	private static Logger logger = Logger.getLogger(SaveManager.class.getCanonicalName());
	private static String SAVE_DIRECTORY = Server.DRIFTERS_DIRECTORY + "/accounts";

	public static List<NetBeing> load() {
		List<NetBeing> beings = new ArrayList<NetBeing>();
		File saveDirectory = new File(SAVE_DIRECTORY);
		logger.info("Loading beings from " + saveDirectory.getAbsolutePath() + "...");
		if(!saveDirectory.isDirectory())
			saveDirectory.mkdirs();
		for(File file : saveDirectory.listFiles()){
			try {
				FileInputStream fis = new FileInputStream(file);
				CodedInputStream inputStream = CodedInputStream.newInstance(fis); 
				NetBeing being = NetBeing.parseFrom(inputStream);
				fis.close();
				synchronized(beings){
					beings.add(being);
					logger.info("Loaded being: " + being.getName());
				}
			} catch (Exception e) {
				logger.severe("Failed to load being " + file.getName());
				e.printStackTrace();
			} 
		}
		logger.info("Done loading " + beings.size() + " being");
		return beings;
	}
	
	public static void save(Being being){
		NetBeing.Builder netBeing = NetBeing.newBuilder();
		netBeing.setBeingClass(being.getType());
		netBeing.setCash(being.getCash());
		netBeing.setCurrentGun(being.getCurrentGun());
		netBeing.setRace(netBeing.getRace());
		netBeing.setHp(being.getHp());
		netBeing.setLevel(being.getLevel());
		netBeing.setMaxHp(being.getMaxHP());
		netBeing.setName(being.getName());
		netBeing.setPosX(being.getPosition().x);
		netBeing.setPosY(being.getPosition().y);
		netBeing.setVelX(being.getVelocity().x);
		netBeing.setVelY(being.getVelocity().y);
		netBeing.setXp(being.getXp());
		for(Gun gun : being.getGuns())
			netBeing.addGuns(gun);
		save(netBeing.build());
	}

	public static void save(NetBeing being){
		File accountFile = new File(SAVE_DIRECTORY + "/" + being.getName());
		try{
			FileOutputStream fos = new FileOutputStream(accountFile);
			CodedOutputStream output = CodedOutputStream.newInstance(fos);
			being.writeTo(output);
			output.flush();
			fos.close();
		}catch(Exception e){
			logger.severe("Failed to write account " + being.getName());
			e.printStackTrace();
		}
		logger.info("Saved " + being.getName() + " successfully");
	}
}
