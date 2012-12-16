package com.blastedstudios.drifters.client;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;
import com.blastedstudios.drifters.network.Generated.WeaponLocker;
import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;

public class WeaponLockerManager implements EventListener {
	private static final float LOCKER_DISTANCE = 4f;
	private List<WeaponLocker> weaponLockers;

	public WeaponLockerManager(){
		weaponLockers = new ArrayList<WeaponLocker>();
		EventManager.addListener(EventEnum.WORLD_WEAPON_LOCKER_ADDED, this);
		EventManager.addListener(EventEnum.WORLD_WEAPON_LOCKER_REMOVED, this);
	}

	@Override
	public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case WORLD_WEAPON_LOCKER_ADDED:
			weaponLockers.add((WeaponLocker) data[0]);
			break;
		case WORLD_WEAPON_LOCKER_REMOVED:
			WeaponLocker locker = (WeaponLocker) data[0];
			for(int i=0; i<weaponLockers.size(); i++){
				Vector2 lockerPosition = new Vector2(weaponLockers.get(i).getPosX(), weaponLockers.get(i).getPosY());
				if(lockerPosition.dst(locker.getPosX(), locker.getPosY()) < LOCKER_DISTANCE)
					weaponLockers.remove(i);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * @return true if the given position is close enough to a weapon locker to buy
	 */
	public boolean isWithinRange(Vector2 pos){
		for(WeaponLocker locker : weaponLockers)
			if(pos.dst(locker.getPosX(), locker.getPosY()) < LOCKER_DISTANCE)
				return true;
		return false;
	}
}
