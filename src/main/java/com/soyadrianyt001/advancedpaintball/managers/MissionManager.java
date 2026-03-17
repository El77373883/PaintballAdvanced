package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.entity.Player;

public class MissionManager {

    private final AdvancedPaintball plugin;
    private static final int MISSION_KILLS = 10;
    private static final int MISSION_WINS  = 2;
    private static final int REWARD_KILLS  = 30;
    private static final int REWARD_WINS   = 50;
    private static final long DAY_MS       = 86400000L;

    public MissionManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    public void onKill(Player p) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        checkReset(stats);
        stats.addMissionKill();
        if (stats.getMissionKills() == MISSION_KILLS) {
            stats.addCoins(REWARD_KILLS);
            p.sendMessage(Msg.ok("&6¡Misión diaria completada! &7(10 kills) &6+&e" + REWARD_KILLS + " coins!"));
        }
    }

    public void onWin(Player p) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        checkReset(stats);
        stats.addMissionWin();
        if (stats.getMissionWins() == MISSION_WINS) {
            stats.addCoins(REWARD_WINS);
            p.sendMessage(Msg.ok("&6¡Misión diaria completada! &7(2 victorias) &6+&e" + REWARD_WINS + " coins!"));
        }
    }

    private void checkReset(PlayerStats stats) {
        long now = System.currentTimeMillis();
        if (now - stats.getLastMissionReset() >= DAY_MS) {
            stats.setMissionKills(0);
            stats.setMissionWins(0);
            stats.setLastMissionReset(now);
        }
    }

    public String getMissionStatus(Player p) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        checkReset(stats);
        return Msg.c("&7Kills: &f" + stats.getMissionKills() + "&7/&f" + MISSION_KILLS +
               " &8| &7Victorias: &f" + stats.getMissionWins() + "&7/&f" + MISSION_WINS);
    }

    public void saveAll() {
        plugin.getStatsManager().saveAll();
    }
}
