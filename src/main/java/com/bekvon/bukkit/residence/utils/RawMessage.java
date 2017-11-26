package com.bekvon.bukkit.residence.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RawMessage {

    List<String> parts = new ArrayList<String>();
    List<String> cleanParts = new ArrayList<String>();
    String combined = "";
    String combinedClean = "";

    public void clear() {
        parts = new ArrayList<String>();
        cleanParts = new ArrayList<String>();
        combined = "";
        combinedClean = "";
    }

    public RawMessage add(String text) {
        return add(text, null, null, null);
    }

    public RawMessage add(String text, String hoverText) {
        return add(text, hoverText, null, null);
    }

    public RawMessage add(String text, String hoverText, String command) {
        return add(text, hoverText, command, null);
    }

    public RawMessage add(String text, String hoverText, String command, String suggestion) {
        if (text == null)
            return this;
        String f = "{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', text) + "\"";

        String last = ChatColor.getLastColors(text);
        if (last != null && !last.isEmpty()) {
            ChatColor color = ChatColor.getByChar(last.replace("&", ""));
            if (color != null) {
                f += ",\"color\":\"" + color.name().toLowerCase() + "\"";
            }
        }
        if (hoverText != null)
            f += ",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', hoverText) + "\"}]}}";
        if (suggestion != null)
            f += ",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + suggestion + "\"}";
        if (command != null) {
            if (!command.startsWith("/"))
                command = "/" + command;
            f += ",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + command + "\"}";
        }
        f += "}";
        parts.add(f);
        cleanParts.add(ChatColor.translateAlternateColorCodes('&', text));
        return this;
    }

    public RawMessage combine() {
        String f = "";
        for (String part : parts) {
            if (f.isEmpty())
                f = "[\"\",";
            else
                f += ",";
            f += part;
        }
        if (!f.isEmpty())
            f += "]";
        combined = f;
        return this;
    }

    public RawMessage combineClean() {
        String f = "";
        for (String part : cleanParts) {
            f += part;
        }
        combinedClean = f;
        return this;
    }

    public RawMessage show(Player player) {
        if (combined.isEmpty())
            combine();
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + player.getName() + " " + combined);
        return this;
    }

    public RawMessage showClean(Player player) {
        if (combinedClean.isEmpty())
            combineClean();
        player.sendMessage(combined);
        return this;
    }

    public RawMessage show(CommandSender sender) {
        if (combined.isEmpty())
            combine();
        if (sender instanceof Player)
            show((Player) sender);
        else
            sender.sendMessage(this.combineClean().combinedClean);
        return this;
    }

    public String getRaw() {
        if (combined.isEmpty())
            combine();
        return combined;
    }

}
