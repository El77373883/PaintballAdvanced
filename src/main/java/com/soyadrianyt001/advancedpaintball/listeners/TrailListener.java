package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class TrailListener implements Listener {

    private final AdvancedPaintball plugin;
    private int tickCount = 0;

    public TrailListener(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getGameManager().inGame(p)) return;
        Game game = plugin.getGameManager().getGame(p);
        if (game == null || game.getState() != Game.State.IN_GAME) return;
        if (game.isSpectator(p)) return;

        tickCount++;
        PlayerStats stats = plugin.getStatsManager().get(p);
        boolean isVIP     = p.hasPermission("advancedpaintball.vip");

        // Trail básico para todos según equipo
        Color teamColor = switch (game.getTeam(p)) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };

        // Trail según rango y VIP
        switch (stats.getRank()) {

            case "Novato", "Aprendiz" -> {
                // Trail básico de puntos
                if (tickCount % 3 == 0) {
                    p.spawnParticle(Particle.DUST,
                        p.getLocation().add(0, 0.1, 0),
                        2, 0.1, 0.1, 0.1,
                        new Particle.DustOptions(teamColor, 0.8f));
                }
            }

            case "Soldado" -> {
                // Trail con más partículas
                if (tickCount % 2 == 0) {
                    p.spawnParticle(Particle.DUST,
                        p.getLocation().add(0, 0.1, 0),
                        4, 0.2, 0.1, 0.2,
                        new Particle.DustOptions(teamColor, 1.0f));
                }
            }

            case "Pro" -> {
                // Trail de polvo brillante
                p.spawnParticle(Particle.DUST,
                    p.getLocation().add(0, 0.1, 0),
                    5, 0.2, 0.1, 0.2,
                    new Particle.DustOptions(teamColor, 1.2f));
                if (tickCount % 5 == 0) {
                    p.spawnParticle(Particle.END_ROD,
                        p.getLocation().add(0, 0.3, 0),
                        1, 0.1, 0.1, 0.1, 0.01);
                }
            }

            case "Elite" -> {
                // Trail con llamas y polvo
                p.spawnParticle(Particle.DUST,
                    p.getLocation().add(0, 0.1, 0),
                    6, 0.25, 0.1, 0.25,
                    new Particle.DustOptions(teamColor, 1.5f));
                if (tickCount % 3 == 0) {
                    p.spawnParticle(Particle.FLAME,
                        p.getLocation().add(0, 0.1, 0),
                        2, 0.15, 0.1, 0.15, 0.01);
                }
            }

            case "Leyenda" -> {
                // Trail épico arcoíris + end rod + polvo
                p.spawnParticle(Particle.DUST,
                    p.getLocation().add(0, 0.1, 0),
                    8, 0.3, 0.1, 0.3,
                    new Particle.DustOptions(teamColor, 2.0f));
                p.spawnParticle(Particle.END_ROD,
                    p.getLocation().add(0, 0.3, 0),
                    2, 0.1, 0.2, 0.1, 0.02);

                // Arcoíris VIP
                if (tickCount % 2 == 0) {
                    Color[] rainbow = {Color.RED, Color.ORANGE, Color.YELLOW,
                                       Color.GREEN, Color.AQUA, Color.BLUE, Color.FUCHSIA};
                    Color rc = rainbow[tickCount % rainbow.length];
                    p.spawnParticle(Particle.DUST,
                        p.getLocation().add(0, 0.5, 0),
                        3, 0.1, 0.1, 0.1,
                        new Particle.DustOptions(rc, 1.2f));
                }
            }
        }

        // Trail extra VIP exclusivo
        if (isVIP) {
            if (tickCount % 4 == 0) {
                p.spawnParticle(Particle.TOTEM_OF_UNDYING,
                    p.getLocation().add(0, 0.5, 0),
                    3, 0.2, 0.3, 0.2, 0.05);
            }
            // Sonido suave al correr (VIP)
            if (tickCount % 20 == 0) {
                p.playSound(p.getLocation(),
                    Sound.BLOCK_AMETHYST_BLOCK_STEP, 0.1f, 1.5f);
            }
        }
    }
}
