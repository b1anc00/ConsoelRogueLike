package domain.gameplay;

import domain.model.Weapon;

public class WeaponEquipResult
{
    private final boolean success;
    private final Weapon droppedWeapon;

    public WeaponEquipResult(boolean success, Weapon droppedWeapon)
    {
        this.success = success;
        this.droppedWeapon = droppedWeapon;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public Weapon getDroppedWeapon()
    {
        return droppedWeapon;
    }
}