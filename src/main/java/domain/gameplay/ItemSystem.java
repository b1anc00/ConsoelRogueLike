package domain.gameplay;

import domain.model.Elixir;
import domain.model.Food;
import domain.model.Player;
import domain.model.Scroll;
import domain.model.StatType;
import domain.model.TemporaryEffect;

public class ItemSystem
{
    public boolean useFood(Player player, int index)
    {
        Food food = player.getBackpack().removeFood(index);

        if (food == null)
        {
            return false;
        }

        int newHealth = player.getHealth() + food.getHealthRestore();

        if (newHealth > player.getMaxHealth())
        {
            newHealth = player.getMaxHealth();
        }

        player.setHealth(newHealth);

        return true;
    }

    public boolean useScroll(Player player, int index)
    {
        Scroll scroll = player.getBackpack().removeScroll(index);

        if (scroll == null)
        {
            return false;
        }

        applyStatIncrease(
                player,
                scroll.getStatType(),
                scroll.getAmount()
        );

        return true;
    }

    public TemporaryEffect useElixir(
            Player player,
            int index,
            int durationTurns)
    {
        if (durationTurns <= 0)
        {
            return null;
        }

        Elixir elixir = player.getBackpack().removeElixir(index);

        if (elixir == null)
        {
            return null;
        }

        applyStatIncrease(
                player,
                elixir.getStatType(),
                elixir.getAmount()
        );

        return new TemporaryEffect(
                elixir.getStatType(),
                elixir.getAmount(),
                durationTurns
        );
    }

    public void expireEffect(Player player, TemporaryEffect effect)
    {
        switch (effect.getStatType())
        {
            case MAX_HEALTH:
                player.setMaxHealth(
                        player.getMaxHealth() - effect.getAmount()
                );

                int newHealth =
                        player.getHealth() - effect.getAmount();

                if (newHealth <= 0)
                {
                    newHealth = 1;
                }

                player.setHealth(newHealth);
                break;

            case AGILITY:
                player.setAgility(
                        player.getAgility() - effect.getAmount()
                );
                break;

            case STRENGTH:
                player.setStrength(
                        player.getStrength() - effect.getAmount()
                );
                break;
        }
    }

    private void applyStatIncrease(
            Player player,
            StatType statType,
            int amount)
    {
        switch (statType)
        {
            case MAX_HEALTH:
                player.setMaxHealth(
                        player.getMaxHealth() + amount
                );

                player.setHealth(
                        player.getHealth() + amount
                );
                break;

            case AGILITY:
                player.setAgility(
                        player.getAgility() + amount
                );
                break;

            case STRENGTH:
                player.setStrength(
                        player.getStrength() + amount
                );
                break;
        }
    }
}