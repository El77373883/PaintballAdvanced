package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryBackupManager {

    private final AdvancedPaintball plugin;
    private final Map<UUID, ItemStack[]> invBackup    = new HashMap<>();
    private final Map<UUID, ItemStack[]> armorBackup  = new HashMap<>();

    public InventoryBackupManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
    }

    public void backup(Player p) {
        invBackup.put(p.getUniqueId(), p.getInventory().getContents().clone());
        armorBackup.put(p.getUniqueId(), p.getInventory().getArmorContents().clone());
    }

    public void restore(Player p) {
        UUID uid = p.getUniqueId();
        if (invBackup.containsKey(uid)) {
            p.getInventory().setContents(invBackup.get(uid));
            p.getInventory().setArmorContents(armorBackup.get(uid));
            invBackup.remove(uid);
            armorBackup.remove(uid);
        }
    }

    public void clear(Player p) {
        invBackup.remove(p.getUniqueId());
        armorBackup.remove(p.getUniqueId());
    }

    public boolean hasBackup(Player p) {
        return invBackup.containsKey(p.getUniqueId());
    }
}
