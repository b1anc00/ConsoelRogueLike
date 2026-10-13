package domain.gameplay;

import domain.model.Elixir;
import domain.model.Food;
import domain.model.Item;
import domain.model.Player;
import domain.model.Scroll;
import domain.model.Treasure;
import domain.model.Weapon;

public class InventorySystem
{
    public boolean pickUpItem(Player player, Item item)
    {
        if (item instanceof Food food)
        {
            return player.getBackpack().addFood(food);
        }

        if (item instanceof Elixir elixir)
        {
            return player.getBackpack().addElixir(elixir);
        }

        if (item instanceof Scroll scroll)
        {
            return player.getBackpack().addScroll(scroll);
        }

        if (item instanceof Weapon weapon)
        {
            return player.getBackpack().addWeapon(weapon);
        }

        if (item instanceof Treasure treasure)
        {
            player.getBackpack().addTreasure(treasure.getCost());
            return true;
        }

        return false;
    }

    public WeaponEquipResult equipWeapon(Player player, int index)
    {
        Weapon newWeapon =
                player.getBackpack().removeWeapon(index);

        if (newWeapon == null)
        {
            return new WeaponEquipResult(false, null);
        }

        Weapon oldWeapon = player.getCurrentWeapon();

        player.setCurrentWeapon(newWeapon);

        return new WeaponEquipResult(true, oldWeapon);
    }

    public boolean unequipWeapon(Player player)
    {
        Weapon currentWeapon = player.getCurrentWeapon();

        if (currentWeapon == null)
        {
            return false;
        }

        boolean added =
                player.getBackpack().addWeapon(currentWeapon);

        if (!added)
        {
            return false;
        }

        player.setCurrentWeapon(null);

        return true;
    }
}