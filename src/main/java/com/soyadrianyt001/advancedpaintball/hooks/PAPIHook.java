package com.soyadrianyt001.advancedpaintball.hooks;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PAPIHook extends PlaceholderExpansion {

    private final AdvancedPaintball plugin;

    public PAPIHook(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() { return "apb"; }

    @Override
    public @NotNull String getAuthor() { return "SoyAdrianYT001"; }

    @Override
    public @NotNull String getVersion() { return "1.0.0"; }

    @Override
    public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player p, @NotNull String identifier) {
        if (p == null) return "";
        PlayerStats stats = plugin.getStatsManager().get(p);

        return switch (identifier) {
            case "kills"      -> String.valueOf(stats.getKills());
            case "deaths"     -> String.valueOf(stats.getDeaths());
            case "kdr"        -> String.valueOf(stats.kdr());
            case "wins"       -> String.valueOf(stats.getWins());
            case "games"      -> String.valueOf(stats.getGames());
            case "coins"      -> String.valueOf(stats.getCoins());
            case "rank"       -> stats.getRank();
            case "rank_color" -> plugin.getRankManager().getRankColor(stats);
            case "level"      -> String.valueOf(stats.getLevel());
            case "xp"         -> String.valueOf(stats.getXp());
            case "kit"        -> stats.getKit();
            case "prefix"     -> plugin.getChatManager().getPrefix(p);
            case "ingame"     -> plugin.getGameManager().inGame(p) ? "Sí" : "No";
            default           -> null;
        };
    }
}
