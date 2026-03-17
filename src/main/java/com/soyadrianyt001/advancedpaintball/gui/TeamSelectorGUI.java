package com.soyadrianyt001.advancedpaintball.gui;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.models.Game.Team;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

        // Equipo actual del jugador
        Team current   = game.getTeam(p);
        int redSize    = game.getTeamPlayers(Team.RED).size();
        int pinkSize   = game.getTeamPlayers(Team.PINK).size();
        int greenSize  = game.getTeamPlayers(Team.GREEN).size();
        int yellowSize = game.getTeamPlayers(Team.YELLOW).size();

        // Info del equipo actual
        String tc = plugin.getGameManager().teamColor(current);
        String tn = plugin.getGameManager().teamName(current);
        inv.setItem(4, make(Material.COMPASS,
            Msg.c("&7Equipo actual: " + tc + "&l" + tn),
            Arrays.asList(
                Msg.c("&7Click en un equipo para unirte"),
                Msg.c("&8&m──────────────────"),
                Msg.c("&7Total jugadores: &f" + game.totalActive())
            )));

        // Botones de equipos
        inv.setItem(10, teamBtn(Material.RED_WOOL,
            "&c&l❤ Equipo Rojo",
            redSize, Team.RED, current));

        inv.setItem(12, teamBtn(Material.PINK_WOOL,
            "&d&l✿ Equipo Rosa",
            pinkSize, Team.PINK, current));

        inv.setItem(14, teamBtn(Material.GREEN_WOOL,
            "&a&l✦ Equipo Verde",
            greenSize, Team.GREEN, current));

        inv.setItem(16, teamBtn(Material.YELLOW_WOOL,
            "&e&l★ Equipo Amarillo",
            yellowSize, Team.YELLOW, current));

        // Cerrar
        inv.setItem(22, make(Material.BARRIER,
            Msg.c("&c&l✗ Cerrar"), null));

        p.openInventory(inv);
    }

    private ItemStack teamBtn(Material mat, String name, int size,
                               Team team, Team current) {
        boolean isCurrent = team == current;
        List<String> lore = Arrays.asList(
            Msg.c("&8&m──────────────────"),
            Msg.c("&7Jugadores: &f" + size),
            Msg.c("&8&m──────────────────"),
            Msg.c(isCurrent ? "&a&l✔ EQUIPO ACTUAL" : "&eClick para unirte")
        );
        return make(mat, Msg.c(name), lore);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals(TITLE)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Game game = plugin.getGameManager().getGame(p);
        if (game == null) { p.closeInventory(); return; }

        // No cambiar equipo durante la partida
        if (game.getState() == Game.State.IN_GAME) {
            p.sendMessage(Msg.err("No puedes cambiar de equipo durante la partida."));
            p.closeInventory();
            return;
        }

        Team chosen = switch (e.getSlot()) {
            case 10 -> Team.RED;
            case 12 -> Team.PINK;
            case 14 -> Team.GREEN;
            case 16 -> Team.YELLOW;
            default -> null;
        };

        if (e.getSlot() == 22) { p.closeInventory(); return; }
        if (chosen == null) return;

        // Cambiar equipo
        game.getPlayers().put(p.getUniqueId(), chosen);
        String tc = plugin.getGameManager().teamColor(chosen);
        String tn = plugin.getGameManager().teamName(chosen);

        p.sendMessage(Msg.ok("Cambiaste al equipo " + tc + "&l" + tn + "&a!"));
        p.sendTitle(
            Msg.c(tc + "&l" + tn),
            Msg.c("&7¡Listo para jugar!"),
            5, 30, 5);
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        plugin.getScoreboardManager().update(p, game);
        p.closeInventory();
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
