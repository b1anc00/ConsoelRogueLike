package domain.gameplay;

import domain.model.Enemy;
import domain.model.GameSession;
import domain.model.TemporaryEffect;
import java.util.ArrayList;
import java.util.List;

public class TurnSystem
{
    private final ItemSystem itemSystem;

    public TurnSystem(ItemSystem itemSystem)
    {
        this.itemSystem = itemSystem;
    }

    public boolean canPlayerAct(GameSession session)
    {
        if (!session.getPlayer().isSleeping())
        {
            return true;
        }

        session.getPlayer().tickSleep();

        return false;
    }

    public boolean canEnemyAct(Enemy enemy)
    {
        if (!enemy.isResting())
        {
            return true;
        }

        enemy.tickRest();

        return false;
    }

    public void finishTurn(GameSession session)
    {
        processTemporaryEffects(session);

        if (!session.getPlayer().isAlive())
        {
            session.lose();
        }
    }

    private void processTemporaryEffects(
            GameSession session)
    {
        List<TemporaryEffect> effects =
                new ArrayList<>(
                        session.getActiveEffects()
                );

        for (TemporaryEffect effect : effects)
        {
            if (effect.tick())
            {
                itemSystem.expireEffect(
                        session.getPlayer(),
                        effect
                );

                session.removeTemporaryEffect(
                        effect
                );
            }
        }
    }
}