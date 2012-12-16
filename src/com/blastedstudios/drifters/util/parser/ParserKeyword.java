package com.blastedstudios.drifters.util.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;

public class ParserKeyword {
	private final List<ParserKeyword> children;
	private final String keyword;
	private final EventEnum event;
	
	public ParserKeyword(String keyword, EventEnum event, List<ParserKeyword> children){
		this.keyword = keyword;
		this.children = children;
		this.event = event;
	}

	public ParserKeyword(String keyword, EventEnum event, ParserKeyword... children){
		this.keyword = keyword;
		this.children = Arrays.asList(children);
		this.event = event;
	}
	
	/**
	 * @return if keyword path is valid (NOT if content is valid)
	 */
	public boolean isValid(String command){
		if(command.split(" ").length > 0){
			String current = command.split(" ")[0];
			for(ParserKeyword child : children)
				if(child.getKeyword().equals(current))
					return child.isValid(command.replace(current+" ", ""));
		}
		// Checked all the children, might want to check content 
		// (bool/float/etc) 
		return true;
	}
	
	public List<String> getSuggestions(String command){
		List<String> suggestions = new ArrayList<String>();
		for(ParserKeyword child : children)
			if(command.length() == 0 || command.contains("?") || 
					child.getKeyword().contains(command))
				suggestions.add(child.getKeyword());
		return suggestions;
	}
	
	public void execute(String command){
		if(!command.contains(" "))
			EventManager.sendEvent(event, command);
		else
			for(ParserKeyword child : children){
				String current = command.split(" ")[0];
				if(child.getKeyword().equals(current))
					child.execute(command.replace(current+" ", ""));
			}
	}

	public List<ParserKeyword> getChildren() {
		return children;
	}

	public String getKeyword() {
		return keyword;
	}

	public EventEnum getEvent() {
		return event;
	}
}
