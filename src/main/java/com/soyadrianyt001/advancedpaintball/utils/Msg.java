package com.soyadrianyt001.advancedpaintball.utils;

import org.bukkit.ChatColor;

public final class Msg {

    public static final String PREFIX    = "&8[&b&lAdvanced&f&lPaintball&8] &r";
    public static final String PRE_ADMIN = "&8[&c&lAP&7-Admin&8] &r";
    public static final String PRE_OK    = "&8[&a&l✔&8] &r";
    public static final String PRE_ERR   = "&8[&c&l✗&8] &r";
    public static final String PRE_INFO  = "&8[&e&l!&8] &r";

    private Msg() {}

    public static String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
    public static String prefix(String s) { return c(PREFIX + s); }
    public static String admin(String s)  { return c(PRE_ADMIN + s); }
    public static String ok(String s)     { return c(PRE_OK + "&a" + s); }
    public static String err(String s)    { return c(PRE_ERR + "&c" + s); }
    public static String info(String s)   { return c(PRE_INFO + "&7" + s); }
    public static String sep()            { return c("&8&m" + "━".repeat(48)); }
}
