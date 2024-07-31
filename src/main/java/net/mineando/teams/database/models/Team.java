package net.mineando.teams.database.models;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class Team {
    private int id;
    private String name;
    private UUID leader;
    private List<TeamMember> members;

    public Team(int id, String name, UUID leader) {
        this.id = id;
        this.name = name;
        this.leader = leader;
        this.members = new ArrayList<>();
        // Add the leader as the first member of the team
        this.members.add(new TeamMember(-1, id, leader));
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public List<TeamMember> getMembers() { return members; }
    public void setMembers(List<TeamMember> members) { this.members = members; }

    public void addMember(TeamMember member) {
        members.add(member);
    }

    public void removeMember(UUID playerUUID) {
        members.removeIf(member -> member.getPlayerUUID().equals(playerUUID));
    }
}
