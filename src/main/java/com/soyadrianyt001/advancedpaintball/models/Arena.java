package com.soyadrianyt001.advancedpaintball.models;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class Arena {

    public enum State { WAITING, STARTING, IN_GAME, ENDING, DISABLED }

    private String name, displayName;
    private Location lobby, pos1, pos2;
    private final List<Location> spawnsRed    = new ArrayList<>();
    private final List<Location> spawnsPink   = new ArrayList<>();
    private final List<Location> spawnsGreen  = new ArrayList<>();
    private final List<Location> spawnsYellow = new ArrayList<>();
    private int minPlayers = 2, maxPlayers = 16, gameTime = 300, killsToWin = 30;
    private State state = State.WAITING;
    private boolean visibleInMenu = false; // El admin la activa manualmente

    public Arena(String name) {
        this.name        = name;
        this.displayName = "&6" + name;
    }

    public boolean isReady() {
        return lobby != null && !spawnsRed.isEmpty() && !spawnsPink.isEmpty();
    }

    public List<Location> getSpawnsByTeam(Game.Team team) {
        return switch (team) {
            case RED    -> spawnsRed;
            case PINK   -> spawnsPink;
            case GREEN  -> spawnsGreen;
            case YELLOW -> spawnsYellow;
            default     -> spawnsRed;
        };
    }

    public String getName()             { return name; }
    public void   setName(String v)     { name = v; }
    public String getDisplayName()      { return displayName; }
    public void   setDisplayName(String v) { displayName = v; }
    public Location getLobby()          { return lobby; }
    public void   setLobby(Location v)  { lobby = v; }
    public Location getPos1()           { return pos1; }
    public void   setPos1(Location v)   { pos1 = v; }
    public Location getPos2()           { return pos2; }
    public void   setPos2(Location v)   { pos2 = v; }
    public List<Location> getSpawnsRed()    { return spawnsRed; }
    public List<Location> getSpawnsPink()   { return spawnsPink; }
    public List<Location> getSpawnsGreen()  { return spawnsGreen; }
    public List<Location> getSpawnsYellow() { return spawnsYellow; }
    public int  getMinPlayers()         { return minPlayers; }
    public void setMinPlayers(int v)    { minPlayers = v; }
    public int  getMaxPlayers()         { return maxPlayers; }
    public void setMaxPlayers(int v)    { maxPlayers = v; }
    public int  getGameTime()           { return gameTime; }
    public void setGameTime(int v)      { gameTime = v; }
    public int  getKillsToWin()         { return killsToWin; }
    public void setKillsToWin(int v)    { killsToWin = v; }
    public State getState()             { return state; }
    public void  setState(State v)      { state = v; }
    public boolean isVisibleInMenu()    { return visibleInMenu; }
    public void setVisibleInMenu(boolean v) { visibleInMenu = v; }
}
