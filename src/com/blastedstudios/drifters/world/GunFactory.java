package com.blastedstudios.drifters.world;

import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.network.Generated.Gun.WeaponType;

public class GunFactory {
	public static Gun[] getStockGuns(){
		return new Gun[]{GunFactory.getAk47(), 
			GunFactory.getBenelliM4(), GunFactory.getPSG1()};
	}
	
	public static Gun getAk47(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.9f);
		gun.setDamage(40);
		gun.setMuzzleVelocity(715);
		gun.setRateOfFire(10);
		gun.setRecoil(20);
		gun.setReloadSpeed(4.5f);
		gun.setType(WeaponType.RIFLE);
		gun.setRoundsPerClip(30);
		gun.setCurrentRounds(30);
		gun.setName("Ak-47");
		gun.setCost(750);
		return gun.build();
	}
	
	public static Gun getBenelliM4(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.6f);
		gun.setDamage(20);
		gun.setMuzzleVelocity(500);
		gun.setRateOfFire(.7f);
		gun.setRecoil(50);
		gun.setReloadSpeed(10f);
		gun.setType(WeaponType.SHOTGUN);
		gun.setRoundsPerClip(8);
		gun.setCurrentRounds(8);
		gun.setName("Benelli M4");
		gun.setCost(1700);
		return gun.build();
	}

	public static Gun getFAMAS(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.95f);
		gun.setDamage(33);
		gun.setMuzzleVelocity(960);
		gun.setRateOfFire(16);
		gun.setRecoil(15);
		gun.setReloadSpeed(4f);
		gun.setType(WeaponType.RIFLE);
		gun.setRoundsPerClip(25);
		gun.setCurrentRounds(25);
		gun.setName("FAMAS");
		gun.setCost(1750);
		return gun.build();
	}
	
	public static Gun getGlock(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.83f);
		gun.setDamage(25);
		gun.setMuzzleVelocity(375);
		gun.setRateOfFire(1.5f);
		gun.setRecoil(11.7f);
		gun.setReloadSpeed(3);
		gun.setType(WeaponType.PISTOL);
		gun.setRoundsPerClip(16);
		gun.setCurrentRounds(16);
		gun.setName("Glock");
		gun.setCost(100);
		return gun.build();
	}

	public static Gun getMP5(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.72f);
		gun.setDamage(23);
		gun.setMuzzleVelocity(400);
		gun.setRateOfFire(13);
		gun.setRecoil(7.5f);
		gun.setReloadSpeed(6);
		gun.setType(WeaponType.SMG);
		gun.setRoundsPerClip(30);
		gun.setCurrentRounds(30);
		gun.setName("MP5");
		gun.setCost(1200);
		return gun.build();
	}

	public static Gun getPSG1(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.99f);
		gun.setDamage(75);
		gun.setMuzzleVelocity(868);
		gun.setRateOfFire(.4f);
		gun.setRecoil(10);
		gun.setReloadSpeed(6);
		gun.setType(WeaponType.SNIPER);
		gun.setRoundsPerClip(5);
		gun.setCurrentRounds(5);
		gun.setName("PSG1");
		gun.setCost(10000);
		return gun.build();
	}

	public static Gun getSteyrAUG(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.93f);
		gun.setDamage(35);
		gun.setMuzzleVelocity(970);
		gun.setRateOfFire(12);
		gun.setRecoil(15);
		gun.setReloadSpeed(4f);
		gun.setType(WeaponType.RIFLE);
		gun.setRoundsPerClip(30);
		gun.setCurrentRounds(30);
		gun.setName("Steyr AUG");
		gun.setCost(2230);
		return gun.build();
	}

	public static Gun getUZI(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.65f);
		gun.setDamage(20);
		gun.setMuzzleVelocity(400);
		gun.setRateOfFire(10);
		gun.setRecoil(7);
		gun.setReloadSpeed(7);
		gun.setType(WeaponType.SMG);
		gun.setRoundsPerClip(32);
		gun.setCurrentRounds(32);
		gun.setName("Uzi");
		gun.setCost(600);
		return gun.build();
	}

	public static Gun getWaltherPPK(){
		Gun.Builder gun = Gun.newBuilder();
		gun.setAccuracy(.9f);
		gun.setDamage(20);
		gun.setMuzzleVelocity(256);
		gun.setRateOfFire(5);
		gun.setRecoil(1);
		gun.setReloadSpeed(2);
		gun.setType(WeaponType.PISTOL);
		gun.setRoundsPerClip(9);
		gun.setCurrentRounds(9);
		gun.setName("Walther PPK");
		gun.setCost(1000);
		return gun.build();
	}
}
