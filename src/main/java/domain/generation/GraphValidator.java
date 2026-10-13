package domain.generation;

import domain.model.Corridor;
import domain.model.Room;
import domain.model.Position;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphValidator {

    public boolean isConnected(List<Room> rooms, List<Corridor> corridors) {
        if (rooms.isEmpty()) {
            return true;
        }

        Map<Room, List<Room>> adjacencyList = buildAdjacencyList(rooms, corridors);

        Set<Room> visited = new HashSet<>();
        dfs(rooms.get(0), adjacencyList, visited);

        return visited.size() == rooms.size();
    }

    private Map<Room, List<Room>> buildAdjacencyList(List<Room> rooms, List<Corridor> corridors) {
        Map<Room, List<Room>> adjacencyList = new HashMap<>();
        
        for (Room room : rooms) {
            adjacencyList.put(room, new ArrayList<>());
        }

        for (Corridor corridor : corridors) {
            Room from = corridor.getFrom();
            Room to = corridor.getTo();
            
            adjacencyList.get(from).add(to);
            adjacencyList.get(to).add(from);
        }

        return adjacencyList;
    }

    private void dfs(Room current, Map<Room, List<Room>> adjacencyList, Set<Room> visited) {
        visited.add(current);

        for (Room neighbor : adjacencyList.get(current)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, adjacencyList, visited);
            }
        }
    }
}