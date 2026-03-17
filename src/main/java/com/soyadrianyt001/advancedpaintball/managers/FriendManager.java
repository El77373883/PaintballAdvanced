package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FriendManager {

    private final AdvancedPaintball plugin;
    private File file;
    private FileConfiguration cfg;

    // UUID -> lista de UUIDs amigos
    private final Map<UUID, List<UUID>> friends = new HashMap<>();
    // UUID -> lista de solicitudes pendientes
    private final Map<UUID, List<UUID>> requests = new HashMap<>();

    public FriendManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "friends.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (!cfg.contains("friends")) return;
        for (String uid : cfg.getConfigurationSection("friends").getKeys(false)) {
            UUID uuid = UUID.fromString(uid);
            List<UUID> list = new ArrayList<>();
            cfg.getStringList("friends." + uid).forEach(s -> list.add(UUID.fromString(s)));
            friends.put(uuid, list);
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, List<UUID>> e : friends.entrySet()) {
            List<String> list = new ArrayList<>();
            e.getValue().forEach(u -> list.add(u.toString()));
            cfg.set("friends." + e.getKey().toString(), list);
        }
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    public void sendRequest(Player sender, Player target) {
        if (sender.getUniqueId().equals(target.getUniqueId())) {
            sender.sendMessage(Msg.err("No puedes agregarte a ti mismo."));
            return;
        }
        if (areFriends(sender.getUniqueId(), target.getUniqueId())) {
            sender.sendMessage(Msg.err(target.getName() + " ya es tu amigo."));
            return;
        }
        requests.computeIfAbsent(target.getUniqueId(), k -> new ArrayList<>())
            .add(sender.getUniqueId());
        sender.sendMessage(Msg.ok("Solicitud enviada a &f" + target.getName()));
        target.sendMessage(Msg.prefix("&e" + sender.getName()
            + " &7te envió solicitud de amistad!"));
        target.sendMessage(Msg.info("Escribe &f/pa amigos aceptar "
            + sender.getName() + " &7para aceptar."));
    }

    public void acceptRequest(Player p, Player sender) {
        List<UUID> reqs = requests.get(p.getUniqueId());
        if (reqs == null || !reqs.contains(sender.getUniqueId())) {
            p.sendMessage(Msg.err("No tienes solicitud de " + sender.getName()));
            return;
        }
        reqs.remove(sender.getUniqueId());
        friends.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>())
            .add(sender.getUniqueId());
        friends.computeIfAbsent(sender.getUniqueId(), k -> new ArrayList<>())
            .add(p.getUniqueId());
        saveAll();
        p.sendMessage(Msg.ok("&f" + sender.getName() + " &aes ahora tu amigo!"));
        sender.sendMessage(Msg.ok("&f" + p.getName() + " &aaceptó tu solicitud!"));
    }

    public void removeFriend(Player p, Player target) {
        List<UUID> myFriends = friends.get(p.getUniqueId());
        if (myFriends == null || !myFriends.contains(target.getUniqueId())) {
            p.sendMessage(Msg.err(target.getName() + " no es tu amigo."));
            return;
        }
        myFriends.remove(target.getUniqueId());
        List<UUID> theirFriends = friends.get(target.getUniqueId());
        if (theirFriends != null) theirFriends.remove(p.getUniqueId());
        saveAll();
        p.sendMessage(Msg.ok("Eliminaste a &f" + target.getName() + " &ade tus amigos."));
    }

    public void showFriends(Player p) {
        List<UUID> myFriends = friends.getOrDefault(p.getUniqueId(), new ArrayList<>());
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lAmigos &8(&f" + myFriends.size() + "&8)"));
        if (myFriends.isEmpty()) {
            p.sendMessage(Msg.c("  &7No tienes amigos aún."));
            p.sendMessage(Msg.c("  &7Usa &f/pa amigos agregar <jugador>"));
        } else {
            myFriends.forEach(uid -> {
                OfflinePlayer op = Bukkit.getOfflinePlayer(uid);
                boolean online = op.isOnline();
                p.sendMessage(Msg.c("  " + (online ? "&a● " : "&7○ ")
                    + op.getName() + (online ? " &aEn línea" : " &7Desconectado")));
            });
        }
        p.sendMessage(Msg.sep());
    }

    public void joinSameArena(Player p, Player friend) {
        if (!areFriends(p.getUniqueId(), friend.getUniqueId())) {
            p.sendMessage(Msg.err(friend.getName() + " no es tu amigo."));
            return;
        }
        if (!plugin.getGameManager().inGame(friend)) {
            p.sendMessage(Msg.err(friend.getName() + " no está en ninguna arena."));
            return;
        }
        var game = plugin.getGameManager().getGame(friend);
        if (game == null) return;
        plugin.getGameManager().join(p, game.getArena());
        p.sendMessage(Msg.ok("Uniéndote a la arena de &f" + friend.getName()));
    }

    public boolean areFriends(UUID a, UUID b) {
        List<UUID> list = friends.get(a);
        return list != null && list.contains(b);
    }

    public List<UUID> getFriends(UUID uuid) {
        return friends.getOrDefault(uuid, new ArrayList<>());
    }
}
