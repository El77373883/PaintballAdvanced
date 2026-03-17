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

public class ParticleSelectorGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE = "§d§lEfectos de Partículas";

    // Guarda la partícula elegida por jugador
    private final Map<UUID, String> selectedParticle = new HashMap<>();

    public ParticleSelectorGUI(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    public void open(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        PlayerStats stats = plugin.getStatsManager().get(p);
        String current = selectedParticle.getOrDefault(p.getUniqueId(), "ninguna");

        // Fondo
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        // Decoración
        ItemStack deco = make(Material.PURPLE_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 9; i++) inv.setItem(i, deco);
        for (int i = 45; i < 54; i++) inv.setItem(i, deco);

        // Info
        inv.setItem(4, make(Material.NETHER_STAR,
            Msg.c("&d&lTus Partículas"),
            Arrays.asList(
                Msg.c("&7Actual: &d" + current),
                Msg.c("&7Se muestran al eliminar a alguien"),
                Msg.c("&8&m──────────────────"),
                Msg.c("&7Coins: &e" + stats.getCoins())
            )));

        // Partículas disponibles
        inv.setItem(19, particleBtn("corazon",  Material.RED_DYE,
            "&c&l❤ Corazones",
            "&cRojo brillante",
            0, current));

        inv.setItem(20, particleBtn("explosion", Material.GUNPOWDER,
            "&8&l💥 Explosión",
            "&8Humo y cenizas",
            50, current));

        inv.setItem(21, particleBtn("estrella", Material.GLOWSTONE_DUST,
            "&e&l⭐ Estrellas",
            "&eDoradas brillantes",
            100, current));

        inv.setItem(22, particleBtn("fuego", Material.BLAZE_POWDER,
            "&6&l🔥 Llamas",
            "&6Fuego y brasas",
            100, current));

        inv.setItem(23, particleBtn("agua", Material.PRISMARINE_CRYSTALS,
            "&b&l💧 Agua",
            "&bGotitas de agua",
            150, current));

        inv.setItem(24, particleBtn("magia", Material.MAGENTA_DYE,
            "&5&l✨ Magia",
            "&5Polvo encantado",
            150, current));

        inv.setItem(25, particleBtn("arcoiris", Material.DIAMOND,
            "&b&la&cr&6c&ai&9r&5i&ds",
            "&7Todos los colores",
            300, current));

        // Segunda fila
        inv.setItem(28, particleBtn("nieve", Material.SNOWBALL,
            "&f&l❄ Nieve",
            "&fCopos de nieve",
            50, current));

        inv.setItem(29, particleBtn("sangre", Material.REDSTONE,
            "&4&l🩸 Sangre",
            "&4Rojo intenso",
            200, current));

        inv.setItem(30, particleBtn("totem", Material.TOTEM_OF_UNDYING,
            "&a&l🗿 Totem",
            "&aParticulas de tótem",
            250, current));

        inv.setItem(31, particleBtn("dragon", Material.DRAGON_BREATH,
            "&8&l🐉 Dragón",
            "&8Aliento de dragón",
            300, current));

        inv.setItem(32, particleBtn("portal", Material.ENDER_PEARL,
            "&5&l🌀 Portal",
            "&5Partículas de portal",
            200, current));

        // Quitar partícula
        inv.setItem(40, make(Material.BARRIER,
            Msg.c("&c&l✗ Sin partículas"),
            Arrays.asList(
                Msg.c("&7Quitar efecto actual"),
                Msg.c("&aGratis"),
                Msg.c(current.equals("ninguna") ? "&b✔ ACTIVA" : "&eClick para quitar")
            )));

        // Cerrar
        inv.setItem(49, make(Material.ARROW, Msg.c("&7◀ Volver"), null));

        p.openInventory(inv);
    }

    private ItemStack particleBtn(String id, Material mat, String name,
                                   String desc, int price, String current) {
        boolean active = current.equals(id);
        PlayerStats stats = null;
        List<String> lore = new ArrayList<>();
        lore.add(Msg.c("&8&m──────────────────"));
        lore.add(Msg.c("&7Tipo: &f" + desc));
        lore.add(Msg.c(price == 0 ? "&aGratis" : "&6Precio: &e" + price + " coins"));
        lore.add(Msg.c("&8&m──────────────────"));
        lore.add(Msg.c(active ? "&b&l✔ ACTIVA" : "&eClick para activar"));
        return make(mat, Msg.c(name), lore);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        if (e.getSlot() == 49) { p.closeInventory(); plugin.getMainMenuGUI().open(p); return; }

        PlayerStats stats = plugin.getStatsManager().get(p);

        // Mapa slot → id de partícula y precio
        Map<Integer, Object[]> particles = new LinkedHashMap<>();
        particles.put(19, new Object[]{"corazon",  0});
        particles.put(20, new Object[]{"explosion",50});
        particles.put(21, new Object[]{"estrella", 100});
        particles.put(22, new Object[]{"fuego",    100});
        particles.put(23, new Object[]{"agua",     150});
        particles.put(24, new Object[]{"magia",    150});
        particles.put(25, new Object[]{"arcoiris", 300});
        particles.put(28, new Object[]{"nieve",    50});
        particles.put(29, new Object[]{"sangre",   200});
        particles.put(30, new Object[]{"totem",    250});
        particles.put(31, new Object[]{"dragon",   300});
        particles.put(32, new Object[]{"portal",   200});
        particles.put(40, new Object[]{"ninguna",  0});

        if (!particles.containsKey(e.getSlot())) return;

        String id    = (String) particles.get(e.getSlot())[0];
        int    price = (int)    particles.get(e.getSlot())[1];

        // Ya activa
        String current = selectedParticle.getOrDefault(p.getUniqueId(), "ninguna");
        if (current.equals(id)) {
            p.sendMessage(Msg.info("Esa partícula ya está activa."));
            return;
        }

        // Verificar coins
        if (stats.getCoins() < price && !p.hasPermission("advancedpaintball.vip")) {
            p.sendMessage(Msg.err("Necesitas &e" + price + " coins&c. Tienes: &e" + stats.getCoins()));
            return;
        }

        // Cobrar si tiene precio
        if (price > 0) {
            stats.addCoins(-price);
            plugin.getStatsManager().saveAll();
        }

        selectedParticle.put(p.getUniqueId(), id);
        p.sendMessage(Msg.ok("Partícula &d" + id + " &aactivada!"));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        // Preview
        spawnParticle(p, id);
        open(p);
    }

    public void spawnKillParticle(Player killer, org.bukkit.Location loc) {
        String id = selectedParticle.getOrDefault(killer.getUniqueId(), "ninguna");
        if (id.equals("ninguna")) return;
        spawnParticleAt(loc, id);
    }

    private void spawnParticle(Player p, String id) {
        spawnParticleAt(p.getLocation().add(0, 1, 0), id);
    }

    private void spawnParticleAt(org.bukkit.Location loc, String id) {
        World w = loc.getWorld();
        switch (id) {
            case "corazon"   -> w.spawnParticle(Particle.HEART,        loc, 15, 0.5, 0.5, 0.5, 0);
            case "explosion" -> w.spawnParticle(Particle.LARGE_SMOKE,  loc, 20, 0.5, 0.5, 0.5, 0.1);
            case "estrella"  -> w.spawnParticle(Particle.END_ROD,      loc, 20, 0.5, 0.5, 0.5, 0.1);
            case "fuego"     -> w.spawnParticle(Particle.FLAME,        loc, 20, 0.5, 0.5, 0.5, 0.05);
            case "agua"      -> w.spawnParticle(Particle.DRIPPING_WATER, loc, 20, 0.5, 0.5, 0.5, 0);
            case "magia"     -> w.spawnParticle(Particle.ENCHANT,      loc, 30, 0.5, 0.5, 0.5, 1);
            case "arcoiris"  -> {
                Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW,
                                  Color.GREEN, Color.AQUA, Color.BLUE, Color.FUCHSIA};
                for (Color c : colors) {
                    w.spawnParticle(Particle.DUST, loc, 5, 0.3, 0.3, 0.3,
                        new Particle.DustOptions(c, 1.5f));
                }
            }
            case "nieve"     -> w.spawnParticle(Particle.SNOWFLAKE,    loc, 20, 0.5, 0.5, 0.5, 0.1);
            case "sangre"    -> w.spawnParticle(Particle.DUST,         loc, 30, 0.5, 0.5, 0.5,
                new Particle.DustOptions(Color.RED, 2f));
            case "totem"     -> w.spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 30, 0.5, 0.8, 0.5, 0.5);
            case "dragon"    -> w.spawnParticle(Particle.DRAGON_BREATH, loc, 20, 0.5, 0.5, 0.5, 0.1);
            case "portal"    -> w.spawnParticle(Particle.PORTAL,       loc, 30, 0.5, 0.5, 0.5, 1);
        }
    }

    public String getParticle(Player p) {
        return selectedParticle.getOrDefault(p.getUniqueId(), "ninguna");
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
