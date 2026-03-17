package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatManager implements Listener {

    private final AdvancedPaintball plugin;

    public ChatManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        // No interferir con chat de admin panel
        if (plugin.getAdminPanelGUI().getWaitingName().contains(p.getUniqueId())) return;

        PlayerStats stats = plugin.getStatsManager().get(p);
        String rankColor  = plugin.getRankManager().getRankColor(stats);
        String rankName   = stats.getRank();
        String chatColor  = getChatColor(stats);

        // Formato: [Rango] Nombre: mensaje
        String format = Msg.c(rankColor + "&l[" + rankName + "] "
            + "&f" + p.getName()
            + " &8» " + chatColor + "%s");

        e.setFormat(format);
    }

    private String getChatColor(PlayerStats stats) {
        return switch (stats.getRank()) {
            case "Novato"    -> "&7";
            case "Aprendiz"  -> "&f";
            case "Soldado"   -> "&a";
            case "Pro"       -> "&b";
            case "Elite"     -> "&c";
            case "Leyenda"   -> "&d";
            default          -> "&7";
        };
    }

    public String getPrefix(Player p) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        String rankColor  = plugin.getRankManager().getRankColor(stats);
        return Msg.c(rankColor + "[" + stats.getRank() + "] ");
    }
}
