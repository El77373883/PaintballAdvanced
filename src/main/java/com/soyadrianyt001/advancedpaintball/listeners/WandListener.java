package com.soyadrianyt001.advancedpaintball.listeners;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WandListener implements Listener {

    private final AdvancedPaintball plugin;

    public WandListener(AdvancedPaintball plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        var p    = e.getPlayer();
        var item = e.getItem();
        var wm   = plugin.getWandManager();
        if (!wm.inMode(p) || !wm.isWand(item) || e.getClickedBlock() == null) return;
        e.setCancelled(true);
        String arenaName = wm.getArena(p);
        Arena arena = plugin.getArenaManager().get(arenaName);
        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            wm.setPos1(p, e.getClickedBlock().getLocation());
            if (arena != null) { arena.setPos1(e.getClickedBlock().getLocation()); plugin.getArenaManager().save(arena); }
        } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            wm.setPos2(p, e.getClickedBlock().getLocation());
            if (arena != null) { arena.setPos2(e.getClickedBlock().getLocation()); plugin.getArenaManager().save(arena); }
        }
        if (wm.hasBoth(p))
            p.sendMessage(Msg.ok("Pos1 y Pos2 guardadas para &f" + arenaName + " &a✔"));
    }
}
