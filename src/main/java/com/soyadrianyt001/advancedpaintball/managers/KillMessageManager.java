package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.entity.Player;

import java.util.*;

public class KillMessageManager {

    private final AdvancedPaintball plugin;

    // Mensajes personalizados por jugador
    private final Map<UUID, String> customMessages = new HashMap<>();

    // Mensajes por defecto según rango
    private static final Map<String, List<String>> RANK_MESSAGES = new LinkedHashMap<>();

    static {
        RANK_MESSAGES.put("Novato", Arrays.asList(
            "&f{killer} &7eliminó a &e{victim}",
            "&f{killer} &7le dio a &e{victim}"
        ));
        RANK_MESSAGES.put("Aprendiz", Arrays.asList(
            "&a{killer} &7pintó a &e{victim} &7de colores!",
            "&a{killer} &7aprendió a apuntar, &e{victim} &7lo sabe!"
        ));
        RANK_MESSAGES.put("Soldado", Arrays.asList(
            "&b{killer} &7disparó certero a &e{victim}!",
            "&b⚡ &7Soldado &b{killer} &7derrotó a &e{victim}"
        ));
        RANK_MESSAGES.put("Pro", Arrays.asList(
            "&6★ {killer} &7destruyó a &e{victim} &7sin piedad!",
            "&6{killer} &7Pro Edition: &e{victim} &7eliminado!"
        ));
        RANK_MESSAGES.put("Elite", Arrays.asList(
            "&c🔥 ELITE &f{killer} &7aniquiló a &e{victim}!",
            "&c⚔ {killer} &7no tuvo misericordia con &e{victim}!"
        ));
        RANK_MESSAGES.put("Leyenda", Arrays.asList(
            "&d✨ LEYENDA &f{killer} &7borró del mapa a &e{victim}!",
            "&d👑 {killer} &7demostró por qué es LEYENDA: &e{victim} &7eliminado!",
            "&d⚡ {killer} &7es imparable! &e{victim} &7no tuvo oportunidad!"
        ));
    }

    public KillMessageManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    public String getKillMessage(Player killer, Player victim) {
        // Si tiene mensaje personalizado usarlo
        if (customMessages.containsKey(killer.getUniqueId())) {
            return Msg.c(customMessages.get(killer.getUniqueId())
                .replace("{killer}", killer.getName())
                .replace("{victim}", victim.getName()));
        }

        // Mensaje según rango
        PlayerStats stats = plugin.getStatsManager().get(killer);
        String rank = stats.getRank();
        List<String> messages = RANK_MESSAGES.getOrDefault(rank,
            RANK_MESSAGES.get("Novato"));

        String template = messages.get(new Random().nextInt(messages.size()));
        return Msg.c(template
            .replace("{killer}", killer.getName())
            .replace("{victim}", victim.getName()));
    }

    public void setCustomMessage(Player p, String message) {
        customMessages.put(p.getUniqueId(), message);
    }

    public void removeCustomMessage(Player p) {
        customMessages.remove(p.getUniqueId());
    }

    public boolean hasCustomMessage(Player p) {
        return customMessages.containsKey(p.getUniqueId());
    }

    public String getCustomMessage(Player p) {
        return customMessages.getOrDefault(p.getUniqueId(), "ninguno");
    }

    public Map<String, List<String>> getRankMessages() {
        return RANK_MESSAGES;
    }
}
