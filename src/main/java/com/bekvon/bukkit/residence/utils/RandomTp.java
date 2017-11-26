package com.bekvon.bukkit.residence.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.RandomTeleport;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Random;

public class RandomTp {

    static int miny = 63;

    private Residence plugin;

    public RandomTp(Residence plugin) {
        this.plugin = plugin;
    }

    public boolean isDefinedRnadomTp(World world) {
        for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {
            if (!one.getCenter().getWorld().equals(world))
                continue;
            return true;
        }
        return false;
    }

    public Location getRandomlocation(World world) {

        if (world == null)
            return null;

        Random random = new Random(System.currentTimeMillis());

        boolean ok = false;
        double x = 0;
        double z = 0;

        int tries = 0;

        RandomTeleport rtloc = null;

        for (RandomTeleport one : plugin.getConfigManager().getRandomTeleport()) {

            if (!one.getCenter().getWorld().equals(world))
                continue;

            rtloc = one;
            break;
        }

        if (rtloc == null)
            return null;

        int inerrange = rtloc.getMinCord();
        int outerrange = rtloc.getMaxCord();
        if (outerrange < 1)
            outerrange = 1;
        int maxtries = plugin.getConfigManager().getrtMaxTries();

        int centerX = rtloc.getCenter().getBlockX();
        int centerY = rtloc.getCenter().getBlockZ();

        Location loc = null;

        c:
        while (!ok) {
            tries++;
            if (tries > maxtries)
                return null;

            x = random.nextInt(outerrange * 2) - outerrange + 0.5 + centerX;

            if (x > inerrange * -1 && x < inerrange)
                continue;

            z = random.nextInt(outerrange * 2) - outerrange + 0.5 + centerY;
            if (z > inerrange * -1 && z < inerrange)
                continue;

            loc = new Location(world, x, world.getMaxHeight(), z);

            int dir = random.nextInt(359);

            int max = loc.getWorld().getMaxHeight();
            max = loc.getWorld().getEnvironment() == Environment.NETHER ? 100 : max;

            loc.setYaw(dir);

            for (int i = max; i > 0; i--) {
                loc.setY(i);
                Block block = loc.getBlock();
                Block block2 = loc.clone().add(0, 1, 0).getBlock();
                Block block3 = loc.clone().add(0, -1, 0).getBlock();
                if (!plugin.getNms().isEmptyBlock(block3) && plugin.getNms().isEmptyBlock(block) && plugin.getNms().isEmptyBlock(block2)) {
                    break;
                }
                if (i <= 3) {
                    loc = null;
                    continue c;
                }
            }

            if (!plugin.getNms().isEmptyBlock(loc.getBlock()))
                continue;

            if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.LAVA || loc.clone().add(0, -1, 0).getBlock().getState()
                    .getType() == Material.STATIONARY_LAVA)
                continue;

            if (loc.clone().add(0, -1, 0).getBlock().getState().getType() == Material.WATER || loc.clone().add(0, -1, 0).getBlock().getState()
                    .getType() == Material.STATIONARY_WATER)
                continue;

            ClaimedResidence res = plugin.getResidenceManager().getByLoc(loc);

            if (res != null)
                continue;

            loc.setY(loc.getY() + 2);
            break;
        }
        return loc;
    }

    public void performDelaydTp(final Location loc, final Player targetPlayer) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!plugin.getTeleportDelayMap().contains(targetPlayer.getName()) && plugin.getConfigManager().getTeleportDelay() > 0)
                    return;
                else if (plugin.getTeleportDelayMap().contains(targetPlayer.getName()))
                    plugin.getTeleportDelayMap().remove(targetPlayer.getName());
                targetPlayer.closeInventory();
                targetPlayer.teleport(loc);
                plugin.msg(targetPlayer, lm.RandomTeleport_TeleportSuccess, loc.getX(), loc.getY(), loc.getZ());
                return;
            }
        }, plugin.getConfigManager().getTeleportDelay() * 20L);
    }

    public void performInstantTp(Location loc, Player targetPlayer) {
        targetPlayer.closeInventory();
        targetPlayer.teleport(loc);
        plugin.msg(targetPlayer, lm.RandomTeleport_TeleportSuccess, loc.getX(), loc.getY(), loc.getZ());
    }
}
