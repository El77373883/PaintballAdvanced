package com.soyadrianyt001.advancedpaintball.commands;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PACommand implements CommandExecutor, TabCompleter {

    private final AdvancedPaintball plugin;

    public PACommand(AdvancedPaintball plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player p)) { s.sendMessage("Solo jugadores."); return true; }

        if (args.length == 0) { plugin.getArenaMenuGUI().open(p); return true; }

        switch (args[0].toLowerCase()) {
            case "unirse", "join" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa unirse <arena>")); return true; }
                Arena a = plugin.getArenaManager().get(args[1]);
                if (a == null) { p.sendMessage(Msg.err("Arena no encontrada.")); return true; }
                plugin.getGameManager().join(p, a);
            }
            case "salir", "leave" -> plugin.getGameManager().leave(p);
            case "espectador", "spec" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa espectador <arena>")); return true; }
                Arena a = plugin.getArenaManager().get(args[1]);
                if (a == null) { p.sendMessage(Msg.err("Arena no encontrada.")); return true; }
                plugin.getGameManager().spectate(p, a);
            }
            case "stats" -> {
                Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : p;
                if (target == null) { p.sendMessage(Msg.err("Jugador no encontrado.")); return true; }
                PlayerStats st = plugin.getStatsManager().get(target);
                p.sendMessage(Msg.sep());
                p.sendMessage(Msg.c("  &b&lStats de &f" + target.getName()));
                p.sendMessage(Msg.c("  &7Rango:    " + plugin.getRankManager().getFormattedRank(st)));
                p.sendMessage(Msg.c("  &7Kills:    &a" + st.getKills()));
                p.sendMessage(Msg.c("  &7Muertes:  &c" + st.getDeaths()));
                p.sendMessage(Msg.c("  &7KDR:      &e" + st.kdr()));
                p.sendMessage(Msg.c("  &7Victorias:&a " + st.getWins()));
                p.sendMessage(Msg.c("  &7Partidas: &f" + st.getGames()));
                p.sendMessage(Msg.c("  &6Coins:    &e" + st.getCoins()));
                p.sendMessage(Msg.c("  &7Kit:      &b" + st.getKit()));
                p.sendMessage(Msg.c("  &7Misiones: " + plugin.getMissionManager().getMissionStatus(p)));
                p.sendMessage(Msg.sep());
            }
            case "top" -> {
                p.sendMessage(Msg.sep());
                p.sendMessage(Msg.c("  &6&lTOP 10 — AdvancedPaintball"));
                List<PlayerStats> top = plugin.getStatsManager().top(10);
                for (int i = 0; i < top.size(); i++) {
                    PlayerStats st = top.get(i);
                    String medal = i == 0 ? "&6#1" : i == 1 ? "&7#2" : i == 2 ? "&c#3" : "&f#" + (i+1);
                    p.sendMessage(Msg.c("  " + medal + " &e" + st.getName() + " &7— &aKills: " + st.getKills() + " &7| KDR: " + st.kdr()));
                }
                p.sendMessage(Msg.sep());
            }
            case "kit" -> plugin.getKitShopGUI().open(p);
            case "equipo", "team" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa equipo <mensaje>")); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getGameManager().teamChat(p, msg);
            }
            case "creator" -> {
                p.sendMessage(Msg.sep());
                p.sendMessage(Msg.c("  &b&lAdvancedPaintball &7v1.0.0"));
                p.sendMessage(Msg.c("  &7Creado por: &e&lSoyAdrianYT001"));
                p.sendMessage(Msg.c("  &7Para: &fMinecraft 1.21.1"));
                p.sendMessage(Msg.c("  &7Plugin: &aPremium &7✔"));
                p.sendMessage(Msg.sep());
            }
            default -> sendHelp(p);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lAdvancedPaintball &7— Comandos"));
        p.sendMessage(Msg.c("  &f/pa &7— Abrir menú de arenas"));
        p.sendMessage(Msg.c("  &f/pa unirse <arena> &7— Unirse"));
        p.sendMessage(Msg.c("  &f/pa salir &7— Salir de la partida"));
        p.sendMessage(Msg.c("  &f/pa espectador <arena> &7— Observar"));
        p.sendMessage(Msg.c("  &f/pa stats [jugador] &7— Estadísticas"));
        p.sendMessage(Msg.c("  &f/pa top &7— Top 10"));
        p.sendMessage(Msg.c("  &f/pa kit &7— Tienda de kits"));
        p.sendMessage(Msg.c("  &f/pa equipo <msg> &7— Chat de equipo"));
        p.sendMessage(Msg.c("  &f/pa creator &7— Créditos"));
        p.sendMessage(Msg.sep());
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) out.addAll(Arrays.asList("unirse","salir","espectador","stats","top","kit","equipo","creator"));
        else if (args.length == 2 && (args[0].equalsIgnoreCase("unirse") || args[0].equalsIgnoreCase("espectador")))
            plugin.getArenaManager().all().forEach(a -> out.add(a.getName()));
        else if (args.length == 2 && args[0].equalsIgnoreCase("stats"))
            Bukkit.getOnlinePlayers().forEach(pl -> out.add(pl.getName()));
        out.removeIf(e -> !e.toLowerCase().startsWith(args[args.length-1].toLowerCase()));
        return out;
    }
}
