package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShootListener implements Listener {

    private final AdvancedPaintball plugin;
    private final Map<UUID, Long> cooldown = new HashMap<>();
    private static final long CD = 350L;

    public ShootListener(AdvancedPaintball plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!plugin.getGameManager().inGame(p)) return;
        Game game = plugin.getGameManager().getGame(p);
        if (game == null || game.getState() != Game.State.IN_GAME) return;
        if (game.isSpectator(p)) return;
        if (game.isSafe(p)) return;
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.SNOWBALL) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        e.setCancelled(true);
        long now = System.currentTimeMillis();
        if (cooldown.getOrDefault(p.getUniqueId(), 0L) + CD > now) return;
        cooldown.put(p.getUniqueId(), now);

        Snowball ball = p.launchProjectile(Snowball.class);
        ball.setVelocity(p.getLocation().getDirection().multiply(2.2));

        if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
        else p.getInventory().remove(item);

        // Partícula de color SOLO para el que dispara
        Color teamColor = switch (game.getTeam(p)) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };
        p.spawnParticle(Particle.DUST,
            p.getEyeLocation().add(p.getLocation().getDirection().multiply(0.5)),
            8, 0.05, 0.05, 0.05,
            new Particle.DustOptions(teamColor, 1.2f));
        p.playSound(p.getLocation(), Sound.BLOCK_SNOW_BREAK, 0.7f, 1.8f);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Snowball ball)) return;
        if (!(ball.getShooter() instanceof Player shooter)) return;
        if (!plugin.getGameManager().inGame(shooter)) return;
        Game game = plugin.getGameManager().getGame(shooter);
        if (game == null || game.getState() != Game.State.IN_GAME) return;

        Color teamColor = switch (game.getTeam(shooter)) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };

        if (e.getHitEntity() instanceof Player victim) {
            Game vg = plugin.getGameManager().getGame(victim);
            if (vg == null || !vg.getArena().getName().equals(game.getArena().getName())) return;
            if (victim.getUniqueId().equals(shooter.getUniqueId())) return;
            if (game.isSpectator(victim)) return;
            if (game.isSafe(victim)) { shooter.sendMessage(Msg.err("¡Ese jugador está en zona segura!")); return; }
            if (game.getTeam(shooter) == game.getTeam(victim)) { shooter.sendMessage(Msg.err("¡No dispares a tu equipo!")); return; }

            // Partículas de impacto visibles para todos
            victim.getWorld().spawnParticle(Particle.DUST,
                victim.getLocation().add(0, 1, 0), 40, 0.4, 0.6, 0.4,
                new Particle.DustOptions(teamColor, 2.5f));
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1f, 0.4f);

            plugin.getGameManager().handleKill(shooter, victim, game);
        } else {
            // Pintura en bloque - solo partículas, no afectan bloques
            ball.getWorld().spawnParticle(Particle.DUST,
                ball.getLocation(), 20, 0.3, 0.3, 0.3,
                new Particle.DustOptions(teamColor, 1.8f));
        }
    }
}
