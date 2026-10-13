package domain.gameplay;

import domain.model.Level;
import domain.model.Position;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PathFinder
{
    private final MovementSystem movementSystem;

    public PathFinder(MovementSystem movementSystem)
    {
        this.movementSystem = movementSystem;
    }

    public List<Position> findPath(
            Level level,
            Position start,
            Position target,
            Set<Position> blocked)
    {
        if (start.equals(target))
        {
            return List.of(start);
        }

        Queue<Position> queue =
                new ArrayDeque<>();

        Set<Position> visited =
                new HashSet<>();

        Map<Position, Position> parent =
                new HashMap<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty())
        {
            Position current =
                    queue.poll();

            for (Position next : getNeighbors(current))
            {
                if (visited.contains(next))
                {
                    continue;
                }

                if (blocked.contains(next)
                        && !next.equals(target))
                {
                    continue;
                }

                if (!movementSystem.isWalkable(
                        level,
                        next))
                {
                    continue;
                }

                visited.add(next);
                parent.put(next, current);

                if (next.equals(target))
                {
                    return buildPath(
                            parent,
                            start,
                            target
                    );
                }

                queue.add(next);
            }
        }

        return Collections.emptyList();
    }

    private List<Position> getNeighbors(
            Position position)
    {
        List<Position> result =
                new ArrayList<>();

        result.add(
                new Position(
                        position.x() + 1,
                        position.y()
                )
        );

        result.add(
                new Position(
                        position.x() - 1,
                        position.y()
                )
        );

        result.add(
                new Position(
                        position.x(),
                        position.y() + 1
                )
        );

        result.add(
                new Position(
                        position.x(),
                        position.y() - 1
                )
        );

        return result;
    }

    private List<Position> buildPath(
            Map<Position, Position> parent,
            Position start,
            Position target)
    {
        List<Position> path =
                new ArrayList<>();

        Position current = target;

        while (current != null)
        {
            path.add(current);

            if (current.equals(start))
            {
                break;
            }

            current = parent.get(current);
        }

        Collections.reverse(path);

        return path;
    }
}