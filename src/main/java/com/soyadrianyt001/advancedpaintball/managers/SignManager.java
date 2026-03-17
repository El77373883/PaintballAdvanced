package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SignManager {

    private final AdvancedPaintball plugin;
    private File file;
    private FileConfiguration cfg;

    // Location -> arena name
    private final Map<Location, String> signs = new HashMap<>();

    public SignManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        load();
        // Actualizar signs cada 2 segundos
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 40L, 40L);
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "signs.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (!cfg.contains("signs")) return;

        for (String key : cfg.getConfigurationSection("signs").getKeys(false)) {
            String p    = "signs." + key + ".";
            String wn   = cfg.getString(p + "world");
            World w     = Bukkit.getWorld(wn != null ? wn : "world");
            if (w == null) continue;
            Location loc = new Location(w,
                cfg.getDouble(p + "x"),
                cfg.getDouble(p + "y"),
                cfg.getDouble(p + "z"));
            String arena = cfg.getString(p + "arena");
            signs.put(loc, arena);
        }
    }

    private void saveAll() {
        cfg.set("signs", null);
        int i = 0;
        for (Map.Entry<Location, String> e : signs.entrySet()) {
            String p = "signs." + i + ".";
            cfg.set(p + "world",  e.getKey().getWorld().getName());
            cfg.set(p + "x",      e.getKey().getX());
            cfg.set(p + "y",      e.getKey().getY());
            cfg.set(p + "z",      e.getKey().getZ());
            cfg.set(p + "arena",  e.getValue());
            i++;
        }
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void addSign(Location loc, String arenaName) {
        signs.put(loc, arenaName);
        saveAll();
        updateSign(loc, arenaName);
    }

    public void removeSign(Location loc) {
        signs.remove(loc);
        saveAll();
    }

    public boolean isSign(Location loc) { return signs.containsKey(loc); }
    public String getArena(Location loc) { return signs.get(loc); }

    public void updateAll() {
        signs.forEach(this::updateSign);
    }

    private void updateSign(Location loc, String arenaName) {
        Block block = loc.getBlock();
        if (!(block.getState() instanceof Sign sign)) return;

        Arena arena = plugin.getArenaManager().get(arenaName);
        Game game   = plugin.getGameManager().getGameByArena(arenaName);

        int current = game != null ? game.totalActive() : 0;
        String status = game == null ? "§aEsperando"
            : game.getState() == Game.State.IN_GAME ? "§cEn juego"
            : game.getState() == Game.State.STARTING ? "§eContando..."
            : "§aEsperando";

        String maxP = arena != null ? String.valueOf(arena.getMaxPlayers()) : "?";
        String kills = arena != null ? String.valueOf(arena.getKillsToWin()) : "?";

        try {
            var side = sign.getSide(Side.FRONT);
            side.line(0, net.kyori.adventure.text.Component.text(
                "§b§lAdvancedPaintball"));
            side.line(1, net.kyori.adventure.text.Component.text(
                "§f" + arenaName));
            side.line(2, net.kyori.adventure.text.Component.text(
                status + " §7(" + current + "/" + maxP + ")"));
            side.line(3, net.kyori.adventure.text.Component.text(
                "§eKills: " + kills));
            sign.update();
        } catch (Exception ignored) {}
    }
}
