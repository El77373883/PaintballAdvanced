package com.soyadrianyt001.advancedpaintball.npc;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.*;
import org.bukkit.configuration.file.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ShopNPC implements Listener {

    private final AdvancedPaintball plugin;
    private final Set<Integer> shopNPCIds = new HashSet<>();
    private File file;
    private FileConfiguration cfg;
    private boolean citizensEnabled = false;

    public ShopNPC(AdvancedPaintball plugin) {
        this.plugin = plugin;
        citizensEnabled = Bukkit.getPluginManager().isPluginEnabled("Citizens");
        if (citizensEnabled) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            load();
        }
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "npcs.yml");
        if (!file.exists()) { try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (cfg.contains("shop-npcs"))
            cfg.getIntegerList("shop-npcs").forEach(shopNPCIds::add);
    }

    public void create(Player p, String arenaName) {
        if (!citizensEnabled) { p.sendMessage(Msg.err("Citizens no está instalado.")); return; }
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, Msg.c("&b&lTienda &7de Snowballs"));
        npc.spawn(p.getLocation());
        shopNPCIds.add(npc.getId());
        saveNPCs();
        spawnHologram(p.getLocation(), arenaName);
        p.sendMessage(Msg.ok("NPC de tienda creado para arena &f" + arenaName));
    }

    public void removeNearest(Player p) {
        if (!citizensEnabled) { p.sendMessage(Msg.err("Citizens no instalado.")); return; }
        NPCRegistry registry = CitizensAPI.getNPCRegistry();
        NPC nearest = null; double dist = 10;
        for (int id : shopNPCIds) {
            NPC npc = registry.getById(id);
            if (npc != null && npc.isSpawned()) {
                double d = npc.getEntity().getLocation().distance(p.getLocation());
                if (d < dist) { dist = d; nearest = npc; }
            }
        }
        if (nearest == null) { p.sendMessage(Msg.err("No hay NPC cerca.")); return; }
        shopNPCIds.remove(nearest.getId());
        nearest.destroy(); saveNPCs();
        p.sendMessage(Msg.ok("NPC eliminado."));
    }

    @EventHandler
    public void onNPCClick(NPCRightClickEvent e) {
        if (!shopNPCIds.contains(e.getNPC().getId())) return;
        Player p = e.getClicker();
        int price = plugin.getConfig().getInt("snowball-price", 5);
        PlayerStats stats = plugin.getStatsManager().get(p);
        if (stats.getCoins() < price) {
            p.sendMessage(Msg.err("Necesitas &e" + price + " coins &cpara 1 stack de snowballs."));
            return;
        }
        stats.addCoins(-price);
        p.getInventory().addItem(new ItemStack(org.bukkit.Material.SNOWBALL, 64));
        p.sendMessage(Msg.ok("Compraste 64 snowballs por &e" + price + " coins&a! Coins restantes: &e" + stats.getCoins()));
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
    }

    private void spawnHologram(Location loc, String arenaName) {
        Location hLoc = loc.clone().add(0, 2.2, 0);
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            hLoc.getWorld().spawnParticle(Particle.END_ROD, hLoc, 1, 0, 0, 0, 0);
        }, 0L, 10L);
    }

    private void saveNPCs() {
        cfg.set("shop-npcs", new ArrayList<>(shopNPCIds));
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }
}
