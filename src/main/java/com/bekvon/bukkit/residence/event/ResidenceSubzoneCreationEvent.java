package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ResidenceSubzoneCreationEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    protected String resname;
    CuboidArea area;

    public ResidenceSubzoneCreationEvent(Player player, String name, ClaimedResidence resref, CuboidArea resarea) {
        super("RESIDENCE_SUBZONE_CREATE", resref, player);
        resname = name;
        area = resarea;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getResidenceName() {
        return resname;
    }

    public void setResidenceName(String name) {
        resname = name;
    }

    public CuboidArea getPhysicalArea() {
        return area;
    }

    public void setPhysicalArea(CuboidArea newarea) {
        area = newarea;
    }
}
