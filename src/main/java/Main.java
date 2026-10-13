import application.GameApplicationService;
import datalayer.dto.RunRecordData;
import presentation.GameScreen;
import presentation.GameRenderer;
import presentation.InputController;
import presentation.InventoryView;
import domain.model.GameSession;
import domain.model.GameStatus;
import domain.gameplay.PlayerActionResult;
import domain.gameplay.InventoryActionResult;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import java.io.IOException;
import java.util.List;

public class Main {
    private static final int MAP_WIDTH = 80;
    private static final int MAP_HEIGHT = 25;

    public static void main(String[] args) {
        GameApplicationService game = GameApplicationService.createDefault();

        try (GameScreen gameScreen = new GameScreen()) {
            GameRenderer renderer = new GameRenderer(MAP_WIDTH, MAP_HEIGHT);
            InputController input = new InputController(gameScreen.getScreen());

            runMainMenu(game, renderer, input, gameScreen);

        } catch (IOException e) {
            System.err.println("Критическая ошибка терминала: " + e.getMessage());
        }
    }

    private static void runMainMenu(GameApplicationService game, GameRenderer renderer,
                                    InputController input, GameScreen gameScreen) throws IOException {
        boolean running = true;

        while (running) {
            gameScreen.clear();
            var graphics = gameScreen.getScreen().newTextGraphics();

            graphics.putString(30, 8, "=== JAVA ROGUE ===");
            graphics.putString(30, 11, "1. New Game");
            graphics.putString(30, 12, "2. Continue" + (game.hasSavedGame() ? "" : " (no save)"));
            graphics.putString(30, 13, "3. Statistics");
            graphics.putString(30, 14, "4. Exit");
            graphics.putString(30, 17, "Press 1, 2, 3, or 4");

            gameScreen.getScreen().refresh();

            KeyStroke key = input.readKey();
            if (key.getKeyType() != KeyType.Character) continue;

            char c = key.getCharacter();
            try {
                switch (c) {
                    case '1':
                        game.newGame();
                        runGameLoop(game, renderer, input, gameScreen);
                        break;
                    case '2':
                        if (game.hasSavedGame()) {
                            boolean loaded = game.continueGame();
                            if (loaded) {
                                runGameLoop(game, renderer, input, gameScreen);
                            } else {
                                renderer.addMessage("Failed to load save.");
                            }
                        } else {
                            renderer.addMessage("No saved game found!");
                        }
                        break;
                    case '3':
                        showStatistics(game, renderer, input, gameScreen);
                        break;
                    case '4':
                        running = false;
                        break;
                }
            } catch (IOException e) {
                showErrorScreen(renderer, input, gameScreen, "Unable to read or write save data.");
            }
        }
    }

    private static void runGameLoop(GameApplicationService game, GameRenderer renderer,
                                    InputController input, GameScreen gameScreen) throws IOException {
        renderer.addMessage("Welcome to Java Rogue!");
        boolean gameRunning = true;

        while (gameRunning) {
            GameSession session = game.getSession();

            if (session.getStatus() == GameStatus.WON) {
                showEndScreen(renderer, input, gameScreen, session, "YOU WON THE GAME!");
                return;
            }
            if (session.getStatus() == GameStatus.LOST) {
                showEndScreen(renderer, input, gameScreen, session, "YOU DIED!");
                return;
            }

            renderer.render(gameScreen.getScreen(), session);

            KeyStroke key = input.readKey();

            if (input.isQuit(key)) {
                return;
            }

            if (input.isMoveKey(key)) {
                int[] dir = input.getDirection(key);
                try {
                    PlayerActionResult result = game.move(dir[0], dir[1]);
                    handleMoveResult(result, renderer);
                } catch (IOException e) {
                    renderer.addMessage("Error: " + e.getMessage());
                }
                continue;
            }

            if (input.isInventoryKey(key)) {
                char invType = input.getInventoryType(key);
                try {
                    handleInventory(game, renderer, input, gameScreen, invType);
                } catch (IOException e) {
                    renderer.addMessage("Inventory error: " + e.getMessage());
                }
                continue;
            }
        }
    }

    private static void handleMoveResult(PlayerActionResult result, GameRenderer renderer) {
        switch (result.getType()) {
            case ATTACKED:
                if (result.getAttackResult() != null && result.getAttackResult().isHit()) {
                    renderer.addMessage("Hit enemy for " + result.getAttackResult().getDamage() + " dmg");
                } else {
                    renderer.addMessage("Attack missed");
                }
                break;
            case ENEMY_KILLED:
                renderer.addMessage("Enemy killed!");
                break;
            case ITEM_PICKED_UP:
                if (result.getItem() != null) {
                    renderer.addMessage("Picked up: " + result.getItem().getClass().getSimpleName());
                }
                break;
            case INVENTORY_FULL:
                renderer.addMessage("Inventory is full!");
                break;
            case LEVEL_EXIT:
                renderer.addMessage("Descending to next level...");
                break;
            case SLEEPING:
                renderer.addMessage("You are sleeping...");
                break;
            case BLOCKED:
                renderer.addMessage("Blocked!");
                break;
            case INVALID_DIRECTION:
                renderer.addMessage("Invalid direction");
                break;
            case MOVED:
                break;
        }
    }

    private static void handleInventory(
            GameApplicationService game,
            GameRenderer renderer,
            InputController input,
            GameScreen gameScreen,
            char invType) throws IOException
    {
        InventoryView inventoryView =
                new InventoryView(gameScreen.getScreen(), input);

        InventoryView.Selection selection =
                inventoryView.showInventory(game.getSession(), invType);

        if (selection == null)
        {
            return;
        }

        InventoryActionResult result;

        switch (selection.category())
        {
            case WEAPONS:
                if (selection.index() == -1)
                {
                    result = game.unequipWeapon();
                }
                else
                {
                    result = game.equipWeapon(selection.index());
                }
                break;

            case FOOD:
                result = game.useFood(selection.index());
                break;

            case ELIXIRS:
                result = game.useElixir(selection.index());
                break;

            case SCROLLS:
                result = game.useScroll(selection.index());
                break;

            default:
                return;
        }

        renderer.addMessage(getInventoryMessage(result));
    }

    private static String getInventoryMessage(InventoryActionResult result)
    {
        return switch (result.getType().name())
        {
            case "FOOD_USED" -> "Food used";
            case "ELIXIR_USED" -> "Elixir used";
            case "SCROLL_USED" -> "Scroll used";
            case "WEAPON_EQUIPPED" -> "Weapon equipped";
            case "WEAPON_UNEQUIPPED" -> "Weapon unequipped";
            case "NO_WEAPON_EQUIPPED" -> "No equipped weapon";
            case "NO_DROP_SPACE" -> "No space to drop weapon";
            case "INVALID_INDEX" -> "Invalid item selection";
            case "SLEEPING" -> "You are sleeping";
            case "GAME_NOT_RUNNING" -> "Game is not running";
            default -> result.getType().toString();
        };
    }

    private static void showStatistics(
            GameApplicationService game,
            GameRenderer renderer,
            InputController input,
            GameScreen gameScreen) throws IOException
    {
        List<RunRecordData> runs = game.getBestRuns();

        int page = 0;
        int pageSize = 7;

        int totalPages = Math.max(
                1,
                (runs.size() + pageSize - 1) / pageSize
        );

        while (true)
        {
            gameScreen.clear();

            var graphics =
                    gameScreen.getScreen().newTextGraphics();

            graphics.putString(
                    4,
                    2,
                    "=== STATISTICS / LEADERBOARD ==="
            );

            graphics.putString(
                    4,
                    4,
                    "Runs: " + runs.size()
                            + " | Page "
                            + (page + 1)
                            + "/" + totalPages
            );

            if (runs.isEmpty())
            {
                graphics.putString(
                        4,
                        7,
                        "No completed runs yet."
                );
            }
            else
            {
                int start = page * pageSize;
                int end = Math.min(
                        start + pageSize,
                        runs.size()
                );

                int y = 7;

                for (int i = start; i < end; i++)
                {
                    RunRecordData run = runs.get(i);

                    String firstLine = String.format(
                            "%d. %s | Treasure: %d | Level: %d | Enemies: %d",
                            i + 1,
                            run.won() ? "WIN" : "LOSS",
                            run.treasure(),
                            run.reachedLevel(),
                            run.defeatedEnemies()
                    );

                    String secondLine = String.format(
                            "Food: %d | Elixirs: %d | Scrolls: %d | Hits dealt: %d | Hits received: %d | Cells: %d",
                            run.foodEaten(),
                            run.elixirsDrunk(),
                            run.scrollsRead(),
                            run.hitsDealt(),
                            run.hitsReceived(),
                            run.cellsWalked()
                    );

                    graphics.putString(4, y++, firstLine);
                    graphics.putString(7, y++, secondLine);
                }
            }

            graphics.putString(
                    4,
                    24,
                    "A/D or Left/Right: Change page | Q: Back"
            );

            gameScreen.getScreen().refresh();

            KeyStroke key = input.readKey();

            if (key.getKeyType() == KeyType.ArrowLeft)
            {
                page = Math.max(0, page - 1);
                continue;
            }

            if (key.getKeyType() == KeyType.ArrowRight)
            {
                page = Math.min(totalPages - 1, page + 1);
                continue;
            }

            if (key.getKeyType() == KeyType.Character)
            {
                char c = Character.toLowerCase(
                        key.getCharacter()
                );

                if (c == 'q')
                {
                    return;
                }

                if (c == 'a')
                {
                    page = Math.max(0, page - 1);
                }

                if (c == 'd')
                {
                    page = Math.min(totalPages - 1, page + 1);
                }
            }
        }
    }

    private static void showEndScreen(GameRenderer renderer, InputController input,
                                      GameScreen gameScreen, GameSession session, String message) throws IOException {
        gameScreen.clear();
        var graphics = gameScreen.getScreen().newTextGraphics();
        
        int centerX = 40;
        int centerY = 10;
        
        if (session.getStatus() == GameStatus.WON) {
            graphics.setForegroundColor(TextColor.ANSI.GREEN);
            graphics.putString(centerX - 10, centerY - 2, "╔════════════════════╗");
            graphics.putString(centerX - 10, centerY - 1, "║    YOU WON!        ║");
            graphics.putString(centerX - 10, centerY,     "╚════════════════════╝");
        } else {
            graphics.setForegroundColor(TextColor.ANSI.RED);
            graphics.putString(centerX - 10, centerY - 2, "╔════════════════════╗");
            graphics.putString(centerX - 10, centerY - 1, "║    YOU DIED!       ║");
            graphics.putString(centerX - 10, centerY,     "╚════════════════════╝");
        }
        
        graphics.setForegroundColor(TextColor.ANSI.DEFAULT);
        
        int y = centerY + 3;
        graphics.putString(centerX - 15, y++, "Level reached: " + session.getCurrentLevelNumber());
        graphics.putString(centerX - 15, y++, "Treasure: " + session.getPlayer().getBackpack().getTreasureAmount());
        graphics.putString(centerX - 15, y++, "Cells walked: " + session.getStatistics().getCellsWalked());
        
        y++;
        graphics.putString(centerX - 15, y, "Press any key to return to menu...");
        
        gameScreen.getScreen().refresh();
        input.readKey();
    }

    private static void showErrorScreen(GameRenderer renderer, InputController input,
                                        GameScreen gameScreen, String message) throws IOException {
        gameScreen.clear();
        var graphics = gameScreen.getScreen().newTextGraphics();
        graphics.putString(20, 10, "ERROR: " + message);
        graphics.putString(20, 12, "Press any key to return to menu...");
        gameScreen.getScreen().refresh();
        input.readKey();
    }
}