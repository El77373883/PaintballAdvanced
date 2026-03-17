package com.soyadrianyt001.advancedpaintball;

import com.soyadrianyt001.advancedpaintball.commands.PAAdminCommand;
import com.soyadrianyt001.advancedpaintball.commands.PACommand;
import com.soyadrianyt001.advancedpaintball.gui.AdminPanelGUI;
import com.soyadrianyt001.advancedpaintball.gui.ArenaMenuGUI;
import com.soyadrianyt001.advancedpaintball.gui.KitShopGUI;
import com.soyadrianyt001.advancedpaintball.gui.TeamSelectorGUI;
import com.soyadrianyt001.advancedpaintball.listeners.GameListener;
import com.soyadrianyt001.advancedpaintball.listeners.ShootListener;
import com.soyadrianyt001.advancedpaintball.listeners.TrailListener;
import com.soyadrianyt001.advancedpaintball.listeners.WandListener;
import com.soyadrianyt001.advancedpaintball.managers.*;
import com.soyadrianyt001.advancedpaintball.npc.ShopNPC;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedPaintball extends JavaPlugin {

    private static AdvancedPaintball instance;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private WandManager wandManager;
    private MissionManager missionManager;
    private RankManager rankManager;
    private InventoryBackupManager inventoryBackupManager;
    private ShopNPC shopNPC;
    private ArenaMenuGUI arenaMenuGUI;
    private AdminPanelGUI adminPanelGUI;
    private KitShopGUI kitShopGUI;
    private TeamSelectorGUI teamSelectorGUI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        arenaManager           = new ArenaManager(this);
        statsManager           = new StatsManager(this);
        rankManager            = new RankManager(this);
        missionManager         = new MissionManager(this);
        inventoryBackupManager = new InventoryBackupManager(this);
        gameManager            = new GameManager(this);
        scoreboardManager      = new ScoreboardManager(this);
        wandManager            = new WandManager(this);
        shopNPC                = new ShopNPC(this);

        arenaMenuGUI    = new ArenaMenuGUI(this);
        adminPanelGUI   = new AdminPanelGUI(this);
        kitShopGUI      = new KitShopGUI(this);
        teamSelectorGUI = new TeamSelectorGUI(this);

        PACommand paCmd = new PACommand(this);
        getCommand("pa").setExecutor(paCmd);
        getCommand("pa").setTabCompleter(paCmd);

        PAAdminCommand paAdminCmd = new PAAdminCommand(this);
        getCommand("pa-admin").setExecutor(paAdminCmd);
        getCommand("pa-admin").setTabCompleter(paAdminCmd);

        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new ShootListener(this), this);
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new TrailListener(this), this);
        getServer().getPluginManager().registerEvents(arenaMenuGUI, this);
        getServer().getPluginManager().registerEvents(adminPanelGUI, this);
        getServer().getPluginManager().registerEvents(kitShopGUI, this);
        getServer().getPluginManager().registerEvents(teamSelectorGUI, this);

        banner();
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.stopAll();
        if (statsManager != null) statsManager.saveAll();
        if (missionManager != null) missionManager.saveAll();
    }

    private void banner() {
        getLogger().info("§r");
        getLogger().info("§b  ╔══════════════════════════════════════════╗");
        getLogger().info("§b  ║   §f§lAdvanced§bPaintball §7v1.0.0 §a[PREMIUM]   §b║");
        getLogger().info("§b  ║   §7Autor:  §eSoyAdrianYT001                  §b║");
        getLogger().info("§b  ║   §7Server: §f1.21.1 Paper/Spigot             §b║");
        getLogger().info("§b  ║   §aPlugin cargado correctamente §a✔           §b║");
        getLogger().info("§b  ╚══════════════════════════════════════════╝");
        getLogger().info("§r");
    }

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static AdvancedPaintball get()             { return instance; }
    public ArenaManager getArenaManager()             { return arenaManager; }
    public GameManager getGameManager()               { return gameManager; }
    public StatsManager getStatsManager()             { return statsManager; }
    public ScoreboardManager getScoreboardManager()   { return scoreboardManager; }
    public WandManager getWandManager()               { return wandManager; }
    public MissionManager getMissionManager()         { return missionManager; }
    public RankManager getRankManager()               { return rankManager; }
    public InventoryBackupManager getInventoryBackupManager() { return inventoryBackupManager; }
    public ShopNPC getShopNPC()                       { return shopNPC; }
    public ArenaMenuGUI getArenaMenuGUI()             { return arenaMenuGUI; }
    public AdminPanelGUI getAdminPanelGUI()           { return adminPanelGUI; }
    public KitShopGUI getKitShopGUI()                 { return kitShopGUI; }
    public TeamSelectorGUI getTeamSelectorGUI()       { return teamSelectorGUI; }
}
