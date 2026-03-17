package com.soyadrianyt001.advancedpaintball.gui;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.Arena;
import com.soyadrianyt001.advancedpaintball.utils.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AdminPanelGUI implements Listener {

    private final AdvancedPaintball plugin;
    private static final String TITLE_MAIN    = "§c§lAP §7Admin Panel";
    private static final String TITLE_EDIT    = "§c§lAP §7Editar: ";
    private static final String TITLE_CONFIRM = "§c§l¿Eliminar Arena?";
    private final Map<UUID, String> editing     = new HashMap<>();
    private final Map<UUID, String> confirmDelete = new HashMap<>();
    private final Set<UUID> waitingName         = new HashSet<>();

    public AdminPanelGUI(AdvancedPaintball plugin) { this.plugin = plugin; }

    // ── Panel principal ───────────────────────────────────────────────────────
    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_MAIN);
        ItemStack bg = make(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        int slot = 0;
        for (Arena a : plugin.getArenaManager().all()) {
            if (slot >= 45) break;
            inv.setItem(slot++, arenaIcon(a));
        }

        // Botón crear arena
        inv.setItem(45, make(Material.LIME_CONCRETE,
            Msg.c("&a&l+ Crear Nueva Arena"),
            Arrays.asList(
                Msg.c("&7Click para crear y registrar"),
                Msg.c("&7una nueva arena de paintball")
            )));

        // Botón registrar área (aparece en /pa para jugadores)
        inv.setItem(46, make(Material.CYAN_CONCRETE,
            Msg.c("&b&l⚑ Registrar Arena en /pa"),
            Arrays.asList(
                Msg.c("&7Activa o desactiva si la arena"),
                Msg.c("&7aparece en el menú de jugadores"),
                Msg.c("&7Click en una arena para activarla")
            )));

        // Botón instrucciones
        inv.setItem(47, make(Material.BOOK,
            Msg.c("&e&l? Instrucciones"),
            Arrays.asList(
                Msg.c("&71. &fCrea la arena"),
                Msg.c("&72. &fPon el lobby"),
                Msg.c("&73. &fAñade spawns R/Ro/V/A"),
                Msg.c("&74. &fUsa varita Pos1/Pos2"),
                Msg.c("&75. &fGuarda y activa la arena")
            )));

        // Recargar
        inv.setItem(49, make(Material.COMPARATOR,
            Msg.c("&b&l↺ Recargar Config"),
            Arrays.asList(Msg.c("&7Recarga la configuración del plugin"))));

        // Cerrar
        inv.setItem(53, make(Material.BARRIER, Msg.c("&c&l✗ Cerrar"), null));

        p.openInventory(inv);
    }

    // ── Panel editar arena ────────────────────────────────────────────────────
    public void openEdit(Player p, Arena a) {
        editing.put(p.getUniqueId(), a.getName());
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_EDIT + a.getName());
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, bg);

        boolean hasLobby  = a.getLobby() != null;
        boolean hasRed    = !a.getSpawnsRed().isEmpty();
        boolean hasPink   = !a.getSpawnsPink().isEmpty();
        boolean hasGreen  = !a.getSpawnsGreen().isEmpty();
        boolean hasYellow = !a.getSpawnsYellow().isEmpty();
        boolean ready     = a.isReady();
        boolean visible   = a.isVisibleInMenu();

        // Fila 1
        inv.setItem(10, make(
            hasLobby ? Material.GREEN_CONCRETE : Material.RED_CONCRETE,
            Msg.c("&b&l⌂ Lobby"),
            Arrays.asList(
                Msg.c("&7Estado: " + (hasLobby ? "&a✔ Configurado" : "&c✗ Sin configurar")),
                Msg.c("&7Click = guardar tu posición")
            )));

        inv.setItem(11, make(
            hasRed ? Material.RED_CONCRETE : Material.RED_WOOL,
            Msg.c("&c&l+ Spawn Rojo"),
            Arrays.asList(
                Msg.c("&7Spawns: &f" + a.getSpawnsRed().size()),
                Msg.c("&7Click = añadir | Shift = limpiar")
            )));

        inv.setItem(12, make(
            hasPink ? Material.PINK_CONCRETE : Material.PINK_WOOL,
            Msg.c("&d&l+ Spawn Rosa"),
            Arrays.asList(
                Msg.c("&7Spawns: &f" + a.getSpawnsPink().size()),
                Msg.c("&7Click = añadir | Shift = limpiar")
            )));

        inv.setItem(13, make(
            hasGreen ? Material.GREEN_CONCRETE : Material.GREEN_WOOL,
            Msg.c("&a&l+ Spawn Verde"),
            Arrays.asList(
                Msg.c("&7Spawns: &f" + a.getSpawnsGreen().size()),
                Msg.c("&7Click = añadir | Shift = limpiar")
            )));

        inv.setItem(14, make(
            hasYellow ? Material.YELLOW_CONCRETE : Material.YELLOW_WOOL,
            Msg.c("&e&l+ Spawn Amarillo"),
            Arrays.asList(
                Msg.c("&7Spawns: &f" + a.getSpawnsYellow().size()),
                Msg.c("&7Click = añadir | Shift = limpiar")
            )));

        inv.setItem(15, make(Material.GOLDEN_SWORD,
            Msg.c("&6&l⚒ Varita Pos1/Pos2"),
            Arrays.asList(
                Msg.c("&7Click Izq = &aPos1"),
                Msg.c("&7Click Der = &cPos2"),
                Msg.c(a.getPos1() != null ? "&aPos1 ✔" : "&cPos1 ✗"),
                Msg.c(a.getPos2() != null ? "&aPos2 ✔" : "&cPos2 ✗")
            )));

        // Fila 2
        inv.setItem(28, make(Material.CLOCK,
            Msg.c("&e&l⏱ Tiempo"),
            Arrays.asList(
                Msg.c("&7Actual: &e" + a.getGameTime() + "s"),
                Msg.c("&7Click = 5min | Shift = 10min")
            )));

        inv.setItem(29, make(Material.DIAMOND_SWORD,
            Msg.c("&a&l⚔ Kills para Ganar"),
            Arrays.asList(
                Msg.c("&7Actual: &f" + a.getKillsToWin()),
                Msg.c("&7Click = +5 | Shift = -5")
            )));

        inv.setItem(30, make(Material.PLAYER_HEAD,
            Msg.c("&d&l👥 Min Jugadores"),
            Arrays.asList(
                Msg.c("&7Mín: &f" + a.getMinPlayers() + " &7Máx: &f" + a.getMaxPlayers()),
                Msg.c("&7Click = +1 | Shift = -1")
            )));

        inv.setItem(31, make(Material.CHEST,
            Msg.c("&b&l⚙ Max Jugadores"),
            Arrays.asList(
                Msg.c("&7Actual: &f" + a.getMaxPlayers()),
                Msg.c("&7Click = +2 | Shift = -2")
            )));

        // Registrar en /pa (visible para jugadores)
        inv.setItem(33, make(
            visible ? Material.LIME_CONCRETE : Material.GRAY_CONCRETE,
            Msg.c(visible ? "&a&l⚑ Visible en /pa ✔" : "&7&l⚑ Oculta en /pa ✗"),
            Arrays.asList(
                Msg.c("&7Estado: " + (visible ? "&aVisible para jugadores" : "&7Oculta para jugadores")),
                Msg.c("&7Click para " + (visible ? "&cdesactivar" : "&aactivar"))
            )));

        // Estado
        inv.setItem(34, make(
            ready ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK,
            Msg.c(ready ? "&a&l✔ ARENA LISTA" : "&c&l✗ INCOMPLETA"),
            Arrays.asList(
                Msg.c("&7Lobby:    " + (hasLobby  ? "&a✔" : "&c✗")),
                Msg.c("&7Sp.Rojo:  " + (hasRed    ? "&a✔ (" + a.getSpawnsRed().size()    + ")" : "&c✗")),
                Msg.c("&7Sp.Rosa:  " + (hasPink   ? "&a✔ (" + a.getSpawnsPink().size()   + ")" : "&c✗")),
                Msg.c("&7Sp.Verde: " + (hasGreen  ? "&a✔ (" + a.getSpawnsGreen().size()  + ")" : "&c✗")),
                Msg.c("&7Sp.Amar.: " + (hasYellow ? "&a✔ (" + a.getSpawnsYellow().size() + ")" : "&c✗"))
            )));

        // Botones inferiores
        inv.setItem(45, make(Material.ARROW, Msg.c("&7◀ Volver"), null));

        inv.setItem(47, make(Material.LIME_CONCRETE,
            Msg.c("&a&l✔ Guardar Arena"),
            Arrays.asList(
                Msg.c("&7Guarda todos los cambios"),
                Msg.c(ready ? "&aArena lista para jugar!" : "&cAún faltan configuraciones")
            )));

        inv.setItem(49, make(Material.NAME_TAG,
            Msg.c("&e&l✎ Cambiar Nombre"),
            Arrays.asList(
                Msg.c("&7Actual: &f" + a.getDisplayName()),
                Msg.c("&7Click para cambiar en chat")
            )));

        // Eliminar con confirmación
        inv.setItem(51, make(Material.TNT,
            Msg.c("&c&l🗑 Eliminar Arena"),
            Arrays.asList(
                Msg.c("&c¡CUIDADO! Irreversible"),
                Msg.c("&7Click para abrir confirmación")
            )));

        inv.setItem(53, make(Material.BARRIER, Msg.c("&c&l✗ Cerrar"), null));

        p.openInventory(inv);
    }

    // ── Menú de confirmación eliminar ─────────────────────────────────────────
    public void openConfirmDelete(Player p, String arenaName) {
        confirmDelete.put(p.getUniqueId(), arenaName);
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_CONFIRM);

        // Fondo premium negro
        ItemStack bg = make(Material.BLACK_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, bg);

        // Titulo pregunta
        inv.setItem(4, make(Material.TNT,
            Msg.c("&c&l¿Eliminar &f" + arenaName + "&c?"),
            Arrays.asList(
                Msg.c("&7Esta acción es &c&lIRREVERSIBLE"),
                Msg.c("&7Se borrarán todos los datos"),
                Msg.c("&7de la arena &f" + arenaName)
            )));

        // Vidrios rojos a la izquierda = CANCELAR
        inv.setItem(9,  make(Material.RED_STAINED_GLASS_PANE, Msg.c("&c&l✗ CANCELAR"), Arrays.asList(Msg.c("&7Volver sin eliminar"))));
        inv.setItem(10, make(Material.RED_STAINED_GLASS_PANE, Msg.c("&c&l✗ CANCELAR"), Arrays.asList(Msg.c("&7Volver sin eliminar"))));
        inv.setItem(11, make(Material.RED_STAINED_GLASS_PANE, Msg.c("&c&l✗ CANCELAR"), Arrays.asList(Msg.c("&7Volver sin eliminar"))));
        inv.setItem(18, make(Material.RED_STAINED_GLASS_PANE, Msg.c("&c&l✗ CANCELAR"), Arrays.asList(Msg.c("&7Volver sin eliminar"))));
        inv.setItem(19, make(Material.RED_STAINED_GLASS_PANE, Msg.c("&c&l✗ CANCELAR"), Arrays.asList(Msg.c("&7Volver sin eliminar"))));
        inv.setItem(20, make(Material.RED_STAINED_GLASS_PANE, Msg.c("&c&l✗ CANCELAR"), Arrays.asList(Msg.c("&7Volver sin eliminar"))));

        // Vidrios verdes a la derecha = ACEPTAR
        inv.setItem(15, make(Material.LIME_STAINED_GLASS_PANE, Msg.c("&a&l✔ ACEPTAR"), Arrays.asList(Msg.c("&7Eliminar la arena"))));
        inv.setItem(16, make(Material.LIME_STAINED_GLASS_PANE, Msg.c("&a&l✔ ACEPTAR"), Arrays.asList(Msg.c("&7Eliminar la arena"))));
        inv.setItem(17, make(Material.LIME_STAINED_GLASS_PANE, Msg.c("&a&l✔ ACEPTAR"), Arrays.asList(Msg.c("&7Eliminar la arena"))));
        inv.setItem(24, make(Material.LIME_STAINED_GLASS_PANE, Msg.c("&a&l✔ ACEPTAR"), Arrays.asList(Msg.c("&7Eliminar la arena"))));
        inv.setItem(25, make(Material.LIME_STAINED_GLASS_PANE, Msg.c("&a&l✔ ACEPTAR"), Arrays.asList(Msg.c("&7Eliminar la arena"))));
        inv.setItem(26, make(Material.LIME_STAINED_GLASS_PANE, Msg.c("&a&l✔ ACEPTAR"), Arrays.asList(Msg.c("&7Eliminar la arena"))));

        p.openInventory(inv);
    }

    // ── Clicks ────────────────────────────────────────────────────────────────
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        String title = e.getView().getTitle();

        if (title.equals(TITLE_MAIN))         { mainClick(e, p);    return; }
        if (title.startsWith(TITLE_EDIT))     { editClick(e, p);    return; }
        if (title.equals(TITLE_CONFIRM))      { confirmClick(e, p); }
    }

    private void mainClick(InventoryClickEvent e, Player p) {
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;
        String name = item.getItemMeta().getDisplayName();
        if (name.isBlank()) return;

        if (item.getType() == Material.BARRIER)   { p.closeInventory(); return; }
        if (item.getType() == Material.COMPARATOR) {
            plugin.reloadConfig();
            p.sendMessage(Msg.admin("Config recargada ✔"));
            openMain(p);
            return;
        }
        if (item.getType() == Material.LIME_CONCRETE) {
            p.closeInventory();
            waitingName.add(p.getUniqueId());
            p.sendMessage(Msg.admin("Escribe el &fnombre de la nueva arena &7en el chat:"));
            p.sendMessage(Msg.info("Escribe &ccancel &7para cancelar."));
            return;
        }
        if (item.getType() == Material.CYAN_CONCRETE) {
            p.sendMessage(Msg.admin("Abre una arena específica y activa '&bVisible en /pa&7'."));
            return;
        }

        for (Arena a : plugin.getArenaManager().all()) {
            if (Msg.c(a.getDisplayName()).equals(name)) {
                openEdit(p, a);
                return;
            }
        }
    }

    private void editClick(InventoryClickEvent e, Player p) {
        e.setCancelled(true);
        String arenaName = editing.get(p.getUniqueId());
        if (arenaName == null) return;
        Arena a = plugin.getArenaManager().get(arenaName);
        if (a == null) return;
        boolean shift = e.isShiftClick();

        switch (e.getSlot()) {
            case 10 -> { a.setLobby(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Lobby guardado.")); openEdit(p, a); }
            case 11 -> { if (shift) a.getSpawnsRed().clear(); else a.getSpawnsRed().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn rojo " + (shift ? "limpiado" : "#" + a.getSpawnsRed().size()) + ".")); openEdit(p, a); }
            case 12 -> { if (shift) a.getSpawnsPink().clear(); else a.getSpawnsPink().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn rosa " + (shift ? "limpiado" : "#" + a.getSpawnsPink().size()) + ".")); openEdit(p, a); }
            case 13 -> { if (shift) a.getSpawnsGreen().clear(); else a.getSpawnsGreen().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn verde " + (shift ? "limpiado" : "#" + a.getSpawnsGreen().size()) + ".")); openEdit(p, a); }
            case 14 -> { if (shift) a.getSpawnsYellow().clear(); else a.getSpawnsYellow().add(p.getLocation()); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Spawn amarillo " + (shift ? "limpiado" : "#" + a.getSpawnsYellow().size()) + ".")); openEdit(p, a); }
            case 15 -> { p.closeInventory(); plugin.getWandManager().give(p, arenaName); }
            case 28 -> { a.setGameTime(shift ? 600 : 300); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Tiempo: &e" + a.getGameTime() + "s")); openEdit(p, a); }
            case 29 -> { a.setKillsToWin(Math.max(1, a.getKillsToWin() + (shift ? -5 : 5))); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Kills to win: &e" + a.getKillsToWin())); openEdit(p, a); }
            case 30 -> { a.setMinPlayers(Math.max(1, a.getMinPlayers() + (shift ? -1 : 1))); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Min jugadores: &e" + a.getMinPlayers())); openEdit(p, a); }
            case 31 -> { a.setMaxPlayers(Math.max(2, a.getMaxPlayers() + (shift ? -2 : 2))); plugin.getArenaManager().save(a); p.sendMessage(Msg.ok("Max jugadores: &e" + a.getMaxPlayers())); openEdit(p, a); }
            case 33 -> {
                // Toggle visible en /pa
                a.setVisibleInMenu(!a.isVisibleInMenu());
                plugin.getArenaManager().save(a);
                p.sendMessage(Msg.ok("Arena " + (a.isVisibleInMenu() ? "&aactivada" : "&cdesactivada") + " en el menú de jugadores."));
                openEdit(p, a);
            }
            case 45 -> openMain(p);
            case 47 -> {
                plugin.getArenaManager().save(a);
                p.sendMessage(Msg.ok("Arena &f" + arenaName + " &aguardada" + (a.isReady() ? " y lista!" : ". Faltan configuraciones.")));
                openMain(p);
            }
            case 49 -> {
                p.closeInventory();
                waitingName.add(p.getUniqueId());
                p.sendMessage(Msg.admin("Escribe el &fnuevo nombre &7en el chat:"));
            }
            case 51 -> openConfirmDelete(p, arenaName); // Abre confirmación
            case 53 -> p.closeInventory();
        }
    }

    private void confirmClick(InventoryClickEvent e, Player p) {
        e.setCancelled(true);
        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        String arenaName = confirmDelete.get(p.getUniqueId());
        if (arenaName == null) return;

        if (item.getType() == Material.LIME_STAINED_GLASS_PANE) {
            // ACEPTAR - eliminar arena
            plugin.getArenaManager().delete(arenaName);
            confirmDelete.remove(p.getUniqueId());
            editing.remove(p.getUniqueId());
            p.sendMessage(Msg.ok("Arena &f" + arenaName + " &aeliminada correctamente."));
            openMain(p);
        } else if (item.getType() == Material.RED_STAINED_GLASS_PANE) {
            // CANCELAR - volver
            confirmDelete.remove(p.getUniqueId());
            Arena a = plugin.getArenaManager().get(arenaName);
            if (a != null) openEdit(p, a);
            else openMain(p);
        }
    }

    // ── Chat listener ─────────────────────────────────────────────────────────
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!waitingName.contains(p.getUniqueId())) return;
        e.setCancelled(true);
        waitingName.remove(p.getUniqueId());

        String input = e.getMessage().trim();
        if (input.equalsIgnoreCase("cancel")) {
            p.sendMessage(Msg.info("Cancelado."));
            Bukkit.getScheduler().runTask(plugin, () -> openMain(p));
            return;
        }
        if (input.contains(" ") || input.length() > 16) {
            p.sendMessage(Msg.err("Nombre inválido. Sin espacios, máx 16 caracteres."));
            return;
        }

        final String arenaName = input.toLowerCase();
        Bukkit.getScheduler().runTask(plugin, () -> {
            String currentEditing = editing.get(p.getUniqueId());
            // Cambiar nombre de arena existente
            if (currentEditing != null && plugin.getArenaManager().exists(currentEditing)) {
                Arena a = plugin.getArenaManager().get(currentEditing);
                a.setDisplayName("&6" + input);
                plugin.getArenaManager().save(a);
                p.sendMessage(Msg.ok("Nombre cambiado a &f" + input));
                openEdit(p, a);
                return;
            }
            // Crear nueva arena
            if (plugin.getArenaManager().exists(arenaName)) {
                p.sendMessage(Msg.err("Ya existe una arena con ese nombre."));
                return;
            }
            Arena a = plugin.getArenaManager().create(arenaName);
            a.setLobby(p.getLocation());
            plugin.getArenaManager().save(a);
            p.sendMessage(Msg.ok("Arena &f" + arenaName + " &acreada! Configúrala en el panel."));
            openEdit(p, a);
        });
    }

    private ItemStack arenaIcon(Arena a) {
        boolean ready   = a.isReady();
        boolean visible = a.isVisibleInMenu();
        Material mat    = ready ? (visible ? Material.CYAN_CONCRETE : Material.BLUE_CONCRETE) : Material.ORANGE_CONCRETE;
        return make(mat, Msg.c(a.getDisplayName()), Arrays.asList(
            Msg.c(ready ? "&aLista ✔" : "&cIncompleta ✗"),
            Msg.c(visible ? "&bVisible en /pa ✔" : "&7Oculta en /pa ✗"),
            Msg.c("&7R/Ro/V/A: &f" + a.getSpawnsRed().size() + "/" + a.getSpawnsPink().size() + "/" + a.getSpawnsGreen().size() + "/" + a.getSpawnsYellow().size()),
            Msg.c("&eClick para editar")
        ));
    }

    private ItemStack make(Material m, String name, List<String> lore) {
        ItemStack item = new ItemStack(m);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public Set<UUID> getWaitingName() { return waitingName; }
}
