package com.soyadrianyt001.advancedpaintball.utils;

import com.soyadrianyt001.advancedpaintball.models.Game.Team;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.Arrays;
import java.util.List;

public final class KitUtils {

    private KitUtils() {}

    public static void give(Player p, Team team, String kit) {
        p.getInventory().clear();
        clearFx(p);
        switch (kit.toLowerCase()) {
            case "sniper"  -> sniper(p);
            case "assault" -> assault(p);
            case "heavy"   -> heavy(p);
            default        -> defaultKit(p);
        }
        armor(p, team);
        p.getInventory().setItem(1, new ItemStack(Material.SNOWBALL, 64));
        p.getInventory().setItem(2, new ItemStack(Material.SNOWBALL, 64));
    }

    private static void defaultKit(Player p) {
        p.getInventory().setItem(0, crossbow("&fPistola de Paintball",
            Arrays.asList("&7Kit: &fDefault", "&7Equilibrado"),
            Enchantment.QUICK_CHARGE, 2));
    }

    private static void sniper(Player p) {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta m = bow.getItemMeta();
        m.setDisplayName(Msg.c("&bFrancotirador Paintball"));
        m.setLore(Arrays.asList(Msg.c("&7Kit: &bSniper"), Msg.c("&7Largo alcance")));
        m.addEnchant(Enchantment.POWER, 5, true);
        m.addEnchant(Enchantment.INFINITY, 1, true);
        bow.setItemMeta(m);
        p.getInventory().setItem(0, bow);
        p.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
        p.addPotionEffect(fx(PotionEffectType.SPEED, 0));
    }

    private static void assault(Player p) {
        p.getInventory().setItem(0, crossbow("&6Rifle de Asalto",
            Arrays.asList("&7Kit: &6Assault", "&7Disparo rápido"),
            Enchantment.QUICK_CHARGE, 3));
        p.addPotionEffect(fx(PotionEffectType.SPEED, 1));
    }

    private static void heavy(Player p) {
        p.getInventory().setItem(0, crossbow("&cLanzagranadas",
            Arrays.asList("&7Kit: &cHeavy", "&7+Resistencia -Velocidad"),
            Enchantment.PIERCING, 4));
        p.addPotionEffect(fx(PotionEffectType.RESISTANCE, 0));
        p.addPotionEffect(fx(PotionEffectType.SLOWNESS, 0));
    }

    private static ItemStack crossbow(String name, List<String> lore, Enchantment e, int lvl) {
        ItemStack cb = new ItemStack(Material.CROSSBOW);
        CrossbowMeta m = (CrossbowMeta) cb.getItemMeta();
        m.setDisplayName(Msg.c(name));
        List<String> colored = new java.util.ArrayList<>();
        lore.forEach(l -> colored.add(Msg.c(l)));
        m.setLore(colored);
        m.addEnchant(e, lvl, true);
        cb.setItemMeta(m);
        return cb;
    }

    private static void armor(Player p, Team team) {
        Color col = switch (team) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };
        String prefix = switch (team) {
            case RED    -> "&c";
            case PINK   -> "&d";
            case GREEN  -> "&a";
            case YELLOW -> "&e";
            default     -> "&f";
        };
        p.getInventory().setHelmet(leather(Material.LEATHER_HELMET, col, prefix + "Casco"));
        p.getInventory().setChestplate(leather(Material.LEATHER_CHESTPLATE, col, prefix + "Pecho"));
        p.getInventory().setLeggings(leather(Material.LEATHER_LEGGINGS, col, prefix + "Pantalón"));
        p.getInventory().setBoots(leather(Material.LEATHER_BOOTS, col, prefix + "Botas"));
    }

    private static ItemStack leather(Material mat, Color color, String name) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta m = (LeatherArmorMeta) item.getItemMeta();
        m.setColor(color);
        m.setDisplayName(Msg.c(name));
        item.setItemMeta(m);
        return item;
    }

    private static PotionEffect fx(PotionEffectType t, int amp) {
        return new PotionEffect(t, Integer.MAX_VALUE, amp, false, false);
    }

    private static void clearFx(Player p) {
        p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
    }
}
