package com.bekvon.bukkit.residence;

import com.bekvon.bukkit.residence.containers.cmd;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResidenceCommandListener extends Residence {

    private static List<String> AdminCommands = new ArrayList<String>();

    public static List<String> getAdminCommands() {
        if (AdminCommands.size() == 0)
            AdminCommands = Residence.getInstance().getCommandFiller().getCommands(false);
        return AdminCommands;
    }

    private static cmd getCmdClass(String[] args) {
        cmd cmdClass = null;
        try {
            Class<?> nmsClass;
            nmsClass = Class.forName("com.bekvon.bukkit.residence.commands." + args[0].toLowerCase());
            if (cmd.class.isAssignableFrom(nmsClass)) {
                cmdClass = (cmd) nmsClass.getConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                | SecurityException e) {
        }
        return cmdClass;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        ResidenceCommandEvent cevent = new ResidenceCommandEvent(command.getName(), args, sender);
        getServ().getPluginManager().callEvent(cevent);
        if (cevent.isCancelled()) {
            return true;
        }

        if (sender instanceof Player && !getPermissionManager().isResidenceAdmin(sender) && isDisabledWorldCommand(((Player) sender)
                .getWorld())) {
            this.msg(sender, lm.General_DisabledWorld);
            return true;
        }

        if (command.getName().equals("resreload") && args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (getPermissionManager().isResidenceAdmin(player) && player.hasPermission("residence.topadmin")) {
                    this.reloadPlugin();
                    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded config.");
                    System.out.println("[Residence] Reloaded by " + player.getName() + ".");
                } else
                    this.msg(player, lm.General_NoPermission);
            } else {
                this.reloadPlugin();
                System.out.println("[Residence] Reloaded by console.");
            }
            return true;
        }
        if (command.getName().equals("resload")) {
            if (!(sender instanceof Player) || sender instanceof Player && getPermissionManager().isResidenceAdmin(sender) && ((Player) sender).hasPermission(
                    "residence.topadmin")) {
                try {
                    this.loadYml();
                    sender.sendMessage(ChatColor.GREEN + "[Residence] Reloaded save file...");
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "[Residence] Unable to reload the save file, exception occured!");
                    sender.sendMessage(ChatColor.RED + ex.getMessage());
                    Logger.getLogger(getInstance().getClass().getName()).log(Level.SEVERE, null, ex);
                }
            } else
                msg(sender, lm.General_NoPermission);
            return true;
        } else if (command.getName().equals("rc")) {
            cmd cmdClass = getCmdClass(new String[]{"rc"});
            if (cmdClass == null) {
                sendUsage(sender, command.getName());
                return true;
            }
            boolean respond = cmdClass.perform(getInstance(), args, false, command, sender);
            if (!respond)
                sendUsage(sender, command.getName());
            return true;
        } else if (command.getName().equals("res") || command.getName().equals("residence") || command.getName().equals("resadmin")) {
            boolean resadmin = false;
            if (sender instanceof Player) {
                if (command.getName().equals("resadmin") && getPermissionManager().isResidenceAdmin(sender)) {
                    resadmin = true;
                }
                if (command.getName().equals("resadmin") && !getPermissionManager().isResidenceAdmin(sender)) {
                    ((Player) sender).sendMessage(msg(lm.Residence_NonAdmin));
                    return true;
                }
                if (command.getName().equals("res") && getPermissionManager().isResidenceAdmin(sender) && getConfigManager().getAdminFullAccess()) {
                    resadmin = true;
                }
            } else {
                resadmin = true;
            }

            if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("?") || args.length > 1 && args[args.length - 2].equals("?")) {
                return commandHelp(args, resadmin, sender, command);
            }

            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            } else {
                resadmin = true;
            }
            if (getConfigManager().allowAdminsOnly()) {
                if (!resadmin && player != null) {
                    msg(player, lm.General_AdminOnly);
                    return true;
                }
            }
            if (args.length == 0) {
                args = new String[1];
                args[0] = "?";
            }

            String cmd = args[0].toLowerCase();

            switch (cmd) {
                case "delete":
                    cmd = "remove";
                    break;
                case "sz":
                    cmd = "subzone";
                    break;
            }

            cmd cmdClass = getCmdClass(args);
            if (cmdClass == null) {
                return commandHelp(new String[]{"?"}, resadmin, sender, command);
            }

            if (!resadmin && !this.hasPermission(sender, "residence.command." + args[0].toLowerCase()))
                return true;

            if (!resadmin && player != null && resadminToggle.contains(player.getName())) {
                if (!getPermissionManager().isResidenceAdmin(player)) {
                    resadminToggle.remove(player.getName());
                }
            }
            boolean respond = cmdClass.perform(getInstance(), args, resadmin, command, sender);
            if (!respond) {
                String[] tempArray = new String[args.length + 1];
                for (int i = 0; i < args.length; i++) {
                    tempArray[i] = args[i];
                }
                tempArray[args.length] = "?";
                args = tempArray;
                return commandHelp(args, resadmin, sender, command);
            }

            return true;
        }
        return this.onCommand(sender, command, label, args);
    }

    public void sendUsage(CommandSender sender, String command) {
        msg(sender, lm.General_DefaultUsage, command);
    }

    private boolean commandHelp(String[] args, boolean resadmin, CommandSender sender, Command command) {
        if (getHelpPages() == null)
            return false;

        String helppath = getHelpPath(args);

        int page = 1;
        if (!args[args.length - 1].equalsIgnoreCase("?")) {
            try {
                page = Integer.parseInt(args[args.length - 1]);
            } catch (Exception ex) {
                msg(sender, lm.General_InvalidHelp);
            }
        }

        if (command.getName().equalsIgnoreCase("res"))
            resadmin = false;
        if (getHelpPages().containesEntry(helppath))
            getHelpPages().printHelp(sender, page, helppath, resadmin);
        return true;
    }

    private String getHelpPath(String[] args) {
        String helppath = "res";
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("?")) {
                break;
            }
            helppath = helppath + "." + args[i];
        }
        if (!getHelpPages().containesEntry(helppath) && args.length > 0)
            return getHelpPath(Arrays.copyOf(args, args.length - 1));
        return helppath;
    }

}
