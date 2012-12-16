package com.blastedstudios.drifters.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import com.blastedstudios.drifters.network.Generated.NetAccount;
import com.blastedstudios.drifters.network.Generated.NetBeing;
import com.blastedstudios.drifters.util.Properties;
import com.blastedstudios.drifters.world.Being;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;

public class AccountThread {
	private static Logger logger = Logger.getLogger(AccountThread.class.getCanonicalName());
	private static int SAVE_FREQUENCY_SECONDS = Properties.getInt("account.save.frequency");
	private static String ACCOUNT_DIRECTORY = Server.BADGE_DIRECTORY + "/accounts";
	public Map<String, NetAccount> accounts;
	final Server server;
	
	public AccountThread(final Server server){
		this.server = server;
		accounts = Collections.synchronizedMap(new HashMap<String, NetAccount>());
		loadAccounts();
		Timer timer = new Timer("Account", true);
		timer.schedule(new TimerTask() {
			@Override public void run() {
				synchronized(accounts){
					for(NetAccount account : accounts.values())
						saveAccount(account);
				}
			}
		}, SAVE_FREQUENCY_SECONDS, SAVE_FREQUENCY_SECONDS);
	}

	private void loadAccounts() {
		logger.info("Loading accounts...");
		File accountDirectory = new File(ACCOUNT_DIRECTORY);
		if(!accountDirectory.isDirectory())
			accountDirectory.mkdirs();
		for(File file : accountDirectory.listFiles()){
			try {
				FileInputStream fis = new FileInputStream(file);
				CodedInputStream inputStream = CodedInputStream.newInstance(fis); 
				NetAccount account = NetAccount.parseFrom(inputStream);
				fis.close();
				synchronized(accounts){
					accounts.put(account.getEmail(), account);
				}
				logger.info("Loaded account: " + account.getEmail());
			} catch (Exception e) {
				logger.severe("Failed to load account " + file.getName());
				e.printStackTrace();
			} 
		}
		logger.info("Done loading accounts");
	}

	public void saveAccount(NetAccount account){
		File accountFile = new File(ACCOUNT_DIRECTORY + "/" + account.getEmail());
		try{
			FileOutputStream fos = new FileOutputStream(accountFile);
			CodedOutputStream output = CodedOutputStream.newInstance(fos);
			account.writeTo(output);
			output.flush();
			fos.close();
		}catch(Exception e){
			logger.severe("Failed to write account " + account.getEmail());
			e.printStackTrace();
		}
		logger.info("Saved account " + account.getEmail() + " successfully");
	}

	public NetAccount getAccount(String email, String pass) {
		if(!accounts.containsKey(email)){
			NetAccount.Builder builder = NetAccount.newBuilder();
			builder.setEmail(email);
			builder.setPassword(pass);
			synchronized(accounts){
				accounts.put(email, builder.build());
			}
			logger.info("Added account: " + email);
		}
		return accounts.get(email);
	}

	public void updateAccount(Being being) {
		NetAccount.Builder accountBuilder = NetAccount.newBuilder(accounts.get(being.getAccount().getEmail()));
		for(int i=0; i<accountBuilder.getBeingsCount(); i++) {
			NetBeing netBeing = accountBuilder.getBeings(i);
			if(netBeing.getName().equals(being.getName())){
				NetBeing.Builder beingBuilder = NetBeing.newBuilder(netBeing);
				beingBuilder.setPosX(being.getPosition().x);
				beingBuilder.setPosY(being.getPosition().y);
				beingBuilder.setMaxHp(being.getMaxHP());
				beingBuilder.setHp(being.getHp());
				while(beingBuilder.getGunsCount() > 0)
					beingBuilder.removeGuns(0);
				beingBuilder.addAllGuns(being.getGuns());
				beingBuilder.setCurrentGun(being.getCurrentGun());
				beingBuilder.setCash(being.getCash());
				beingBuilder.setLevel(being.getLevel());
				beingBuilder.setXp(being.getXp());
				logger.info("Updating being " + being.getName() + " to " + being.getPosition());
				accountBuilder.removeBeings(i);
				accountBuilder.addBeings(beingBuilder.build());
			}
		}
		NetAccount account = accountBuilder.build();
		synchronized(accounts){
			accounts.put(account.getEmail(), account);
		}
		being.setAccount(account);
	}
}
