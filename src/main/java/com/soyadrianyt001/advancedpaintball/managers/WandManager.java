package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WandManager {

    private final AdvancedPaintball plugin;
    private final Map<UUID, Location> pos1    = new HashMap<>();
    private final Map<UUID, Location> pos2    = new HashMap<>();
    private final Map<UUID, String>   mode    = new HashMap<>();

    public WandManager(AdvancedPaintball plugin) { this.plugin = plugin; }

    public void give(Player p, String arenaName) {
        ItemStack wand = new ItemStack(Material.GOLDEN_SWORD);
        ItemMeta m = wand.getItemMeta();
        m.setDisplayName(Msg.c("&b&l⚡ AP-Wand &8[" + arenaName + "]"));
        m.setLore(Arrays.asList(
            Msg.c("&7Click Izquierdo: &aPos1"),
            Msg.c("&7Click Derecho:   &cPos2"),
            Msg.c("&8Arena: &f" + arenaName)));
        m.addEnchant(Enchantment.SHARPNESS, 1, true);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        wand.setItemMeta(m);
        p.getInventory().addItem(wand);
        mode.put(p.getUniqueId(), arenaName);
        p.sendMessage(Msg.ok("Varita entregada. &7Izq=&aPos1 &7| Der=&cPos2"));
    }

    public boolean isWand(ItemStack i) {
        if (i == null || i.getType() != Material.GOLDEN_SWORD || !i.hasItemMeta()) return false;
        String n = i.getItemMeta().getDisplayName();
        return n != null && n.contains("AP-Wand");
    }

    public boolean inMode(Player p) { return mode.containsKey(p.getUniqueId()); }
    public String  getArena(Player p) { return mode.get(p.getUniqueId()); }

    public void setPos1(Player p, Location l) {
        pos1.put(p.getUniqueId(), l);
        p.sendMessage(Msg.ok("&aPos1 &7→ &f" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ()));
    }

    public void setPos2(Player p, Location l) {
        pos2.put(p.getUniqueId(), l);
        p.sendMessage(Msg.ok("&cPos2 &7→ &f" + l.getBlockX() + ", " + l.getBlockY() + ", " + l.getBlockZ()));
    }

    public Location getPos1(Player p) { return pos1.get(p.getUniqueId()); }
    public Location getPos2(Player p) { return pos2.get(p.getUniqueId()); }
    public boolean  hasBoth(Player p) { return pos1.containsKey(p.getUniqueId()) && pos2.containsKey(p.getUniqueId()); }
    public void     clearMode(Player p) { mode.remove(p.getUniqueId()); }
}
