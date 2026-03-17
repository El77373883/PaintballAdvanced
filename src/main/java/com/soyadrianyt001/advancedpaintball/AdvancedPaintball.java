package com.soyadrianyt001.advancedpaintball;

import com.soyadrianyt001.advancedpaintball.commands.PAAdminCommand;
import com.soyadrianyt001.advancedpaintball.commands.PACommand;
import com.soyadrianyt001.advancedpaintball.gui.*;
import com.soyadrianyt001.advancedpaintball.hooks.PAPIHook;
import com.soyadrianyt001.advancedpaintball.listeners.*;
import com.soyadrianyt001.advancedpaintball.managers.*;
import com.soyadrianyt001.advancedpaintball.npc.ShopNPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedPaintball extends JavaPlugin {

    private static AdvancedPaintball instance;

    // Managers
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private StatsManager statsManager;
    private ScoreboardManager scoreboardManager;
    private WandManager wandManager;
    private MissionManager missionManager;
    private RankManager rankManager;
    private InventoryBackupManager inventoryBackupManager;
    private ChatManager chatManager;
    private MySQLManager mySQLManager;

    // GUIs
    private MainMenuGUI mainMenuGUI;
    private AdminPanelGUI adminPanelGUI;
    private ArenaSelectorGUI arenaSelectorGUI;
    private ShopGUI shopGUI;
    private TeamSelectorGUI teamSelectorGUI;

    // NPC
    private ShopNPC shopNPC;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Managers en orden
        mySQLManager           = new MySQLManager(this);
        arenaManager           = new ArenaManager(this);
        statsManager           = new StatsManager(this);
        rankManager            = new RankManager(this);
        missionManager         = new MissionManager(this);
        inventoryBackupManager = new InventoryBackupManager(this);
        gameManager            = new GameManager(this);
        scoreboardManager      = new ScoreboardManager(this);
        wandManager            = new WandManager(this);
        chatManager            = new ChatManager(this);
        shopNPC                = new ShopNPC(this);

        // GUIs
        mainMenuGUI     = new MainMenuGUI(this);
        adminPanelGUI   = new AdminPanelGUI(this);
        arenaSelectorGUI = new ArenaSelectorGUI(this);
        shopGUI         = new ShopGUI(this);
        teamSelectorGUI = new TeamSelectorGUI(this);

        // Comandos
        PACommand paCmd = new PACommand(this);
        getCommand("pa").setExecutor(paCmd);
        getCommand("pa").setTabCompleter(paCmd);

        PAAdminCommand paAdminCmd = new PAAdminCommand(this);
        getCommand("pa-admin").setExecutor(paAdminCmd);
        getCommand("pa-admin").setTabCompleter(paAdminCmd);

        // Listeners
        getServer().getPluginManager().registerEvents(new GameListener(this), this);
        getServer().getPluginManager().registerEvents(new ShootListener(this), this);
        getServer().getPluginManager().registerEvents(new WandListener(this), this);
        getServer().getPluginManager().registerEvents(new TrailListener(this), this);
        getServer().getPluginManager().registerEvents(new VIPParticleListener(this), this);
        getServer().getPluginManager().registerEvents(mainMenuGUI, this);
        getServer().getPluginManager().registerEvents(adminPanelGUI, this);
        getServer().getPluginManager().registerEvents(arenaSelectorGUI, this);
        getServer().getPluginManager().registerEvents(shopGUI, this);
        getServer().getPluginManager().registerEvents(teamSelectorGUI, this);
        getServer().getPluginManager().registerEvents(chatManager, this);

        // PlaceholderAPI
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIHook(this).register();
            getLogger().info("§a[AdvancedPaintball] PlaceholderAPI conectado!");
        }

        // Autosave cada 5 minutos
        int interval = getConfig().getInt("autosave-interval", 300) * 20L > 0
            ? getConfig().getInt("autosave-interval", 300) : 300;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            statsManager.saveAll();
            missionManager.saveAll();
            getLogger().info("§7[AdvancedPaintball] Autosave completado.");
        }, interval * 20L, interval * 20L);

        banner();
    }

    @Override
    public void onDisable() {
        if (gameManager != null) gameManager.stopAll();
        if (statsManager != null) statsManager.saveAll();
        if (missionManager != null) missionManager.saveAll();
        if (mySQLManager != null) mySQLManager.disconnect();
        getLogger().info("§c[AdvancedPaintball] Plugin desactivado.");
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

    // Getters
    public static AdvancedPaintball get()             { return instance; }
    public ArenaManager getArenaManager()             { return arenaManager; }
    public GameManager getGameManager()               { return gameManager; }
    public StatsManager getStatsManager()             { return statsManager; }
    public ScoreboardManager getScoreboardManager()   { return scoreboardManager; }
    public WandManager getWandManager()               { return wandManager; }
    public MissionManager getMissionManager()         { return missionManager; }
    public RankManager getRankManager()               { return rankManager; }
    public InventoryBackupManager getInventoryBackupManager() { return inventoryBackupManager; }
    public ChatManager getChatManager()               { return chatManager; }
    public MySQLManager getMySQLManager()             { return mySQLManager; }
    public ShopNPC getShopNPC()                       { return shopNPC; }
    public MainMenuGUI getMainMenuGUI()               { return mainMenuGUI; }
    public AdminPanelGUI getAdminPanelGUI()           { return adminPanelGUI; }
    public ArenaSelectorGUI getArenaSelectorGUI()     { return arenaSelectorGUI; }
    public ShopGUI getShopGUI()                       { return shopGUI; }
    public TeamSelectorGUI getTeamSelectorGUI()       { return teamSelectorGUI; }
}
