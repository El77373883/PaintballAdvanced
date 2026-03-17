package com.soyadrianyt001.advancedpaintball.managers;

import com.soyadrianyt001.advancedpaintball.AdvancedPaintball;
import com.soyadrianyt001.advancedpaintball.models.PlayerStats;
import com.soyadrianyt001.advancedpaintball.utils.Msg;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class MySQLManager {

    private final AdvancedPaintball plugin;
    private Connection connection;
    private boolean enabled = false;

    public MySQLManager(AdvancedPaintball plugin) {
        this.plugin = plugin;
        if (plugin.getConfig().getBoolean("mysql.enabled", false)) {
            connect();
        }
    }

    private void connect() {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        int    port = plugin.getConfig().getInt("mysql.port", 3306);
        String db   = plugin.getConfig().getString("mysql.database", "paintball");
        String user = plugin.getConfig().getString("mysql.username", "root");
        String pass = plugin.getConfig().getString("mysql.password", "");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + db
                + "?useSSL=false&autoReconnect=true&characterEncoding=utf8",
                user, pass);
            createTable();
            enabled = true;
            plugin.getLogger().info("§a[AdvancedPaintball] MySQL conectado correctamente!");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                "[AdvancedPaintball] Error conectando a MySQL: " + e.getMessage());
            enabled = false;
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS ap_stats ("
            + "uuid VARCHAR(36) PRIMARY KEY,"
            + "name VARCHAR(16),"
            + "kills INT DEFAULT 0,"
            + "deaths INT DEFAULT 0,"
            + "wins INT DEFAULT 0,"
            + "games INT DEFAULT 0,"
            + "coins INT DEFAULT 0,"
            + "kit VARCHAR(20) DEFAULT 'default',"
            + "rank_name VARCHAR(20) DEFAULT 'Novato',"
            + "level INT DEFAULT 0,"
            + "xp INT DEFAULT 0,"
            + "mission_kills INT DEFAULT 0,"
            + "mission_wins INT DEFAULT 0,"
            + "last_mission_reset BIGINT DEFAULT 0"
            + ")";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void saveStats(PlayerStats stats) {
        if (!enabled) return;
        String sql = "INSERT INTO ap_stats "
            + "(uuid, name, kills, deaths, wins, games, coins, kit, rank_name, level, xp, mission_kills, mission_wins, last_mission_reset) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
            + "ON DUPLICATE KEY UPDATE "
            + "name=?, kills=?, deaths=?, wins=?, games=?, coins=?, kit=?, rank_name=?, level=?, xp=?, mission_kills=?, mission_wins=?, last_mission_reset=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            // INSERT
            ps.setString(1, stats.getUuid().toString());
            ps.setString(2, stats.getName());
            ps.setInt(3, stats.getKills());
            ps.setInt(4, stats.getDeaths());
            ps.setInt(5, stats.getWins());
            ps.setInt(6, stats.getGames());
            ps.setInt(7, stats.getCoins());
            ps.setString(8, stats.getKit());
            ps.setString(9, stats.getRank());
            ps.setInt(10, stats.getLevel());
            ps.setInt(11, stats.getXp());
            ps.setInt(12, stats.getMissionKills());
            ps.setInt(13, stats.getMissionWins());
            ps.setLong(14, stats.getLastMissionReset());
            // UPDATE
            ps.setString(15, stats.getName());
            ps.setInt(16, stats.getKills());
            ps.setInt(17, stats.getDeaths());
            ps.setInt(18, stats.getWins());
            ps.setInt(19, stats.getGames());
            ps.setInt(20, stats.getCoins());
            ps.setString(21, stats.getKit());
            ps.setString(22, stats.getRank());
            ps.setInt(23, stats.getLevel());
            ps.setInt(24, stats.getXp());
            ps.setInt(25, stats.getMissionKills());
            ps.setInt(26, stats.getMissionWins());
            ps.setLong(27, stats.getLastMissionReset());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error guardando stats en MySQL: " + e.getMessage());
        }
    }

    public PlayerStats loadStats(UUID uuid, String name) {
        if (!enabled) return null;
        String sql = "SELECT * FROM ap_stats WHERE uuid=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                PlayerStats stats = new PlayerStats(uuid, rs.getString("name"));
                stats.setKills(rs.getInt("kills"));
                stats.setDeaths(rs.getInt("deaths"));
                stats.setWins(rs.getInt("wins"));
                stats.setGames(rs.getInt("games"));
                stats.setCoins(rs.getInt("coins"));
                stats.setKit(rs.getString("kit"));
                stats.setRank(rs.getString("rank_name"));
                stats.setLevel(rs.getInt("level"));
                stats.setXp(rs.getInt("xp"));
                stats.setMissionKills(rs.getInt("mission_kills"));
                stats.setMissionWins(rs.getInt("mission_wins"));
                stats.setLastMissionReset(rs.getLong("last_mission_reset"));
                return stats;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error cargando stats de MySQL: " + e.getMessage());
        }
        return null;
    }

    public void disconnect() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean isEnabled() { return enabled; }
}
