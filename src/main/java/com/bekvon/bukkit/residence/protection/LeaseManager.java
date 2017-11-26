package com.bekvon.bukkit.residence.protection;

import cn.plugin.islandadd.api.IsLandAddAPI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.ResidencePlayer;
import com.bekvon.bukkit.residence.containers.lm;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent.DeleteCause;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.utils.GetTime;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LeaseManager {

    ResidenceManager manager;
    private Set<ClaimedResidence> leaseExpireTime;
    private Residence plugin;

    public LeaseManager(Residence plugin) {
        this.plugin = plugin;
        manager = plugin.getResidenceManager();
        leaseExpireTime = new HashSet<ClaimedResidence>();
    }

    private static long daysToMs(int days) {
        return ((days) * 24L * 60L * 60L * 1000L);
    }

    private static int msToDays(long ms) {
        return (int) Math.ceil((((ms / 1000D) / 60D) / 60D) / 24D);
    }

    private static int daysRemaining(ClaimedResidence res) {
        if (res == null)
            return 999;
        Long get = res.getLeaseExpireTime();
        if (get <= System.currentTimeMillis())
            return 0;
        return msToDays((int) (get - System.currentTimeMillis()));
    }

    @Deprecated
    public boolean leaseExpires(ClaimedResidence res) {
        return isLeased(res);
    }

    public boolean isLeased(ClaimedResidence res) {
        if (res == null)
            return false;
        return res.getLeaseExpireTime() != null;
    }

    @Deprecated
    public boolean leaseExpires(String area) {
        return isLeased(plugin.getResidenceManager().getByName(area));
    }

    public String getExpireTime(ClaimedResidence res) {
        if (res == null)
            return null;
        Long time = res.getLeaseExpireTime();
        if (time != null) {
            return GetTime.getTime(time);
        }
        return null;
    }

    @Deprecated
    public String getExpireTime(String area) {
        return getExpireTime(plugin.getResidenceManager().getByName(area));
    }

    public void removeExpireTime(ClaimedResidence res) {
        if (res == null)
            return;
        leaseExpireTime.remove(res);
    }

    @Deprecated
    public void removeExpireTime(String area) {
        removeExpireTime(plugin.getResidenceManager().getByName(area));
    }

    public void setExpireTime(Player player, ClaimedResidence res, int days) {
        if (res == null) {
            if (player != null)
                plugin.msg(player, lm.Invalid_Area);
            return;
        }

        res.setLeaseExpireTime(daysToMs(days) + System.currentTimeMillis());
        leaseExpireTime.add(res);
        if (player != null)
            plugin.msg(player, lm.Economy_LeaseRenew, getExpireTime(res));
    }

    @Deprecated
    public void setExpireTime(String area, int days) {
        this.setExpireTime(null, area, days);
    }

//    @Deprecated
//    public int getRenewCost(ClaimedResidence res) {
//	double cost = res.getOwnerGroup().getLeaseRenewCost();
//	int amount = (int) Math.ceil(res.getTotalSize() * cost);
//	return amount;
//    }

    @Deprecated
    public void setExpireTime(Player player, String area, int days) {
        setExpireTime(player, plugin.getResidenceManager().getByName(area), days);
    }

    @Deprecated
    public void renewArea(String area, Player player) {
        renewArea(plugin.getResidenceManager().getByName(area), player);
    }

    public int getPlayerPermissfree(String playerName){
        Player player = Bukkit.getPlayerExact(playerName);
        int size = 100;
        if(player != null){
            for(PermissionAttachmentInfo perms : player.getEffectivePermissions()){
                if (perms.getPermission().startsWith("residence.addfree.")) {
                    if (perms.getPermission().contains("residence.addfree.*")) {
                        size = 0;
                        break;
                    } else {
                        // Get the max value should there be more than one
                        String[] spl = perms.getPermission().split("residence.addfree.");
                        if (spl.length > 1) {
                            if (!NumberUtils.isDigits(spl[1])) {
                                Residence.getInstance().getLogger().severe("Player " + player.getName() + " has permission: " + perms.getPermission() + " <-- the last part MUST be a number! Ignoring...");
                            } else {
                                size = Integer.valueOf(spl[1]);
                            }
                        }
                    }
                }
            }
        }
        return size;
    }

    public void renewArea(ClaimedResidence res, Player player) {
        if (res == null)
            return;
        if (!isLeased(res)) {
            plugin.msg(player, lm.Economy_LeaseNotExpire);
            return;
        }

        ResidencePlayer rPlayer = plugin.getPlayerManager().getResidencePlayer(player);
        PermissionGroup group = rPlayer.getGroup();
        int max = group.getMaxLeaseTime();
        int add = group.getLeaseGiveTime();
        int rem = daysRemaining(res);
        EconomyInterface econ = plugin.getEconomyManager();
        if (econ != null) {
            double cost = group.getLeaseRenewCost();
            int amount = (int) Math.ceil(res.getTotalSize() * cost);
            if (cost != 0D) {
                //Account account = iConomy.getBank().getAccount(player.getName());
                int freeAmount = amount * (getPlayerPermissfree(player.getName())/100);
                freeAmount = freeAmount * (IsLandAddAPI.getPlayerTakeMoney(player)/100);
                if (econ.canAfford(player.getName(), freeAmount)/*account.hasEnough(amount)*/) {
                    econ.subtract(player.getName(), freeAmount);
                    econ.add("Lease Money", freeAmount);
                    plugin.msg(player, lm.Economy_MoneyCharged, plugin.getEconomyManager().format(freeAmount), econ.getName());
                } else {
                    plugin.msg(player, lm.Economy_NotEnoughMoney);
                    return;
                }
            }
        }
        if (rem + add > max) {
            setExpireTime(player, res, max);
            plugin.msg(player, lm.Economy_LeaseRenewMax);
            return;
        }
        Long get = res.getLeaseExpireTime();
        if (get != null) {
            get = get + daysToMs(add);
            res.setLeaseExpireTime(get);

            leaseExpireTime.add(res);
        } else {
            res.setLeaseExpireTime(daysToMs(add));
            leaseExpireTime.add(res);
        }
        plugin.msg(player, lm.Economy_LeaseRenew, getExpireTime(res));
    }

    public double getRenewCostD(ClaimedResidence res) {
        double cost = res.getOwnerGroup().getLeaseRenewCost();
        double amount = res.getTotalSize() * cost;
        amount = Math.round(amount * 100) / 100D;
        return amount;
    }

    public void doExpirations() {

        Set<ClaimedResidence> t = new HashSet<ClaimedResidence>(leaseExpireTime);

        for (ClaimedResidence res : t) {
            if (res == null) {
                leaseExpireTime.remove(res);
                continue;
            }
            if (res.getLeaseExpireTime() > System.currentTimeMillis())
                continue;

            String resname = res.getName();
            boolean renewed = false;
            String owner = res.getPermissions().getOwner();

            PermissionGroup group = res.getOwnerGroup();

            double cost = this.getRenewCostD(res);
            if (plugin.getConfigManager().enableEconomy() && plugin.getConfigManager().autoRenewLeases()) {
                if (cost == 0) {
                    renewed = true;
                } else if (res.getBank().hasEnough(cost)) {
                    res.getBank().subtract(cost);
                    renewed = true;
                    if (plugin.getConfigManager().debugEnabled())
                        System.out.println("Lease Renewed From Residence Bank: " + resname);
                } else if (plugin.getEconomyManager().canAfford(owner, cost)) {
                    if (plugin.getEconomyManager().subtract(owner, cost)) {
                        renewed = true;
                        if (plugin.getConfigManager().debugEnabled())
                            System.out.println("Lease Renewed From Economy: " + resname);
                    }
                }
            }
            if (!renewed) {
                if (!plugin.getConfigManager().enabledRentSystem() || !plugin.getRentManager().isRented(resname)) {
                    ResidenceDeleteEvent resevent = new ResidenceDeleteEvent(null, res, DeleteCause.LEASE_EXPIRE);
                    plugin.getServ().getPluginManager().callEvent(resevent);
                    if (!resevent.isCancelled()) {
                        manager.removeResidence(res);
                        leaseExpireTime.remove(res);
                        if (plugin.getConfigManager().debugEnabled())
                            System.out.println("Lease NOT removed, Removing: " + resname);
                    }
                }
            } else {
                if (plugin.getConfigManager().enableEconomy() && plugin.getConfigManager().enableLeaseMoneyAccount()) {
                    plugin.getEconomyManager().add("Lease Money", cost);
                }
                if (plugin.getConfigManager().debugEnabled())
                    System.out.println("Lease Renew Old: " + res.getName());
                res.setLeaseExpireTime(System.currentTimeMillis() + daysToMs(group.getLeaseGiveTime()));
                if (plugin.getConfigManager().debugEnabled())
                    System.out.println("Lease Renew New: " + res.getName());
            }

        }
    }

    public void resetLeases() {
        leaseExpireTime.clear();
        String[] list = manager.getResidenceList();
        for (String item : list) {
            if (item == null)
                continue;
            ClaimedResidence res = plugin.getResidenceManager().getByName(item);
            if (res != null)
                setExpireTime(null, res, res.getOwnerGroup().getLeaseGiveTime());

        }
        System.out.println("[Residence] - Set default leases.");
    }

    public Map<String, Long> save() {
        Map<String, Long> m = new HashMap<String, Long>();
        for (ClaimedResidence one : leaseExpireTime) {
            m.put(one.getName(), one.getLeaseExpireTime());
        }
        return m;
    }

    @SuppressWarnings("unchecked")
    public LeaseManager load(@SuppressWarnings("rawtypes") Map root) {
        LeaseManager l = new LeaseManager(plugin);
        if (root == null)
            return l;

        for (Object val : root.values()) {
            if (!(val instanceof Long)) {
                root.remove(val);
            }
        }

        Map<String, Long> m = root;

        for (Entry<String, Long> one : m.entrySet()) {
            ClaimedResidence res = plugin.getResidenceManager().getByName(one.getKey());
            if (res == null)
                continue;
            res.setLeaseExpireTime(one.getValue());
            l.leaseExpireTime.add(res);
        }

        return l;
    }
}
