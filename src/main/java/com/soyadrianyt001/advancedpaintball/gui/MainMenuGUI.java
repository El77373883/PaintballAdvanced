package com.soyadrianyt001.advancedpaintball.gui;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MainMenuGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE = "§b§lAdvancedPaintball §8▸ Menú";

    // Animación del título
    private final Map<UUID, BukkitTask> animations = new HashMap<>();

    public MainMenuGUI(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        String rank = plugin.getRankManager().getFormattedRank(stats);

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        // Fondo animado
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        // Bordes de color
        ItemStack border = make(Material.CYAN_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 54; i += 9) inv.setItem(i, border);
        for (int i = 8; i < 54; i += 9) inv.setItem(i, border);

        // Perfil del jugador
        inv.setItem(4, make(Material.PLAYER_HEAD,
            Msg.c("&f&l" + p.getName()),
            Arrays.asList(
                Msg.c("&8&m──────────────────"),
                Msg.c("&7Rango: " + rank),
                Msg.c("&7Nivel: &e" + stats.getLevel()),
                Msg.c("&6Coins: &e" + stats.getCoins()),
                Msg.c("&7Kills: &a" + stats.getKills()),
                Msg.c("&7Victorias: &b" + stats.getWins()),
                Msg.c("&7KDR: &f" + stats.kdr()),
                Msg.c("&8&m──────────────────")
            )));

        // Jugar
        inv.setItem(20, make(Material.LIME_CONCRETE,
            Msg.c("&a&l▶ JUGAR"),
            Arrays.asList(
                Msg.c("&7Únete a una partida"),
                Msg.c("&7de AdvancedPaintball"),
                Msg.c("&8&m──────────────────"),
                Msg.c("&eClick para ver arenas")
            )));

        // Tienda
        inv.setItem(22, make(Material.CHEST,
            Msg.c("&6&l☆ TIENDA"),
            Arrays.asList(
                Msg.c("&7Compra kits y mejoras"),
                Msg.c("&6Coins: &e" + stats.getCoins()),
                Msg.c("&8&m──────────────────"),
                Msg.c("&eClick para abrir")
            )));

        // Stats
        inv.setItem(24, make(Material.BOOK,
            Msg.c("&b&l📊 ESTADÍSTICAS"),
            Arrays.asList(
                Msg.c("&8&m──────────────────"),
                Msg.c("&7Kills:     &a" + stats.getKills()),
                Msg.c("&7Muertes:   &c" + stats.getDeaths()),
                Msg.c("&7KDR:       &f" + stats.kdr()),
                Msg.c("&7Victorias: &b" + stats.getWins()),
                Msg.c("&7Partidas:  &f" + stats.getGames()),
                Msg.c("&8&m──────────────────")
            )));

        // Top 10
        inv.setItem(29, make(Material.GOLDEN_SWORD,
            Msg.c("&6&l🏆 TOP 10"),
            Arrays.asList(
                Msg.c("&7Ver el ranking"),
                Msg.c("&7de mejores jugadores"),
                Msg.c("&eClick para ver")
            )));

        // Misiones
        inv.setItem(31, make(Material.WRITABLE_BOOK,
            Msg.c("&d&l📋 MISIONES DIARIAS"),
            Arrays.asList(
                Msg.c("&8&m──────────────────"),
                Msg.c(plugin.getMissionManager().getMissionStatus(p)),
                Msg.c("&8&m──────────────────"),
                Msg.c("&eClick para ver misiones")
            )));

        // Créditos
        inv.setItem(33, make(Material.NETHER_STAR,
            Msg.c("&e&l⭐ CRÉDITOS"),
            Arrays.asList(
                Msg.c("&8&m──────────────────"),
                Msg.c("&7Plugin: &bAdvancedPaintball"),
                Msg.c("&7Versión: &f1.0.0"),
                Msg.c("&7Autor: &eSoyAdrianYT001"),
                Msg.c("&7Server: &f1.21.1"),
                Msg.c("&8&m──────────────────")
            )));

        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);

        // Animación del menú
        startAnimation(p, inv);
    }

    private void startAnimation(Player p, Inventory inv) {
        // Cancelar animación anterior si existe
        if (animations.containsKey(p.getUniqueId())) {
            animations.get(p.getUniqueId()).cancel();
        }

        final Material[] borderMats = {
            Material.CYAN_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.WHITE_STAINED_GLASS_PANE,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE
        };
        final int[] frame = {0};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (p.getOpenInventory() == null ||
                !p.getOpenInventory().getTitle().equals(TITLE)) {
                animations.remove(p.getUniqueId());
                return;
            }
            Material mat = borderMats[frame[0] % borderMats.length];
            ItemStack border = make(mat, " ", null);
            for (int i = 0; i < 9; i++) inv.setItem(i, border);
            for (int i = 45; i < 54; i++) inv.setItem(i, border);
            for (int i = 0; i < 54; i += 9) inv.setItem(i, border);
            for (int i = 8; i < 54; i += 9) inv.setItem(i, border);
            frame[0]++;
        }, 0L, 10L);

        animations.put(p.getUniqueId(), task);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        switch (e.getSlot()) {
            case 20 -> { // Jugar
                p.closeInventory();
                cancelAnimation(p);
                plugin.getArenaSelectorGUI().open(p);
            }
            case 22 -> { // Tienda
                if (!plugin.getGameManager().inGame(p)) {
                    p.sendMessage(Msg.err("Debes estar en una arena para usar la tienda."));
                    return;
                }
                p.closeInventory();
                cancelAnimation(p);
                plugin.getShopGUI().open(p);
            }
            case 24 -> { // Stats
                p.closeInventory();
                cancelAnimation(p);
                showStats(p);
            }
            case 29 -> { // Top
                p.closeInventory();
                cancelAnimation(p);
                showTop(p);
            }
            case 33 -> { // Créditos
                p.closeInventory();
                cancelAnimation(p);
                showCredits(p);
            }
        }
    }

    private void showStats(Player p) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        String rank = plugin.getRankManager().getFormattedRank(stats);
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lEstadísticas de &f" + p.getName()));
        p.sendMessage(Msg.c("  &7Rango:     " + rank));
        p.sendMessage(Msg.c("  &7Nivel:     &e" + stats.getLevel()));
        p.sendMessage(Msg.c("  &6Coins:     &e" + stats.getCoins()));
        p.sendMessage(Msg.c("  &7Kills:     &a" + stats.getKills()));
        p.sendMessage(Msg.c("  &7Muertes:   &c" + stats.getDeaths()));
        p.sendMessage(Msg.c("  &7KDR:       &f" + stats.kdr()));
        p.sendMessage(Msg.c("  &7Victorias: &b" + stats.getWins()));
        p.sendMessage(Msg.c("  &7Partidas:  &f" + stats.getGames()));
        p.sendMessage(Msg.c("  &7Kit:       &e" + stats.getKit()));
        p.sendMessage(Msg.c("  &7Misiones:  " + plugin.getMissionManager().getMissionStatus(p)));
        p.sendMessage(Msg.sep());
    }

    private void showTop(Player p) {
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &6&lTOP 10 — AdvancedPaintball"));
        List<com.soyadrianyt001.advancedpaintball.models.PlayerStats> top =
            plugin.getStatsManager().top(10);
        for (int i = 0; i < top.size(); i++) {
            var s = top.get(i);
            String medal = switch (i) {
                case 0 -> "&6&l#1 🥇";
                case 1 -> "&7&l#2 🥈";
                case 2 -> "&c&l#3 🥉";
                default -> "&f#" + (i + 1);
            };
            p.sendMessage(Msg.c("  " + medal + " &e" + s.getName()
                + " &8│ &aKills: " + s.getKills()
                + " &8│ &7KDR: " + s.kdr()
                + " &8│ &bVictorias: " + s.getWins()));
        }
        p.sendMessage(Msg.sep());
    }

    private void showCredits(Player p) {
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lAdvancedPaintball &7v1.0.0"));
        p.sendMessage(Msg.c("  &7Creado por: &e&lSoyAdrianYT001"));
        p.sendMessage(Msg.c("  &7Minecraft: &f1.21.1 Paper/Spigot"));
        p.sendMessage(Msg.c("  &7Tipo: &aPremium &7✔"));
        p.sendMessage(Msg.c("  &74 equipos | 4 kits | NPC tienda"));
        p.sendMessage(Msg.c("  &7Rangos | Misiones | MySQL"));
        p.sendMessage(Msg.sep());
    }

    public void cancelAnimation(Player p) {
        if (animations.containsKey(p.getUniqueId())) {
            animations.get(p.getUniqueId()).cancel();
            animations.remove(p.getUniqueId());
        }
    }

    private ItemStack make(Material m, String name, List<String> lore) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
