package com.soyadrianyt001.advancedpaintball.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;

public class Game {

    public enum Team  { RED, PINK, GREEN, YELLOW, SPECTATOR }
    public enum State { WAITING, STARTING, IN_GAME, ENDING }

    private final Arena arena;
    private State state = State.WAITING;
    private final Map<UUID, Team>    players = new LinkedHashMap<>();
    private final Map<UUID, Integer> kills   = new HashMap<>();
    private final Map<UUID, Integer> deaths  = new HashMap<>();
    private final Map<UUID, Integer> streak  = new HashMap<>();
    private int scoreRed, scorePink, scoreGreen, scoreYellow;
    private int countdown = 10, timeLeft, taskId = -1;
    private final Set<UUID> safeSpawn = new HashSet<>();
    private String votedMap = null;

    public Game(Arena arena) {
        this.arena = arena;
        this.timeLeft = arena.getGameTime();
    }

    public void addPlayer(Player p, Team t) {
        players.put(p.getUniqueId(), t);
        kills.put(p.getUniqueId(), 0);
        deaths.put(p.getUniqueId(), 0);
        streak.put(p.getUniqueId(), 0);
    }

    public void addSpectator(Player p) { players.put(p.getUniqueId(), Team.SPECTATOR); }
    public void removePlayer(Player p) {
        players.remove(p.getUniqueId());
        kills.remove(p.getUniqueId());
        deaths.remove(p.getUniqueId());
        streak.remove(p.getUniqueId());
    }

    public boolean isSpectator(Player p) {
        return players.getOrDefault(p.getUniqueId(), Team.SPECTATOR) == Team.SPECTATOR;
    }

    public Team getTeam(Player p) {
        return players.getOrDefault(p.getUniqueId(), Team.SPECTATOR);
    }

    public int getKills(Player p)  { return kills.getOrDefault(p.getUniqueId(), 0); }
    public int getDeaths(Player p) { return deaths.getOrDefault(p.getUniqueId(), 0); }
    public int getStreak(Player p) { return streak.getOrDefault(p.getUniqueId(), 0); }

    public void addKill(Player p) {
        kills.merge(p.getUniqueId(), 1, Integer::sum);
        streak.merge(p.getUniqueId(), 1, Integer::sum);
    }

    public void resetStreak(Player p) { streak.put(p.getUniqueId(), 0); }
    public void addDeath(Player p) { deaths.merge(p.getUniqueId(), 1, Integer::sum); }

    public List<UUID> getTeamPlayers(Team t) {
        List<UUID> list = new ArrayList<>();
        players.forEach((u, team) -> { if (team == t) list.add(u); });
        return list;
    }

    public int totalActive() {
        return (int) players.values().stream().filter(t -> t != Team.SPECTATOR).count();
    }

    public UUID getMVP() {
        return kills.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(null);
    }

    public List<Player> getOnlinePlayers() {
        List<Player> list = new ArrayList<>();
        players.keySet().forEach(uid -> {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) list.add(pl);
        });
        return list;
    }

    public void addSafe(Player p)    { safeSpawn.add(p.getUniqueId()); }
    public void removeSafe(Player p) { safeSpawn.remove(p.getUniqueId()); }
    public boolean isSafe(Player p)  { return safeSpawn.contains(p.getUniqueId()); }

    public int getTeamScore(Team t) {
        return switch (t) {
            case RED    -> scoreRed;
            case PINK   -> scorePink;
            case GREEN  -> scoreGreen;
            case YELLOW -> scoreYellow;
            default     -> 0;
        };
    }

    public void addTeamScore(Team t) {
        switch (t) {
            case RED    -> scoreRed++;
            case PINK   -> scorePink++;
            case GREEN  -> scoreGreen++;
            case YELLOW -> scoreYellow++;
        }
    }

    public Team getWinnerByScore() {
        Map<Team, Integer> scores = new HashMap<>();
        scores.put(Team.RED,    scoreRed);
        scores.put(Team.PINK,   scorePink);
        scores.put(Team.GREEN,  scoreGreen);
        scores.put(Team.YELLOW, scoreYellow);
        return scores.entrySet().stream()
            .filter(e -> !e.getKey().equals(Team.SPECTATOR))
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse(null);
    }

    public Arena getArena()    { return arena; }
    public State getState()    { return state; }
    public void setState(State v) { state = v; }
    public Map<UUID, Team> getPlayers() { return players; }
    public int getCountdown()  { return countdown; }
    public void setCountdown(int v) { countdown = v; }
    public int getTimeLeft()   { return timeLeft; }
    public void setTimeLeft(int v) { timeLeft = v; }
    public int getTaskId()     { return taskId; }
    public void setTaskId(int v)   { taskId = v; }
    public String getVotedMap() { return votedMap; }
    public void setVotedMap(String v) { votedMap = v; }
    public int getScoreRed()    { return scoreRed; }
    public int getScorePink()   { return scorePink; }
    public int getScoreGreen()  { return scoreGreen; }
    public int getScoreYellow() { return scoreYellow; }
}
