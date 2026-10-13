package datalayer;

import datalayer.dto.GameSaveData;
import domain.model.Backpack;
import domain.model.Corridor;
import domain.model.Elixir;
import domain.model.Enemy;
import domain.model.Food;
import domain.model.GameSession;
import domain.model.GameStatistics;
import domain.model.GroundItem;
import domain.model.Item;
import domain.model.Level;
import domain.model.Position;
import domain.model.Room;
import domain.model.Scroll;
import domain.model.TemporaryEffect;
import domain.model.Treasure;
import domain.model.Weapon;
import domain.model.EnemyType;
import domain.model.GameStatus;
import domain.model.ItemType;
import domain.model.Player;
import domain.model.StatType;
import java.util.ArrayList;
import java.util.List;

public class GameSaveMapper
{
    public GameSaveData toData(
            GameSession session)
    {
        return new GameSaveData(
                session.getCurrentLevelNumber(),
                session.getStatus().name(),
                mapPlayer(session),
                mapLevel(
                        session.getCurrentLevel()
                ),
                mapStatistics(
                        session.getStatistics()
                ),
                mapEffects(
                        session.getActiveEffects()
                )
        );
    }

    private Level restoreLevel(
            GameSaveData.LevelData data)
    {
        if (data == null)
        {
            return null;
        }

        List<Room> rooms =
                new ArrayList<>();

        for (GameSaveData.RoomData roomData :
                data.rooms())
        {
            rooms.add(
                    new Room(
                            roomData.x(),
                            roomData.y(),
                            roomData.width(),
                            roomData.height()
                    )
            );
        }

        List<Corridor> corridors =
                new ArrayList<>();

        for (GameSaveData.CorridorData corridorData :
                data.corridors())
        {
            List<Position> cells =
                    new ArrayList<>();

            for (GameSaveData.PositionData position :
                    corridorData.cells())
            {
                cells.add(
                        restorePosition(
                                position
                        )
                );
            }

            Room from =
                    rooms.get(
                            corridorData
                                    .fromRoomIndex()
                    );

            Room to =
                    rooms.get(
                            corridorData
                                    .toRoomIndex()
                    );

            corridors.add(
                    new Corridor(
                            cells,
                            from,
                            to
                    )
            );
        }

        Level level =
                new Level(
                        data.levelNumber(),
                        rooms,
                        corridors
                );

        if (data.startRoomIndex() >= 0)
        {
            level.setStartRoom(
                    rooms.get(
                            data.startRoomIndex()
                    )
            );
        }

        if (data.exitRoomIndex() >= 0)
        {
            level.setExitRoom(
                    rooms.get(
                            data.exitRoomIndex()
                    )
            );
        }

        for (GameSaveData.EnemyData enemy :
                data.enemies())
        {
            level.addEnemy(
                    restoreEnemy(enemy)
            );
        }

        for (GameSaveData.GroundItemData item :
                data.groundItems())
        {
            level.addGroundItem(
                    new GroundItem(
                            restoreItem(
                                    item.item()
                            ),
                            restorePosition(
                                    item.position()
                            )
                    )
            );
        }

        return level;
    }

    private Enemy restoreEnemy(
            GameSaveData.EnemyData data)
    {
        Enemy enemy =
                new Enemy(
                        restorePosition(
                                data.position()
                        ),
                        data.maxHealth(),
                        data.health(),
                        data.agility(),
                        data.strength(),
                        EnemyType.valueOf(
                                data.type()
                        ),
                        data.hostility()
                );

        enemy.restoreRuntimeState(
                data.firstAttackMiss(),
                data.restTurns(),
                data.guaranteedHit(),
                data.visible(),
                data.inCombat(),
                data.snakeHorizontalDirection()
        );

        return enemy;
    }

    private Item restoreItem(
            GameSaveData.ItemData data)
    {
        ItemType type =
                ItemType.valueOf(
                        data.type()
                );

        return switch (type)
        {
            case FOOD ->
                    new Food(
                            data.subtype(),
                            data.healthRestore()
                    );

            case ELIXIR ->
                    new Elixir(
                            data.subtype(),
                            StatType.valueOf(
                                    data.statType()
                            ),
                            data.amount()
                    );

            case SCROLL ->
                    new Scroll(
                            data.subtype(),
                            StatType.valueOf(
                                    data.statType()
                            ),
                            data.amount()
                    );

            case WEAPON ->
                    new Weapon(
                            data.subtype(),
                            data.strength()
                    );

            case TREASURE ->
                    new Treasure(
                            data.subtype(),
                            data.cost()
                    );
        };
    }

    private void restoreStatistics(
            GameStatistics statistics,
            GameSaveData.StatisticsData data)
    {
        statistics.restore(
                data.treasure(),
                data.reachedLevel(),
                data.defeatedEnemies(),
                data.foodEaten(),
                data.elixirsDrunk(),
                data.scrollsRead(),
                data.hitsDealt(),
                data.hitsReceived(),
                data.cellsWalked()
        );
    }

    private Position restorePosition(
            GameSaveData.PositionData data)
    {
        return new Position(
                data.x(),
                data.y()
        );
    }

    private Player restorePlayer(
            GameSaveData.PlayerData data)
    {
        Player player =
                new Player(
                        restorePosition(
                                data.position()
                        ),
                        data.maxHealth(),
                        data.health(),
                        data.agility(),
                        data.strength()
                );

        if (data.sleepTurns() > 0)
        {
            player.sleep(
                    data.sleepTurns()
            );
        }

        if (data.currentWeapon() != null)
        {
            player.setCurrentWeapon(
                    (Weapon) restoreItem(
                            data.currentWeapon()
                    )
            );
        }

        Backpack backpack =
                player.getBackpack();

        for (GameSaveData.ItemData item :
                data.backpack().food())
        {
            backpack.addFood(
                    (Food) restoreItem(item)
            );
        }

        for (GameSaveData.ItemData item :
                data.backpack().elixirs())
        {
            backpack.addElixir(
                    (Elixir) restoreItem(item)
            );
        }

        for (GameSaveData.ItemData item :
                data.backpack().scrolls())
        {
            backpack.addScroll(
                    (Scroll) restoreItem(item)
            );
        }

        for (GameSaveData.ItemData item :
                data.backpack().weapons())
        {
            backpack.addWeapon(
                    (Weapon) restoreItem(item)
            );
        }

        backpack.addTreasure(
                data.backpack()
                        .treasureAmount()
        );

        return player;
    }

    public GameSession fromData(
            GameSaveData data)
    {
        Player player =
                restorePlayer(
                        data.player()
                );

        GameSession session =
                new GameSession(player);

        session.restoreState(
                data.currentLevelNumber(),
                GameStatus.valueOf(
                        data.status()
                )
        );

        Level level =
                restoreLevel(
                        data.level()
                );

        session.setCurrentLevel(level);

        restoreStatistics(
                session.getStatistics(),
                data.statistics()
        );

        for (GameSaveData.TemporaryEffectData effectData :
                data.activeEffects())
        {
            TemporaryEffect effect =
                    new TemporaryEffect(
                            StatType.valueOf(
                                    effectData.statType()
                            ),
                            effectData.amount(),
                            effectData.remainingTurns()
                    );

            session.addTemporaryEffect(
                    effect
            );
        }

        return session;
    }

    private GameSaveData.PlayerData mapPlayer(
            GameSession session)
    {
        var player =
                session.getPlayer();

        GameSaveData.ItemData weapon = null;

        if (player.getCurrentWeapon() != null)
        {
            weapon =
                    mapItem(
                            player.getCurrentWeapon()
                    );
        }

        return new GameSaveData.PlayerData(
                mapPosition(
                        player.getPosition()
                ),
                player.getMaxHealth(),
                player.getHealth(),
                player.getAgility(),
                player.getStrength(),
                player.getSleepTurns(),
                weapon,
                mapBackpack(
                        player.getBackpack()
                )
        );
    }

    private GameSaveData.BackpackData mapBackpack(
            Backpack backpack)
    {
        return new GameSaveData.BackpackData(
                mapItems(backpack.getFood()),
                mapItems(backpack.getElixirs()),
                mapItems(backpack.getScrolls()),
                mapItems(backpack.getWeapons()),
                backpack.getTreasureAmount()
        );
    }

    private GameSaveData.LevelData mapLevel(
            Level level)
    {
        if (level == null)
        {
            return null;
        }

        List<GameSaveData.RoomData> rooms =
                new ArrayList<>();

        for (Room room : level.getRooms())
        {
            rooms.add(
                    mapRoom(room)
            );
        }

        List<GameSaveData.CorridorData> corridors =
                new ArrayList<>();

        for (Corridor corridor :
                level.getCorridors())
        {
            int fromIndex =
                    level.getRooms()
                            .indexOf(
                                    corridor.getFrom()
                            );

            int toIndex =
                    level.getRooms()
                            .indexOf(
                                    corridor.getTo()
                            );

            List<GameSaveData.PositionData> cells =
                    new ArrayList<>();

            for (Position position :
                    corridor.getCells())
            {
                cells.add(
                        mapPosition(position)
                );
            }

            corridors.add(
                    new GameSaveData.CorridorData(
                            fromIndex,
                            toIndex,
                            cells
                    )
            );
        }

        int startRoomIndex = -1;
        int exitRoomIndex = -1;

        if (level.getStartRoom() != null)
        {
            startRoomIndex =
                    level.getRooms()
                            .indexOf(
                                    level.getStartRoom()
                            );
        }

        if (level.getExitRoom() != null)
        {
            exitRoomIndex =
                    level.getRooms()
                            .indexOf(
                                    level.getExitRoom()
                            );
        }

        List<GameSaveData.EnemyData> enemies =
                new ArrayList<>();

        for (Enemy enemy :
                level.getEnemies())
        {
            enemies.add(
                    mapEnemy(enemy)
            );
        }

        List<GameSaveData.GroundItemData> items =
                new ArrayList<>();

        for (GroundItem groundItem :
                level.getGroundItems())
        {
            items.add(
                    new GameSaveData.GroundItemData(
                            mapPosition(
                                    groundItem
                                            .getPosition()
                            ),
                            mapItem(
                                    groundItem
                                            .getItem()
                            )
                    )
            );
        }

        return new GameSaveData.LevelData(
                level.getLevelNumber(),
                rooms,
                corridors,
                startRoomIndex,
                exitRoomIndex,
                enemies,
                items
        );
    }

    private GameSaveData.RoomData mapRoom(
            Room room)
    {
        return new GameSaveData.RoomData(
                room.getX(),
                room.getY(),
                room.getWidth(),
                room.getHeight()
        );
    }

    private GameSaveData.EnemyData mapEnemy(
            Enemy enemy)
    {
        return new GameSaveData.EnemyData(
                enemy.getType().name(),
                mapPosition(
                        enemy.getPosition()
                ),
                enemy.getMaxHealth(),
                enemy.getHealth(),
                enemy.getAgility(),
                enemy.getStrength(),
                enemy.getHostility(),
                enemy.isFirstAttackMissPending(),
                enemy.getRestTurns(),
                enemy.isGuaranteedHitPending(),
                enemy.isVisible(),
                enemy.isInCombat(),
                enemy.getSnakeHorizontalDirection()
        );
    }

    private GameSaveData.ItemData mapItem(
            Item item)
    {
        String statType = null;
        int amount = 0;
        int healthRestore = 0;
        int strength = 0;
        int cost = 0;

        if (item instanceof Food food)
        {
            healthRestore =
                    food.getHealthRestore();
        }
        else if (item instanceof Elixir elixir)
        {
            statType =
                    elixir.getStatType()
                            .name();

            amount =
                    elixir.getAmount();
        }
        else if (item instanceof Scroll scroll)
        {
            statType =
                    scroll.getStatType()
                            .name();

            amount =
                    scroll.getAmount();
        }
        else if (item instanceof Weapon weapon)
        {
            strength =
                    weapon.getStrength();
        }
        else if (item instanceof Treasure treasure)
        {
            cost =
                    treasure.getCost();
        }

        return new GameSaveData.ItemData(
                item.getType().name(),
                item.getSubtype(),
                statType,
                amount,
                healthRestore,
                strength,
                cost
        );
    }

    private List<GameSaveData.ItemData> mapItems(
            List<? extends Item> items)
    {
        List<GameSaveData.ItemData> result =
                new ArrayList<>();

        for (Item item : items)
        {
            result.add(
                    mapItem(item)
            );
        }

        return result;
    }

    private List<GameSaveData.TemporaryEffectData> mapEffects(
            List<TemporaryEffect> effects)
    {
        List<GameSaveData.TemporaryEffectData> result =
                new ArrayList<>();

        for (TemporaryEffect effect :
                effects)
        {
            result.add(
                    new GameSaveData.TemporaryEffectData(
                            effect.getStatType()
                                    .name(),
                            effect.getAmount(),
                            effect.getRemainingTurns()
                    )
            );
        }

        return result;
    }

    private GameSaveData.StatisticsData mapStatistics(
            GameStatistics statistics)
    {
        return new GameSaveData.StatisticsData(
                statistics.getTreasure(),
                statistics.getReachedLevel(),
                statistics.getDefeatedEnemies(),
                statistics.getFoodEaten(),
                statistics.getElixirsDrunk(),
                statistics.getScrollsRead(),
                statistics.getHitsDealt(),
                statistics.getHitsReceived(),
                statistics.getCellsWalked()
        );
    }

    private GameSaveData.PositionData mapPosition(
            Position position)
    {
        return new GameSaveData.PositionData(
                position.x(),
                position.y()
        );
    }
}