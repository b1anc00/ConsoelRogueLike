package domain.gameplay;

import domain.model.Enemy;
import domain.model.GameSession;
import domain.model.Item;
import domain.model.Room;
import domain.model.Player;
import domain.model.GroundItem;
import domain.model.Level;
import domain.model.Position;
import domain.model.TemporaryEffect;
import domain.model.Treasure;
import domain.model.Elixir;
import domain.model.Food;
import domain.model.Scroll;
import domain.model.Weapon;

public class GameplayService
{
    private final GameSession session;

    private final CombatSystem combatSystem;
    private final InventorySystem inventorySystem;
    private final ItemSystem itemSystem;
    private final RewardSystem rewardSystem;
    private final TurnSystem turnSystem;
    private final MovementSystem movementSystem;
    private final EnemyMovementSystem enemyMovementSystem;
    private final EnemyBehaviorSystem enemyBehaviorSystem;
    private final LevelProgressionSystem levelProgressionSystem;
    private final ItemPlacementSystem itemPlacementSystem;
    private static final int ELIXIR_DURATION_TURNS = 10;

    public GameplayService(GameSession session)
    {
        this.session = session;

        this.combatSystem = new CombatSystem();
        this.inventorySystem = new InventorySystem();
        this.itemSystem = new ItemSystem();
        this.rewardSystem = new RewardSystem();
        this.turnSystem = new TurnSystem(itemSystem);
        this.movementSystem = new MovementSystem();
        this.enemyMovementSystem = new EnemyMovementSystem(movementSystem);
        this.enemyBehaviorSystem = new EnemyBehaviorSystem(movementSystem);
        this.levelProgressionSystem = new LevelProgressionSystem();
        this.itemPlacementSystem = new ItemPlacementSystem(movementSystem);
    }

    private InventoryActionResult prepareInventoryAction()
    {
        if (!session.isRunning())
        {
            return new InventoryActionResult(
                    InventoryActionType.GAME_NOT_RUNNING
            );
        }

        if (!beginPlayerTurn())
        {
            processEnemyTurns();
            finishTurn();

            return new InventoryActionResult(
                    InventoryActionType.SLEEPING
            );
        }

        return null;
    }

    private void finishInventoryAction()
    {
        processEnemyTurns();
        finishTurn();
    }

    public InventoryActionResult performUnequipWeapon()
    {
        InventoryActionResult state =
                prepareInventoryAction();

        if (state != null)
        {
            return state;
        }

        Player player =
                session.getPlayer();

        Weapon weapon =
                player.getCurrentWeapon();

        if (weapon == null)
        {
            return new InventoryActionResult(
                    InventoryActionType.NO_WEAPON_EQUIPPED
            );
        }

        if (!inventorySystem.unequipWeapon(player))
        {
            return new InventoryActionResult(
                    InventoryActionType.INVENTORY_FULL
            );
        }

        finishInventoryAction();

        return new InventoryActionResult(
                InventoryActionType.WEAPON_UNEQUIPPED,
                weapon
        );
    }

    public InventoryActionResult performEquipWeapon(
            int index)
    {
        InventoryActionResult state =
                prepareInventoryAction();

        if (state != null)
        {
            return state;
        }

        Player player =
                session.getPlayer();

        if (index < 0
                || index >= player
                .getBackpack()
                .getWeapons()
                .size())
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        Weapon selectedWeapon =
                player.getBackpack()
                        .getWeapons()
                        .get(index);

        Weapon currentWeapon =
                player.getCurrentWeapon();

        Position dropPosition = null;

        if (currentWeapon != null)
        {
            Level level =
                    session.getCurrentLevel();

            if (level == null)
            {
                return new InventoryActionResult(
                        InventoryActionType.NO_DROP_SPACE
                );
            }

            dropPosition =
                    itemPlacementSystem
                            .findFreeAdjacentPosition(
                                    level,
                                    player
                            );

            if (dropPosition == null)
            {
                return new InventoryActionResult(
                        InventoryActionType.NO_DROP_SPACE
                );
            }
        }

        WeaponEquipResult equipResult =
                inventorySystem.equipWeapon(
                        player,
                        index
                );

        if (!equipResult.isSuccess())
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        Weapon droppedWeapon =
                equipResult.getDroppedWeapon();

        if (droppedWeapon != null)
        {
            session.getCurrentLevel()
                    .addGroundItem(
                            new GroundItem(
                                    droppedWeapon,
                                    dropPosition
                            )
                    );
        }

        finishInventoryAction();

        return new InventoryActionResult(
                InventoryActionType.WEAPON_EQUIPPED,
                selectedWeapon,
                dropPosition
        );
    }

    public InventoryActionResult performUseFood(
            int index)
    {
        InventoryActionResult state =
                prepareInventoryAction();

        if (state != null)
        {
            return state;
        }

        if (index < 0
                || index >= session.getPlayer()
                .getBackpack()
                .getFood()
                .size())
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        Food food =
                session.getPlayer()
                        .getBackpack()
                        .getFood()
                        .get(index);

        if (!useFood(index))
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        finishInventoryAction();

        return new InventoryActionResult(
                InventoryActionType.FOOD_USED,
                food
        );
    }

    public InventoryActionResult performUseScroll(
            int index)
    {
        InventoryActionResult state =
                prepareInventoryAction();

        if (state != null)
        {
            return state;
        }

        if (index < 0
                || index >= session.getPlayer()
                .getBackpack()
                .getScrolls()
                .size())
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        Scroll scroll =
                session.getPlayer()
                        .getBackpack()
                        .getScrolls()
                        .get(index);

        if (!useScroll(index))
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        finishInventoryAction();

        return new InventoryActionResult(
                InventoryActionType.SCROLL_USED,
                scroll
        );
    }

    public InventoryActionResult performUseElixir(
            int index)
    {
        InventoryActionResult state =
                prepareInventoryAction();

        if (state != null)
        {
            return state;
        }

        if (index < 0
                || index >= session.getPlayer()
                .getBackpack()
                .getElixirs()
                .size())
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        Elixir elixir =
                session.getPlayer()
                        .getBackpack()
                        .getElixirs()
                        .get(index);

        boolean used =
                useElixir(
                        index,
                        ELIXIR_DURATION_TURNS
                );

        if (!used)
        {
            return new InventoryActionResult(
                    InventoryActionType.INVALID_INDEX
            );
        }

        finishInventoryAction();

        return new InventoryActionResult(
                InventoryActionType.ELIXIR_USED,
                elixir
        );
    }

    public boolean beginPlayerTurn()
    {
        return turnSystem.canPlayerAct(session);
    }

    public boolean beginEnemyTurn(Enemy enemy)
    {
        return turnSystem.canEnemyAct(enemy);
    }

    public void finishTurn()
    {
        turnSystem.finishTurn(session);
    }

    public void processEnemyTurns()
    {
        Level level =
                session.getCurrentLevel();

        if (level == null
                || !session.isRunning())
        {
            return;
        }

        Player player =
                session.getPlayer();

        for (Enemy enemy : level.getEnemies())
        {
            if (!enemy.isAlive())
            {
                continue;
            }

            if (!player.isAlive())
            {
                break;
            }

            if (!turnSystem.canEnemyAct(enemy))
            {
                continue;
            }

            if (enemyMovementSystem.isAdjacent(
                    enemy,
                    player))
            {
                enemyAttack(enemy);
                continue;
            }

            boolean chasing =
                    enemyMovementSystem.moveTowardPlayer(
                            enemy,
                            player,
                            level
                    );

            if (!chasing)
            {
                enemyBehaviorSystem.moveByPattern(
                        enemy,
                        player,
                        level
                );
            }
        }

        if (!player.isAlive())
        {
            session.lose();
        }
    }

    public PlayerActionResult moveOrInteract(int deltaX, int deltaY)
    {
        Level level = session.getCurrentLevel();

        if (level == null)
        {
            return new PlayerActionResult(
                    PlayerActionType.BLOCKED
            );
        }

        if (Math.abs(deltaX) + Math.abs(deltaY) != 1)
        {
            return new PlayerActionResult(
                    PlayerActionType.INVALID_DIRECTION
            );
        }

        Player player = session.getPlayer();
        Position current = player.getPosition();

        Position target = new Position(
                current.x() + deltaX,
                current.y() + deltaY
        );

        Enemy enemy = level.getEnemyAt(target);

        if (enemy != null)
        {
            AttackResult attack = playerAttack(enemy);

            if (attack.isKilled())
            {
                level.removeEnemy(enemy);

                return new PlayerActionResult(
                        PlayerActionType.ENEMY_KILLED,
                        attack
                );
            }

            return new PlayerActionResult(
                    PlayerActionType.ATTACKED,
                    attack
            );
        }

        MovementResult movement = movementSystem.movePlayer(
                player,
                level,
                deltaX,
                deltaY
        );

        if (movement == MovementResult.BLOCKED)
        {
            return new PlayerActionResult(
                    PlayerActionType.BLOCKED
            );
        }

        if (movement == MovementResult.INVALID_DIRECTION)
        {
            return new PlayerActionResult(
                    PlayerActionType.INVALID_DIRECTION
            );
        }

        session.getStatistics().incrementCellsWalked();

        Room exitRoom = level.getExitRoom();

        if (exitRoom != null)
        {
            Position exitPosition = new Position(
                    exitRoom.getCenterX(),
                    exitRoom.getCenterY()
            );

            if (player.getPosition().equals(exitPosition))
            {
                return new PlayerActionResult(
                        PlayerActionType.LEVEL_EXIT
                );
            }
        }

        GroundItem groundItem = level.getGroundItemAt(
                player.getPosition()
        );

        if (groundItem != null)
        {
            boolean picked = pickUpItem(
                    groundItem.getItem()
            );

            if (picked)
            {
                level.removeGroundItem(groundItem);

                return new PlayerActionResult(
                        PlayerActionType.ITEM_PICKED_UP,
                        groundItem.getItem()
                );
            }

            return new PlayerActionResult(
                    PlayerActionType.INVENTORY_FULL,
                    groundItem.getItem()
            );
        }

        return new PlayerActionResult(
                PlayerActionType.MOVED
        );
    }
    public MovementResult movePlayer(
            int deltaX,
            int deltaY)
    {
        if (session.getCurrentLevel() == null)
        {
            return MovementResult.BLOCKED;
        }

        MovementResult result =
                movementSystem.movePlayer(
                        session.getPlayer(),
                        session.getCurrentLevel(),
                        deltaX,
                        deltaY
                );

        if (result == MovementResult.MOVED)
        {
            session.getStatistics()
                    .incrementCellsWalked();
        }

        return result;
    }

    public boolean pickUpItem(Item item)
    {
        boolean pickedUp =
                inventorySystem.pickUpItem(
                        session.getPlayer(),
                        item
                );

        if (pickedUp && item instanceof Treasure treasure)
        {
            session.getStatistics()
                    .addTreasure(treasure.getCost());
        }

        return pickedUp;
    }

    public boolean useFood(int index)
    {
        boolean used =
                itemSystem.useFood(
                        session.getPlayer(),
                        index
                );

        if (used)
        {
            session.getStatistics()
                    .incrementFoodEaten();
        }

        return used;
    }

    public boolean useScroll(int index)
    {
        boolean used =
                itemSystem.useScroll(
                        session.getPlayer(),
                        index
                );

        if (used)
        {
            session.getStatistics()
                    .incrementScrollsRead();
        }

        return used;
    }

    public boolean useElixir(
            int index,
            int durationTurns)
    {
        TemporaryEffect effect =
                itemSystem.useElixir(
                        session.getPlayer(),
                        index,
                        durationTurns
                );

        if (effect == null)
        {
            return false;
        }

        session.addTemporaryEffect(effect);

        session.getStatistics()
                .incrementElixirsDrunk();

        return true;
    }

    public WeaponEquipResult equipWeapon(int index)
    {
        return inventorySystem.equipWeapon(
                session.getPlayer(),
                index
        );
    }

    public boolean unequipWeapon()
    {
        return inventorySystem.unequipWeapon(
                session.getPlayer()
        );
    }

    public AttackResult playerAttack(Enemy enemy)
    {
        Player player = session.getPlayer();

        AttackResult result =
                combatSystem.attack(
                        player,
                        enemy
                );

        if (result.isHit())
        {
            session.getStatistics()
                    .incrementHitsDealt();
        }

        if (result.isKilled())
        {
            rewardSystem.giveEnemyReward(
                    session,
                    enemy
            );
        }

        return result;
    }

    public AttackResult enemyAttack(Enemy enemy)
    {
        Player player = session.getPlayer();

        AttackResult result =
                combatSystem.attack(
                        enemy,
                        player
                );

        if (result.isHit())
        {
            session.getStatistics()
                    .incrementHitsReceived();
        }

        if (!player.isAlive())
        {
            session.lose();
        }

        return result;
    }

    public void nextLevel()
    {
        session.nextLevel();
    }

    public GameSession getSession()
    {
        return session;
    }

    public PlayerActionResult performPlayerMove(
            int deltaX,
            int deltaY)
    {
        if (!session.isRunning())
        {
            return new PlayerActionResult(
                    PlayerActionType.BLOCKED
            );
        }

        if (!beginPlayerTurn())
        {
            processEnemyTurns();
            finishTurn();

            return new PlayerActionResult(
                    PlayerActionType.SLEEPING
            );
        }

        PlayerActionResult result =
                moveOrInteract(
                        deltaX,
                        deltaY
                );

        if (!consumesTurn(result))
        {
            return result;
        }

        if (result.getType()
                == PlayerActionType.LEVEL_EXIT)
        {
            finishTurn();

            if (session.isRunning())
            {
                levelProgressionSystem
                        .advanceToNextLevel(
                                session
                        );
            }

            return result;
        }

        processEnemyTurns();
        finishTurn();

        return result;
    }

    private boolean consumesTurn(
            PlayerActionResult result)
    {
        return switch (result.getType())
        {
            case MOVED,
                 BLOCKED,
                 ITEM_PICKED_UP,
                 INVENTORY_FULL,
                 ATTACKED,
                 ENEMY_KILLED,
                 LEVEL_EXIT -> true;

            case INVALID_DIRECTION,
                 SLEEPING -> false;
        };
    }
}