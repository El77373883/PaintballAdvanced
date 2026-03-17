package com.soyadrianyt001.advancedpaintball.gui;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaMenuGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE = "§b§lAdvancedPaintball §8▸ Arenas";

    public ArenaMenuGUI(AdvancedPaintball plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Collection<Arena> arenas = plugin.getArenaManager().all();
        int size = Math.max(9, (int)(Math.ceil(arenas.size() / 9.0)) * 9);
        if (size > 54) size = 54;
        Inventory inv = Bukkit.createInventory(null, size, TITLE);
        int slot = 0;
        for (Arena a : arenas) {
            if (slot >= size - 9) break;
            inv.setItem(slot++, arenaItem(a));
        }
        // Fill
        ItemStack glass = make(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < size; i++) if (inv.getItem(i) == null) inv.setItem(i, glass);
        // Kit shop
        inv.setItem(size - 1, make(Material.CHEST, Msg.c("&6&l☆ Tienda de Kits"),
            Arrays.asList(Msg.c("&7Compra y cambia tu kit"), Msg.c("&eClick para abrir"))));
        p.openInventory(inv);
        p.playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1f);
    }

    private ItemStack arenaItem(Arena a) {
        Game game = plugin.getGameManager().getGameByArena(a.getName());
        boolean ready  = a.isReady();
        boolean active = game != null && game.getState() == Game.State.IN_GAME;
        int current    = game != null ? game.totalActive() : 0;

        Material mat = !ready ? Material.GRAY_WOOL : active ? Material.RED_WOOL : Material.GREEN_WOOL;
        String status = !ready ? "&7Sin configurar" : active ? "&cEn juego" : "&aEsperando";

        List<String> lore = new ArrayList<>();
        lore.add(Msg.c("&8&m──────────────────"));
        lore.add(Msg.c("&7Estado:    " + status));
        lore.add(Msg.c("&7Jugadores: &f" + current + "&7/&f" + a.getMaxPlayers()));
        lore.add(Msg.c("&7Tiempo:    &e5 minutos"));
        lore.add(Msg.c("&7Kills win: &e" + a.getKillsToWin()));
        lore.add(Msg.c("&8&m──────────────────"));
        if (ready && !active) {
            lore.add(Msg.c("&a▶ Click para unirte"));
            lore.add(Msg.c("&b▶ Shift+Click para observar"));
        } else if (active) {
            lore.add(Msg.c("&b▶ Click para observar"));
        } else {
            lore.add(Msg.c("&c✗ No disponible"));
        }
        return make(mat, Msg.c(a.getDisplayName()), lore);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();
        if (name.isBlank() || name.equals(" ")) return;
        if (item.getType() == Material.CHEST) { p.closeInventory(); plugin.getKitShopGUI().open(p); return; }
        for (Arena a : plugin.getArenaManager().all()) {
            if (Msg.c(a.getDisplayName()).equals(name)) {
                p.closeInventory();
                if (e.isShiftClick()) plugin.getGameManager().spectate(p, a);
                else plugin.getGameManager().join(p, a);
                return;
            }
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
