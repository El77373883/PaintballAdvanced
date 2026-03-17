package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.models.Game;
import com.soyadrianyt001.advancedpaintball.models.Game.Team;
import com.soyadrianyt001.advancedpaintball.utils.FireworkUtils;
import com.soyadrianyt001.advancedpaintball.utils.KitUtils;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final AdvancedPaintball plugin;
    private final Map<String, Game> games  = new HashMap<>();
    private final Map<UUID, String> pArena = new HashMap<>();

    public GameManager(AdvancedPaintball plugin) { this.plugin = plugin; }

    public boolean join(Player p, Arena arena) {
        if (pArena.containsKey(p.getUniqueId())) { p.sendMessage(Msg.err("Ya estás en una partida.")); return false; }
        Game game = games.computeIfAbsent(arena.getName(), k -> new Game(arena));
        if (game.getState() == Game.State.IN_GAME || game.getState() == Game.State.ENDING) { p.sendMessage(Msg.err("Partida en curso.")); return false; }
        if (game.totalActive() >= arena.getMaxPlayers()) { p.sendMessage(Msg.err("Arena llena.")); return false; }

        plugin.getInventoryBackupManager().backup(p);
        Team team = balanceTeam(game);
        game.addPlayer(p, team);
        pArena.put(p.getUniqueId(), arena.getName());
        prepare(p);
        if (arena.getLobby() != null) p.teleport(arena.getLobby());
        p.getInventory().addItem(new ItemStack(Material.SNOWBALL, 64));
        p.getInventory().addItem(new ItemStack(Material.SNOWBALL, 64));

        String tc = teamColor(team), tn = teamName(team);
        broadcast(game, Msg.prefix("&e" + p.getName() + " &7se unió al equipo " + tc + tn
            + " &8(&f" + game.totalActive() + "&8/&f" + arena.getMaxPlayers() + "&8)"));
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        plugin.getScoreboardManager().update(p, game);
        checkAutoStart(game);
        return true;
    }

    public void spectate(Player p, Arena arena) {
        if (pArena.containsKey(p.getUniqueId())) { p.sendMessage(Msg.err("Ya estás en una partida.")); return; }
        Game game = games.get(arena.getName());
        if (game == null || game.getState() != Game.State.IN_GAME) { p.sendMessage(Msg.err("No hay partida en curso.")); return; }
        plugin.getInventoryBackupManager().backup(p);
        game.addSpectator(p);
        pArena.put(p.getUniqueId(), arena.getName());
        prepare(p);
        p.setGameMode(GameMode.SPECTATOR);
        if (arena.getLobby() != null) p.teleport(arena.getLobby());
        p.sendMessage(Msg.ok("Estás observando &f" + arena.getName()));
    }

    public void leave(Player p) {
        String name = pArena.get(p.getUniqueId());
        if (name == null) { p.sendMessage(Msg.err("No estás en ninguna partida.")); return; }
        Game game = games.get(name);
        if (game == null) { pArena.remove(p.getUniqueId()); return; }
        game.removePlayer(p);
        pArena.remove(p.getUniqueId());
        plugin.getScoreboardManager().remove(p);
        plugin.getInventoryBackupManager().restore(p);
        broadcast(game, Msg.prefix("&e" + p.getName() + " &7abandonó la partida."));
        if (game.getState() == Game.State.STARTING && game.totalActive() < game.getArena().getMinPlayers()) {
            game.setState(Game.State.WAITING); cancelTask(game);
            broadcast(game, Msg.info("Jugadores insuficientes. Cancelado."));
        }
        if (game.totalActive() == 0) cleanup(game);
    }

    private void checkAutoStart(Game game) {
        if (game.getState() == Game.State.WAITING && game.totalActive() >= game.getArena().getMinPlayers())
            startCountdown(game);
    }

    private void startCountdown(Game game) {
        game.setState(Game.State.STARTING);
        game.setCountdown(10);
        broadcast(game, Msg.prefix("&aPartida encontrada! Comienza en &e10s"));
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int cd = game.getCountdown();
            if (game.totalActive() < game.getArena().getMinPlayers()) {
                game.setState(Game.State.WAITING); cancelTask(game);
                broadcast(game, Msg.info("Cancelado. Jugadores insuficientes.")); return;
            }
            game.getOnlinePlayers().forEach(pl -> {
                pl.sendTitle(Msg.c(cd <= 3 ? "&c&l" + cd : "&e&l" + cd),
                    Msg.c("&7¡Prepárate!"), 2, 18, 2);
                pl.playSound(pl.getLocation(), cd <= 3 ? Sound.BLOCK_NOTE_BLOCK_BASS
                    : Sound.BLOCK_NOTE_BLOCK_PLING, 1f, cd <= 3 ? 0.5f : 1f);
            });
            if (cd <= 0) { cancelTask(game); startGame(game); return; }
            game.setCountdown(cd - 1);
        }, 0L, 20L);
        game.setTaskId(t.getTaskId());
    }

    private void startGame(Game game) {
        game.setState(Game.State.IN_GAME);
        game.setTimeLeft(300);
        spawnAll(game);
        broadcast(game, Msg.sep());
        broadcast(game, Msg.c("    &b&l⚡ ¡PAINTBALL INICIADO!"));
        broadcast(game, Msg.c("    &c❤Rojo &8| &d✿Rosa &8| &a✦Verde &8| &e★Amarillo"));
        broadcast(game, Msg.c("    &7Kills para ganar: &e" + game.getArena().getKillsToWin()));
        broadcast(game, Msg.sep());
        game.getOnlinePlayers().forEach(pl -> {
            pl.sendTitle(Msg.c("&b&l¡PAINTBALL!"), Msg.c("&7¡A eliminar al enemigo!"), 10, 50, 10);
            pl.playSound(pl.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1f);
        });
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int tl = game.getTimeLeft();
            if (tl <= 0) { cancelTask(game); endGame(game, null); return; }
            if (tl == 60 || tl == 30 || tl == 10 || tl == 5)
                broadcast(game, Msg.info("&eQuedan &c" + tl + "s!"));
            game.setTimeLeft(tl - 1);
            if (tl % 2 == 0) game.getOnlinePlayers().forEach(pl ->
                plugin.getScoreboardManager().update(pl, game));
        }, 20L, 20L);
        game.setTaskId(t.getTaskId());
    }

    public void handleKill(Player killer, Player victim, Game game) {
        game.addKill(killer);
        game.addDeath(victim);
        game.resetStreak(victim);
        game.addTeamScore(game.getTeam(killer));
        plugin.getStatsManager().get(killer).addKill();
        plugin.getStatsManager().get(victim).addDeath();

        int streak = game.getStreak(killer);
        int streakNeeded = plugin.getConfig().getInt("streak-kills", 3);
        int coinsHit = plugin.getConfig().getInt("coins-hit", 3);
        int multiplier = streak >= streakNeeded ? plugin.getConfig().getInt("streak-multiplier", 2) : 1;
        int earned = coinsHit * multiplier;

        plugin.getStatsManager().get(killer).addCoins(earned);
        plugin.getRankManager().addXp(killer, 10);
        plugin.getMissionManager().onKill(killer);

        // Rayo visual
        victim.getWorld().strikeLightningEffect(victim.getLocation());

        // Partículas de pintura
        Color paintColor = switch (game.getTeam(killer)) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };
        victim.getWorld().spawnParticle(Particle.DUST,
            victim.getLocation().add(0, 1, 0), 60, 0.5, 0.8, 0.5,
            new Particle.DustOptions(paintColor, 2.5f));
        victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.2f);

        String streakMsg = streak >= streakNeeded ? Msg.c(" &6&l[RACHA x" + streak + "! x" + multiplier + " coins]") : "";
        broadcast(game, Msg.c(teamColor(game.getTeam(killer)) + "⚡ " + killer.getName()
            + " &7eliminó a &e" + victim.getName() + streakMsg
            + "  &8│ &c" + game.getScoreRed() + " &d" + game.getScorePink()
            + " &a" + game.getScoreGreen() + " &e" + game.getScoreYellow()));

        killer.sendMessage(Msg.ok("&6+" + earned + " coins" + (multiplier > 1 ? " &7(Racha!)" : "")));
        killer.sendTitle(Msg.c("&a+1 Kill!"), Msg.c("&e+" + earned + " coins"), 5, 20, 5);
        killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);
        victim.sendTitle(Msg.c("&c&lEliminado!"), Msg.c("&7Reapareciendo en 3s..."), 5, 40, 10);

        // Zona segura 3s al respawnear
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!pArena.containsKey(victim.getUniqueId())) return;
            Team vt = game.getTeam(victim);
            if (vt == Team.SPECTATOR) return;
            List<Location> spawns = game.getArena().getSpawnsByTeam(vt);
            if (spawns.isEmpty()) return;
            Location spawn = spawns.get(new Random().nextInt(spawns.size()));
            victim.teleport(spawn);
            victim.setHealth(20); victim.setFoodLevel(20);
            KitUtils.give(victim, vt, plugin.getStatsManager().get(victim).getKit());
            game.addSafe(victim);
            victim.sendTitle(Msg.c("&a✔ Reapareciste"), Msg.c("&7Zona segura por 3s"), 5, 20, 5);
            Bukkit.getScheduler().runTaskLater(plugin, () -> game.removeSafe(victim), 60L);
            plugin.getScoreboardManager().update(victim, game);
        }, 60L);

        int needed = game.getArena().getKillsToWin();
        for (Team t : new Team[]{Team.RED, Team.PINK, Team.GREEN, Team.YELLOW}) {
            if (needed > 0 && game.getTeamScore(t) >= needed) { cancelTask(game); endGame(game, t); return; }
        }
        game.getOnlinePlayers().forEach(pl -> plugin.getScoreboardManager().update(pl, game));
    }

    public void endGame(Game game, Team forceWinner) {
        if (game.getState() == Game.State.ENDING) return;
        game.setState(Game.State.ENDING);
        cancelTask(game);

        Team winner = forceWinner != null ? forceWinner : game.getWinnerByScore();
        String winColor = winner != null ? teamColor(winner) : "&e";
        String winName  = winner != null ? teamName(winner) : "EMPATE";
        String winTitle = winColor + "&l🏆 " + winName + " GANA!";

        if (winner != null) {
            game.getTeamPlayers(winner).forEach(uid -> {
                Player pl = Bukkit.getPlayer(uid);
                if (pl != null) {
                    plugin.getStatsManager().get(pl).addWin();
                    plugin.getStatsManager().get(pl).addCoins(plugin.getConfig().getInt("coins-win", 50));
                    plugin.getMissionManager().onWin(pl);
                    plugin.getRankManager().addXp(pl, 50);
                    // Fuegos artificiales para ganadores
                    for (int i = 0; i < 5; i++) {
                        final int delay = i * 10;
                        Bukkit.getScheduler().runTaskLater(plugin, () ->
                            FireworkUtils.launchTeam(pl.getLocation(), winner), delay);
                    }
                }
            });
        }

        UUID mvpUid = game.getMVP();
        String mvpName = mvpUid != null && Bukkit.getPlayer(mvpUid) != null
            ? Bukkit.getPlayer(mvpUid).getName() : "N/A";

        broadcast(game, Msg.sep());
        broadcast(game, Msg.c("  " + winTitle));
        broadcast(game, Msg.c("  &c❤ Rojo: &f" + game.getScoreRed() + "  &d✿ Rosa: &f" + game.getScorePink()));
        broadcast(game, Msg.c("  &a✦ Verde: &f" + game.getScoreGreen() + "  &e★ Amarillo: &f" + game.getScoreYellow()));
        broadcast(game, Msg.c("  &6⭐ MVP: &e" + mvpName));
        broadcast(game, Msg.sep());

        // Titulo animado parpadeante
        final Team fw = winner;
        final String[] colors = {"&c", "&e", "&a", "&b", "&d", "&6"};
        final int[] frame = {0};
        BukkitTask titleAnim = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String col = colors[frame[0] % colors.length];
            frame[0]++;
            game.getOnlinePlayers().forEach(pl ->
                pl.sendTitle(Msg.c(col + "&l" + (fw != null ? teamName(fw) + " GANA!" : "¡EMPATE!")),
                    Msg.c("&7¡Gracias por jugar!"), 0, 25, 0));
        }, 0L, 10L);

        Bukkit.getScheduler().runTaskLater(plugin, () -> titleAnim.cancel(), 80L);

        game.getOnlinePlayers().forEach(pl -> {
            plugin.getStatsManager().get(pl).addGame();
            pl.playSound(pl.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            plugin.getScoreboardManager().remove(pl);
        });
        plugin.getStatsManager().saveAll();

        // Revancha
        broadcast(game, Msg.info("&e¿Revancha? Usa &f/pa unirse " + game.getArena().getName()));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location mainLobby = getMainLobby();
            new HashSet<>(game.getPlayers().keySet()).forEach(uid -> {
                Player pl = Bukkit.getPlayer(uid);
                if (pl != null) {
                    // Limpiar inventario (no se llevan nada)
                    pl.getInventory().clear();
                    plugin.getInventoryBackupManager().restore(pl);
                    if (mainLobby != null) pl.teleport(mainLobby);
                    pl.setGameMode(GameMode.SURVIVAL);
                }
                pArena.remove(uid);
            });
            cleanup(game);
        }, 120L);
    }

    public void stopAll() { new ArrayList<>(games.values()).forEach(g -> endGame(g, null)); }

    private void spawnAll(Game game) {
        for (Team t : new Team[]{Team.RED, Team.PINK, Team.GREEN, Team.YELLOW}) {
            List<UUID> members = game.getTeamPlayers(t);
            List<Location> spawns = game.getArena().getSpawnsByTeam(t);
            if (spawns.isEmpty()) continue;
            for (int i = 0; i < members.size(); i++) {
                Player pl = Bukkit.getPlayer(members.get(i));
                if (pl == null) continue;
                pl.teleport(spawns.get(i % spawns.size()));
                pl.setHealth(20); pl.setFoodLevel(20);
                pl.setGameMode(GameMode.SURVIVAL);
                KitUtils.give(pl, t, plugin.getStatsManager().get(pl).getKit());
                plugin.getScoreboardManager().update(pl, game);
            }
        }
    }

    private Team balanceTeam(Game game) {
        Team best = Team.RED; int min = Integer.MAX_VALUE;
        for (Team t : new Team[]{Team.RED, Team.PINK, Team.GREEN, Team.YELLOW}) {
            int size = game.getTeamPlayers(t).size();
            if (size < min) { min = size; best = t; }
        }
        return best;
    }

    private void broadcast(Game game, String msg) {
        game.getPlayers().keySet().forEach(uid -> {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) pl.sendMessage(msg);
        });
    }

    public void teamChat(Player p, String message) {
        Game game = getGame(p);
        if (game == null) { p.sendMessage(Msg.err("No estás en ninguna partida.")); return; }
        Team team = game.getTeam(p);
        String tc = teamColor(team), tn = teamName(team);
        String msg = Msg.c(tc + "[" + tn + "] &7" + p.getName() + ": &f" + message);
        game.getTeamPlayers(team).forEach(uid -> {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) pl.sendMessage(msg);
        });
    }

    private void prepare(Player p) {
        p.getInventory().clear(); p.setHealth(20); p.setFoodLevel(20);
        p.setGameMode(GameMode.ADVENTURE); p.setFireTicks(0); p.setLevel(0); p.setExp(0f);
        p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
    }

    public void restore(Player p) {
        p.getInventory().clear(); p.setHealth(20); p.setFoodLevel(20);
        p.setGameMode(GameMode.SURVIVAL); p.setFireTicks(0); p.setLevel(0); p.setExp(0f);
        p.getActivePotionEffects().forEach(e -> p.removePotionEffect(e.getType()));
    }

    private void cancelTask(Game game) {
        if (game.getTaskId() != -1) { Bukkit.getScheduler().cancelTask(game.getTaskId()); game.setTaskId(-1); }
    }

    private void cleanup(Game game) { games.remove(game.getArena().getName()); game.getArena().setState(Arena.State.WAITING); }

    private Location getMainLobby() {
        var cfg = plugin.getConfig();
        if (!cfg.contains("main-lobby")) return null;
        World w = Bukkit.getWorld(cfg.getString("main-lobby.world", "world"));
        if (w == null) return null;
        return new Location(w, cfg.getDouble("main-lobby.x"), cfg.getDouble("main-lobby.y"),
            cfg.getDouble("main-lobby.z"), (float)cfg.getDouble("main-lobby.yaw"), (float)cfg.getDouble("main-lobby.pitch"));
    }

    public String teamColor(Team t) { return switch(t) { case RED->"&c"; case PINK->"&d"; case GREEN->"&a"; case YELLOW->"&e"; default->"&7"; }; }
    public String teamName(Team t)  { return switch(t) { case RED->"Rojo"; case PINK->"Rosa"; case GREEN->"Verde"; case YELLOW->"Amarillo"; default->"Espectador"; }; }

    public Game   getGame(Player p)          { String n=pArena.get(p.getUniqueId()); return n!=null?games.get(n):null; }
    public Game   getGameByArena(String n)   { return games.get(n); }
    public boolean inGame(Player p)          { return pArena.containsKey(p.getUniqueId()); }
    public Map<String, Game> all()           { return games; }
}
