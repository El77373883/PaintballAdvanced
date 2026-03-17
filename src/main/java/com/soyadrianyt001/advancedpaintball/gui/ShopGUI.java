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

import java.util.*;

public class ShopGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE = "§6§lAdvancedPaintball §8▸ Tienda";

    public ShopGUI(AdvancedPaintball plugin) { this.plugin = plugin; }

    public void open(Player p) {
        // Bloquear si no está en arena
        if (!plugin.getGameManager().inGame(p)) {
            p.sendMessage(Msg.err("Debes estar dentro de una arena para usar la tienda."));
            return;
        }

        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        PlayerStats stats = plugin.getStatsManager().get(p);

        // Fondo negro
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        // Decoración superior cyan
        ItemStack deco = make(Material.CYAN_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inv.setItem(i, deco);

        // Decoración inferior cyan
        for (int i = 45; i < 54; i++) inv.setItem(i, deco);

        // Coins info
        inv.setItem(4, make(Material.SUNFLOWER,
            Msg.c("&6&lTus Coins: &e" + stats.getCoins()),
            Arrays.asList(
                Msg.c("&8&m──────────────────"),
                Msg.c("&7+&e3 coins &7por kill"),
                Msg.c("&7+&e50 coins &7por victoria"),
                Msg.c("&7+&e30 coins &7misión diaria kills"),
                Msg.c("&8&m──────────────────")
            )));

        // ── Kits ─────────────────────────────────────────────────────────────
        inv.setItem(19, kitItem("default", Material.SNOWBALL,
            "&f&lKit Default",
            Arrays.asList(
                Msg.c("&7Pistola balanceada"),
                Msg.c("&72 stacks de snowballs"),
                Msg.c("&7Velocidad normal")
            ), 0, stats));

        inv.setItem(21, kitItem("sniper", Material.BOW,
            "&b&lKit Sniper",
            Arrays.asList(
                Msg.c("&7Arco de largo alcance"),
                Msg.c("&7Alta precisión"),
                Msg.c("&7+Velocidad I")
            ), 100, stats));

        inv.setItem(23, kitItem("assault", Material.CROSSBOW,
            "&6&lKit Assault",
            Arrays.asList(
                Msg.c("&7Disparo rápido"),
                Msg.c("&7Multishot"),
                Msg.c("&7+Velocidad II")
            ), 150, stats));

        inv.setItem(25, kitItem("heavy", Material.TNT,
            "&c&lKit Heavy",
            Arrays.asList(
                Msg.c("&7Penetración de área"),
                Msg.c("&7+Resistencia I"),
                Msg.c("&7-Velocidad")
            ), 200, stats));

        // ── Snowballs ─────────────────────────────────────────────────────────
        inv.setItem(37, make(Material.SNOWBALL,
            Msg.c("&f&l❄ Comprar Snowballs"),
            Arrays.asList(
                Msg.c("&8&m──────────────────"),
                Msg.c("&7Compra &f64 snowballs"),
                Msg.c("&6Precio: &e5 coins"),
                Msg.c("&7Tienes: &e" + stats.getCoins() + " coins"),
                Msg.c("&8&m──────────────────"),
                Msg.c("&eClick para comprar")
            )));

        // Separadores azules laterales
        ItemStack sep = make(Material.BLUE_STAINED_GLASS_PANE, " ", null);
        inv.setItem(18, sep); inv.setItem(27, sep); inv.setItem(36, sep);
        inv.setItem(26, sep); inv.setItem(35, sep); inv.setItem(44, sep);

        // Cerrar
        inv.setItem(49, make(Material.BARRIER, Msg.c("&c&l✗ Cerrar"), null));

        p.openInventory(inv);
    }

    private ItemStack kitItem(String kit, Material mat, String name,
                               List<String> desc, int price, PlayerStats stats) {
        boolean current = stats.getKit().equals(kit);
        boolean afford  = stats.getCoins() >= price || price == 0;

        List<String> lore = new ArrayList<>();
        lore.add(Msg.c("&8&m──────────────────"));
        lore.addAll(desc);
        lore.add(Msg.c("&8&m──────────────────"));
        lore.add(Msg.c(price == 0 ? "&aGratis" : "&6Precio: &e" + price + " coins"));
        lore.add(Msg.c(afford ? "&a✔ Puedes comprarlo" : "&c✗ Coins insuficientes"));
        lore.add(Msg.c(current ? "&b&l✔ KIT ACTIVO" : "&7Click para seleccionar"));

        return make(mat, Msg.c(name), lore);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;

        // Doble verificación
        if (!plugin.getGameManager().inGame(p)) {
            p.closeInventory();
            p.sendMessage(Msg.err("Debes estar en una arena para usar la tienda."));
            return;
        }

        int slot = e.getSlot();

        // Cerrar
        if (slot == 49) { p.closeInventory(); return; }

        PlayerStats stats = plugin.getStatsManager().get(p);

        // Comprar snowballs
        if (slot == 37) {
            int price = plugin.getConfig().getInt("snowball-price", 5);
            if (stats.getCoins() < price) {
                p.sendMessage(Msg.err("Necesitas &e" + price
                    + " coins&c. Tienes: &e" + stats.getCoins()));
                return;
            }
            stats.addCoins(-price);
            p.getInventory().addItem(new ItemStack(Material.SNOWBALL, 64));
            p.sendMessage(Msg.ok("Compraste &f64 snowballs &apor &e"
                + price + " coins&a!"));
            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
            plugin.getStatsManager().saveAll();
            open(p);
            return;
        }

        // Kits
        String kit = switch (slot) {
            case 19 -> "default";
            case 21 -> "sniper";
            case 23 -> "assault";
            case 25 -> "heavy";
            default -> null;
        };
        if (kit == null) return;

        int price = plugin.getConfig().getInt("kit-prices." + kit, 0);

        if (stats.getKit().equals(kit)) {
            p.sendMessage(Msg.info("Ya tienes ese kit equipado."));
            return;
        }
        if (stats.getCoins() < price && !p.hasPermission("advancedpaintball.vip")) {
            p.sendMessage(Msg.err("Necesitas &e" + price
                + " coins&c. Tienes: &e" + stats.getCoins()));
            return;
        }

        stats.setKit(kit);
        plugin.getStatsManager().saveAll();
        p.sendMessage(Msg.ok("Kit cambiado a &e" + kit + "&a!"));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        open(p);
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
