package presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import domain.model.*;
import presentation.symbols.GameSymbols;

import java.io.IOException;
import java.util.List;

public class GameRenderer {
    private final int mapWidth;
    private final int mapHeight;
    private final int hudX;
    private static final int PANEL_WIDTH = 27;
    private static final int PANEL_RIGHT_MARGIN = 1;
    private final HudRenderer hudRenderer;
    private final MessageRenderer messageRenderer;
    private final MessageLog messageLog;
    private final VisibilitySystem visibilitySystem;

    public GameRenderer(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.hudX = mapWidth + 2;

        this.hudRenderer = new HudRenderer(hudX, 0);
        this.messageRenderer = new MessageRenderer(hudX, 19);
        this.messageLog = new MessageLog(5);
        this.visibilitySystem = new VisibilitySystem(mapWidth, mapHeight);
    }

    public void addMessage(String message) {
        messageLog.addMessage(message);
    }

    public void render(Screen screen, GameSession session) throws IOException
    {
        screen.clear();

        TextGraphics graphics =
                screen.newTextGraphics();

        int screenWidth =
                screen.getTerminalSize().getColumns();

        int screenHeight =
                screen.getTerminalSize().getRows();

        int minWidth =
                mapWidth + 2 + PANEL_WIDTH + PANEL_RIGHT_MARGIN;

        if (screenWidth < minWidth || screenHeight < mapHeight)
        {
            String warning =
                    "Terminal too small. Minimum: "
                            + minWidth
                            + "x"
                            + mapHeight;

            if (screenWidth > 0 && screenHeight > 0)
            {
                graphics.putString(
                        0,
                        0,
                        warning.substring(
                                0,
                                Math.min(screenWidth, warning.length())
                        )
                );
            }

            screen.refresh();
            return;
        }

        int panelX =
                screenWidth - PANEL_WIDTH - PANEL_RIGHT_MARGIN;

        hudRenderer.setStartX(panelX);
        messageRenderer.setStartX(panelX);

        Level level = session.getCurrentLevel();
        Player player = session.getPlayer();

        visibilitySystem.updateVisibility(level, player);

        drawMap(graphics, level, player);

        hudRenderer.render(graphics, player, level);
        messageRenderer.render(graphics, messageLog);

        screen.refresh();
    }

    private void drawMap(TextGraphics graphics, Level level, Player player) {
        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                Position pos = new Position(x, y);
                
                if (!visibilitySystem.isVisible(pos, level, player)) {
                    if (!visibilitySystem.isExplored(pos)) {
                        graphics.setCharacter(x, y, ' ');
                        continue;
                    }
                    char symbol = getCellSymbol(level, pos);
                    if (symbol == GameSymbols.WALL) {
                        graphics.setCharacter(x, y, symbol);
                    } else {
                        graphics.setCharacter(x, y, ' ');
                    }
                    continue;
                }
                
                char symbol = getCellSymbol(level, pos);
                graphics.setCharacter(x, y, symbol);
            }
        }
        
        drawGroundItems(graphics, level, player);
        
        drawEnemies(graphics, level, player);
        
        graphics.setCharacter(
            player.getPosition().x(),
            player.getPosition().y(),
            GameSymbols.PLAYER
        );
    }

    private char getCellSymbol(Level level, Position pos)
    {
        Room exitRoom = level.getExitRoom();

        if (exitRoom != null
                && pos.x() == exitRoom.getCenterX()
                && pos.y() == exitRoom.getCenterY())
        {
            return GameSymbols.EXIT;
        }

        for (Room room : level.getRooms())
        {
            if (room.contains(pos))
            {
                if (isRoomBorder(room, pos))
                {
                    for (Corridor corridor : level.getCorridors())
                    {
                        if (corridor.contains(pos))
                        {
                            return GameSymbols.CORRIDOR;
                        }
                    }

                    return GameSymbols.WALL;
                }

                return GameSymbols.FLOOR;
            }
        }

        for (Corridor corridor : level.getCorridors())
        {
            if (corridor.contains(pos))
            {
                return GameSymbols.CORRIDOR;
            }
        }

        return GameSymbols.WALL;
    }

    private boolean isRoomBorder(Room room, Position pos) {
        int x = pos.x();
        int y = pos.y();
        int roomX = room.getX();
        int roomY = room.getY();
        int roomWidth = room.getWidth();
        int roomHeight = room.getHeight();
        
        return x == roomX || x == roomX + roomWidth - 1 ||
               y == roomY || y == roomY + roomHeight - 1;
    }

    private void drawGroundItems(TextGraphics graphics, Level level, Player player) {
        List<GroundItem> groundItems = level.getGroundItems();
        if (groundItems == null) return;
        
        for (GroundItem groundItem : groundItems) {
            Position pos = groundItem.getPosition();
            if (visibilitySystem.isVisible(pos, level, player)) {
                char symbol = getItemSymbol(groundItem.getItem());
                graphics.setCharacter(pos.x(), pos.y(), symbol);
            }
        }
    }

    private char getItemSymbol(Item item) {
        if (item instanceof Food) return GameSymbols.FOOD;
        if (item instanceof Elixir) return GameSymbols.ELIXIR;
        if (item instanceof Scroll) return GameSymbols.SCROLL;
        if (item instanceof Weapon) return GameSymbols.WEAPON;
        if (item instanceof Treasure) return '$';
        return '?';
    }

    private void drawEnemies(TextGraphics graphics, Level level, Player player) {
    List<Enemy> enemies = level.getEnemies();
    if (enemies == null) return;
    
    for (Enemy enemy : enemies) {
        if (!enemy.isAlive() || !enemy.isVisible()) continue;
        
        Position pos = enemy.getPosition();
        if (!visibilitySystem.isVisible(pos, level, player)) continue;
        
        char symbol = getEnemySymbol(enemy);
        TextColor color = getEnemyColor(enemy);
        
        graphics.setForegroundColor(color);
        graphics.setCharacter(pos.x(), pos.y(), symbol);
        graphics.setForegroundColor(TextColor.ANSI.DEFAULT);
    }
}

    private TextColor getEnemyColor(Enemy enemy) {
        switch (enemy.getType()) {
        case ZOMBIE:     return TextColor.ANSI.GREEN;
        case VAMPIRE:    return TextColor.ANSI.RED;
        case GHOST:      return TextColor.ANSI.WHITE;
        case OGRE:       return TextColor.ANSI.YELLOW;
        case SNAKE_MAGE: return TextColor.ANSI.WHITE;
        default:         return TextColor.ANSI.DEFAULT;
    }
}

    private char getEnemySymbol(Enemy enemy) {
        switch (enemy.getType()) {
            case ZOMBIE: return GameSymbols.ZOMBIE;
            case VAMPIRE: return GameSymbols.VAMPIRE;
            case GHOST: return GameSymbols.GHOST;
            case OGRE: return GameSymbols.OGRE;
            case SNAKE_MAGE: return GameSymbols.SNAKE_MAGE;
            default: return '?';
        }
    }
}