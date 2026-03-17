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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final AdvancedPaintball plugin;
    private final Map<String, Game> games  = new HashMap<>();
    private final Map<UUID, String> pArena = new HashMap<>();

    public GameManager(AdvancedPaintball plugin) { this.plugin = plugin; }

    // ── Items de lobby ────────────────────────────────────────────────────────

    public ItemStack getLeaveItem() {
        ItemStack item = new ItemStack(Material.RED_BED);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(Msg.c("&c&l✗ Salir de la Arena"));
        meta.setLore(Arrays.asList(
            Msg.c("&7Click para salir"),
            Msg.c("&7Tu inventario será devuelto"),
            Msg.c("&7Serás llevado al lobby")
        ));
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getTeamItem() {
        ItemStack item = new ItemStack(Material.WHITE_WOOL);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(Msg.c("&e&l⚑ Elegir Equipo"));
        meta.setLore(Arrays.asList(
            Msg.c("&7Click para cambiar de equipo"),
            Msg.c("&8&m──────────────────"),
            Msg.c("&c❤ Rojo    &d✿ Rosa"),
            Msg.c("&a✦ Verde   &e★ Amarillo"),
            Msg.c("&8&m──────────────────"),
            Msg.c("&7Se equipará armadura del color")
        ));
        item.setItemMeta(meta);
        return item;
    }

    private void giveLobbyItems(Player p) {
        p.getInventory().setItem(7, getTeamItem());
        p.getInventory().setItem(8, getLeaveItem());
    }

    // ── JOIN ──────────────────────────────────────────────────────────────────

    public boolean join(Player p, Arena arena) {
        if (pArena.containsKey(p.getUniqueId())) {
            p.sendMessage(Msg.err("Ya estás en una partida. Usa el ítem rojo para salir."));
            return false;
        }
        Game game = games.computeIfAbsent(arena.getName(), k -> new Game(arena));

        if (game.getState() == Game.State.IN_GAME || game.getState() == Game.State.ENDING) {
            p.sendMessage(Msg.err("Esa partida ya está en curso."));
            return false;
        }
        if (game.totalActive() >= arena.getMaxPlayers()) {
            p.sendMessage(Msg.err("La arena está llena."));
            return false;
        }

        // Guardar inventario original
        plugin.getInventoryBackupManager().backup(p);

        Team team = balanceTeam(game);
        game.addPlayer(p, team);
        pArena.put(p.getUniqueId(), arena.getName());

        // Preparar jugador
        prepare(p);

        // Teleportar al lobby de la arena
        if (arena.getLobby() != null) p.teleport(arena.getLobby());

        // Dar 2 stacks de snowballs
        p.getInventory().addItem(new ItemStack(Material.SNOWBALL, 64));
        p.getInventory().addItem(new ItemStack(Material.SNOWBALL, 64));

        // Dar items de lobby en slots 7 y 8
        giveLobbyItems(p);

        // Equipar armadura del equipo automáticamente
        equipTeamArmor(p, team);

        String tc = teamColor(team);
        String tn = teamName(team);

        // Mensaje de bienvenida
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lAdvancedPaintball &8▸ &f" + arena.getName()));
        p.sendMessage(Msg.c("  &7Equipo asignado: " + tc + "&l" + tn));
        p.sendMessage(Msg.c("  &7Slot &f8 &7= &e⚑ Elegir equipo"));
        p.sendMessage(Msg.c("  &7Slot &f9 &7= &c✗ Salir de la arena"));
        p.sendMessage(Msg.sep());

        // Título de bienvenida
        p.sendTitle(
            Msg.c("&b&lAdvancedPaintball"),
            Msg.c(tc + "Equipo " + tn + " &8| &7Esperando jugadores..."),
            10, 60, 10
        );
        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        broadcast(game, Msg.prefix("&e" + p.getName()
            + " &7se unió " + tc + "[" + tn + "]"
            + " &8(&f" + game.totalActive() + "&8/&f" + arena.getMaxPlayers() + "&8)"));

        plugin.getScoreboardManager().update(p, game);
        checkAutoStart(game);
        return true;
    }

    // ── SPECTATE ──────────────────────────────────────────────────────────────

    public void spectate(Player p, Arena arena) {
        if (pArena.containsKey(p.getUniqueId())) {
            p.sendMessage(Msg.err("Ya estás en una partida."));
            return;
        }
        Game game = games.get(arena.getName());
        if (game == null || game.getState() != Game.State.IN_GAME) {
            p.sendMessage(Msg.err("No hay partida en curso en esa arena."));
            return;
        }
        plugin.getInventoryBackupManager().backup(p);
        game.addSpectator(p);
        pArena.put(p.getUniqueId(), arena.getName());
        prepare(p);
        p.setGameMode(GameMode.SPECTATOR);
        if (arena.getLobby() != null) p.teleport(arena.getLobby());
        p.getInventory().setItem(8, getLeaveItem());
        p.sendMessage(Msg.ok("Estás observando &f" + arena.getName()));
        p.sendTitle(Msg.c("&7Modo Espectador"),
            Msg.c("&f" + arena.getName()), 10, 40, 10);
    }

    // ── LEAVE ─────────────────────────────────────────────────────────────────

    public void leave(Player p) {
        String name = pArena.get(p.getUniqueId());
        if (name == null) {
            p.sendMessage(Msg.err("No estás en ninguna partida."));
            return;
        }
        Game game = games.get(name);
        if (game == null) { pArena.remove(p.getUniqueId()); return; }

        boolean wasInGame = game.getState() == Game.State.IN_GAME;
        game.removePlayer(p);
        pArena.remove(p.getUniqueId());
        plugin.getScoreboardManager().remove(p);

        // Devolver inventario
        plugin.getInventoryBackupManager().restore(p);
        restore(p);

        // Teleportar al lobby principal
        Location mainLobby = getMainLobby();
        if (mainLobby != null) p.teleport(mainLobby);

        p.sendMessage(Msg.ok("Saliste de la arena. &7Tu inventario fue devuelto."));
        p.sendTitle(Msg.c("&7Saliste"),
            Msg.c("&fHasta la próxima!"), 5, 40, 10);

        broadcast(game, Msg.prefix("&e" + p.getName() + " &7abandonó la partida."));

        // Si estaba en partida y queda 1 equipo ese gana
        if (wasInGame) {
            Team remaining = getRemainingTeam(game);
            if (remaining != null) {
                endGame(game, remaining);
                return;
            }
        }

        // Si en lobby y quedan pocos jugadores
        if (game.getState() == Game.State.STARTING
                && game.totalActive() < game.getArena().getMinPlayers()) {
            game.setState(Game.State.WAITING);
            cancelTask(game);
            broadcast(game, Msg.info("Jugadores insuficientes. Esperando más..."));
            game.getOnlinePlayers().forEach(pl -> {
                giveLobbyItems(pl);
                pl.sendTitle(
                    Msg.c("&e&lEsperando jugadores"),
                    Msg.c("&7Necesitas &f" + game.getArena().getMinPlayers()
                        + " &7jugadores mínimo"),
                    5, 60, 10);
            });
        }

        if (game.totalActive() == 0) cleanup(game);
    }

    // ── CHANGE TEAM ───────────────────────────────────────────────────────────

    public void changeTeam(Player p) {
        String name = pArena.get(p.getUniqueId());
        if (name == null) { p.sendMessage(Msg.err("No estás en ninguna partida.")); return; }
        Game game = games.get(name);
        if (game == null) return;
        if (game.getState() == Game.State.IN_GAME) {
            p.sendMessage(Msg.err("No puedes cambiar de equipo durante la partida."));
            return;
        }
        plugin.getTeamSelectorGUI().open(p, game);
    }

    // ── AUTO START CHECK ──────────────────────────────────────────────────────

    private void checkAutoStart(Game game) {
        if (game.getState() == Game.State.WAITING
                && game.totalActive() >= game.getArena().getMinPlayers()) {
            startCountdown(game);
        }
    }

    // ── COUNTDOWN ─────────────────────────────────────────────────────────────

    private void startCountdown(Game game) {
        game.setState(Game.State.STARTING);
        game.setCountdown(10);

        int active  = game.totalActive();
        String modo = active <= 2 ? "1v1"
                    : active <= 4 ? "2v2"
                    : active <= 6 ? "3v3" : "4v4";

        broadcast(game, Msg.sep());
        broadcast(game, Msg.c("  &b&lPartida encontrada! Modo: &e" + modo));
        broadcast(game, Msg.c("  &7Comienza en &e10 segundos&7!"));
        broadcast(game, Msg.sep());

        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int cd = game.getCountdown();

            if (game.totalActive() < game.getArena().getMinPlayers()) {
                game.setState(Game.State.WAITING);
                cancelTask(game);
                broadcast(game, Msg.info("Cancelado. Esperando más jugadores..."));
                game.getOnlinePlayers().forEach(pl -> {
                    giveLobbyItems(pl);
                    pl.sendTitle(
                        Msg.c("&c&lCancelado"),
                        Msg.c("&7Esperando más jugadores..."),
                        5, 50, 10);
                    pl.playSound(pl.getLocation(),
                        Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                });
                return;
            }

            game.getOnlinePlayers().forEach(pl -> {
                String color = cd <= 3 ? "&c&l"
                             : cd <= 6 ? "&e&l" : "&a&l";
                pl.sendTitle(
                    Msg.c(color + cd),
                    Msg.c("&7¡La partida está por comenzar!"),
                    2, 18, 2);
                pl.playSound(pl.getLocation(),
                    cd <= 3 ? Sound.BLOCK_NOTE_BLOCK_BASS
                            : Sound.BLOCK_NOTE_BLOCK_PLING,
                    1f, cd <= 3 ? 0.5f : 1.2f);
            });

            if (cd <= 0) { cancelTask(game); startGame(game); return; }
            game.setCountdown(cd - 1);
        }, 0L, 20L);
        game.setTaskId(t.getTaskId());
    }

    // ── START GAME ────────────────────────────────────────────────────────────

    private void startGame(Game game) {
        game.setState(Game.State.IN_GAME);
        game.setTimeLeft(300);
        spawnAll(game);

        broadcast(game, Msg.sep());
        broadcast(game, Msg.c("    &b&l⚡ ¡PAINTBALL INICIADO!"));
        broadcast(game, Msg.c("    &c❤Rojo &8| &d✿Rosa &8| &a✦Verde &8| &e★Amarillo"));
        broadcast(game, Msg.c("    &7Kills para ganar: &e"
            + game.getArena().getKillsToWin()));
        broadcast(game, Msg.c("    &7Tiempo: &e5 minutos"));
        broadcast(game, Msg.sep());

        game.getOnlinePlayers().forEach(pl -> {
            pl.sendTitle(
                Msg.c("&b&l¡PAINTBALL!"),
                Msg.c("&7¡A eliminar al equipo contrario!"),
                10, 50, 10);
            pl.playSound(pl.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1f);
            pl.playSound(pl.getLocation(),
                Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1f);
        });

        // Timer de partida
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            int tl = game.getTimeLeft();
            if (tl <= 0) { cancelTask(game); endGame(game, null); return; }
            if (tl == 60 || tl == 30 || tl == 10 || tl == 5) {
                broadcast(game, Msg.info("&eQuedan &c" + tl + "s &ede partida!"));
                if (tl <= 10) {
                    game.getOnlinePlayers().forEach(pl ->
                        pl.playSound(pl.getLocation(),
                            Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f));
                }
            }
            game.setTimeLeft(tl - 1);
            if (tl % 2 == 0) {
                game.getOnlinePlayers().forEach(pl ->
                    plugin.getScoreboardManager().update(pl, game));
            }
        }, 20L, 20L);
        game.setTaskId(t.getTaskId());
    }

    // ── HANDLE KILL ───────────────────────────────────────────────────────────

    public void handleKill(Player killer, Player victim, Game game) {
        game.addKill(killer);
        game.addDeath(victim);
        game.resetStreak(victim);
        game.addTeamScore(game.getTeam(killer));

        plugin.getStatsManager().get(killer).addKill();
        plugin.getStatsManager().get(victim).addDeath();

        int streak       = game.getStreak(killer);
        int streakNeeded = plugin.getConfig().getInt("streak-kills", 3);
        int coinsHit     = plugin.getConfig().getInt("coins-hit", 3);
        int multiplier   = streak >= streakNeeded
            ? plugin.getConfig().getInt("streak-multiplier", 2) : 1;
        int earned       = coinsHit * multiplier;

        plugin.getStatsManager().get(killer).addCoins(earned);
        plugin.getRankManager().addXp(killer, 10);
        plugin.getMissionManager().onKill(killer);

        // ⚡ Trueno donde murió el jugador
        victim.getWorld().strikeLightningEffect(victim.getLocation());
        victim.getWorld().playSound(victim.getLocation(),
            Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f);

        // Partículas personalizadas del killer
        plugin.getParticleSelectorGUI()
            .spawnKillParticle(killer, victim.getLocation());

        // Partículas de pintura del equipo asesino
        Color paintColor = switch (game.getTeam(killer)) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };
        victim.getWorld().spawnParticle(Particle.DUST,
            victim.getLocation().add(0, 1, 0), 80,
            0.5, 0.8, 0.5,
            new Particle.DustOptions(paintColor, 3f));

        // Mensajes
        String streakMsg = streak >= streakNeeded
            ? Msg.c(" &6&l[RACHA x" + streak + "! x" + multiplier + " coins]") : "";

        broadcast(game, Msg.c(teamColor(game.getTeam(killer))
            + "⚡ " + killer.getName()
            + " &7eliminó a &e" + victim.getName() + streakMsg
            + "  &8│ &c" + game.getScoreRed()
            + " &d" + game.getScorePink()
            + " &a" + game.getScoreGreen()
            + " &e" + game.getScoreYellow()));

        killer.sendMessage(Msg.ok("&6+" + earned + " coins"
            + (multiplier > 1 ? " &7(¡Racha x" + streak + "!)" : "")));
        killer.sendTitle(
            Msg.c("&a&l+1 Kill!"),
            Msg.c("&e+" + earned + " coins"),
            5, 20, 5);
        killer.playSound(killer.getLocation(),
            Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.5f);

        victim.sendTitle(
            Msg.c("&c&l⚡ Eliminado!"),
            Msg.c("&7Reapareciendo en 3 segundos..."),
            5, 50, 10);
        victim.playSound(victim.getLocation(),
            Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1f, 0.8f);

        // Respawn en 3s con zona segura
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!pArena.containsKey(victim.getUniqueId())) return;
            Team vt = game.getTeam(victim);
            if (vt == Team.SPECTATOR) return;
            List<Location> spawns = game.getArena().getSpawnsByTeam(vt);
            if (spawns.isEmpty()) return;
            Location spawn = spawns.get(new Random().nextInt(spawns.size()));
            victim.teleport(spawn);
            victim.setHealth(20);
            victim.setFoodLevel(20);
            KitUtils.give(victim, vt,
                plugin.getStatsManager().get(victim).getKit());
            game.addSafe(victim);
            victim.sendTitle(
                Msg.c("&a✔ Reapareciste"),
                Msg.c("&7Zona segura por 3 segundos"),
                5, 20, 5);
            Bukkit.getScheduler().runTaskLater(plugin,
                () -> game.removeSafe(victim), 60L);
            plugin.getScoreboardManager().update(victim, game);
        }, 60L);

        // Verificar condición de victoria
        int needed = game.getArena().getKillsToWin();
        for (Team t : new Team[]{Team.RED, Team.PINK, Team.GREEN, Team.YELLOW}) {
            if (needed > 0 && game.getTeamScore(t) >= needed) {
                cancelTask(game);
                endGame(game, t);
                return;
            }
        }
        game.getOnlinePlayers().forEach(pl ->
            plugin.getScoreboardManager().update(pl, game));
    }

    // ── END GAME ──────────────────────────────────────────────────────────────

    public void endGame(Game game, Team forceWinner) {
        if (game.getState() == Game.State.ENDING) return;
        game.setState(Game.State.ENDING);
        cancelTask(game);

        Team winner    = forceWinner != null ? forceWinner : game.getWinnerByScore();
        String winColor = winner != null ? teamColor(winner) : "&e";
        String winName  = winner != null ? teamName(winner) : "EMPATE";

        // MVP
        UUID mvpUid    = game.getMVP();
        String mvpName = "N/A";
        if (mvpUid != null) {
            Player mvpPl = Bukkit.getPlayer(mvpUid);
            if (mvpPl != null) mvpName = mvpPl.getName();
        }

        broadcast(game, Msg.sep());
        broadcast(game, Msg.c("  " + winColor + "&l🏆 "
            + winName.toUpperCase() + " GANA!"));
        broadcast(game, Msg.c("  &c❤ Rojo:     &f" + game.getScoreRed()));
        broadcast(game, Msg.c("  &d✿ Rosa:     &f" + game.getScorePink()));
        broadcast(game, Msg.c("  &a✦ Verde:    &f" + game.getScoreGreen()));
        broadcast(game, Msg.c("  &e★ Amarillo: &f" + game.getScoreYellow()));
        broadcast(game, Msg.c("  &6⭐ MVP: &e" + mvpName));
        broadcast(game, Msg.sep());

        // Recompensar ganadores + cuetes
        final Team finalWinner = winner;
        if (finalWinner != null) {
            game.getTeamPlayers(finalWinner).forEach(uid -> {
                Player pl = Bukkit.getPlayer(uid);
                if (pl != null) {
                    plugin.getStatsManager().get(pl).addWin();
                    plugin.getStatsManager().get(pl).addCoins(
                        plugin.getConfig().getInt("coins-win", 50));
                    plugin.getMissionManager().onWin(pl);
                    plugin.getRankManager().addXp(pl, 50);

                    // Cuetes y partículas de celebración
                    for (int i = 0; i < 6; i++) {
                        final int delay = i * 15;
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            FireworkUtils.launchTeam(pl.getLocation(), finalWinner);
                            pl.getWorld().spawnParticle(
                                Particle.HAPPY_VILLAGER,
                                pl.getLocation().add(0, 1, 0),
                                30, 0.5, 0.5, 0.5, 0.1);
                            pl.getWorld().spawnParticle(
                                Particle.END_ROD,
                                pl.getLocation().add(0, 1, 0),
                                20, 0.3, 0.5, 0.3, 0.1);
                        }, delay);
                    }
                }
            });
        }

        // Título animado parpadeante
        final String[] colors = {"&c", "&e", "&a", "&b", "&d", "&6"};
        final int[] frame = {0};
        BukkitTask titleAnim = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            String col = colors[frame[0] % colors.length];
            frame[0]++;
            game.getOnlinePlayers().forEach(pl ->
                pl.sendTitle(
                    Msg.c(col + "&l" + (finalWinner != null
                        ? "🏆 " + teamName(finalWinner) + " GANA!"
                        : "🏆 ¡EMPATE!")),
                    Msg.c("&7¡Gracias por jugar! &6+50 coins"),
                    0, 25, 0));
        }, 0L, 8L);
        Bukkit.getScheduler().runTaskLater(plugin,
            () -> titleAnim.cancel(), 100L);

        // Sonidos de victoria
        game.getOnlinePlayers().forEach(pl -> {
            plugin.getStatsManager().get(pl).addGame();
            pl.playSound(pl.getLocation(),
                Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            pl.playSound(pl.getLocation(),
                Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1f, 1f);
            plugin.getScoreboardManager().remove(pl);
        });

        plugin.getStatsManager().saveAll();
        broadcast(game, Msg.info(
            "&e¿Revancha? Escribe &f/pa &epara unirte de nuevo!"));

        // Regresar al lobby en 6s
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location mainLobby = getMainLobby();
            new HashSet<>(game.getPlayers().keySet()).forEach(uid -> {
                Player pl = Bukkit.getPlayer(uid);
                if (pl != null) {
                    pl.getInventory().clear();
                    plugin.getInventoryBackupManager().restore(pl);
                    if (mainLobby != null) pl.teleport(mainLobby);
                    pl.setGameMode(GameMode.SURVIVAL);
                    pl.sendMessage(Msg.ok("Tu inventario fue devuelto."));
                }
                pArena.remove(uid);
            });
            cleanup(game);
        }, 120L);
    }

    public void stopAll() {
        new ArrayList<>(games.values()).forEach(g -> endGame(g, null));
    }

    // ── Equipo restante si alguien se sale ────────────────────────────────────

    private Team getRemainingTeam(Game game) {
        Set<Team> teams = new HashSet<>();
        game.getPlayers().forEach((uid, t) -> {
            if (t != Team.SPECTATOR) teams.add(t);
        });
        if (teams.size() == 1) return teams.iterator().next();
        return null;
    }

    // ── Equipar armadura del equipo ───────────────────────────────────────────

    public void equipTeamArmor(Player p, Team team) {
        Color color = switch (team) {
            case RED    -> Color.RED;
            case PINK   -> Color.FUCHSIA;
            case GREEN  -> Color.GREEN;
            case YELLOW -> Color.YELLOW;
            default     -> Color.WHITE;
        };
        String prefix = switch (team) {
            case RED    -> "&c";
            case PINK   -> "&d";
            case GREEN  -> "&a";
            case YELLOW -> "&e";
            default     -> "&f";
        };
        p.getInventory().setHelmet(
            leatherArmor(Material.LEATHER_HELMET, color, prefix + "Casco"));
        p.getInventory().setChestplate(
            leatherArmor(Material.LEATHER_CHESTPLATE, color, prefix + "Pecho"));
        p.getInventory().setLeggings(
            leatherArmor(Material.LEATHER_LEGGINGS, color, prefix + "Pantalón"));
        p.getInventory().setBoots(
            leatherArmor(Material.LEATHER_BOOTS, color, prefix + "Botas"));
    }

    private ItemStack leatherArmor(Material mat, Color color, String name) {
        ItemStack item = new ItemStack(mat);
        org.bukkit.inventory.meta.LeatherArmorMeta meta =
            (org.bukkit.inventory.meta.LeatherArmorMeta) item.getItemMeta();
        meta.setColor(color);
        meta.setDisplayName(Msg.c(name));
        item.setItemMeta(meta);
        return item;
    }

    // ── Spawn todos los equipos ───────────────────────────────────────────────

    private void spawnAll(Game game) {
        for (Team t : new Team[]{Team.RED, Team.PINK, Team.GREEN, Team.YELLOW}) {
            List<UUID> members = game.getTeamPlayers(t);
            List<Location> spawns = game.getArena().getSpawnsByTeam(t);
            if (spawns.isEmpty()) continue;
            for (int i = 0; i < members.size(); i++) {
                Player pl = Bukkit.getPlayer(members.get(i));
                if (pl == null) continue;
                pl.teleport(spawns.get(i % spawns.size()));
                pl.setHealth(20);
                pl.setFoodLevel(20);
                pl.setGameMode(GameMode.SURVIVAL);
                KitUtils.give(pl, t,
                    plugin.getStatsManager().get(pl).getKit());
                plugin.getScoreboardManager().update(pl, game);
            }
        }
    }

    // ── Balance de equipos ────────────────────────────────────────────────────

    private Team balanceTeam(Game game) {
        Team best = Team.RED;
        int min   = Integer.MAX_VALUE;
        for (Team t : new Team[]{Team.RED, Team.PINK, Team.GREEN, Team.YELLOW}) {
            int size = game.getTeamPlayers(t).size();
            if (size < min) { min = size; best = t; }
        }
        return best;
    }

    // ── Broadcast ─────────────────────────────────────────────────────────────

    private void broadcast(Game game, String msg) {
        game.getPlayers().keySet().forEach(uid -> {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) pl.sendMessage(msg);
        });
    }

    // ── Team chat ─────────────────────────────────────────────────────────────

    public void teamChat(Player p, String message) {
        Game game = getGame(p);
        if (game == null) {
            p.sendMessage(Msg.err("No estás en ninguna partida."));
            return;
        }
        Team team = game.getTeam(p);
        String msg = Msg.c(teamColor(team) + "[" + teamName(team) + "] &7"
            + p.getName() + ": &f" + message);
        game.getTeamPlayers(team).forEach(uid -> {
            Player pl = Bukkit.getPlayer(uid);
            if (pl != null) pl.sendMessage(msg);
        });
    }

    // ── Global chat ───────────────────────────────────────────────────────────

    public void globalChat(Player p, String message) {
        String rank  = plugin.getRankManager().getFormattedRank(
            plugin.getStatsManager().get(p));
        String clan  = plugin.getClanManager().inClan(p)
            ? Msg.c(" &8[&b" + plugin.getClanManager().getTag(
                plugin.getClanManager().getClan(p)) + "&8]") : "";
        String msg   = Msg.c("&8[&aGlobal&8]" + clan + " " + rank
            + " &f" + p.getName() + " &8» &7" + message);
        org.bukkit.Bukkit.getOnlinePlayers().forEach(pl -> pl.sendMessage(msg));
    }

    // ── Preparar jugador ──────────────────────────────────────────────────────

    private void prepare(Player p) {
        p.getInventory().clear();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setGameMode(GameMode.ADVENTURE);
        p.setFireTicks(0);
        p.setLevel(0);
        p.setExp(0f);
        p.getActivePotionEffects()
            .forEach(e -> p.removePotionEffect(e.getType()));
    }

    public void restore(Player p) {
        p.getInventory().clear();
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setGameMode(GameMode.SURVIVAL);
        p.setFireTicks(0);
        p.setLevel(0);
        p.setExp(0f);
        p.getActivePotionEffects()
            .forEach(e -> p.removePotionEffect(e.getType()));
    }

    // ── Cancelar tarea ────────────────────────────────────────────────────────

    private void cancelTask(Game game) {
        if (game.getTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(game.getTaskId());
            game.setTaskId(-1);
        }
    }

    private void cleanup(Game game) {
        games.remove(game.getArena().getName());
        game.getArena().setState(Arena.State.WAITING);
    }

    private Location getMainLobby() {
        var cfg = plugin.getConfig();
        if (!cfg.contains("main-lobby")) return null;
        World w = Bukkit.getWorld(cfg.getString("main-lobby.world", "world"));
        if (w == null) return null;
        return new Location(w,
            cfg.getDouble("main-lobby.x"),
            cfg.getDouble("main-lobby.y"),
            cfg.getDouble("main-lobby.z"),
            (float) cfg.getDouble("main-lobby.yaw"),
            (float) cfg.getDouble("main-lobby.pitch"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public String teamColor(Team t) {
        return switch (t) {
            case RED    -> "&c";
            case PINK   -> "&d";
            case GREEN  -> "&a";
            case YELLOW -> "&e";
            default     -> "&7";
        };
    }

    public String teamName(Team t) {
        return switch (t) {
            case RED    -> "Rojo";
            case PINK   -> "Rosa";
            case GREEN  -> "Verde";
            case YELLOW -> "Amarillo";
            default     -> "Espectador";
        };
    }

    public Game    getGame(Player p)        { String n = pArena.get(p.getUniqueId()); return n != null ? games.get(n) : null; }
    public Game    getGameByArena(String n) { return games.get(n); }
    public boolean inGame(Player p)         { return pArena.containsKey(p.getUniqueId()); }
    public Map<String, Game> all()          { return games; }
}
