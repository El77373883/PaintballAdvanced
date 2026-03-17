package com.soyadrianyt001.advancedpaintball.gui;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AdminPanelGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE_MAIN  = "§c§lAP §7Admin Panel";
    private static final String TITLE_ARENA = "§c§lAP §7Editar: ";
    private final Map<UUID, String> editing = new HashMap<>();

    public AdminPanelGUI(AdvancedPaintball plugin) { this.plugin = plugin; }

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_MAIN);
        ItemStack bg = make(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);
        int slot = 0;
        for (Arena a : plugin.getArenaManager().all()) {
            if (slot >= 45) break;
            boolean ready = a.isReady();
            inv.setItem(slot++, make(ready ? Material.CYAN_CONCRETE : Material.ORANGE_CONCRETE,
                Msg.c(a.getDisplayName()),
                Arrays.asList(Msg.c(ready ? "&aLista ✔" : "&cIncompleta ✗"),
                    Msg.c("&7Spawns R/Ro/V/A: &f" + a.getSpawnsRed().size() + "/" + a.getSpawnsPink().size() + "/" + a.getSpawnsGreen().size() + "/" + a.getSpawnsYellow().size()),
                    Msg.c("&eClick para editar"))));
        }
        inv.setItem(45, make(Material.LIME_WOOL, Msg.c("&a&l+ Nueva Arena"),
            Arrays.asList(Msg.c("&7Usa /pa-admin crear <nombre>"))));
        inv.setItem(53, make(Material.BARRIER, Msg.c("&c✗ Cerrar"), null));
        p.openInventory(inv);
    }

    public void openArena(Player p, Arena a) {
        editing.put(p.getUniqueId(), a.getName());
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_ARENA + a.getName());
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        inv.setItem(10, make(a.getLobby() != null ? Material.GREEN_CONCRETE : Material.RED_CONCRETE,
            Msg.c("&b Lobby"), Arrays.asList(Msg.c(a.getLobby() != null ? "&a✔ Configurado" : "&c✗ Sin configurar"), Msg.c("&7Click = tu posición"))));
        inv.setItem(11, make(Material.RED_WOOL,    Msg.c("&c+ Spawn Rojo"),    spawnLore(a.getSpawnsRed().size())));
        inv.setItem(12, make(Material.PINK_WOOL,   Msg.c("&d+ Spawn Rosa"),    spawnLore(a.getSpawnsPink().size())));
        inv.setItem(13, make(Material.GREEN_WOOL,  Msg.c("&a+ Spawn Verde"),   spawnLore(a.getSpawnsGreen().size())));
        inv.setItem(14, make(Material.YELLOW_WOOL, Msg.c("&e+ Spawn Amarillo"),spawnLore(a.getSpawnsYellow().size())));
        inv.setItem(16, make(Material.GOLDEN_SWORD, Msg.c("&6 Wand Pos1/Pos2"),
            Arrays.asList(Msg.c("&7Click para recibir la varita"),
                Msg.c(a.getPos1() != null ? "&aPos1 ✔" : "&cPos1 ✗"),
                Msg.c(a.getPos2() != null ? "&aPos2 ✔" : "&cPos2 ✗"))));
        inv.setItem(28, make(Material.CLOCK, Msg.c("&e Tiempo"),
            Arrays.asList(Msg.c("&7Actual: &f" + a.getGameTime() + "s"), Msg.c("&7Click=5min | Shift=10min"))));
        inv.setItem(30, make(Material.DIAMOND_SWORD, Msg.c("&a Kills para ganar"),
            Arrays.asList(Msg.c("&7Actual: &f" + a.getKillsToWin()), Msg.c("&7Click +5 | Shift -5"))));
        inv.setItem(32, make(Material.PLAYER_HEAD, Msg.c("&d Jugadores"),
            Arrays.asList(Msg.c("&7Min: &f" + a.getMinPlayers() + "  Max: &f" + a.getMaxPlayers()), Msg.c("&7Click +min | Shift -min"))));
        inv.setItem(34, make(a.isReady() ? Material.EMERALD : Material.REDSTONE,
            Msg.c(a.isReady() ? "&a✔ Arena Lista!" : "&c✗ Incompleta"),
            Arrays.asList(Msg.c("&7Lobby: " + (a.getLobby()!=null?"&a✔":"&c✗")),
                Msg.c("&7SpawnsR: " + a.getSpawnsRed().size() + " SpawnsPink: " + a.getSpawnsPink().size()))));
        inv.setItem(45, make(Material.ARROW, Msg.c("&7◀ Volver"), null));
        inv.setItem(49, make(Material.TNT, Msg.c("&c Eliminar Arena"),
            Arrays.asList(Msg.c("&cShift+Click para eliminar"))));
        inv.setItem(53, make(Material.BARRIER, Msg.c("&c✗ Cerrar"), null));
        p.openInventory(inv);
    }

    private List<String> spawnLore(int count) {
        return Arrays.asList(Msg.c("&7Spawns: &f" + count), Msg.c("&7Click=añadir | Shift=limpiar"));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();
        if (title.equals(TITLE_MAIN)) { mainClick(e, p); return; }
        if (title.startsWith(TITLE_ARENA)) { arenaClick(e, p); }
    }

    private void mainClick(InventoryClickEvent e, Player p) {
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();
        if (name.isBlank()) return;
        if (item.getType() == Material.BARRIER) { p.closeInventory(); return; }
        if (item.getType() == Material.LIME_WOOL) { p.sendMessage(Msg.admin("Usa &f/pa-admin crear <nombre>")); p.closeInventory(); return; }
        for (Arena a : plugin.getArenaManager().all()) {
            if (Msg.c(a.getDisplayName()).equals(name)) { openArena(p, a); return; }
        }
    }

    private void arenaClick(InventoryClickEvent e, Player p) {
        e.setCancelled(true);
        String arenaName = editing.get(p.getUniqueId());
        if (arenaName == null) return;
        Arena a = plugin.getArenaManager().get(arenaName);
        if (a == null) return;
        boolean shift = e.isShiftClick();
        switch (e.getSlot()) {
            case 10 -> { a.setLobby(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Lobby guardado.")); openArena(p, a); }
            case 11 -> { if (shift) a.getSpawnsRed().clear(); else a.getSpawnsRed().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn Rojo " + (shift?"limpiado":"añadido #"+a.getSpawnsRed().size()))); openArena(p, a); }
            case 12 -> { if (shift) a.getSpawnsPink().clear(); else a.getSpawnsPink().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn Rosa " + (shift?"limpiado":"añadido #"+a.getSpawnsPink().size()))); openArena(p, a); }
            case 13 -> { if (shift) a.getSpawnsGreen().clear(); else a.getSpawnsGreen().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn Verde " + (shift?"limpiado":"añadido #"+a.getSpawnsGreen().size()))); openArena(p, a); }
            case 14 -> { if (shift) a.getSpawnsYellow().clear(); else a.getSpawnsYellow().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn Amarillo " + (shift?"limpiado":"añadido #"+a.getSpawnsYellow().size()))); openArena(p, a); }
            case 16 -> { p.closeInventory(); plugin.getWandManager().give(p, arenaName); }
            case 28 -> { a.setGameTime(shift ? 600 : 300); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Tiempo: " + a.getGameTime() + "s")); openArena(p, a); }
            case 30 -> { a.setKillsToWin(Math.max(1, a.getKillsToWin() + (shift ? -5 : 5))); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Kills to win: " + a.getKillsToWin())); openArena(p, a); }
            case 32 -> { a.setMinPlayers(Math.max(1, a.getMinPlayers() + (shift ? -1 : 1))); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Min jugadores: " + a.getMinPlayers())); openArena(p, a); }
            case 45 -> openMain(p);
            case 49 -> { if (shift) { plugin.getArenaManager().delete(arenaName); editing.remove(p.getUniqueId()); p.sendMessage(Msg.ok("Arena eliminada.")); openMain(p); } else p.sendMessage(Msg.info("Shift+Click para confirmar.")); }
            case 53 -> p.closeInventory();
        }
    }

    private ItemStack make(Material m, String name, List<String> lore) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
