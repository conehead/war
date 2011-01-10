package com.tommytony.war;

import java.util.List;

import org.bukkit.Block;
import org.bukkit.BlockDamageLevel;
import org.bukkit.Player;
import org.bukkit.event.block.BlockDamagedEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlacedEvent;


public class WarBlockListener extends BlockListener {

	private War war;

	public WarBlockListener(War war) {
		this.war = war;
	}
	
	public void onBlockPlaced(BlockPlacedEvent event) {
		Player player = event.getPlayer();
    	Block block = event.getBlock();
    	if(player != null) {
			Team team = war.getPlayerTeam(player.getName()); 
			Warzone zone = war.getPlayerWarzone(player.getName());
			if(team != null && block != null && zone != null 
					&& zone.isMonumentCenterBlock(block)
					&& block.getType() == team.getMaterial()) {
				Monument monument = zone.getMonumentFromCenterBlock(block);
				if(!monument.hasOwner()) {
					monument.capture(team);
					List<Team> teams = zone.getTeams();
					for(Team t : teams) {
						t.teamcast(war.str("Monument " + monument.getName() + " has been captured by team " + team.getName() + "."));
					}
				}
			} else {
				player.sendMessage(war.str("You can't capture a monument without team block. Get one from your team spawn."));
				event.setCancelled(true);
			}
		}
    }
	
    public void onBlockDamaged(BlockDamagedEvent event) {
    	Player player = event.getPlayer();
    	Block block = event.getBlock();
    	if(player != null && block != null && event.getDamageLevel() == BlockDamageLevel.BROKEN) {
	    	Warzone warzone = war.warzone(player.getLocation());
	    	Team team = war.getPlayerTeam(player.getName());
	    	
	    	if(warzone != null && war.getPlayerTeam(player.getName()) == null) {
	    		// can't actually destroy blocks in a warzone if not part of a team
	    		player.sendMessage(war.str("Can't destroy part of a warzone if you're not in a team."));
	    		event.setCancelled(true);
	    	}
	    	
	    	if(warzone != null && warzone.isImportantBlock(block)) {
	    		if(team != null && team.getVolume().contains(block)) {
	    			if(player.getInventory().contains(team.getMaterial())) {
	    				player.sendMessage(war.str("You already have a " + team.getName() + " block."));
	    			}
	    			// let team members loot one block the spawn for monument captures
	    		} else {
		    		player.sendMessage(war.str("Can't destroy this."));
		    		event.setCancelled(true);
	    		}
	    	}
	    	
	    	if(team != null && block != null && warzone != null 
					&& warzone.isMonumentCenterBlock(block)
					){
	    		Monument monument = warzone.getMonumentFromCenterBlock(block);
	    		if(monument.hasOwner()) {
					monument.uncapture();
					List<Team> teams = warzone.getTeams();
					for(Team t : teams) {
						t.teamcast(war.str("Team " + team.getName() + " loses control of monument " + monument.getName()));
					}
				}
	    	}
    	}
    }
}
