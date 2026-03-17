package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClanManager {

    private final AdvancedPaintball plugin;
    private File file;
    private FileConfiguration cfg;

    // clan name -> list of UUIDs
    private final Map<String, List<UUID>> clans       = new HashMap<>();
    // clan name -> owner UUID
    private final Map<String, UUID> clanOwners        = new HashMap<>();
    // player UUID -> clan name
    private final Map<UUID, String> playerClan        = new HashMap<>();
    // clan name -> tag
    private final Map<String, String> clanTags        = new HashMap<>();
    // pending invites: invitedUUID -> clanName
    private final Map<UUID, String> pendingInvites    = new HashMap<>();

    public ClanManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        file = new File(plugin.getDataFolder(), "clans.yml");
        if (!file.exists()) {
            try { file.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        cfg = YamlConfiguration.loadConfiguration(file);
        if (!cfg.contains("clans")) return;

        for (String name : cfg.getConfigurationSection("clans").getKeys(false)) {
            String p    = "clans." + name + ".";
            UUID owner  = UUID.fromString(cfg.getString(p + "owner"));
            String tag  = cfg.getString(p + "tag", name.substring(0, Math.min(4, name.length())).toUpperCase());
            List<UUID> members = new ArrayList<>();
            if (cfg.contains(p + "members")) {
                for (String uid : cfg.getStringList(p + "members")) {
                    UUID u = UUID.fromString(uid);
                    members.add(u);
                    playerClan.put(u, name);
                }
            }
            clans.put(name.toLowerCase(), members);
            clanOwners.put(name.toLowerCase(), owner);
            clanTags.put(name.toLowerCase(), tag);
        }
    }

    private void save() {
        for (Map.Entry<String, List<UUID>> entry : clans.entrySet()) {
            String name = entry.getKey();
            String p    = "clans." + name + ".";
            cfg.set(p + "owner", clanOwners.get(name).toString());
            cfg.set(p + "tag",   clanTags.getOrDefault(name, name.toUpperCase()));
            List<String> members = new ArrayList<>();
            entry.getValue().forEach(u -> members.add(u.toString()));
            cfg.set(p + "members", members);
        }
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public boolean createClan(Player p, String name, String tag) {
        if (playerClan.containsKey(p.getUniqueId())) {
            p.sendMessage(Msg.err("Ya perteneces a un clan."));
            return false;
        }
        if (clans.containsKey(name.toLowerCase())) {
            p.sendMessage(Msg.err("Ya existe un clan con ese nombre."));
            return false;
        }
        if (tag.length() > 5) {
            p.sendMessage(Msg.err("El tag no puede tener más de 5 caracteres."));
            return false;
        }
        List<UUID> members = new ArrayList<>();
        members.add(p.getUniqueId());
        clans.put(name.toLowerCase(), members);
        clanOwners.put(name.toLowerCase(), p.getUniqueId());
        clanTags.put(name.toLowerCase(), tag.toUpperCase());
        playerClan.put(p.getUniqueId(), name.toLowerCase());
        save();
        p.sendMessage(Msg.ok("Clan &b" + name + " &a[&e" + tag.toUpperCase() + "&a] creado!"));
        return true;
    }

    public void disbandClan(Player p) {
        String clan = playerClan.get(p.getUniqueId());
        if (clan == null) { p.sendMessage(Msg.err("No perteneces a ningún clan.")); return; }
        if (!clanOwners.get(clan).equals(p.getUniqueId())) {
            p.sendMessage(Msg.err("Solo el líder puede disolver el clan."));
            return;
        }
        clans.get(clan).forEach(playerClan::remove);
        clans.remove(clan);
        clanOwners.remove(clan);
        clanTags.remove(clan);
        cfg.set("clans." + clan, null);
        try { cfg.save(file); } catch (IOException e) { e.printStackTrace(); }
        p.sendMessage(Msg.ok("Clan disuelto."));
    }

    public void invitePlayer(Player leader, Player target) {
        String clan = playerClan.get(leader.getUniqueId());
        if (clan == null) { leader.sendMessage(Msg.err("No perteneces a ningún clan.")); return; }
        if (!clanOwners.get(clan).equals(leader.getUniqueId())) {
            leader.sendMessage(Msg.err("Solo el líder puede invitar jugadores."));
            return;
        }
        if (playerClan.containsKey(target.getUniqueId())) {
            leader.sendMessage(Msg.err(target.getName() + " ya está en un clan."));
            return;
        }
        pendingInvites.put(target.getUniqueId(), clan);
        leader.sendMessage(Msg.ok("Invitación enviada a &f" + target.getName()));
        target.sendMessage(Msg.prefix("&e" + leader.getName()
            + " &7te invitó al clan &b" + clan
            + " &7[&e" + clanTags.get(clan) + "&7]"));
        target.sendMessage(Msg.info("Escribe &f/pa clan aceptar &7para unirte."));
    }

    public void acceptInvite(Player p) {
        String clan = pendingInvites.get(p.getUniqueId());
        if (clan == null) { p.sendMessage(Msg.err("No tienes invitaciones pendientes.")); return; }
        pendingInvites.remove(p.getUniqueId());
        clans.get(clan).add(p.getUniqueId());
        playerClan.put(p.getUniqueId(), clan);
        save();
        p.sendMessage(Msg.ok("Te uniste al clan &b" + clan + "&a!"));
        broadcastClan(clan, Msg.prefix("&e" + p.getName() + " &7se unió al clan!"));
    }

    public void leaveClam(Player p) {
        String clan = playerClan.get(p.getUniqueId());
        if (clan == null) { p.sendMessage(Msg.err("No perteneces a ningún clan.")); return; }
        if (clanOwners.get(clan).equals(p.getUniqueId())) {
            p.sendMessage(Msg.err("El líder no puede salir. Usa &f/pa clan disolver"));
            return;
        }
        clans.get(clan).remove(p.getUniqueId());
        playerClan.remove(p.getUniqueId());
        save();
        p.sendMessage(Msg.ok("Saliste del clan &b" + clan));
        broadcastClan(clan, Msg.prefix("&e" + p.getName() + " &7salió del clan."));
    }

    public void clanChat(Player p, String message) {
        String clan = playerClan.get(p.getUniqueId());
        if (clan == null) { p.sendMessage(Msg.err("No perteneces a ningún clan.")); return; }
        String tag = clanTags.getOrDefault(clan, clan.toUpperCase());
        broadcastClan(clan, Msg.c("&8[&b" + tag + "&8] &7" + p.getName() + ": &f" + message));
    }

    public void showInfo(Player p) {
        String clan = playerClan.get(p.getUniqueId());
        if (clan == null) { p.sendMessage(Msg.err("No perteneces a ningún clan.")); return; }
        String tag  = clanTags.getOrDefault(clan, "???");
        List<UUID> members = clans.get(clan);
        p.sendMessage(Msg.sep());
        p.sendMessage(Msg.c("  &b&lClan: &f" + clan + " &8[&e" + tag + "&8]"));
        p.sendMessage(Msg.c("  &7Miembros: &f" + members.size()));
        members.forEach(uid -> {
            org.bukkit.OfflinePlayer op = org.bukkit.Bukkit.getOfflinePlayer(uid);
            boolean isOwner = clanOwners.get(clan).equals(uid);
            boolean online  = op.isOnline();
            p.sendMessage(Msg.c("  " + (isOwner ? "&6👑 " : "&7• ")
                + (online ? "&a" : "&7") + op.getName()
                + (isOwner ? " &7(Líder)" : "")));
        });
        p.sendMessage(Msg.sep());
    }

    private void broadcastClan(String clan, String msg) {
        clans.getOrDefault(clan, new ArrayList<>()).forEach(uid -> {
            org.bukkit.Player pl = org.bukkit.Bukkit.getPlayer(uid);
            if (pl != null) pl.sendMessage(msg);
        });
    }

    public String getClan(Player p) { return playerClan.get(p.getUniqueId()); }
    public boolean inClan(Player p) { return playerClan.containsKey(p.getUniqueId()); }
    public String getTag(String clan) { return clanTags.getOrDefault(clan.toLowerCase(), "???"); }
    public Map<String, List<UUID>> getClans() { return clans; }
}
