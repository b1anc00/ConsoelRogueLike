package domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Corridor {
    private final List<Position> cells;
    private final Room from;
    private final Room to;

    public Corridor(List<Position> cells, Room from, Room to) {

        this.cells = new ArrayList<>(cells);
        this.from = from;
        this.to = to;
    }

    public List<Position> getCells() {
        return Collections.unmodifiableList(cells);
    }

    public Room getFrom() {
        return from;
    }

    public Room getTo() {
        return to;
    }

    public boolean contains(Position position) {
        return cells.contains(position);
    }
}