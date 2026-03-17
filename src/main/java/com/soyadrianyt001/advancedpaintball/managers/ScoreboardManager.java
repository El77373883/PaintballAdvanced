package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final AdvancedPaintball plugin;
    private final Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardManager(AdvancedPaintball plugin) { this.plugin = plugin; }

    public void update(Player p, Game game) {
        org.bukkit.scoreboard.ScoreboardManager mgr = Bukkit.getScoreboardManager();
        Scoreboard board = boards.computeIfAbsent(p.getUniqueId(), k -> mgr.getNewScoreboard());

        Objective old = board.getObjective("ap");
        if (old != null) old.unregister();

        Objective obj = board.registerNewObjective("ap", Criteria.DUMMY,
            c("&b&lAdvancedPaintball"));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        PlayerStats stats = plugin.getStatsManager().get(p);
        Game.Team team = game.getTeam(p);
        String teamName = switch (team) {
            case RED    -> "&c❤ Rojo";
            case PINK   -> "&d✿ Rosa";
            case GREEN  -> "&a✦ Verde";
            case YELLOW -> "&e★ Amarillo";
            default     -> "&7Espectador";
        };

        int tl = game.getTimeLeft();
        String time = String.format("%d:%02d", tl / 60, tl % 60);
        String rank = plugin.getRankManager().getFormattedRank(stats);

        int line = 15;
        set(board, obj, c(" "), line--);
        set(board, obj, c("&f" + p.getName()), line--);
        set(board, obj, c(rank), line--);
        set(board, obj, c("  "), line--);
        set(board, obj, c("&8▸ &7Equipo: " + teamName), line--);
        set(board, obj, c("&8▸ &7Kills:  &a" + game.getKills(p)), line--);
        set(board, obj, c("&8▸ &7Muertes: &c" + game.getDeaths(p)), line--);
        set(board, obj, c("&8▸ &7Racha:  &e" + game.getStreak(p)), line--);
        set(board, obj, c("   "), line--);
        set(board, obj, c("&c❤ &f" + game.getScoreRed() + "  &d✿ &f" + game.getScorePink()), line--);
        set(board, obj, c("&a✦ &f" + game.getScoreGreen() + "  &e★ &f" + game.getScoreYellow()), line--);
        set(board, obj, c("    "), line--);
        set(board, obj, c("&e⏱ " + time), line--);
        set(board, obj, c("&6☆ &e" + stats.getCoins() + " coins"), line--);
        set(board, obj, c("&8" + AdvancedPaintball.get().getConfig().getString("server-name", "server")), line--);

        p.setScoreboard(board);
    }

    private void set(Scoreboard b, Objective o, String text, int score) {
        String entry = text;
        while (b.getEntries().contains(entry)) entry = entry + ChatColor.RESET;
        o.getScore(entry).setScore(score);
    }

    public void remove(Player p) {
        boards.remove(p.getUniqueId());
        p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
