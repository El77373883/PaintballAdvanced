package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TablistManager {

    private final AdvancedPaintball plugin;

    public TablistManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        // Actualizar tablist cada 2s
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateAll, 40L, 40L);
    }

    public void updateAll() {
        Bukkit.getOnlinePlayers().forEach(this::update);
    }

    public void update(Player p) {
        PlayerStats stats = plugin.getStatsManager().get(p);
        String rank  = plugin.getRankManager().getFormattedRank(stats);
        String rankC = plugin.getRankManager().getRankColor(stats);
        Game game    = plugin.getGameManager().getGame(p);

        // Header
        String header = Msg.c(
            "\n&b&lAdvancedPaintball &7v1.0.0\n"
            + "&7Autor: &eSoyAdrianYT001\n"
            + (game != null
                ? "&7Arena: &f" + game.getArena().getName()
                + "  &7Equipo: " + plugin.getGameManager().teamColor(game.getTeam(p))
                + plugin.getGameManager().teamName(game.getTeam(p))
                : "&7Jugadores en línea: &f" + Bukkit.getOnlinePlayers().size())
            + "\n"
        );

        // Footer
        String footer = Msg.c(
            "\n"
            + "&7Rango: " + rank + "  "
            + "&7Nivel: &e" + stats.getLevel() + "  "
            + "&6Coins: &e" + stats.getCoins() + "\n"
            + "&7Kills: &a" + stats.getKills() + "  "
            + "&7Victorias: &b" + stats.getWins() + "  "
            + "&7KDR: &f" + stats.kdr()
            + "\n"
        );

        p.sendPlayerListHeaderAndFooter(
            Component.text(header),
            Component.text(footer)
        );

        // Prefijo en tablist
        Bukkit.getScoreboardManager().getMainScoreboard()
            .getTeams().forEach(t -> {
                if (t.getName().equals("ap_" + p.getName())) t.unregister();
            });

        try {
            var sb   = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "ap_" + p.getName();
            if (teamName.length() > 16) teamName = teamName.substring(0, 16);
            var team = sb.getTeam(teamName);
            if (team == null) team = sb.registerNewTeam(teamName);
            team.prefix(Component.text(Msg.c(rankC + "[" + stats.getRank() + "] ")));
            team.addPlayer(p);
        } catch (Exception ignored) {}
    }

    public void reset(Player p) {
        p.sendPlayerListHeaderAndFooter(
            Component.empty(),
            Component.empty()
        );
    }
}
