package domain.model;

import java.util.ArrayList;
import java.util.List;

public class Backpack
{
    private static final int MAX_ITEMS_PER_TYPE = 9;

    private final List<Food> food;
    private final List<Elixir> elixirs;
    private final List<Scroll> scrolls;
    private final List<Weapon> weapons;

    private int treasureAmount;

    public Backpack()
    {
        food = new ArrayList<>();
        elixirs = new ArrayList<>();
        scrolls = new ArrayList<>();
        weapons = new ArrayList<>();
        treasureAmount = 0;
    }

    public boolean addFood(Food item)
    {
        if (food.size() >= MAX_ITEMS_PER_TYPE)
        {
            return false;
        }

        food.add(item);
        return true;
    }

    public boolean addElixir(Elixir item)
    {
        if (elixirs.size() >= MAX_ITEMS_PER_TYPE)
        {
            return false;
        }

        elixirs.add(item);
        return true;
    }

    public boolean addScroll(Scroll item)
    {
        if (scrolls.size() >= MAX_ITEMS_PER_TYPE)
        {
            return false;
        }

        scrolls.add(item);
        return true;
    }

    public boolean addWeapon(Weapon item)
    {
        if (weapons.size() >= MAX_ITEMS_PER_TYPE)
        {
            return false;
        }

        weapons.add(item);
        return true;
    }

    public void addTreasure(int amount)
    {
        treasureAmount += amount;
    }

    public List<Food> getFood()
    {
        return food;
    }

    public List<Elixir> getElixirs()
    {
        return elixirs;
    }

    public List<Scroll> getScrolls()
    {
        return scrolls;
    }

    public List<Weapon> getWeapons()
    {
        return weapons;
    }

    public int getTreasureAmount()
    {
        return treasureAmount;
    }

    public Food removeFood(int index)
    {
        if (index < 0 || index >= food.size())
        {
            return null;
        }

        return food.remove(index);
    }

    public Elixir removeElixir(int index)
    {
        if (index < 0 || index >= elixirs.size())
        {
            return null;
        }

        return elixirs.remove(index);
    }

    public Scroll removeScroll(int index)
    {
        if (index < 0 || index >= scrolls.size())
        {
            return null;
        }

        return scrolls.remove(index);
    }

    public Weapon removeWeapon(int index)
    {
        if (index < 0 || index >= weapons.size())
        {
            return null;
        }

        return weapons.remove(index);
    }
}