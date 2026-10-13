package presentation;

import com.googlecode.lanterna.graphics.TextGraphics;
import domain.model.Backpack;
import domain.model.Level;
import domain.model.Player;
import domain.model.Weapon;

public class HudRenderer {
    private int startX;
    private final int startY;

    public HudRenderer(int startX, int startY) {
        this.startX = startX;
        this.startY = startY;
    }

    public void setStartX(int startX)
    {
        this.startX = startX;
    }

    public void render(TextGraphics graphics, Player player, Level level) {
        int y = startY;
        
        // Заголовок
        graphics.putString(startX, y++, "=== STATUS ===");
        graphics.putString(startX, y++, "Level: " + level.getLevelNumber());
        graphics.putString(startX, y++, "HP: " + player.getHealth() + "/" + player.getMaxHealth());
        graphics.putString(startX, y++, "Strength: " + player.getStrength());
        graphics.putString(startX, y++, "Agility: " + player.getAgility());
        
        Weapon weapon = player.getCurrentWeapon();
        String weaponInfo = "None";

        if (weapon != null)
        {
            weaponInfo = weapon.getSubtype()
                    + " (+" + weapon.getStrength() + " STR)";
        }

        String weaponLine = "Weapon: " + weaponInfo;

        graphics.putString(
                startX,
                y++,
                weaponLine.substring(0, Math.min(27, weaponLine.length()))
        );
        graphics.putString(startX, y++, "Treasure: " + player.getBackpack().getTreasureAmount());
        
        y++;
        graphics.putString(startX, y++, "=== INVENTORY ===");
        graphics.putString(startX, y++, "Food: " + player.getBackpack().getFood().size());
        graphics.putString(startX, y++, "Elixirs: " + player.getBackpack().getElixirs().size());
        graphics.putString(startX, y++, "Scrolls: " + player.getBackpack().getScrolls().size());
        graphics.putString(startX, y++, "Weapons: " + player.getBackpack().getWeapons().size());
        
        y++;
        graphics.putString(startX, y++, "=== CONTROLS ===");
        graphics.putString(startX, y++, "WASD - Move");
        graphics.putString(startX, y++, "I - Inventory");
        graphics.putString(startX, y++, "E - Inventory (Scrolls)");
        graphics.putString(startX, y++, "Q - Main Menu");
    }
}