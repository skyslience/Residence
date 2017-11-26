package com.bekvon.bukkit.residence.protection;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldFlagManager {
    protected Map<String, Map<String, FlagPermissions>> groupperms;
    protected Map<String, FlagPermissions> worldperms;
    protected FlagPermissions globaldefaults;
    private Residence plugin;

    public WorldFlagManager(Residence plugin) {
        this.plugin = plugin;
        globaldefaults = new FlagPermissions();
        worldperms = new HashMap<>();
        groupperms = new HashMap<>();
        this.parsePerms();
    }

    public FlagPermissions getPerms(Player player) {
        ResidencePlayer resPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        return this.getPerms(player.getWorld().getName(), resPlayer.getGroup().getGroupName());
    }

    public FlagPermissions getPerms(String world, String group) {
        world = world.toLowerCase();
        group = group.toLowerCase();
        Map<String, FlagPermissions> groupworldperms = groupperms.get(group);
        if (groupworldperms == null) {
            return this.getPerms(world);
        }
        FlagPermissions list = groupworldperms.get(world);
        if (list == null) {
            list = groupworldperms.get("global." + world);
            if (list == null) {
                list = groupworldperms.get("global");
            }
            if (list == null) {
                return this.getPerms(world);
            }
        }
        return list;
    }

    public FlagPermissions getPerms(String world) {
        world = world.toLowerCase();
        FlagPermissions list = worldperms.get(world);
        if (list == null) {
            if (globaldefaults == null)
                return new FlagPermissions();
            return globaldefaults;
        }
        return list;
    }

    public final void parsePerms() {
        try {
            FileConfiguration flags = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "flags.yml"));
            FileConfiguration groups = YamlConfiguration.loadConfiguration(new File(plugin.dataFolder, "groups.yml"));

            Set<String> keys = flags.getConfigurationSection("Global.Flags").getKeys(false);
            if (keys != null) {
                for (String key : keys) {
                    if (key.equalsIgnoreCase("Global")) {
                        globaldefaults = FlagPermissions.parseFromConfigNode(key, flags.getConfigurationSection("Global.Flags"));
                    } else {
                        worldperms.put(key.toLowerCase(), FlagPermissions.parseFromConfigNode(key, flags.getConfigurationSection("Global.Flags")));
                    }
                }
            }
            for (Entry<String, FlagPermissions> entry : worldperms.entrySet()) {
                entry.getValue().setParent(globaldefaults);
            }

            if (!groups.isConfigurationSection("Groups")) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Your groups.yml file is incorrect!");
                return;
            }

            keys = groups.getConfigurationSection("Groups").getKeys(false);
            if (keys != null) {
                for (String key : keys) {
                    if (!groups.contains("Groups." + key + ".Flags"))
                        continue;
                    if (!groups.contains("Groups." + key + ".Flags.World"))
                        continue;
                    if (key == null)
                        continue;
                    Set<String> worldkeys = groups.getConfigurationSection("Groups." + key + ".Flags.World").getKeys(false);

                    if (worldkeys == null)
                        continue;

                    Map<String, FlagPermissions> perms = new HashMap<>();
                    for (String wkey : worldkeys) {
                        FlagPermissions list = FlagPermissions.parseFromConfigNode(wkey, groups.getConfigurationSection("Groups." + key + ".Flags.World"));
                        if (wkey.equalsIgnoreCase("global")) {
                            list.setParent(globaldefaults);
                            perms.put(wkey.toLowerCase(), list);
                            for (Entry<String, FlagPermissions> worldperm : worldperms.entrySet()) {
                                list = FlagPermissions.parseFromConfigNode(wkey, groups.getConfigurationSection("Groups." + key + ".Flags.World"));
                                list.setParent(worldperm.getValue());
                                perms.put("global." + worldperm.getKey().toLowerCase(), list);
                            }
                        } else {
                            perms.put(wkey.toLowerCase(), list);
                        }
                    }
                    for (Entry<String, FlagPermissions> entry : perms.entrySet()) {
                        String wkey = entry.getKey();
                        FlagPermissions list = entry.getValue();
                        if (!wkey.startsWith("global.")) {
                            list.setParent(perms.get("global." + wkey));
                            if (list.getParent() == null) {
                                list.setParent(worldperms.get(wkey));
                            }
                            if (list.getParent() == null) {
                                list.setParent(globaldefaults);
                            }
                        }
                    }
                    groupperms.put(key.toLowerCase(), perms);

                }
            }
        } catch (Exception ex) {
            Logger.getLogger(WorldFlagManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
