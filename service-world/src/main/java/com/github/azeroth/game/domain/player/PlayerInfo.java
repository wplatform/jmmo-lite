package com.github.azeroth.game.domain.player;



import com.github.azeroth.dbc.defines.ItemContext;
import com.github.azeroth.dbc.domain.SkillRaceClassInfo;
import com.github.azeroth.defines.UnitClass;
import com.github.azeroth.defines.Race;

import java.util.*;


public class PlayerInfo {
	public Race race;
	public UnitClass playClass;
	public int mapId;
	public float x, y, z, o;
	public int displayIdForMale;
	public int displayIdFemale;
	public Integer introMovieId;

	public ItemContext itemContext;
	public ArrayList<PlayerCreateInfoItem> itemsForMale = new ArrayList<>();

	public ArrayList<PlayerCreateInfoItem> items = new ArrayList<>();
	public ArrayList<PlayerCreateInfoItem> itemsForFemale = new ArrayList<>();
	public HashSet<Integer> customSpells = new HashSet<>();
	public ArrayList<Integer> castSpells = new ArrayList<>();
	public ArrayList<PlayerCreateInfoAction> actions = new ArrayList<>();
	public ArrayList<SkillRaceClassInfo> skills = new ArrayList<>();
	public PlayerLevelInfo[] levelInfo;

}
