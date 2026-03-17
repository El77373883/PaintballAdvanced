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

        if (args.length == 0) {
            plugin.getMainMenuGUI().open(p);
            return true;
        }

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

            case "register" -> {
                if (!p.hasPermission("advancedpaintball.admin")) { p.sendMessage(Msg.err("Sin permiso.")); return true; }
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa register <arena>")); return true; }
                Arena a = plugin.getArenaManager().get(args[1]);
                if (a == null) { p.sendMessage(Msg.err("Arena no encontrada.")); return true; }
                if (!a.isReady()) { p.sendMessage(Msg.err("Arena incompleta. Configúrala primero.")); return true; }
                a.setVisibleInMenu(true);
                plugin.getArenaManager().save(a);
                p.sendMessage(Msg.ok("Arena &f" + args[1] + " &aregistrada en &f/pa&a!"));
            }

            case "unregister" -> {
                if (!p.hasPermission("advancedpaintball.admin")) { p.sendMessage(Msg.err("Sin permiso.")); return true; }
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa unregister <arena>")); return true; }
                Arena a = plugin.getArenaManager().get(args[1]);
                if (a == null) { p.sendMessage(Msg.err("Arena no encontrada.")); return true; }
                a.setVisibleInMenu(false);
                plugin.getArenaManager().save(a);
                p.sendMessage(Msg.ok("Arena &f" + args[1] + " &adesregistrada."));
            }

            case "stats" -> {
                Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : p;
                if (target == null) { p.sendMessage(Msg.err("Jugador no encontrado.")); return true; }
                PlayerStats st = plugin.getStatsManager().get(target);
                String rank    = plugin.getRankManager().getFormattedRank(st);
                p.sendMessage(Msg.sep());
                p.sendMessage(Msg.c("  &b&lStats de &f" + target.getName()));
                p.sendMessage(Msg.c("  &7Rango:     " + rank));
                p.sendMessage(Msg.c("  &7Nivel:     &e" + st.getLevel()));
                p.sendMessage(Msg.c("  &6Coins:     &e" + st.getCoins()));
                p.sendMessage(Msg.c("  &7Kills:     &a" + st.getKills()));
                p.sendMessage(Msg.c("  &7Muertes:   &c" + st.getDeaths()));
                p.sendMessage(Msg.c("  &7KDR:       &f" + st.kdr()));
                p.sendMessage(Msg.c("  &7Victorias: &b" + st.getWins()));
                p.sendMessage(Msg.c("  &7Partidas:  &f" + st.getGames()));
                p.sendMessage(Msg.c("  &7Kit:       &e" + st.getKit()));
                p.sendMessage(Msg.c("  &7Clan:      &b" + (plugin.getClanManager().inClan(p)
                    ? plugin.getClanManager().getClan(p) : "Sin clan")));
                p.sendMessage(Msg.c("  &7Misiones:  " + plugin.getMissionManager().getMissionStatus(p)));
                p.sendMessage(Msg.sep());
            }

            case "top" -> {
                p.sendMessage(Msg.sep());
                p.sendMessage(Msg.c("  &6&lTOP 10 — AdvancedPaintball"));
                List<PlayerStats> top = plugin.getStatsManager().top(10);
                for (int i = 0; i < top.size(); i++) {
                    PlayerStats st = top.get(i);
                    String medal = switch (i) {
                        case 0  -> "&6&l#1 🥇";
                        case 1  -> "&7&l#2 🥈";
                        case 2  -> "&c&l#3 🥉";
                        default -> "&f#" + (i + 1);
                    };
                    p.sendMessage(Msg.c("  " + medal
                        + " &e" + st.getName()
                        + " &8│ &aKills: " + st.getKills()
                        + " &8│ &7KDR: " + st.kdr()
                        + " &8│ &bVictorias: " + st.getWins()));
                }
                p.sendMessage(Msg.sep());
            }

            case "kit", "tienda", "shop" -> {
                if (!plugin.getGameManager().inGame(p)) {
                    p.sendMessage(Msg.err("Debes estar en una arena para usar la tienda."));
                    return true;
                }
                plugin.getShopGUI().open(p);
            }

            case "particulas", "particles", "efectos" ->
                plugin.getParticleSelectorGUI().open(p);

            case "equipo", "team" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa equipo <mensaje>")); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getGameManager().teamChat(p, msg);
            }

            case "global", "g" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa global <mensaje>")); return true; }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                plugin.getGameManager().globalChat(p, msg);
            }

            // ── Clanes ────────────────────────────────────────────────────────
            case "clan" -> {
                if (args.length < 2) { showClanHelp(p); return true; }
                switch (args[1].toLowerCase()) {
                    case "crear" -> {
                        if (args.length < 4) { p.sendMessage(Msg.err("Uso: /pa clan crear <nombre> <tag>")); return true; }
                        plugin.getClanManager().createClan(p, args[2], args[3]);
                    }
                    case "disolver" -> plugin.getClanManager().disbandClan(p);
                    case "invitar"  -> {
                        if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa clan invitar <jugador>")); return true; }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) { p.sendMessage(Msg.err("Jugador no encontrado.")); return true; }
                        plugin.getClanManager().invitePlayer(p, target);
                    }
                    case "aceptar" -> plugin.getClanManager().acceptInvite(p);
                    case "salir"   -> plugin.getClanManager().leaveClam(p);
                    case "info"    -> plugin.getClanManager().showInfo(p);
                    case "chat"    -> {
                        if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa clan chat <mensaje>")); return true; }
                        String msg = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        plugin.getClanManager().clanChat(p, msg);
                    }
                    default -> showClanHelp(p);
                }
            }

            // ── Amigos ────────────────────────────────────────────────────────
            case "amigos", "friends" -> {
                if (args.length < 2) { showFriendHelp(p); return true; }
                switch (args[1].toLowerCase()) {
                    case "agregar", "add" -> {
                        if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa amigos agregar <jugador>")); return true; }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) { p.sendMessage(Msg.err("Jugador no encontrado.")); return true; }
                        plugin.getFriendManager().sendRequest(p, target);
                    }
                    case "aceptar", "accept" -> {
                        if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa amigos aceptar <jugador>")); return true; }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) { p.sendMessage(Msg.err("Jugador no encontrado.")); return true; }
                        plugin.getFriendManager().acceptRequest(p, target);
                    }
                    case "eliminar", "remove" -> {
                        if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa amigos eliminar <jugador>")); return true; }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) { p.sendMessage(Msg.err("Jugador no encontrado.")); return true; }
                        plugin.getFriendManager().removeFriend(p, target);
                    }
                    case "unirse", "join" -> {
                        if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa amigos unirse <jugador>")); return true; }
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target == null) { p.sendMessage(Msg.err("Jugador no encontrado.")); return true; }
                        plugin.getFriendManager().joinSameArena(p, target);
                    }
                    case "lista", "list" -> plugin.getFriendManager().showFriends(p);
                    default -> showFriendHelp(p);
                }
            }

            // ── Mensajes de kill ──────────────────────────────────────────────
            case "killmsg" -> {
                if (args.length < 2) {
                    p.sendMessage(Msg.info("Mensaje actual: &f"
                        + plugin.getKillMessageManager().getCustomMessage(p)));
                    p.sendMessage(Msg.info("Usa: &f/pa killmsg <mensaje> &7o &f/pa killmsg reset"));
                    p.sendMessage(Msg.info("Variables: &f{killer} &7y &f{victim}"));
                    return true;
                }
                if (args[1].equalsIgnoreCase("reset")) {
                    plugin.getKillMessageManager().removeCustomMessage(p);
                    p.sendMessage(Msg.ok("Mensaje de kill reseteado al del rango."));
                    return true;
                }
                String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                if (!msg.contains("{killer}") || !msg.contains("{victim}")) {
                    p.sendMessage(Msg.err("El mensaje debe contener &f{killer} &cy &f{victim}"));
                    return true;
                }
                plugin.getKillMessageManager().setCustomMessage(p, msg);
                p.sendMessage(Msg.ok("Mensaje de kill guardado: &f" + msg));
            }

            case "arenas", "lista" -> plugin.getArenaSelectorGUI().open(p);

            case "creator" -> {
                p.sendMessage(Msg.sep());
                p.sendMessage(Msg.c("  &b&lAdvancedPaintball &7v1.0.0"));
                p.sendMessage(Msg.c("  &7Creado por: &e&lSoyAdrianYT001"));
                p.sendMessage(Msg.c("  &7Minecraft:  &f1.21.1 Paper/Spigot"));
                p.sendMessage(Msg.c("  &7Tipo:       &aPremium &7✔"));
                p.sendMessage(Msg.c("  &74 equipos | Clanes | Amigos | MySQL"));
                p.sendMessage(Msg.sep());
            }

            default -> sendHelp(p);
        }
        return true;
    }

    private void sendHelp(Player p) {
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lAdvancedPaintball &7— Comandos"));
        p.sendMessage(Msg.c("  &f/pa &7— Menú principal"));
        p.sendMessage(Msg.c("  &f/pa arenas &7— Ver arenas"));
        p.sendMessage(Msg.c("  &f/pa unirse <arena> &7— Unirse"));
        p.sendMessage(Msg.c("  &f/pa salir &7— Salir de partida"));
        p.sendMessage(Msg.c("  &f/pa espectador <arena> &7— Observar"));
        p.sendMessage(Msg.c("  &f/pa stats [jugador] &7— Estadísticas"));
        p.sendMessage(Msg.c("  &f/pa top &7— Top 10"));
        p.sendMessage(Msg.c("  &f/pa kit &7— Tienda de kits"));
        p.sendMessage(Msg.c("  &f/pa particulas &7— Elegir partículas"));
        p.sendMessage(Msg.c("  &f/pa equipo <msg> &7— Chat de equipo"));
        p.sendMessage(Msg.c("  &f/pa global <msg> &7— Chat global"));
        p.sendMessage(Msg.c("  &f/pa clan &7— Sistema de clanes"));
        p.sendMessage(Msg.c("  &f/pa amigos &7— Sistema de amigos"));
        p.sendMessage(Msg.c("  &f/pa killmsg <msg> &7— Mensaje de kill"));
        p.sendMessage(Msg.c("  &f/pa creator &7— Créditos"));
        if (p.hasPermission("advancedpaintball.admin")) {
            p.sendMessage(Msg.c("  &c/pa register <arena> &7— Registrar arena"));
            p.sendMessage(Msg.c("  &c/pa unregister <arena> &7— Desregistrar"));
        }
        p.sendMessage(Msg.sep());
    }

    private void showClanHelp(Player p) {
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lClanes — Comandos"));
        p.sendMessage(Msg.c("  &f/pa clan crear <nombre> <tag> &7— Crear clan"));
        p.sendMessage(Msg.c("  &f/pa clan disolver &7— Disolver clan"));
        p.sendMessage(Msg.c("  &f/pa clan invitar <jugador> &7— Invitar"));
        p.sendMessage(Msg.c("  &f/pa clan aceptar &7— Aceptar invitación"));
        p.sendMessage(Msg.c("  &f/pa clan salir &7— Salir del clan"));
        p.sendMessage(Msg.c("  &f/pa clan info &7— Ver info del clan"));
        p.sendMessage(Msg.c("  &f/pa clan chat <msg> &7— Chat del clan"));
        p.sendMessage(Msg.sep());
    }

    private void showFriendHelp(Player p) {
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lAmigos — Comandos"));
        p.sendMessage(Msg.c("  &f/pa amigos agregar <jugador> &7— Enviar solicitud"));
        p.sendMessage(Msg.c("  &f/pa amigos aceptar <jugador> &7— Aceptar"));
        p.sendMessage(Msg.c("  &f/pa amigos eliminar <jugador> &7— Eliminar amigo"));
        p.sendMessage(Msg.c("  &f/pa amigos unirse <jugador> &7— Unirse a su arena"));
        p.sendMessage(Msg.c("  &f/pa amigos lista &7— Ver lista de amigos"));
        p.sendMessage(Msg.sep());
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c,
                                       String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            out.addAll(Arrays.asList(
                "unirse","salir","espectador","stats","top","kit",
                "particulas","equipo","global","clan","amigos",
                "killmsg","arenas","creator","register","unregister"
            ));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "unirse","espectador","register","unregister" ->
                    plugin.getArenaManager().all().forEach(a -> out.add(a.getName()));
                case "stats" ->
                    Bukkit.getOnlinePlayers().forEach(pl -> out.add(pl.getName()));
                case "clan" ->
                    out.addAll(Arrays.asList("crear","disolver","invitar","aceptar","salir","info","chat"));
                case "amigos","friends" ->
                    out.addAll(Arrays.asList("agregar","aceptar","eliminar","unirse","lista"));
                case "killmsg" -> out.add("reset");
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "clan" -> {
                    if (args[1].equalsIgnoreCase("invitar"))
                        Bukkit.getOnlinePlayers().forEach(pl -> out.add(pl.getName()));
                }
                case "amigos","friends" -> {
                    if (!args[1].equalsIgnoreCase("lista"))
                        Bukkit.getOnlinePlayers().forEach(pl -> out.add(pl.getName()));
                }
            }
        }
        out.removeIf(e -> !e.toLowerCase()
            .startsWith(args[args.length - 1].toLowerCase()));
        return out;
    }
}
