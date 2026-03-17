package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.entity.Player;

public class RankManager {

    private final AdvancedPaintball plugin;

    private static final String[] RANKS = { "Novato", "Aprendiz", "Soldado", "Pro", "Elite", "Leyenda" };
    private static final int[]    XP_REQ = { 0, 100, 300, 700, 1500, 3000 };
    private static final String[] COLORS = { "&7", "&a", "&b", "&6", "&c", "&d" };

    public RankManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    public void addXp(Player p, int xp) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        stats.addXp(xp);
        checkLevelUp(p, stats);
    }

    private void checkLevelUp(Player p, PlayerStats stats) {
        int level = stats.getLevel();
        if (level < RANKS.length - 1 && stats.getXp() >= XP_REQ[level + 1]) {
            stats.setLevel(level + 1);
            stats.setRank(RANKS[level + 1]);
            p.sendMessage(Msg.ok("&a¡Subiste de rango a " + COLORS[level+1] + RANKS[level+1] + "&a!"));
            p.sendTitle(Msg.c("&6&l¡NUEVO RANGO!"), Msg.c(COLORS[level+1] + RANKS[level+1]), 10, 50, 10);
        }
    }

    public String getRankColor(PlayerStats stats) {
        int level = Math.min(stats.getLevel(), COLORS.length - 1);
        return COLORS[level];
    }

    public String getFormattedRank(PlayerStats stats) {
        return getRankColor(stats) + stats.getRank();
    }
}
