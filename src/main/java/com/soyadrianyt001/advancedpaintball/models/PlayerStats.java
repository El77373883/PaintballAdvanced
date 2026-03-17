package com.soyadrianyt001.advancedpaintball.models;

import java.util.UUID;

public class PlayerStats {

    private final UUID uuid;
    private String name, kit = "default", rank = "Novato";
    private int kills, deaths, wins, games, coins, level, xp;
    private long lastMissionReset = 0;
    private int missionKills = 0, missionWins = 0;

    public PlayerStats(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public double kdr() {
        if (deaths == 0) return kills;
        return Math.round((double) kills / deaths * 100.0) / 100.0;
    }

    public UUID   getUuid()    { return uuid; }
    public String getName()    { return name; }
    public void   setName(String v) { name = v; }
    public String getKit()     { return kit; }
    public void   setKit(String v) { kit = v; }
    public String getRank()    { return rank; }
    public void   setRank(String v) { rank = v; }
    public int    getKills()   { return kills; }
    public void   setKills(int v) { kills = v; }
    public void   addKill()    { kills++; }
    public int    getDeaths()  { return deaths; }
    public void   setDeaths(int v) { deaths = v; }
    public void   addDeath()   { deaths++; }
    public int    getWins()    { return wins; }
    public void   setWins(int v)  { wins = v; }
    public void   addWin()     { wins++; }
    public int    getGames()   { return games; }
    public void   setGames(int v) { games = v; }
    public void   addGame()    { games++; }
    public int    getCoins()   { return coins; }
    public void   setCoins(int v) { coins = v; }
    public void   addCoins(int v) { coins += v; }
    public int    getLevel()   { return level; }
    public void   setLevel(int v) { level = v; }
    public int    getXp()      { return xp; }
    public void   setXp(int v)    { xp = v; }
    public void   addXp(int v)    { xp += v; }
    public long   getLastMissionReset() { return lastMissionReset; }
    public void   setLastMissionReset(long v) { lastMissionReset = v; }
    public int    getMissionKills() { return missionKills; }
    public void   setMissionKills(int v) { missionKills = v; }
    public void   addMissionKill() { missionKills++; }
    public int    getMissionWins() { return missionWins; }
    public void   setMissionWins(int v) { missionWins = v; }
    public void   addMissionWin() { missionWins++; }
}
