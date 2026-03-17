package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class GameListener implements Listener {

    private final AdvancedPaintball plugin;

    public GameListener(AdvancedPaintball plugin) { this.plugin = plugin; }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (plugin.getGameManager().inGame(e.getPlayer()))
            plugin.getGameManager().leave(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (plugin.getGameManager().inGame(p)) e.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (plugin.getGameManager().inGame(e.getEntity())) {
            e.setCancelled(true);
            e.getDrops().clear();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.getGameManager().inGame(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p && plugin.getGameManager().inGame(p))
            e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (plugin.getGameManager().inGame(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (plugin.getGameManager().inGame(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getGameManager().inGame(p)) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        Game game = plugin.getGameManager().getGame(p);
        if (game == null) return;

        // No durante partida
        if (game.getState() == Game.State.IN_GAME) return;

        // Item SALIR = cama roja → salir de la arena
        if (item.getType() == Material.RED_BED) {
            e.setCancelled(true);
            plugin.getGameManager().leave(p);
            return;
        }

        // Item EQUIPO = lana blanca → abrir TeamSelectorGUI
        if (item.getType() == Material.WHITE_WOOL) {
            e.setCancelled(true);
            plugin.getTeamSelectorGUI().open(p, game);
        }
    }
}
