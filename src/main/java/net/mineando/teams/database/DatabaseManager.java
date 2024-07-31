package net.mineando.teams.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.mineando.teams.Teams;
import net.mineando.teams.database.models.Team;
import net.mineando.teams.database.models.TeamMember;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DatabaseManager {
    private final Teams plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(Teams plugin) {
        this.plugin = plugin;
        initializeDataSource();
        createTables();
    }

    private void initializeDataSource() {
        FileConfiguration config = plugin.getConfig();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
        hikariConfig.setJdbcUrl("jdbc:mariadb://" + config.getString("database.host") + ":" + config.getInt("database.port") + "/" + config.getString("database.database"));
        hikariConfig.setUsername(config.getString("database.username"));
        hikariConfig.setPassword(config.getString("database.password"));
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(hikariConfig);
    }


    private void createTables() {
        String createTeamsTable = "CREATE TABLE IF NOT EXISTS teams (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "name VARCHAR(32) NOT NULL UNIQUE," +
                "leader UUID NOT NULL" +
                ")";

        String createMembersTable = "CREATE TABLE IF NOT EXISTS team_members (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "team_id INT NOT NULL," +
                "player_uuid UUID NOT NULL," +
                "FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmtTeams = conn.prepareStatement(createTeamsTable);
             PreparedStatement stmtMembers = conn.prepareStatement(createMembersTable)) {
            stmtTeams.executeUpdate();
            stmtMembers.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating database tables: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public Optional<Team> createTeam(String name, UUID leader) {
        String sql = "INSERT INTO teams (name, leader) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, leader.toString());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return Optional.empty();
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int teamId = generatedKeys.getInt(1);
                    Team team = new Team(teamId, name, leader);
                    addMemberToTeam(teamId, leader);
                    return Optional.of(team);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating team: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean disbandTeam(int teamId) {
        String sql = "DELETE FROM teams WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error disbanding team: " + e.getMessage());
        }
        return false;
    }

    public boolean renameTeam(int teamId, String newName) {
        String sql = "UPDATE teams SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setInt(2, teamId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error renaming team: " + e.getMessage());
        }
        return false;
    }

    public Optional<Team> getTeam(int teamId) {
        String sql = "SELECT * FROM teams WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Team team = new Team(
                            rs.getInt("id"),
                            rs.getString("name"),
                            UUID.fromString(rs.getString("leader"))
                    );
                    team.setMembers(getTeamMembers(teamId));
                    return Optional.of(team);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting team: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();
        String sql = "SELECT * FROM teams";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Team team = new Team(
                        rs.getInt("id"),
                        rs.getString("name"),
                        UUID.fromString(rs.getString("leader"))
                );
                team.setMembers(getTeamMembers(team.getId()));
                teams.add(team);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting all teams: " + e.getMessage());
        }
        return teams;
    }

    public boolean addMemberToTeam(int teamId, UUID playerUUID) {
        String sql = "INSERT INTO team_members (team_id, player_uuid) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setString(2, playerUUID.toString());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error adding member to team: " + e.getMessage());
        }
        return false;
    }

    public boolean removeMemberFromTeam(int teamId, UUID playerUUID) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND player_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            stmt.setString(2, playerUUID.toString());
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error removing member from team: " + e.getMessage());
        }
        return false;
    }

    private List<TeamMember> getTeamMembers(int teamId) {
        List<TeamMember> members = new ArrayList<>();
        String sql = "SELECT * FROM team_members WHERE team_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, teamId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new TeamMember(
                            rs.getInt("id"),
                            rs.getInt("team_id"),
                            UUID.fromString(rs.getString("player_uuid"))
                    ));
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting team members: " + e.getMessage());
        }
        return members;
    }

    public Optional<Team> getPlayerTeam(UUID playerUUID) {
        String sql = "SELECT t.* FROM teams t " +
                "JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE tm.player_uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Team team = new Team(
                            rs.getInt("id"),
                            rs.getString("name"),
                            UUID.fromString(rs.getString("leader"))
                    );
                    team.setMembers(getTeamMembers(team.getId()));
                    return Optional.of(team);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error getting player team: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean transferTeamLeadership(int teamId, UUID newLeaderUUID) {
        String sql = "UPDATE teams SET leader = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newLeaderUUID.toString());
            stmt.setInt(2, teamId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error transferring team leadership: " + e.getMessage());
        }
        return false;
    }

}