package com.blastedstudios.drifters.client.ui.gameplay;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.drifters.network.Generated.Gun;
import com.blastedstudios.drifters.world.Being;

public class InventoryWindow extends Window {
	public InventoryWindow(final Skin skin, final Being being){
		super("Inventory", skin);
		final Button exitButton = new TextButton("Exit", skin);
		exitButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				event.getListenerActor().getParent().remove();
			}
		});
		addGunTables(skin, being);
		add(exitButton);
		pack();
		setX(Gdx.graphics.getWidth() - getWidth());
		setY(Gdx.graphics.getHeight()/2 - getHeight()/2);
	}
	
	private void addGunTables(final Skin skin, final Being being){
		final List<Gun> guns = being.getGuns();
		final List<GunTable> gunTables = new ArrayList<GunTable>();
		for(int i=0; i<guns.size(); i++){
			final Gun gun = guns.get(i);
			GunTable gunTable = new GunTable(skin, gun);
			gunTables.add(gunTable);
			add(gunTable);
			/*TODO uncomment, swap guns
			final int current = i;
			if(i>0){
				Button upButton = new TextButton("Up", skin);
				upButton.addListener(new ClickListener() {
					@Override public void clicked(InputEvent event, float x, float y) {
						swap(skin, being, true, current, gun, guns, gunTables);
					}
				});
				add(upButton);
			}
			if(i<guns.size()-1){
				Button downButton = new TextButton("Down", skin);
				downButton.addListener(new ClickListener() {
					@Override public void clicked(InputEvent event, float x, float y) {
						swap(skin, being, false, current, gun, guns, gunTables);
					}
				});
				add(downButton);
			}*/
			row();
			row();
		}
	}
	
	private void swap(final Skin skin, final Being being, boolean up, int current, 
			Gun gun, List<Gun> guns, List<GunTable> gunTables){
		List<Gun> newGuns = new ArrayList<Gun>(guns);
		newGuns.remove(current);
		newGuns.add(current+(up?-1:1), gun);
		being.setGuns(newGuns);
		for(GunTable table : gunTables)
			table.remove();
		addGunTables(skin, being);
	}
}
