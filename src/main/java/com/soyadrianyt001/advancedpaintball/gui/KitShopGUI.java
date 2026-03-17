package com.soyadrianyt001.advancedpaintball.gui;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class KitShopGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE = "§6§lTienda de Kits";

    public KitShopGUI(AdvancedPaintball plugin) { this.plugin = plugin; }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        PlayerStats stats = plugin.getStatsManager().get(p);
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);

        inv.setItem(4, make(Material.SUNFLOWER, Msg.c("&6Tus Coins: &e" + stats.getCoins()),
            Arrays.asList(Msg.c("&7+3 coins por kill"), Msg.c("&7+50 coins por victoria"))));

        inv.setItem(10, kitItem("default",  Material.SNOWBALL,      "&fDefault",       0,   stats));
        inv.setItem(12, kitItem("sniper",   Material.BOW,           "&bSniper",        100, stats));
        inv.setItem(14, kitItem("assault",  Material.CROSSBOW,      "&6Assault",       150, stats));
        inv.setItem(16, kitItem("heavy",    Material.TNT,           "&cHeavy",         200, stats));
        p.openInventory(inv);
    }

    private ItemStack kitItem(String kit, Material mat, String name, int price, PlayerStats stats) {
        boolean current = stats.getKit().equals(kit);
        boolean afford  = stats.getCoins() >= price || price == 0;
        List<String> lore = new ArrayList<>();
        lore.add(Msg.c(price == 0 ? "&aGratis" : "&6Precio: &e" + price + " coins"));
        lore.add(Msg.c(afford ? "&a✔ Puedes comprar" : "&c✗ Coins insuficientes"));
        lore.add(Msg.c(current ? "&b✔ KIT ACTIVO" : "&7Click para seleccionar"));
        ItemStack item = make(mat, Msg.c(name), lore);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        int slot = e.getSlot();
        String kit = switch (slot) { case 10 -> "default"; case 12 -> "sniper"; case 14 -> "assault"; case 16 -> "heavy"; default -> null; };
        if (kit == null) return;
        int price = plugin.getConfig().getInt("kit-prices." + kit, 0);
        PlayerStats stats = plugin.getStatsManager().get(p);
        if (stats.getKit().equals(kit)) { p.sendMessage(Msg.info("Ya tienes ese kit.")); return; }
        if (stats.getCoins() < price && !p.hasPermission("advancedpaintball.vip")) {
            p.sendMessage(Msg.err("Necesitas &e" + price + " coins&c. Tienes: &e" + stats.getCoins())); return;
        }
        stats.setKit(kit);
        plugin.getStatsManager().saveAll();
        p.sendMessage(Msg.ok("Kit cambiado a &e" + kit + "&a!"));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        p.closeInventory();
        open(p);
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
