package presentation.symbols;

public final class GameSymbols {

    private GameSymbols() {
    }

    public static final char WALL = '#';
    public static final char FLOOR = '.';
    public static final char CORRIDOR = ':';
    public static final char PLAYER = '@';
    public static final char EXIT = '>';
    public static final char UNKNOWN = ' ';

    public static final char ZOMBIE = 'z';
    public static final char VAMPIRE = 'v';
    public static final char GHOST = 'g';
    public static final char OGRE = 'O';
    public static final char SNAKE_MAGE = 's';

    public static final char FOOD = '%';
    public static final char ELIXIR = '!';
    public static final char SCROLL = '?';
    public static final char WEAPON = ')';

    public static final String HUD_SEPARATOR = "─".repeat(40);
}