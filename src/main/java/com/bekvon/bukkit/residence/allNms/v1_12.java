package com.bekvon.bukkit.residence.allNms;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.containers.NMS;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class v1_12 implements NMS {
    @Override
    public List<Block> getPistonRetractBlocks(BlockPistonRetractEvent event) {
        List<Block> blocks = new ArrayList<Block>();
        blocks.addAll(event.getBlocks());
        return blocks;
    }

    @Override
    public boolean isAnimal(Entity ent) {
        return (ent instanceof Horse ||
                ent instanceof Bat ||
                ent instanceof Snowman ||
                ent instanceof IronGolem ||
                ent instanceof Ocelot ||
                ent instanceof Pig ||
                ent instanceof Sheep ||
                ent instanceof Chicken ||
                ent instanceof Wolf ||
                ent instanceof Cow ||
                ent instanceof Squid ||
                ent instanceof Villager ||
                ent instanceof Rabbit ||
                ent instanceof Llama ||
                ent instanceof PolarBear ||
                ent instanceof Parrot ||
                ent instanceof Donkey);
    }

    @Override
    public boolean isArmorStandEntity(EntityType ent) {
        return ent == EntityType.ARMOR_STAND;
    }

    @Override
    public boolean isArmorStandMaterial(Material material) {
        return material == Material.ARMOR_STAND;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isCanUseEntity_BothClick(Material mat, Block block) {
        switch (mat) {
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case WOODEN_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case ACACIA_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
            case FENCE_GATE:
            case PISTON_BASE:
            case PISTON_STICKY_BASE:
            case DRAGON_EGG:
                return true;
            default:
                return Residence.getInstance().getConfigManager().getCustomBothClick().contains(Integer.valueOf(block.getTypeId()));
        }
    }

    @Override
    public boolean isEmptyBlock(Block block) {
        switch (block.getType()) {
            case AIR:
            case WEB:
            case STRING:
            case WALL_BANNER:
            case WALL_SIGN:
            case SAPLING:
            case VINE:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case PAINTING:
            case ITEM_FRAME:
                return true;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean isSpectator(GameMode mode) {
        return mode == GameMode.SPECTATOR;
    }

    @Override
    public void addDefaultFlags(Map<Material, Flags> matUseFlagList) {
    /* 1.8 Doors */
        matUseFlagList.put(Material.SPRUCE_DOOR, Flags.door);
        matUseFlagList.put(Material.BIRCH_DOOR, Flags.door);
        matUseFlagList.put(Material.JUNGLE_DOOR, Flags.door);
        matUseFlagList.put(Material.ACACIA_DOOR, Flags.door);
        matUseFlagList.put(Material.DARK_OAK_DOOR, Flags.door);
	/* 1.8 Fence Gates */
        matUseFlagList.put(Material.SPRUCE_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.BIRCH_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.JUNGLE_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.ACACIA_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.DARK_OAK_FENCE_GATE, Flags.door);
        matUseFlagList.put(Material.IRON_TRAPDOOR, Flags.door);

        matUseFlagList.put(Material.DAYLIGHT_DETECTOR_INVERTED, Flags.diode);

	/* 1.11 Shulker Box */
        matUseFlagList.put(Material.BLACK_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.BLUE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.BROWN_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.CYAN_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.GRAY_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.GREEN_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.LIGHT_BLUE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.LIME_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.MAGENTA_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.ORANGE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.PINK_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.PURPLE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.RED_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.SILVER_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.WHITE_SHULKER_BOX, Flags.container);
        matUseFlagList.put(Material.YELLOW_SHULKER_BOX, Flags.container);
    }

    @Override
    public boolean isPlate(Material mat) {
        return mat == Material.GOLD_PLATE || mat == Material.IRON_PLATE;
    }

    @Override
    public boolean isMainHand(PlayerInteractEvent event) {
        return event.getHand() == EquipmentSlot.HAND ? true : false;
    }

    @Override
    public ItemStack itemInMainHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public Block getTargetBlock(Player player, int range) {
        return player.getTargetBlock((Set<Material>) null, range);
    }

    @Override
    public boolean isChorusTeleport(TeleportCause tpcause) {
        if (tpcause == TeleportCause.CHORUS_FRUIT)
            return true;
        return false;
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public boolean isBoat(Material mat) {
        switch (mat) {
            case BOAT:
            case BOAT_ACACIA:
            case BOAT_BIRCH:
            case BOAT_DARK_OAK:
            case BOAT_JUNGLE:
            case BOAT_SPRUCE:
                return true;
        }
        return false;
    }
}
