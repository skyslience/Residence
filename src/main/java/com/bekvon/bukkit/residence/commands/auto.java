package com.bekvon.bukkit.residence.commands;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.CommandAnnotation;
import com.bekvon.bukkit.residence.containers.ConfigReader;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class auto implements cmd {

    private static void resize(Residence plugin, Player player, CuboidArea cuboid) {

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();

        int cost = (int) Math.ceil(cuboid.getSize() * group.getCostPerBlock());

        double balance = 0;
        if (plugin.getEconomyManager() != null)
            balance = plugin.getEconomyManager().getBalance(player.getName());

        direction dir = direction.Top;

        List<direction> locked = new ArrayList<direction>();

        boolean checkCollision = plugin.getConfigManager().isAutomaticResidenceCreationCheckCollision();
        int skipped = 0;
        int done = 0;
        while (true) {
            done++;

            if (skipped >= 6) {
                break;
            }

            // fail safe if loop keeps going on
            if (done > 10000)
                break;

            if (locked.contains(dir)) {
                dir = dir.getNext();
                skipped++;
                continue;
            }

            CuboidArea c = new CuboidArea();
            c.setLowLocation(cuboid.getLowLoc().clone().add(-dir.getLow().getX(), -dir.getLow().getY(), -dir.getLow().getZ()));
            c.setHighLocation(cuboid.getHighLoc().clone().add(dir.getHigh().getX(), dir.getHigh().getY(), dir.getHigh().getZ()));

            if (c.getLowLoc().getY() < 0) {
                c.getLowLoc().setY(0);
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (c.getHighLoc().getY() >= c.getWorld().getMaxHeight()) {
                c.getHighLoc().setY(c.getWorld().getMaxHeight() - 1);
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (checkCollision && plugin.getResidenceManager().collidesWithResidence(c) != null) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (c.getXSize() >= group.getMaxX() - group.getMinX()) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (c.getYSize() >= group.getMaxY() - group.getMinY()) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            if (c.getZSize() >= group.getMaxZ() - group.getMinZ()) {
                locked.add(dir);
                dir = dir.getNext();
                skipped++;
                continue;
            }

            skipped = 0;

            if(plugin.getConfigManager().enableEconomy()){
                cost = (int) Math.ceil(c.getSize() * group.getCostPerBlock());
                if (cost > balance)
                    break;
            }

            cuboid.setLowLocation(c.getLowLoc());
            cuboid.setHighLocation(c.getHighLoc());

            dir = dir.getNext();
        }

        plugin.getSelectionManager().placeLoc1(player, cuboid.getLowLoc());
        plugin.getSelectionManager().placeLoc2(player, cuboid.getHighLoc());
    }

    @Override
    @CommandAnnotation(simple = true, priority = 150)
    public boolean perform(Residence plugin, String[] args, boolean resadmin, Command command, CommandSender sender) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        if (args.length != 2 && args.length != 1 && args.length != 3) {
            return false;
        }

        String resName = null;

        if (args.length > 1)
            resName = args[1];
        else
            resName = player.getName();

        int lenght = -1;
        if (args.length == 3) {
            try {
                lenght = Integer.parseInt(args[2]);
            } catch (Exception ex) {
            }
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);

        PermissionGroup group = rPlayer.getGroup();

        Location loc = player.getLocation();

        int X = group.getMinX();
        int Y = group.getMinY();
        int Z = group.getMinZ();

        if (lenght > 0) {
            if (lenght < group.getMaxX() && lenght > X)
                X = lenght;
            if (lenght < group.getMaxZ() && lenght > Z)
                Z = lenght;
        }

        int rX = (X - 1) / 2;
        int rY = (Y - 1) / 2;
        int rZ = (Z - 1) / 2;

        int minX = loc.getBlockX() - rX;
        int maxX = loc.getBlockX() + rX;

        if (maxX - minX + 1 < X)
            maxX++;

        int minY = loc.getBlockY() - rY;
        int maxY = loc.getBlockY() + rY;

        if (maxY - minY + 1 < Y)
            maxY++;

        if (minY < 0) {
            maxY += -minY;
            minY = 0;
        }

        if (maxY > loc.getWorld().getMaxHeight()) {
            int dif = maxY - loc.getWorld().getMaxHeight();
            if (minY > 0)
                minY -= dif;
            if (minY < 0)
                minY = 0;
            maxY = loc.getWorld().getMaxHeight() - 1;
        }

        int minZ = loc.getBlockZ() - rZ;
        int maxZ = loc.getBlockZ() + rZ;
        if (maxZ - minZ + 1 < Z)
            maxZ++;

        plugin.getSelectionManager().placeLoc1(player, new Location(loc.getWorld(), minX, minY, minZ), false);
        plugin.getSelectionManager().placeLoc2(player, new Location(loc.getWorld(), maxX, maxY, maxZ), false);
        resize(plugin, player, plugin.getSelectionManager().getSelectionCuboid(player));

        if (plugin.getResidenceManager().getByName(resName) != null) {
            for (int i = 1; i < 50; i++) {
                String tempName = resName + plugin.getConfigManager().AutomaticResidenceCreationIncrementFormat().replace("[number]", i + "");
                if (plugin.getResidenceManager().getByName(tempName) == null) {
                    resName = tempName;
                    break;
                }
            }
        }

        player.performCommand("res create " + resName);
        return true;
    }

    @Override
    public void getLocale(ConfigReader c, String path) {
        // Main command
        c.get(path + "Description", "Create maximum allowed residence around you");
        c.get(path + "Info", Arrays.asList("&eUsage: &6/res auto (residence name) (radius)"));
    }

    private enum direction {
        Top(new Vector(0, 1, 0), new Vector(0, 0, 0)),
        Bottom(new Vector(0, 0, 0), new Vector(0, 1, 0)),
        East(new Vector(1, 0, 0), new Vector(0, 0, 0)),
        West(new Vector(0, 0, 0), new Vector(1, 0, 0)),
        North(new Vector(0, 0, 1), new Vector(0, 0, 0)),
        South(new Vector(0, 0, 0), new Vector(0, 0, 1));

        private Vector low;
        private Vector high;

        direction(Vector low, Vector high) {
            this.low = low;
            this.high = high;
        }

        public Vector getLow() {
            return low;
        }

        public Vector getHigh() {
            return high;
        }

        public direction getNext() {
            boolean next = false;
            direction dir = direction.Top;
            for (direction one : direction.values()) {
                if (next) {
                    dir = one;
                    next = false;
                    break;
                }
                if (this.equals(one)) {
                    next = true;
                }
            }
            return dir;
        }
    }
}
