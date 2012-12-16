package com.blastedstudios.drifters.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
	private static Map<EventEnum, List<EventListener>> listeners = 
			Collections.synchronizedMap(new HashMap<EventEnum, List<EventListener>>());

	public synchronized static void addListener(EventEnum event, EventListener listener){
		if(!listeners.containsKey(event))
			listeners.put(event, Collections.synchronizedList(new ArrayList<EventListener>()));
		listeners.get(event).add(listener);
	}

	public synchronized static void removeListener(EventEnum event, EventListener listener){
		if(listeners.containsKey(event)){
			listeners.get(event).remove(listener);
			if(listeners.get(event).isEmpty())
				listeners.remove(event);
		}
	}

	public synchronized static void sendEvent(EventEnum event, Object... args) {
		for(int i=0; listeners.containsKey(event) && i<listeners.get(event).size(); i++)
			listeners.get(event).get(i).handleEvent(event, args);
	}

	public interface EventListener{
		public void handleEvent(EventEnum event, Object... data);
	}
}
