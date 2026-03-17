package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

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
            e.setCancelled(true); e.getDrops().clear();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (plugin.getGameManager().inGame(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p && plugin.getGameManager().inGame(p)) e.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (plugin.getGameManager().inGame(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (plugin.getGameManager().inGame(e.getPlayer())) e.setCancelled(true);
    }
}
