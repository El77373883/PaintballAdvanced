package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;

public class TrailListener implements Listener {

    private final AdvancedPaintball plugin;

    public TrailListener(AdvancedPaintball plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getGameManager().inGame(p)) return;
        Game game = plugin.getGameManager().getGame(p);
        if (game == null || game.getState() != Game.State.IN_GAME) return;
        if (game.isSpectator(p)) return;

        Color trailColor = switch (game.getTeam(p)) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };

        // Trail solo visible para el jugador mismo
        p.spawnParticle(Particle.DUST,
            p.getLocation().add(0, 0.1, 0),
            3, 0.1, 0.1, 0.1,
            new Particle.DustOptions(trailColor, 0.8f));
    }
}
