package com.soyadrianyt001.advancedpaintball.commands;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class PAAdminCommand implements CommandExecutor, TabCompleter {

    private final AdvancedPaintball plugin;

    public PAAdminCommand(AdvancedPaintball plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!s.hasPermission("advancedpaintball.admin")) { s.sendMessage(Msg.err("Sin permiso.")); return true; }
        if (!(s instanceof Player p)) { s.sendMessage("Solo jugadores."); return true; }

        if (args.length == 0) { sendAdminHelp(p); return true; }

        switch (args[0].toLowerCase()) {
            case "panel" -> plugin.getAdminPanelGUI().openMain(p);
            case "wand" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa-admin wand <arena>")); return true; }
                if (!plugin.getArenaManager().exists(args[1])) { p.sendMessage(Msg.err("Arena no existe.")); return true; }
                plugin.getWandManager().give(p, args[1]);
            }
            case "crear", "create" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa-admin crear <nombre>")); return true; }
                if (plugin.getArenaManager().exists(args[1])) { p.sendMessage(Msg.err("Esa arena ya existe.")); return true; }
                Arena a = plugin.getArenaManager().create(args[1]);
                plugin.getArenaManager().save(a);
                p.sendMessage(Msg.ok("Arena &f" + args[1] + " &acreada. Usa &f/pa-admin panel &apara configurarla."));
            }
            case "eliminar", "delete" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa-admin eliminar <nombre>")); return true; }
                if (!plugin.getArenaManager().exists(args[1])) { p.sendMessage(Msg.err("Arena no existe.")); return true; }
                plugin.getArenaManager().delete(args[1]);
                p.sendMessage(Msg.ok("Arena &f" + args[1] + " &aeliminada."));
            }
            case "setlobby" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa-admin setlobby <arena>")); return true; }
                Arena a = plugin.getArenaManager().get(args[1]);
                if (a == null) { p.sendMessage(Msg.err("Arena no existe.")); return true; }
                a.setLobby(p.getLocation());
                plugin.getArenaManager().save(a);
                p.sendMessage(Msg.ok("Lobby de &f" + args[1] + " &aguardado."));
            }
            case "setspawn" -> {
                if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa-admin setspawn <arena> <rojo|rosa|verde|amarillo>")); return true; }
                Arena a = plugin.getArenaManager().get(args[1]);
                if (a == null) { p.sendMessage(Msg.err("Arena no existe.")); return true; }
                switch (args[2].toLowerCase()) {
                    case "rojo"     -> { a.getSpawnsRed().add(p.getLocation());    p.sendMessage(Msg.ok("Spawn Rojo #" + a.getSpawnsRed().size() + " añadido.")); }
                    case "rosa"     -> { a.getSpawnsPink().add(p.getLocation());   p.sendMessage(Msg.ok("Spawn Rosa #" + a.getSpawnsPink().size() + " añadido.")); }
                    case "verde"    -> { a.getSpawnsGreen().add(p.getLocation());  p.sendMessage(Msg.ok("Spawn Verde #" + a.getSpawnsGreen().size() + " añadido.")); }
                    case "amarillo" -> { a.getSpawnsYellow().add(p.getLocation()); p.sendMessage(Msg.ok("Spawn Amarillo #" + a.getSpawnsYellow().size() + " añadido.")); }
                    default -> p.sendMessage(Msg.err("Equipos válidos: rojo, rosa, verde, amarillo"));
                }
                plugin.getArenaManager().save(a);
            }
            case "setlobby-principal" -> {
                plugin.getConfig().set("main-lobby.world", p.getWorld().getName());
                plugin.getConfig().set("main-lobby.x", p.getLocation().getX());
                plugin.getConfig().set("main-lobby.y", p.getLocation().getY());
                plugin.getConfig().set("main-lobby.z", p.getLocation().getZ());
                plugin.getConfig().set("main-lobby.yaw", (double) p.getLocation().getYaw());
                plugin.getConfig().set("main-lobby.pitch", (double) p.getLocation().getPitch());
                plugin.saveConfig();
                p.sendMessage(Msg.ok("Lobby principal guardado."));
            }
            case "info" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa-admin info <arena>")); return true; }
                Arena a = plugin.getArenaManager().get(args[1]);
                if (a == null) { p.sendMessage(Msg.err("Arena no existe.")); return true; }
                p.sendMessage(Msg.sep());
                p.sendMessage(Msg.c("  &b&lArena: &f" + a.getName()));
                p.sendMessage(Msg.c("  &7Lobby:     " + (a.getLobby()!=null?"&a✔":"&c✗")));
                p.sendMessage(Msg.c("  &7SpawnsRojo:  &f" + a.getSpawnsRed().size()));
                p.sendMessage(Msg.c("  &7SpawnsRosa:  &f" + a.getSpawnsPink().size()));
                p.sendMessage(Msg.c("  &7SpawnsVerde: &f" + a.getSpawnsGreen().size()));
                p.sendMessage(Msg.c("  &7SpawnsAmar.: &f" + a.getSpawnsYellow().size()));
                p.sendMessage(Msg.c("  &7Min/Max:   &f" + a.getMinPlayers() + "&7/&f" + a.getMaxPlayers()));
                p.sendMessage(Msg.c("  &7Estado:    " + (a.isReady()?"&aLista ✔":"&cIncompleta ✗")));
                p.sendMessage(Msg.sep());
            }
            case "npc" -> {
                if (args.length < 2) { p.sendMessage(Msg.err("Uso: /pa-admin npc <crear|eliminar> [arena]")); return true; }
                if (args[1].equalsIgnoreCase("crear")) {
                    if (args.length < 3) { p.sendMessage(Msg.err("Uso: /pa-admin npc crear <arena>")); return true; }
                    plugin.getShopNPC().create(p, args[2]);
                } else if (args[1].equalsIgnoreCase("eliminar")) {
                    plugin.getShopNPC().removeNearest(p);
                }
            }
            case "reload" -> { plugin.reloadConfig(); p.sendMessage(Msg.ok("Config recargada.")); }
            default -> sendAdminHelp(p);
        }
        return true;
    }

    private void sendAdminHelp(Player p) {
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &c&lAdmin — AdvancedPaintball"));
        p.sendMessage(Msg.c("  &f/pa-admin panel &7— Panel GUI"));
        p.sendMessage(Msg.c("  &f/pa-admin wand <arena> &7— Varita Pos1/Pos2"));
        p.sendMessage(Msg.c("  &f/pa-admin crear <nombre> &7— Crear arena"));
        p.sendMessage(Msg.c("  &f/pa-admin eliminar <nombre> &7— Eliminar"));
        p.sendMessage(Msg.c("  &f/pa-admin setlobby <arena> &7— Lobby arena"));
        p.sendMessage(Msg.c("  &f/pa-admin setspawn <arena> <equipo> &7— Spawn"));
        p.sendMessage(Msg.c("  &f/pa-admin setlobby-principal &7— Lobby servidor"));
        p.sendMessage(Msg.c("  &f/pa-admin info <arena> &7— Info de arena"));
        p.sendMessage(Msg.c("  &f/pa-admin npc crear <arena> &7— Crear NPC"));
        p.sendMessage(Msg.c("  &f/pa-admin npc eliminar &7— Eliminar NPC"));
        p.sendMessage(Msg.c("  &f/pa-admin reload &7— Recargar config"));
        p.sendMessage(Msg.sep());
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command c, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) out.addAll(Arrays.asList("panel","wand","crear","eliminar","setlobby","setspawn","setlobby-principal","info","npc","reload"));
        else if (args.length == 2 && !args[0].equalsIgnoreCase("npc")) plugin.getArenaManager().all().forEach(a -> out.add(a.getName()));
        else if (args.length == 2 && args[0].equalsIgnoreCase("npc")) out.addAll(Arrays.asList("crear","eliminar"));
        else if (args.length == 3 && args[0].equalsIgnoreCase("setspawn")) out.addAll(Arrays.asList("rojo","rosa","verde","amarillo"));
        else if (args.length == 3 && args[0].equalsIgnoreCase("npc") && args[1].equalsIgnoreCase("crear")) plugin.getArenaManager().all().forEach(a -> out.add(a.getName()));
        out.removeIf(e -> !e.toLowerCase().startsWith(args[args.length-1].toLowerCase()));
        return out;
    }
}
