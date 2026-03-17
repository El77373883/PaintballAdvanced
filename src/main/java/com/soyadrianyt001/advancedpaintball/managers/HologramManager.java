package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HologramManager {

    private final AdvancedPaintball plugin;
    private File file;
    private FileConfiguration cfg;

    // Lista de ArmorStands del hologram del top 10
    private final List<ArmorStand> topStands = new ArrayList<>();
    private Location topLocation = null;

    public HologramManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        load();
        // Actualizar hologramas cada 60 segundos
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateTop, 200L, 1200L);
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "holograms.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);

        if (cfg.contains("top-location")) {
            String wn = cfg.getString("top-location.world");
            World w   = Bukkit.getWorld(wn != null ? wn : "world");
            if (w != null) {
                topLocation = new Location(w,
                    cfg.getDouble("top-location.x"),
                    cfg.getDouble("top-location.y"),
                    cfg.getDouble("top-location.z"));
                // Recrear hologramas al cargar
                Bukkit.getScheduler().runTaskLater(plugin, this::updateTop, 40L);
            }
        }
    }

    public void setTopLocation(Location loc) {
        topLocation = loc;
        cfg.set("top-location.world", loc.getWorld().getName());
        cfg.set("top-location.x", loc.getX());
        cfg.set("top-location.y", loc.getY());
        cfg.set("top-location.z", loc.getZ());
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
        updateTop();
    }

    public void updateTop() {
        if (topLocation == null) return;

        // Eliminar ArmorStands anteriores
        topStands.forEach(ArmorStand::remove);
        topStands.clear();

        List<PlayerStats> top = plugin.getStatsManager().top(10);

        // Título del hologram
        double yOffset = top.size() * 0.3 + 1.5;

        spawnLine(topLocation.clone().add(0, yOffset, 0),
            Msg.c("&6&l✦ TOP 10 PAINTBALL ✦"));
        yOffset -= 0.3;
        spawnLine(topLocation.clone().add(0, yOffset, 0),
            Msg.c("&8&m──────────────────"));
        yOffset -= 0.3;

        for (int i = 0; i < top.size(); i++) {
            PlayerStats s = top.get(i);
            String medal = switch (i) {
                case 0 -> "&6#1 🥇";
                case 1 -> "&7#2 🥈";
                case 2 -> "&c#3 🥉";
                default -> "&f#" + (i + 1);
            };
            String rank = plugin.getRankManager().getFormattedRank(s);
            spawnLine(topLocation.clone().add(0, yOffset, 0),
                Msg.c(medal + " " + rank + " &f" + s.getName()
                    + " &8│ &aKills: &f" + s.getKills()
                    + " &8│ &7KDR: &f" + s.kdr()));
            yOffset -= 0.3;
        }

        spawnLine(topLocation.clone().add(0, yOffset, 0),
            Msg.c("&8&m──────────────────"));
        yOffset -= 0.3;
        spawnLine(topLocation.clone().add(0, yOffset, 0),
            Msg.c("&b&lAdvancedPaintball &7by &eSoyAdrianYT001"));
    }

    private void spawnLine(Location loc, String text) {
        ArmorStand stand = (ArmorStand) loc.getWorld()
            .spawnEntity(loc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(text);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        topStands.add(stand);
    }

    public void removeTop() {
        topStands.forEach(ArmorStand::remove);
        topStands.clear();
        topLocation = null;
        cfg.set("top-location", null);
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public boolean hasTopLocation() { return topLocation != null; }
}
