package com.soyadrianyt001.advancedpaintball.gui;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.models.Game.Team;
import com.soyadrianyt001.advancedpaintball.utils.KitUtils;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.List;

public class TeamSelectorGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE = "§e§lElegir Equipo";

    public TeamSelectorGUI(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    public void open(Player p, Game game) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        // Fondo negro
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);

        Team current   = game.getTeam(p);
        int redSize    = game.getTeamPlayers(Team.RED).size();
        int pinkSize   = game.getTeamPlayers(Team.PINK).size();
        int greenSize  = game.getTeamPlayers(Team.GREEN).size();
        int yellowSize = game.getTeamPlayers(Team.YELLOW).size();

        // Info jugador
        inv.setItem(4, make(Material.COMPASS,
            Msg.c("&7Tu equipo actual: "
                + plugin.getGameManager().teamColor(current)
                + "&l" + plugin.getGameManager().teamName(current)),
            Arrays.asList(
                Msg.c("&7Click en un equipo para unirte"),
                Msg.c("&7Se te equipará la armadura del equipo")
            )));

        // Equipo Rojo
        inv.setItem(10, teamBtn(
            Material.RED_WOOL,
            "&c&l❤ Equipo Rojo",
            redSize, Team.RED, current,
            Arrays.asList(
                Msg.c("&7Armadura: &cRoja"),
                Msg.c("&7Jugadores: &f" + redSize)
            )));

        // Equipo Rosa
        inv.setItem(12, teamBtn(
            Material.PINK_WOOL,
            "&d&l✿ Equipo Rosa",
            pinkSize, Team.PINK, current,
            Arrays.asList(
                Msg.c("&7Armadura: &dRosa"),
                Msg.c("&7Jugadores: &f" + pinkSize)
            )));

        // Equipo Verde
        inv.setItem(14, teamBtn(
            Material.GREEN_WOOL,
            "&a&l✦ Equipo Verde",
            greenSize, Team.GREEN, current,
            Arrays.asList(
                Msg.c("&7Armadura: &aVerde"),
                Msg.c("&7Jugadores: &f" + greenSize)
            )));

        // Equipo Amarillo
        inv.setItem(16, teamBtn(
            Material.YELLOW_WOOL,
            "&e&l★ Equipo Amarillo",
            yellowSize, Team.YELLOW, current,
            Arrays.asList(
                Msg.c("&7Armadura: &eAmarilla"),
                Msg.c("&7Jugadores: &f" + yellowSize)
            )));

        // Cerrar
        inv.setItem(22, make(Material.BARRIER,
            Msg.c("&c&l✗ Cerrar"), null));

        p.openInventory(inv);
    }

    private ItemStack teamBtn(Material mat, String name, int size,
                               Team team, Team current, List<String> extra) {
        boolean isCurrent = team == current;
        List<String> lore = new java.util.ArrayList<>();
        lore.add(Msg.c("&8&m──────────────────"));
        lore.addAll(extra);
        lore.add(Msg.c("&8&m──────────────────"));
        lore.add(Msg.c(isCurrent ? "&a&l✔ EQUIPO ACTUAL" : "&eClick para unirte"));
        return make(mat, Msg.c(name), lore);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Game game = plugin.getGameManager().getGame(p);
        if (game == null) { p.closeInventory(); return; }

        if (game.getState() == Game.State.IN_GAME) {
            p.sendMessage(Msg.err("No puedes cambiar equipo durante la partida."));
            p.closeInventory();
            return;
        }

        if (e.getSlot() == 22) { p.closeInventory(); return; }

        Team chosen = switch (e.getSlot()) {
            case 10 -> Team.RED;
            case 12 -> Team.PINK;
            case 14 -> Team.GREEN;
            case 16 -> Team.YELLOW;
            default -> null;
        };
        if (chosen == null) return;

        // Cambiar equipo
        game.getPlayers().put(p.getUniqueId(), chosen);

        // Equipar armadura del color del equipo
        equipTeamArmor(p, chosen);

        String tc = plugin.getGameManager().teamColor(chosen);
        String tn = plugin.getGameManager().teamName(chosen);

        p.sendMessage(Msg.ok("Te uniste al equipo " + tc + "&l" + tn + "&a!"));
        p.sendTitle(
            Msg.c(tc + "&l" + tn),
            Msg.c("&7¡Listo! Esperando inicio de partida..."),
            5, 40, 5);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        plugin.getScoreboardManager().update(p, game);

        // Re-dar items de lobby
        p.getInventory().setItem(7, plugin.getGameManager().getTeamItem());
        p.getInventory().setItem(8, plugin.getGameManager().getLeaveItem());

        p.closeInventory();
    }

    private void equipTeamArmor(Player p, Team team) {
        Color color = switch (team) {
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

        p.getInventory().setHelmet(leatherArmor(Material.LEATHER_HELMET, color, prefix + "Casco"));
        p.getInventory().setChestplate(leatherArmor(Material.LEATHER_CHESTPLATE, color, prefix + "Pecho"));
        p.getInventory().setLeggings(leatherArmor(Material.LEATHER_LEGGINGS, color, prefix + "Pantalón"));
        p.getInventory().setBoots(leatherArmor(Material.LEATHER_BOOTS, color, prefix + "Botas"));
    }

    private ItemStack leatherArmor(Material mat, Color color, String name) {
        ItemStack item = new ItemStack(mat);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        meta.setDisplayName(Msg.c(name));
        item.setItemMeta(meta);
        return item;
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
