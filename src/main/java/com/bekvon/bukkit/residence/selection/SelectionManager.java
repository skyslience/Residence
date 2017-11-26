package com.bekvon.bukkit.residence.selection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.SelectionSides;
import com.bekvon.bukkit.residence.containers.Visualizer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.*;

public class SelectionManager {
    public static final int MIN_HEIGHT = 0;
    protected Map<String, Location> playerLoc1;
    protected Map<String, Location> playerLoc2;
    protected Server server;
    protected Residence plugin;
    Permission p = new Permission("residence.bypass.ignorey", PermissionDefault.FALSE);
    private HashMap<String, Visualizer> vMap = new HashMap<String, Visualizer>();

    public SelectionManager(Server server, Residence plugin) {
        this.plugin = plugin;
        this.server = server;
        playerLoc1 = Collections.synchronizedMap(new HashMap<String, Location>());
        playerLoc2 = Collections.synchronizedMap(new HashMap<String, Location>());
    }

    private static Direction getDirection(Player player) {

        int yaw = (int) player.getLocation().getYaw();

        if (yaw < 0)
            yaw += 360;

        yaw += 45;
        yaw %= 360;

        int facing = yaw / 90;

        float pitch = player.getLocation().getPitch();
        if (pitch < -50)
            return Direction.UP;
        if (pitch > 50)
            return Direction.DOWN;
        if (facing == 1) // east
            return Direction.MINUSX;
        if (facing == 3) // west
            return Direction.PLUSX;
        if (facing == 2) // north
            return Direction.MINUSZ;
        if (facing == 0) // south
            return Direction.PLUSZ;
        return null;
    }

    public void updateLocations(Player player, Location loc1, Location loc2) {
        updateLocations(player, loc1, loc2, false);
    }

    public void updateLocations(Player player, Location loc1, Location loc2, boolean force) {
        if (loc1 != null && loc2 != null) {
            playerLoc1.put(player.getName(), loc1);
            playerLoc2.put(player.getName(), loc2);
            updateForY(player);
            this.afterSelectionUpdate(player, force);
        }
    }

    public void placeLoc1(Player player, Location loc) {
        placeLoc1(player, loc, false);
    }

    public void placeLoc1(Player player, Location loc, boolean show) {
        if (loc != null) {
            playerLoc1.put(player.getName(), loc);
            updateForY(player);
            if (show) {
                this.afterSelectionUpdate(player);
            }
        }
    }

    public void placeLoc2(Player player, Location loc) {
        placeLoc2(player, loc, false);
    }

    public void placeLoc2(Player player, Location loc, boolean show) {
        if (loc != null) {
            playerLoc2.put(player.getName(), loc);
            updateForY(player);
            if (show) {
                this.afterSelectionUpdate(player);
            }
        }
    }

    private void updateForY(Player player) {
        if (plugin.getConfigManager().isSelectionIgnoreY() && hasPlacedBoth(player.getName()) && !player.hasPermission(p)) {
            this.qsky(player);
            this.qbedrock(player);
        }
    }

    public void afterSelectionUpdate(Player player) {
        afterSelectionUpdate(player, false);
    }

    public void afterSelectionUpdate(Player player, boolean force) {
        if (hasPlacedBoth(player.getName())) {
            Visualizer v = vMap.get(player.getName());
            if (v == null) {
                v = new Visualizer(player);
            }
            v.setStart(System.currentTimeMillis());
            v.cancelAll();
            if (force)
                v.setLoc(null);
            v.setAreas(this.getSelectionCuboid(player));
            this.showBounds(player, v);
        }
    }

    public Location getPlayerLoc1(Player player) {
        return getPlayerLoc1(player.getName());
    }

    public Location getPlayerLoc1(String player) {
        return playerLoc1.get(player);
    }

    public Location getPlayerLoc2(Player player) {
        return getPlayerLoc2(player.getName());
    }

    public Location getPlayerLoc2(String player) {
        return playerLoc2.get(player);
    }

    public CuboidArea getSelectionCuboid(Player player) {
        return getSelectionCuboid(player.getName());
    }

    public CuboidArea getSelectionCuboid(String player) {
        if (!hasPlacedBoth(player))
            return null;
        return new CuboidArea(getPlayerLoc1(player), getPlayerLoc2(player));
    }

    public boolean hasPlacedBoth(Player player) {
        return hasPlacedBoth(player.getName());
    }

    public boolean hasPlacedBoth(String player) {
        return playerLoc1.containsKey(player) && playerLoc2.containsKey(player);
    }

    public void showSelectionInfoInActionBar(Player player) {

        if (!plugin.getConfigManager().useActionBarOnSelection())
            return;

        String pname = player.getName();
        CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));

        String Message = plugin.msg(lm.Select_TotalSize, cuboidArea.getSize());

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        if (plugin.getConfigManager().enableEconomy())
            Message += " " + plugin.msg(lm.General_LandCost, ((int) Math.ceil(cuboidArea.getSize() * group.getCostPerBlock())));

        plugin.getAB().send(player, Message);

    }

    public void showSelectionInfo(Player player) {
        String pname = player.getName();
        if (hasPlacedBoth(pname)) {
            plugin.msg(player, lm.General_Separator);
            CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));
            plugin.msg(player, lm.Select_TotalSize, cuboidArea.getSize());

            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
            PermissionGroup group = rPlayer.getGroup();

            if (plugin.getConfigManager().enableEconomy())
                plugin.msg(player, lm.General_LandCost, ((int) Math.ceil(cuboidArea.getSize() * group.getCostPerBlock())));
            player.sendMessage(ChatColor.YELLOW + "X" + plugin.msg(lm.General_Size, cuboidArea.getXSize()));
            player.sendMessage(ChatColor.YELLOW + "Y" + plugin.msg(lm.General_Size, cuboidArea.getYSize()));
            player.sendMessage(ChatColor.YELLOW + "Z" + plugin.msg(lm.General_Size, cuboidArea.getZSize()));
            plugin.msg(player, lm.General_Separator);
            Visualizer v = new Visualizer(player);
            v.setAreas(this.getSelectionCuboid(player));
            this.showBounds(player, v);
        } else
            plugin.msg(player, lm.Select_Points);
    }

    public void showBounds(final Player player, final Visualizer v) {
        if (!plugin.getConfigManager().useVisualizer())
            return;
        Visualizer tv = vMap.get(player.getName());
        if (tv != null) {
            tv.cancelAll();
        }
        vMap.put(player.getName(), v);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (!v.getAreas().isEmpty())
                    MakeBorders(player, false);
                if (!v.getErrorAreas().isEmpty())
                    MakeBorders(player, true);
                return;
            }
        });
    }

    public List<Location> getLocations(Location lowLoc, Location loc, Double TX, Double TY, Double TZ, Double Range, boolean StartFromZero) {

        double eachCollumn = plugin.getConfigManager().getVisualizerRowSpacing();
        double eachRow = plugin.getConfigManager().getVisualizerCollumnSpacing();

        if (TX == 0D)
            TX = eachCollumn + eachCollumn * 0.1;
        if (TY == 0D)
            TY = eachRow + eachRow * 0.1;
        if (TZ == 0D)
            TZ = eachCollumn + eachCollumn * 0.1;

        double CollumnStart = eachCollumn;
        double RowStart = eachRow;

        if (StartFromZero) {
            CollumnStart = 0;
            RowStart = 0;
        }

        List<Location> locList = new ArrayList<Location>();

        if (lowLoc.getWorld() != loc.getWorld())
            return locList;

        for (double x = CollumnStart; x < TX; x += eachCollumn) {
            Location CurrentX = lowLoc.clone();
            if (TX > eachCollumn + eachCollumn * 0.1)
                CurrentX.add(x, 0, 0);
            for (double y = RowStart; y < TY; y += eachRow) {
                Location CurrentY = CurrentX.clone();
                if (TY > eachRow + eachRow * 0.1)
                    CurrentY.add(0, y, 0);
                for (double z = CollumnStart; z < TZ; z += eachCollumn) {
                    Location CurrentZ = CurrentY.clone();
                    if (TZ > eachCollumn + eachCollumn * 0.1)
                        CurrentZ.add(0, 0, z);
                    double dist = loc.distance(CurrentZ);
                    if (dist < Range)
                        locList.add(CurrentZ.clone());
                }
            }
        }

        return locList;
    }

    public List<Location> GetLocationsWallsByData(Location loc, Double TX, Double TY, Double TZ, Location lowLoc, SelectionSides Sides,
                                                  double Range) {
        List<Location> locList = new ArrayList<Location>();

        // North wall
        if (Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, TY, 0D, Range, false));

        // South wall
        if (Sides.ShowSouthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), TX, TY, 0D, Range, false));

        // West wall
        if (Sides.ShowWestSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), 0D, TY, TZ, Range, false));

        // East wall
        if (Sides.ShowEastSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, TY, TZ, Range, false));

        // Roof wall
        if (Sides.ShowTopSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), TX, 0D, TZ, Range, false));

        // Ground wall
        if (Sides.ShowBottomSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, 0D, TZ, Range, false));

        return locList;
    }

    public List<Location> GetLocationsCornersByData(Location loc, Double TX, Double TY, Double TZ, Location lowLoc, SelectionSides Sides,
                                                    double Range) {
        List<Location> locList = new ArrayList<Location>();

        // North bottom line
        if (Sides.ShowBottomSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone(), loc.clone(), TX, 0D, 0D, Range, true));

        // North top line
        if (Sides.ShowTopSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), TX, 0D, 0D, Range, true));

        // South bottom line
        if (Sides.ShowBottomSide() && Sides.ShowSouthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), TX, 0D, 0D, Range, true));

        // South top line
        if (Sides.ShowTopSide() && Sides.ShowSouthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, TZ), loc.clone(), TX, 0D, 0D, Range, true));

        // North - West corner
        if (Sides.ShowWestSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, 0), loc.clone(), 0D, TY, 0D, Range, true));

        // North - East corner
        if (Sides.ShowEastSide() && Sides.ShowNorthSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, TY, 0D, Range, true));

        // South - West corner
        if (Sides.ShowSouthSide() && Sides.ShowWestSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, TZ), loc.clone(), 0D, TY, 0D, Range, true));

        // South - East corner
        if (Sides.ShowSouthSide() && Sides.ShowEastSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, TZ), loc.clone(), 0D, TY + 1, 0D, Range, true));

        // West bottom corner
        if (Sides.ShowWestSide() && Sides.ShowBottomSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, 0, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        // East bottom corner
        if (Sides.ShowEastSide() && Sides.ShowBottomSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, 0, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        // West top corner
        if (Sides.ShowWestSide() && Sides.ShowTopSide())
            locList.addAll(getLocations(lowLoc.clone().add(0, TY, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        // East top corner
        if (Sides.ShowEastSide() && Sides.ShowTopSide())
            locList.addAll(getLocations(lowLoc.clone().add(TX, TY, 0), loc.clone(), 0D, 0D, TZ, Range, true));

        return locList;
    }

    public boolean MakeBorders(final Player player, final boolean error) {

        final Visualizer v = vMap.get(player.getName());

        if (v == null)
            return false;

        List<CuboidArea> areas = null;

        if (!error)
            areas = v.getAreas();
        else
            areas = v.getErrorAreas();

        Location loc = player.getLocation();
        int Range = plugin.getConfigManager().getVisualizerRange();

        final List<Location> locList = new ArrayList<Location>();
        final List<Location> locList2 = new ArrayList<Location>();

        final boolean same = v.isSameLoc();
        if (!same) {
            for (CuboidArea area : areas) {
                if (area == null)
                    continue;
                CuboidArea cuboidArea = new CuboidArea(area.getLowLoc(), area.getHighLoc());
                cuboidArea.getHighLoc().add(1, 1, 1);

                SelectionSides Sides = new SelectionSides();

                double PLLX = loc.getBlockX() - Range;
                double PLLZ = loc.getBlockZ() - Range;
                double PLLY = loc.getBlockY() - Range;
                double PLHX = loc.getBlockX() + Range;
                double PLHZ = loc.getBlockZ() + Range;
                double PLHY = loc.getBlockY() + Range;

                if (cuboidArea.getLowLoc().getBlockX() < PLLX) {
                    cuboidArea.getLowLoc().setX(PLLX);
                    Sides.setWestSide(false);
                }

                if (cuboidArea.getHighLoc().getBlockX() > PLHX) {
                    cuboidArea.getHighLoc().setX(PLHX);
                    Sides.setEastSide(false);
                }

                if (cuboidArea.getLowLoc().getBlockZ() < PLLZ) {
                    cuboidArea.getLowLoc().setZ(PLLZ);
                    Sides.setNorthSide(false);
                }

                if (cuboidArea.getHighLoc().getBlockZ() > PLHZ) {
                    cuboidArea.getHighLoc().setZ(PLHZ);
                    Sides.setSouthSide(false);
                }

                if (cuboidArea.getLowLoc().getBlockY() < PLLY) {
                    cuboidArea.getLowLoc().setY(PLLY);
                    Sides.setBottomSide(false);
                }

                if (cuboidArea.getHighLoc().getBlockY() > PLHY) {
                    cuboidArea.getHighLoc().setY(PLHY);
                    Sides.setTopSide(false);
                }

                double TX = cuboidArea.getXSize() - 1;
                double TY = cuboidArea.getYSize() - 1;
                double TZ = cuboidArea.getZSize() - 1;

                if (!error && v.getId() != -1) {
                    Bukkit.getScheduler().cancelTask(v.getId());
                } else if (error && v.getErrorId() != -1) {
                    Bukkit.getScheduler().cancelTask(v.getErrorId());
                }

                locList.addAll(GetLocationsWallsByData(loc, TX, TY, TZ, cuboidArea.getLowLoc().clone(), Sides, Range));
                locList2.addAll(GetLocationsCornersByData(loc, TX, TY, TZ, cuboidArea.getLowLoc().clone(), Sides, Range));
            }
            v.setLoc(player.getLocation());
        } else {
            if (error) {
                locList.addAll(v.getErrorLocations());
                locList2.addAll(v.getErrorLocations2());
            } else {
                locList.addAll(v.getLocations());
                locList2.addAll(v.getLocations2());
            }
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @SuppressWarnings("deprecation")
            @Override
            public void run() {

                int size = locList.size();
                int errorSize = locList2.size();

                int timesMore = 1;
                int errorTimesMore = 1;

                if (size > plugin.getConfigManager().getVisualizerSidesCap() && !same) {
                    timesMore = size / plugin.getConfigManager().getVisualizerSidesCap() + 1;
                }
                if (errorSize > plugin.getConfigManager().getVisualizerFrameCap() && !same) {
                    errorTimesMore = errorSize / plugin.getConfigManager().getVisualizerFrameCap() + 1;
                }

                List<Location> trimed = new ArrayList<Location>();
                List<Location> trimed2 = new ArrayList<Location>();

                try {
                    boolean spigot = plugin.isSpigot();

                    if (spigot) {
                        if (!error)
                            for (int i = 0; i < locList.size(); i += timesMore) {
                                Location l = locList.get(i);
                                player.spigot().playEffect(l, plugin.getConfigManager().getSelectedSpigotSides(), 0, 0, 0, 0, 0, 0, 1, 128);
                                if (!same)
                                    trimed.add(l);
                            }
                        else
                            for (int i = 0; i < locList.size(); i += timesMore) {
                                Location l = locList.get(i);
                                player.spigot().playEffect(l, plugin.getConfigManager().getOverlapSpigotSides(), 0, 0, 0, 0, 0, 0, 1, 128);
                                if (!same)
                                    trimed.add(l);
                            }

                        if (!error)
                            for (int i = 0; i < locList2.size(); i += errorTimesMore) {
                                Location l = locList2.get(i);
                                player.spigot().playEffect(l, plugin.getConfigManager().getSelectedSpigotFrame(), 0, 0, 0, 0, 0, 0, 1, 128);
                                if (!same)
                                    trimed2.add(l);
                            }
                        else
                            for (int i = 0; i < locList2.size(); i += errorTimesMore) {
                                Location l = locList2.get(i);
                                player.spigot().playEffect(l, plugin.getConfigManager().getOverlapSpigotFrame(), 0, 0, 0, 0, 0, 0, 1, 128);
                                if (!same)
                                    trimed2.add(l);
                            }
                    } else {
                        if (!error)
                            for (int i = 0; i < locList.size(); i += timesMore) {
                                Location l = locList.get(i);
                                plugin.getConfigManager().getSelectedSides().display(0, 0, 0, 0, 1, l, player);
                                if (!same)
                                    trimed.add(l);
                            }
                        else
                            for (int i = 0; i < locList.size(); i += timesMore) {
                                Location l = locList.get(i);
                                plugin.getConfigManager().getOverlapSides().display(0, 0, 0, 0, 1, l, player);
                                if (!same)
                                    trimed.add(l);
                            }
                        if (!error)
                            for (int i = 0; i < locList2.size(); i += errorTimesMore) {
                                Location l = locList2.get(i);
                                plugin.getConfigManager().getSelectedFrame().display(0, 0, 0, 0, 1, l, player);
                                if (!same)
                                    trimed2.add(l);
                            }
                        else
                            for (int i = 0; i < locList2.size(); i += errorTimesMore) {
                                Location l = locList2.get(i);
                                plugin.getConfigManager().getOverlapFrame().display(0, 0, 0, 0, 1, l, player);
                                if (!same)
                                    trimed2.add(l);
                            }
                    }
                } catch (Exception e) {
                    return;
                }

                if (!same) {
                    if (error) {
                        v.setErrorLocations(trimed);
                        v.setErrorLocations2(trimed2);
                    } else {
                        v.setLocations(trimed);
                        v.setLocations2(trimed2);
                    }
                }

                return;
            }
        });

        if (v.isOnce())
            return true;

        if (v.getStart() + plugin.getConfigManager().getVisualizerShowFor() < System.currentTimeMillis())
            return false;

        int scid = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    MakeBorders(player, error);
                }
                return;
            }
        }, plugin.getConfigManager().getVisualizerUpdateInterval() * 1L);
        if (!error)
            v.setId(scid);
        else
            v.setErrorId(scid);

        return true;
    }

    public void vert(Player player, boolean resadmin) {
        if (hasPlacedBoth(player.getName())) {
            this.sky(player, resadmin);
            this.bedrock(player, resadmin);
        } else {
            plugin.msg(player, lm.Select_Points);
        }
    }

    public void qsky(Player player) {
        int y1 = playerLoc1.get(player.getName()).getBlockY();
        int y2 = playerLoc2.get(player.getName()).getBlockY();
        int newy = player.getLocation().getWorld().getMaxHeight() - 1;
        if (y1 > y2)
            playerLoc1.get(player.getName()).setY(newy);
        else
            playerLoc2.get(player.getName()).setY(newy);
    }

    public void qbedrock(Player player) {
        int y1 = playerLoc1.get(player.getName()).getBlockY();
        int y2 = playerLoc2.get(player.getName()).getBlockY();
        if (y1 < y2) {
            int newy = MIN_HEIGHT;
            playerLoc1.get(player.getName()).setY(newy);
        } else {
            int newy = MIN_HEIGHT;
            playerLoc2.get(player.getName()).setY(newy);
        }
    }

    public void sky(Player player, boolean resadmin) {
        if (hasPlacedBoth(player.getName())) {
            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
            PermissionGroup group = rPlayer.getGroup();
            int y1 = playerLoc1.get(player.getName()).getBlockY();
            int y2 = playerLoc2.get(player.getName()).getBlockY();
            int newy = player.getLocation().getWorld().getMaxHeight() - 1;
            if (y1 > y2) {
                if (!resadmin) {
                    if (group.getMaxHeight() < newy)
                        newy = group.getMaxHeight();
                    if (newy - y2 > (group.getMaxY() - 1))
                        newy = y2 + (group.getMaxY() - 1);
                }
                playerLoc1.get(player.getName()).setY(newy);
            } else {
                if (!resadmin) {
                    if (group.getMaxHeight() < newy)
                        newy = group.getMaxHeight();
                    if (newy - y1 > (group.getMaxY() - 1))
                        newy = y1 + (group.getMaxY() - 1);
                }
                playerLoc2.get(player.getName()).setY(newy);
            }
            plugin.msg(player, lm.Select_Sky);
        } else {
            plugin.msg(player, lm.Select_Points);
        }
    }

    public void bedrock(Player player, boolean resadmin) {
        if (hasPlacedBoth(player.getName())) {
            ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
            PermissionGroup group = rPlayer.getGroup();
            int y1 = playerLoc1.get(player.getName()).getBlockY();
            int y2 = playerLoc2.get(player.getName()).getBlockY();
            if (y1 < y2) {
                int newy = MIN_HEIGHT;
                if (!resadmin) {
                    if (newy < group.getMinHeight())
                        newy = group.getMinHeight();
                    if (y2 - newy > (group.getMaxY() - 1))
                        newy = y2 - (group.getMaxY() - 1);
                }
                playerLoc1.get(player.getName()).setY(newy);
            } else {
                int newy = MIN_HEIGHT;
                if (!resadmin) {
                    if (newy < group.getMinHeight())
                        newy = group.getMinHeight();
                    if (y1 - newy > (group.getMaxY() - 1))
                        newy = y1 - (group.getMaxY() - 1);
                }
                playerLoc2.get(player.getName()).setY(newy);
            }
            plugin.msg(player, lm.Select_Bedrock);
        } else {
            plugin.msg(player, lm.Select_Points);
        }
    }

    public void clearSelection(Player player) {
        playerLoc1.remove(player.getName());
        playerLoc2.remove(player.getName());
    }

    public void selectChunk(Player player) {
        Chunk chunk = player.getWorld().getChunkAt(player.getLocation());
        int xcoord = chunk.getX() * 16;
        int zcoord = chunk.getZ() * 16;
        int ycoord = MIN_HEIGHT;
        int xmax = xcoord + 15;
        int zmax = zcoord + 15;
        int ymax = player.getLocation().getWorld().getMaxHeight() - 1;
        playerLoc1.put(player.getName(), new Location(player.getWorld(), xcoord, ycoord, zcoord));
        playerLoc2.put(player.getName(), new Location(player.getWorld(), xmax, ymax, zmax));
        plugin.msg(player, lm.Select_Success);
    }

    public boolean worldEdit(Player player) {
        plugin.msg(player, lm.General_WorldEditNotFound);
        return false;
    }

    public boolean worldEditUpdate(Player player) {
        plugin.msg(player, lm.General_WorldEditNotFound);
        return false;
    }

    public void selectBySize(Player player, int xsize, int ysize, int zsize) {
        Location myloc = player.getLocation();
        Location loc1 = new Location(myloc.getWorld(), myloc.getBlockX() + xsize, myloc.getBlockY() + ysize, myloc.getBlockZ() + zsize);
        Location loc2 = new Location(myloc.getWorld(), myloc.getBlockX() - xsize, myloc.getBlockY() - ysize, myloc.getBlockZ() - zsize);
        placeLoc1(player, loc1, false);
        placeLoc2(player, loc2, true);
        plugin.msg(player, lm.Select_Success);
        showSelectionInfo(player);
    }

    public void modify(Player player, boolean shift, double amount) {
        if (!hasPlacedBoth(player.getName())) {
            plugin.msg(player, lm.Select_Points);
            return;
        }
        Direction d = getDirection(player);
        if (d == null) {
            plugin.msg(player, lm.Invalid_Direction);
            return;
        }
        CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
        switch (d) {
            case DOWN:
                double oldy = area.getLowLoc().getBlockY();
                oldy = oldy - amount;
                if (oldy < MIN_HEIGHT) {
                    plugin.msg(player, lm.Select_TooLow);
                    oldy = MIN_HEIGHT;
                }
                area.getLowLoc().setY(oldy);
                if (shift) {
                    double oldy2 = area.getHighLoc().getBlockY();
                    oldy2 = oldy2 - amount;
                    area.getHighLoc().setY(oldy2);
                    plugin.msg(player, lm.Shifting_Down, amount);
                } else
                    plugin.msg(player, lm.Expanding_Down, amount);
                break;
            case MINUSX:
                double oldx = area.getLowLoc().getBlockX();
                oldx = oldx - amount;
                area.getLowLoc().setX(oldx);
                if (shift) {
                    double oldx2 = area.getHighLoc().getBlockX();
                    oldx2 = oldx2 - amount;
                    area.getHighLoc().setX(oldx2);
                    plugin.msg(player, lm.Shifting_West, amount);
                } else
                    plugin.msg(player, lm.Expanding_West, amount);
                break;
            case MINUSZ:
                double oldz = area.getLowLoc().getBlockZ();
                oldz = oldz - amount;
                area.getLowLoc().setZ(oldz);
                if (shift) {
                    double oldz2 = area.getHighLoc().getBlockZ();
                    oldz2 = oldz2 - amount;
                    area.getHighLoc().setZ(oldz2);
                    plugin.msg(player, lm.Shifting_North, amount);
                } else
                    plugin.msg(player, lm.Expanding_North, amount);
                break;
            case PLUSX:
                oldx = area.getHighLoc().getBlockX();
                oldx = oldx + amount;
                area.getHighLoc().setX(oldx);
                if (shift) {
                    double oldx2 = area.getLowLoc().getBlockX();
                    oldx2 = oldx2 + amount;
                    area.getLowLoc().setX(oldx2);
                    plugin.msg(player, lm.Shifting_East, amount);
                } else
                    plugin.msg(player, lm.Expanding_East, amount);
                break;
            case PLUSZ:
                oldz = area.getHighLoc().getBlockZ();
                oldz = oldz + amount;
                area.getHighLoc().setZ(oldz);
                if (shift) {
                    double oldz2 = area.getLowLoc().getBlockZ();
                    oldz2 = oldz2 + amount;
                    area.getLowLoc().setZ(oldz2);
                    plugin.msg(player, lm.Shifting_South, amount);
                } else
                    plugin.msg(player, lm.Expanding_South, amount);
                break;
            case UP:
                oldy = area.getHighLoc().getBlockY();
                oldy = oldy + amount;
                if (oldy > player.getLocation().getWorld().getMaxHeight() - 1) {
                    plugin.msg(player, lm.Select_TooHigh);
                    oldy = player.getLocation().getWorld().getMaxHeight() - 1;
                }
                area.getHighLoc().setY(oldy);
                if (shift) {
                    double oldy2 = area.getLowLoc().getBlockY();
                    oldy2 = oldy2 + amount;
                    area.getLowLoc().setY(oldy2);
                    plugin.msg(player, lm.Shifting_Up, amount);
                } else
                    plugin.msg(player, lm.Expanding_Up, amount);
                break;
            default:
                break;
        }
        updateLocations(player, area.getHighLoc(), area.getLowLoc(), true);
    }

    public boolean contract(Player player, double amount) {
        return contract(player, amount, false);
    }

    public boolean contract(Player player, double amount, @SuppressWarnings("unused") boolean resadmin) {
        if (!hasPlacedBoth(player.getName())) {
            plugin.msg(player, lm.Select_Points);
            return false;
        }
        Direction d = getDirection(player);
        if (d == null) {
            plugin.msg(player, lm.Invalid_Direction);
            return false;
        }
        CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
        switch (d) {
            case UP:
                double oldy = area.getHighLoc().getBlockY();
                oldy = oldy - amount;
                if (oldy > player.getLocation().getWorld().getMaxHeight() - 1) {
                    plugin.msg(player, lm.Select_TooHigh);
                    oldy = player.getLocation().getWorld().getMaxHeight() - 1;
                }
                area.getHighLoc().setY(oldy);
                plugin.msg(player, lm.Contracting_Down, amount);
                break;
            case PLUSX:
                double oldx = area.getHighLoc().getBlockX();
                oldx = oldx - amount;
                area.getHighLoc().setX(oldx);
                plugin.msg(player, lm.Contracting_West, amount);
                break;
            case PLUSZ:
                double oldz = area.getHighLoc().getBlockZ();
                oldz = oldz - amount;
                area.getHighLoc().setZ(oldz);
                plugin.msg(player, lm.Contracting_North, amount);
                break;
            case MINUSX:
                oldx = area.getLowLoc().getBlockX();
                oldx = oldx + amount;
                area.getLowLoc().setX(oldx);
                plugin.msg(player, lm.Contracting_East, amount);
                break;
            case MINUSZ:
                oldz = area.getLowLoc().getBlockZ();
                oldz = oldz + amount;
                area.getLowLoc().setZ(oldz);
                plugin.msg(player, lm.Contracting_South, amount);
                break;
            case DOWN:
                oldy = area.getLowLoc().getBlockY();
                oldy = oldy + amount;
                if (oldy < MIN_HEIGHT) {
                    plugin.msg(player, lm.Select_TooLow);
                    oldy = MIN_HEIGHT;
                }
                area.getLowLoc().setY(oldy);
                plugin.msg(player, lm.Contracting_Up, amount);
                break;
            default:
                break;
        }

//	if (!ClaimedResidence.isBiggerThanMinSubzone(player, area, resadmin))
//	    return false;

        updateLocations(player, area.getHighLoc(), area.getLowLoc(), true);
        return true;
    }

    public enum Direction {
        UP, DOWN, PLUSX, PLUSZ, MINUSX, MINUSZ
    }

}
