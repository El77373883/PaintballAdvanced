package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StatsManager {

    private final AdvancedPaintball plugin;
    private final Map<UUID, PlayerStats> map = new HashMap<>();
    private File file;
    private FileConfiguration cfg;

    public StatsManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "stats.yml");
        if (!file.exists()) { try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (!cfg.contains("players")) return;
        for (String uid : cfg.getConfigurationSection("players").getKeys(false)) {
            UUID uuid = UUID.fromString(uid);
            String p  = "players." + uid + ".";
            PlayerStats s = new PlayerStats(uuid, cfg.getString(p + "name", "Unknown"));
            s.setKills(cfg.getInt(p + "kills"));   s.setDeaths(cfg.getInt(p + "deaths"));
            s.setWins(cfg.getInt(p + "wins"));     s.setGames(cfg.getInt(p + "games"));
            s.setCoins(cfg.getInt(p + "coins"));   s.setKit(cfg.getString(p + "kit", "default"));
            s.setRank(cfg.getString(p + "rank", "Novato"));
            s.setLevel(cfg.getInt(p + "level"));   s.setXp(cfg.getInt(p + "xp"));
            s.setLastMissionReset(cfg.getLong(p + "lastMissionReset", 0));
            s.setMissionKills(cfg.getInt(p + "missionKills"));
            s.setMissionWins(cfg.getInt(p + "missionWins"));
            map.put(uuid, s);
        }
    }

    public void saveAll() {
        for (PlayerStats s : map.values()) {
            String p = "players." + s.getUuid() + ".";
            cfg.set(p+"name", s.getName());       cfg.set(p+"kills", s.getKills());
            cfg.set(p+"deaths", s.getDeaths());   cfg.set(p+"wins", s.getWins());
            cfg.set(p+"games", s.getGames());     cfg.set(p+"coins", s.getCoins());
            cfg.set(p+"kit", s.getKit());         cfg.set(p+"rank", s.getRank());
            cfg.set(p+"level", s.getLevel());     cfg.set(p+"xp", s.getXp());
            cfg.set(p+"lastMissionReset", s.getLastMissionReset());
            cfg.set(p+"missionKills", s.getMissionKills());
            cfg.set(p+"missionWins", s.getMissionWins());
        }
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public PlayerStats get(Player p) {
        return map.computeIfAbsent(p.getUniqueId(), uid -> new PlayerStats(uid, p.getName()));
    }

    public List<PlayerStats> top(int n) {
        List<PlayerStats> list = new ArrayList<>(map.values());
        list.sort((a, b) -> b.getKills() - a.getKills());
        return list.subList(0, Math.min(n, list.size()));
    }
}
