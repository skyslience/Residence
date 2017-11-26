package com.bekvon.bukkit.residence.utils;

import com.bekvon.bukkit.residence.Residence;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class GetTime {
    public static String getTime(Long time) {
        Date dNow = new Date(time);
        SimpleDateFormat ft = new SimpleDateFormat(Residence.getInstance().getConfigManager().getDateFormat());
        ft.setTimeZone(TimeZone.getTimeZone(Residence.getInstance().getConfigManager().getTimeZone()));
        return ft.format(dNow);
    }
}
