package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.models.Game;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager {

    private final AdvancedPaintball plugin;
    private final Map<String, Arena> arenas = new LinkedHashMap<>();
    private File file;
    private FileConfiguration cfg;

    public ArenaManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "arenas.yml");
        if (!file.exists()) { try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (!cfg.contains("arenas")) return;
        for (String name : cfg.getConfigurationSection("arenas").getKeys(false)) {
            Arena a  = new Arena(name);
            String p = "arenas." + name + ".";
            a.setDisplayName(cfg.getString(p + "displayName", "&6" + name));
            a.setMinPlayers(cfg.getInt(p + "minPlayers", 2));
            a.setMaxPlayers(cfg.getInt(p + "maxPlayers", 16));
            a.setGameTime(cfg.getInt(p + "gameTime", 300));
            a.setKillsToWin(cfg.getInt(p + "killsToWin", 30));
            String wn = cfg.getString(p + "world");
            if (wn != null) {
                World w = plugin.getServer().getWorld(wn);
                if (w != null) {
                    if (cfg.contains(p + "lobby"))  a.setLobby(readLoc(p + "lobby", w));
                    if (cfg.contains(p + "pos1"))   a.setPos1(readLoc(p + "pos1", w));
                    if (cfg.contains(p + "pos2"))   a.setPos2(readLoc(p + "pos2", w));
                    readSpawns(a.getSpawnsRed(),    p + "spawnsRed",    w);
                    readSpawns(a.getSpawnsPink(),   p + "spawnsPink",   w);
                    readSpawns(a.getSpawnsGreen(),  p + "spawnsGreen",  w);
                    readSpawns(a.getSpawnsYellow(), p + "spawnsYellow", w);
                }
            }
            arenas.put(name.toLowerCase(), a);
        }
        plugin.getLogger().info("§aCargadas " + arenas.size() + " arena(s).");
    }

    public void save(Arena a) {
        String p = "arenas." + a.getName() + ".";
        cfg.set(p + "displayName", a.getDisplayName());
        cfg.set(p + "minPlayers",  a.getMinPlayers());
        cfg.set(p + "maxPlayers",  a.getMaxPlayers());
        cfg.set(p + "gameTime",    a.getGameTime());
        cfg.set(p + "killsToWin",  a.getKillsToWin());
        if (a.getLobby() != null) {
            cfg.set(p + "world", a.getLobby().getWorld().getName());
            writeLoc(p + "lobby", a.getLobby());
        }
        if (a.getPos1() != null) writeLoc(p + "pos1", a.getPos1());
        if (a.getPos2() != null) writeLoc(p + "pos2", a.getPos2());
        writeSpawns(a.getSpawnsRed(),    p + "spawnsRed");
        writeSpawns(a.getSpawnsPink(),   p + "spawnsPink");
        writeSpawns(a.getSpawnsGreen(),  p + "spawnsGreen");
        writeSpawns(a.getSpawnsYellow(), p + "spawnsYellow");
        commit();
    }

    public void delete(String name) {
        arenas.remove(name.toLowerCase());
        cfg.set("arenas." + name, null);
        commit();
    }

    private void commit() { try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); } }

    public Arena   create(String name) { Arena a = new Arena(name); arenas.put(name.toLowerCase(), a); return a; }
    public Arena   get(String name)    { return arenas.get(name.toLowerCase()); }
    public boolean exists(String name) { return arenas.containsKey(name.toLowerCase()); }
    public Collection<Arena> all()     { return arenas.values(); }

    private void writeLoc(String path, Location l) {
        cfg.set(path + ".world", l.getWorld().getName());
        cfg.set(path + ".x", l.getX()); cfg.set(path + ".y", l.getY()); cfg.set(path + ".z", l.getZ());
        cfg.set(path + ".yaw", (double) l.getYaw()); cfg.set(path + ".pitch", (double) l.getPitch());
    }

    private Location readLoc(String path, World w) {
        return new Location(w, cfg.getDouble(path+".x"), cfg.getDouble(path+".y"),
            cfg.getDouble(path+".z"), (float)cfg.getDouble(path+".yaw"), (float)cfg.getDouble(path+".pitch"));
    }

    private void writeSpawns(List<Location> list, String path) {
        cfg.set(path, null);
        for (int i = 0; i < list.size(); i++) writeLoc(path + "." + i, list.get(i));
    }

    private void readSpawns(List<Location> list, String path, World w) {
        if (!cfg.contains(path)) return;
        for (String key : cfg.getConfigurationSection(path).getKeys(false))
            list.add(readLoc(path + "." + key, w));
    }
}
