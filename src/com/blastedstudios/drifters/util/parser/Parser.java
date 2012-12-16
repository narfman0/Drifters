package com.blastedstudios.drifters.util.parser;

import java.util.ArrayList;
import java.util.List;

import com.blastedstudios.drifters.util.EventEnum;
import com.blastedstudios.drifters.util.EventManager;
import com.blastedstudios.drifters.util.EventManager.EventListener;

public class Parser implements EventListener {
	private final List<ParserKeyword> children;
	
	public Parser(){
		children = new ArrayList<ParserKeyword>();
		List<ParserKeyword> empty = new ArrayList<ParserKeyword>();
		ParserKeyword visible = new ParserKeyword("visible", EventEnum.PARSER_AI_GRAPH_VISIBLE, empty);
		ParserKeyword graph = new ParserKeyword("graph", null, visible);
		ParserKeyword ai = new ParserKeyword("ai", null, graph);
		children.add(ai);
		EventManager.addListener(EventEnum.PARSER_ADD_CHILD, this);
	}
	
	public void execute(String command){
		for(ParserKeyword keyword : children){
			if(command.split(" ").length > 0 && command.split(" ")[0].equals(keyword.getKeyword())){
				String commandSub = command.substring(command.indexOf(" ")+1);
				if(keyword.isValid(commandSub))
					keyword.execute(commandSub);
			}
		}
	}

	@Override public void handleEvent(EventEnum event, Object... data) {
		switch(event){
		case PARSER_ADD_CHILD:
			children.add((ParserKeyword)data[0]);
			break;
		default:
			break;
		}
	}
}
