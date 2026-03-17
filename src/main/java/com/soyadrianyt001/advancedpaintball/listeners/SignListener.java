package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignListener implements Listener {

    private final AdvancedPaintball plugin;

    public SignListener(AdvancedPaintball plugin) { this.plugin = plugin; }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;
        if (!(e.getClickedBlock().getState() instanceof Sign)) return;

        Player p   = e.getPlayer();
        var loc    = e.getClickedBlock().getLocation();

        if (!plugin.getSignManager().isSign(loc)) return;
        e.setCancelled(true);

        String arenaName = plugin.getSignManager().getArena(loc);
        Arena arena      = plugin.getArenaManager().get(arenaName);
        if (arena == null) {
            p.sendMessage(Msg.err("Esa arena ya no existe."));
            return;
        }
        if (!arena.isReady()) {
            p.sendMessage(Msg.err("Esa arena no está lista."));
            return;
        }

        // Unirse al hacer click en el sign
        plugin.getGameManager().join(p, arena);
    }
}
