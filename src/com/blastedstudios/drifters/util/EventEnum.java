package com.blastedstudios.drifters.util;

public enum EventEnum {
	UNDEFINED, 
	ACCOUNT_RETRIEVE_REQUEST, 
	ACCOUNT_RETRIEVE_RESPONSE, 
	CHARACTER_CHOSEN_INITIATE, 
	CHARACTER_CHOSEN_COMPLETE, 
	CHARACTER_CREATE_REQUEST, 
	CHARACTER_CREATE_FAILED, 
	CHARACTER_CREATE_SUCCESS,
	CHARACTER_GUN_BUY_REQUEST, 
	CHARACTER_GUN_BUY_RESPONSE, 
	CHARACTER_POSITION_CLIENT,
	CHARACTER_POSITION_SERVER, 
	CHARACTER_RELOAD_REQUEST,
	CHARACTER_RELOAD_SUCCESS,
	CHARACTER_REWARD,
	CHARACTER_SHOT,
	GUN_SHOT_REQUEST,
	GUN_SHOT,
	LOGOUT_INITIATE, 
	LOGOUT_COMPLETE, 
	PARSER_ADD_CHILD,
	PARSER_AI_GRAPH_VISIBLE,
	WORLD_STRATEGIC_POINT_CAPTURED,
	WORLD_STRATEGIC_POINT_LOST,
	WORLD_WEAPON_LOCKER_ADDED,
	WORLD_WEAPON_LOCKER_REMOVED,
	WORLD_RENDER;

	public static EventEnum fromInteger(int type){
		for(EventEnum msgType : values())
			if(msgType.ordinal() == type)
				return msgType;
		return UNDEFINED;
	}
}