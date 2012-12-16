package com.blastedstudios.drifters.util;

import java.io.File;
import java.io.FileInputStream;

public class Properties {
	private static java.util.Properties properties;

	static{
		properties = new java.util.Properties();
		try {
			properties.load(new FileInputStream(new File("data/drifters.properties")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean contains(String key){
		return properties.containsKey(key);
	}
	
	public static String get(String key){
		return properties.getProperty(key);
	}
	
	public static boolean getBool(String key){
		return Boolean.parseBoolean(get(key));
	}
	
	public static int getInt(String key){
		return Integer.parseInt(get(key));
	}

	public static float getFloat(String key) {
		return Float.parseFloat(get(key));
	}
}
