package presentation;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import domain.model.Food;
import domain.model.Elixir;
import domain.model.Scroll;
import domain.model.StatType;
import domain.model.Backpack;
import domain.model.GameSession;
import domain.model.Item;
import domain.model.Weapon;

import java.io.IOException;
import java.util.List;

public class InventoryView
{
    public enum Category
    {
        WEAPONS("Weapons"),
        FOOD("Food"),
        ELIXIRS("Elixirs"),
        SCROLLS("Scrolls");

        private final String title;

        Category(String title)
        {
            this.title = title;
        }

        public String getTitle()
        {
            return title;
        }
    }

    public record Selection(Category category, int index)
    {
    }

    private final Screen screen;
    private final InputController inputController;

    public InventoryView(Screen screen, InputController inputController)
    {
        this.screen = screen;
        this.inputController = inputController;
    }

    public Selection showInventory(GameSession session, char initialCategory)
            throws IOException
    {
        Category category = getInitialCategory(initialCategory);

        while (true)
        {
            render(session, category);

            KeyStroke key = inputController.readKey();

            if (key.getKeyType() == KeyType.ArrowUp)
            {
                category = changeCategory(category, -1);
                continue;
            }

            if (key.getKeyType() == KeyType.ArrowDown
                    || key.getKeyType() == KeyType.Tab)
            {
                category = changeCategory(category, 1);
                continue;
            }

            if (key.getKeyType() != KeyType.Character)
            {
                continue;
            }

            char c = Character.toLowerCase(key.getCharacter());

            if (c == 'q')
            {
                return null;
            }

            if (c == 'w')
            {
                category = changeCategory(category, -1);
                continue;
            }

            if (c == 's')
            {
                category = changeCategory(category, 1);
                continue;
            }

            if (c == 'h' || c == 'j' || c == 'k' || c == 'e')
            {
                category = getInitialCategory(c);
                continue;
            }

            if (c == '0' && category == Category.WEAPONS)
            {
                return new Selection(category, -1);
            }

            if (c >= '1' && c <= '9')
            {
                int index = c - '1';

                List<? extends Item> items =
                        getItems(session.getPlayer().getBackpack(), category);

                if (index < items.size())
                {
                    return new Selection(category, index);
                }
            }
        }
    }

    private Category getInitialCategory(char key)
    {
        return switch (Character.toLowerCase(key))
        {
            case 'j' -> Category.FOOD;
            case 'k' -> Category.ELIXIRS;
            case 'e' -> Category.SCROLLS;
            default -> Category.WEAPONS;
        };
    }

    private Category changeCategory(Category current, int direction)
    {
        Category[] categories = Category.values();

        int next = Math.floorMod(
                current.ordinal() + direction,
                categories.length
        );

        return categories[next];
    }

    private List<? extends Item> getItems(Backpack backpack, Category category)
    {
        return switch (category)
        {
            case WEAPONS -> backpack.getWeapons();
            case FOOD -> backpack.getFood();
            case ELIXIRS -> backpack.getElixirs();
            case SCROLLS -> backpack.getScrolls();
        };
    }

    private String getItemDescription(Item item)
    {
        String name = item.getSubtype();

        if (name == null || name.isBlank())
        {
            name = item.getClass().getSimpleName();
        }

        if (item instanceof Food food)
        {
            return name + " | +" + food.getHealthRestore() + " HP";
        }

        if (item instanceof Elixir elixir)
        {
            return name
                    + " | "
                    + getStatName(elixir.getStatType())
                    + " +"
                    + elixir.getAmount()
                    + " (Temp)";
        }

        if (item instanceof Scroll scroll)
        {
            return name
                    + " | "
                    + getStatName(scroll.getStatType())
                    + " +"
                    + scroll.getAmount()
                    + " (Permanent)";
        }

        if (item instanceof Weapon weapon)
        {
            return name
                    + " | +"
                    + weapon.getStrength()
                    + " STR";
        }

        return name;
    }

    private String getStatName(StatType statType)
    {
        return switch (statType)
        {
            case MAX_HEALTH -> "Max HP";
            case AGILITY -> "AGI";
            case STRENGTH -> "STR";
        };
    }

    private void render(GameSession session, Category category)
            throws IOException
    {
        screen.clear();

        TextGraphics graphics = screen.newTextGraphics();

        int screenWidth = screen.getTerminalSize().getColumns();
        int screenHeight = screen.getTerminalSize().getRows();

        int startX = Math.max(2, (screenWidth - 80) / 2);
        int startY = Math.max(1, (screenHeight - 22) / 2);

        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(startX, startY, "=== INVENTORY ===");

        graphics.setForegroundColor(TextColor.ANSI.DEFAULT);
        graphics.putString(
                startX,
                startY + 1,
                "W/S or Arrows: Change category | Q: Close inventory"
        );

        graphics.putString(
                startX,
                startY + 2,
                "1-9: Select and use/equip | H/J/K/E: Jump to category"
        );

        int dividerX = startX + 20;

        for (int y = startY + 3; y < startY + 19; y++)
        {
            graphics.setCharacter(dividerX, y, '|');
        }

        Category[] categories = Category.values();

        for (int i = 0; i < categories.length; i++)
        {
            Category current = categories[i];

            graphics.setForegroundColor(
                    current == category
                            ? TextColor.ANSI.YELLOW
                            : TextColor.ANSI.DEFAULT
            );

            String prefix = current == category ? "> " : "  ";

            graphics.putString(
                    startX,
                    startY + 4 + i * 2,
                    prefix + current.getTitle()
            );
        }

        graphics.setForegroundColor(TextColor.ANSI.YELLOW);
        graphics.putString(
                dividerX + 3,
                startY + 3,
                category.getTitle().toUpperCase()
        );

        graphics.setForegroundColor(TextColor.ANSI.DEFAULT);

        Backpack backpack = session.getPlayer().getBackpack();

        List<? extends Item> items = getItems(backpack, category);

        if (items.isEmpty())
        {
            graphics.putString(
                    dividerX + 3,
                    startY + 5,
                    "No items"
            );
        }
        else
        {
            for (int i = 0; i < items.size() && i < 9; i++)
            {
                Item item = items.get(i);

                String description = getItemDescription(item);

                graphics.putString(
                        dividerX + 3,
                        startY + 5 + i,
                        (i + 1) + ". " + description
                );
            }
        }

        if (category == Category.WEAPONS)
        {
            Weapon equipped = session.getPlayer().getCurrentWeapon();

            String equippedName = equipped == null
                    ? "None"
                    : equipped.getSubtype();

            graphics.setForegroundColor(TextColor.ANSI.YELLOW);

            graphics.putString(
                    dividerX + 3,
                    startY + 16,
                    "Equipped: " + equippedName
            );

            graphics.setForegroundColor(TextColor.ANSI.DEFAULT);

            graphics.putString(
                    dividerX + 3,
                    startY + 18,
                    "0. Unequip weapon"
            );
        }

        screen.refresh();
    }
}