/**
 * @author: Adrian Popa, adrian.popa.box@gmail.com, adrian.popa@aepeak.com
 * 
 * Coursera, Algorithms, Part II, Princeton
 * Assignment: Boggle, Grade 87/100, https://coursera.cs.princeton.edu/algs4/assignments/baseball/specification.php
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {

    private class Team {
        int wins;
        int losses;
        int remaining;
        int[] against;
    }

    private class Division {
        private final List<Team> teams;
        private final Map<String, Integer> teamsId;

        public Division(int teamsCount) {
            this.teams = new ArrayList<>(teamsCount);
            this.teamsId = new HashMap<>(teamsCount);
        }

        public void addTeam(String name, Team team) {
            this.teams.add(team);
            this.teamsId.put(name, this.teams.size() - 1);
        }

        public Team getTeam(String teamName) {
            int id = this.teamsId.get(teamName);
            return this.teams.get(id);
        }
    }

    private final Division division;

    /**
     * create a baseball division from given filename in format specified below
     * 
     * @param filename
     */
    public BaseballElimination(String filename) {
        In in = new In(filename);
        int teams = Integer.parseInt(in.readLine().trim());
        this.division = new Division(teams);

        while (!in.isEmpty()) {
            String line = in.readLine().trim();
            String[] parts = line.split("\\s+");

            Team team = new Team();
            team.wins = Integer.parseInt(parts[1].trim());
            team.losses = Integer.parseInt(parts[2].trim());
            team.remaining = Integer.parseInt(parts[3].trim());

            int[] againsts = new int[teams];
            for (int i = 4; i < parts.length; i++) {
                againsts[i - 4] = Integer.parseInt(parts[i].trim());
            }
            team.against = againsts;

            String teamName = parts[0].trim();
            division.addTeam(teamName, team);
        }
    }

    /**
     * @return number of teams
     */
    public int numberOfTeams() {
        return this.division.teams.size();
    }

    /**
     * @return all teams
     */
    public Iterable<String> teams() {
        return this.division.teamsId.keySet();
    }

    /**
     * @param team
     * @return the number of wins for given team
     */
    public int wins(String team) {
        validateTeam(team);
        return this.division.getTeam(team).wins;
    }

    /**
     * @param team
     * @return number of losses for given team
     */
    public int losses(String team) {
        validateTeam(team);
        return this.division.getTeam(team).losses;
    }

    /**
     * @param team
     * @return number of remaining games for given team
     */
    public int remaining(String team) {
        validateTeam(team);
        return this.division.getTeam(team).remaining;
    }

    /**
     * @param team1
     * @param team2
     * @return number of remaining games between team1 and team2
     */
    public int against(String team1, String team2) {
        validateTeam(team1);
        validateTeam(team2);
        int team2Id = this.division.teamsId.get(team2);
        return this.division.getTeam(team1).against[team2Id];
    }

    /**
     * @param team
     * @return is given team eliminated?
     */
    public boolean isEliminated(String team) {
        validateTeam(team);

        Team x = division.getTeam(team);
        int xfinish = x.wins + x.remaining;

        int rwins = getTotalWins(team);
        int rremainings = getTotalRemainingGames(team);
        double rfinish = (rwins + rremainings) / (numberOfTeams() - 1.0);

        boolean isTriviallyElliminated = rfinish > xfinish;
        if (isTriviallyElliminated) {
            return true;
        }

        Iterable<String> certificate = certificateOfElimination(team);
        return certificate == null ? false : true;
    }

    /**
     * @param exceptTeam
     * @return subset R of teams that eliminates given team; null if not eliminated
     */
    public Iterable<String> certificateOfElimination(String exceptTeam) {
        validateTeam(exceptTeam);
        FordFulkerson maxFlowMinCut = buildNet(exceptTeam);

        List<String> games = getGames(exceptTeam);
        int offset = (games.size() / 2) + 1;

        List<String> result = new ArrayList<>();
        int rremainings = getTotalRemainingGames(exceptTeam);
        if (rremainings > maxFlowMinCut.value()) {
            for (String team : teams()) {
                if (team.equalsIgnoreCase(exceptTeam)) {
                    continue;
                }

                int v = getTeamId(new HashSet<>(games), team) + offset; // division.teamsId.get(team) + 1;
                if (maxFlowMinCut.inCut(v)) {
                    result.add(team);
                }
            }

            return result.isEmpty() ? null : result;
        }

        return result.isEmpty() ? null : result;
    }

    private FordFulkerson buildNet(String exceptTeam) {
//        Map<Integer, String> legend = new HashMap<Integer, String>();
        List<String> games = getGames(exceptTeam);
        Set<String> teamsInGames = new HashSet<>(games);
        int V = (games.size() / 2) + teamsInGames.size() + 2;
        FlowNetwork flowNet = new FlowNetwork(V);

        int s = 0;
//        legend.put(0, "s");
        int t = V - 1;
//        legend.put(t, "t");
        int vgame = 1; // this.numberOfTeams() - 1; // between (numberOfTeams(), all game combinations]
        int offset = (games.size() / 2) + 1;

        for (int i = 0; i <= games.size() - 2; i = i + 2) {
            String team1 = games.get(i);
            String team2 = games.get(i + 1);

            // connect s to games left
            flowNet.addEdge(new FlowEdge(s, vgame, against(team1, team2)));
//            legend.put(vgame, String.format("g%d_%s_vs_%s", vgame, team1, team2));

            // connect games to teams
            int vteam1 = getTeamId(teamsInGames, team1) + offset;
            flowNet.addEdge(new FlowEdge(vgame, vteam1, Double.POSITIVE_INFINITY));
//            legend.put(vteam1, String.format("r%d_%s", vteam1, team1));

            int vteam2 = getTeamId(teamsInGames, team2) + offset;
            flowNet.addEdge(new FlowEdge(vgame, vteam2, Double.POSITIVE_INFINITY));
//            legend.put(vteam2, String.format("r%d_%s", vteam2, team2));

            vgame++;
        }

        // connect teams to t
        Team teamx = division.getTeam(exceptTeam);
        for (String team : teamsInGames) {
            if (exceptTeam.equalsIgnoreCase(team)) {
                continue;
            }

            Team teamr = division.getTeam(team);
            double capacityTeamResult = teamx.wins + teamx.remaining - teamr.wins;
            capacityTeamResult = capacityTeamResult > 0 ? capacityTeamResult : 0;
            int vteam = getTeamId(teamsInGames, team) + offset;
            flowNet.addEdge(new FlowEdge(vteam, t, capacityTeamResult));
        }

//        StdOut.println(flowNet);
//        StdOut.println(toString(flowNet, legend));

        return new FordFulkerson(flowNet, s, t);
    }

    private int getTeamId(Set<String> teamsInGames, String team) {
        int id = this.division.teamsId.get(team);
        
        int i = 0;
        for (String tig : teamsInGames) {
            if (id == this.division.teamsId.get(tig)) {
                return i;
            }
            i++;
        }
        
        return -1;
    }

    private void validateTeam(String team) {
        if (team == null || !this.division.teamsId.containsKey(team)) {
            throw new IllegalArgumentException("Invalid team name");
        }
    }

    private List<String> getGames(String exceptTeam) {
        validateTeam(exceptTeam);

        List<String> games = new ArrayList<>();
        String[] teams = division.teamsId.keySet().toArray(new String[numberOfTeams()]);
        for (int i = 0; i < teams.length; i++) {
            String team1 = teams[i];
            if (team1.equalsIgnoreCase(exceptTeam)) {
                continue;
            }

            for (int j = i; j < teams.length; j++) {
                if (i == j) {
                    continue;
                }

                String team2 = teams[j];
                if (team2.equalsIgnoreCase(exceptTeam)) {
                    continue;
                }

                if (against(team1, team2) == 0) {
                    continue;
                }

                games.add(team1);
                games.add(team2);
            }
        }

        return games;
    }

    private int getTotalWins(String exceptTeam) {
        int total = 0;

        for (String team : teams()) {
            if (team.equalsIgnoreCase(exceptTeam)) {
                continue;
            }
            Team r = division.getTeam(team);
            total += r.wins;
        }

        return total;
    }

    private int getTotalRemainingGames(String exceptTeam) {
        int total = 0;

        List<String> games = getGames(exceptTeam);
        for (int i = 0; i <= games.size() - 2; i = i + 2) {
            String team1 = games.get(i);
            String team2 = games.get(i + 1);
            total += against(team1, team2);
        }

        return total;
    }

//    private String toString(FlowNetwork flowNet, Map<Integer, String> legend) {
//        StringBuilder s = new StringBuilder();
//        int V = flowNet.V();
//        int E = flowNet.E();
//        s.append(V + " " + E + "\n");
//        for (int v = 0; v < V; v++) {
//            for (FlowEdge e : flowNet.adj(v)) {
//                if (e.to() != v) {
//                    s.append(String.format("%s -> %s \n", legend.get(e.from()), legend.get(e.to())));
//                }
//            }
//        }
//        return s.toString();
//    }

    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination("teams4b.txt");

        for (String team : division.teams()) {
//        String team = "Princeton";
//            StdOut.print(team + " is analyzing ");
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
