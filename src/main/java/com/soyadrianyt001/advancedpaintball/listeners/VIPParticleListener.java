package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class VIPParticleListener implements Listener {

    private final AdvancedPaintball plugin;

    public VIPParticleListener(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!p.hasPermission("advancedpaintball.vip")) return;

        PlayerStats stats = plugin.getStatsManager().get(p);

        // Partículas según rango VIP
        Color particleColor = switch (stats.getRank()) {
            case "Pro"     -> Color.AQUA;
            case "Elite"   -> Color.RED;
            case "Leyenda" -> Color.FUCHSIA;
            default        -> Color.YELLOW;
        };

        // Partículas alrededor del jugador VIP
        p.getWorld().spawnParticle(
            Particle.DUST,
            p.getLocation().add(0, 0.1, 0),
            3, 0.3, 0.1, 0.3,
            new Particle.DustOptions(particleColor, 1.0f));

        // Partículas especiales para Leyenda
        if (stats.getRank().equals("Leyenda")) {
            p.getWorld().spawnParticle(
                Particle.END_ROD,
                p.getLocation().add(0, 0.5, 0),
                1, 0.2, 0.2, 0.2, 0.02);
        }
    }
}
