package com.fragrance.util;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class ThemeConfig {
    public static final Color BG_PRIMARY  = new Color(0x1C, 0x1B, 0x2E);
    public static final Color BG_SIDEBAR  = new Color(0x13, 0x12, 0x2A);
    public static final Color BG_CARD     = new Color(0x25, 0x24, 0x40);
    public static final Color BG_TABLE    = new Color(0x1E, 0x1D, 0x38);
    public static final Color ACCENT      = new Color(0xD4, 0xA8, 0x43); 
    public static final Color ACCENT_TEXT = new Color(0x1C, 0x1B, 0x2E); 
    public static final Color TEXT_HEAD   = new Color(0xEE, 0xE8, 0xD0);
    public static final Color TEXT_BODY   = new Color(0xC0, 0xBD, 0xD8);
    public static final Color TEXT_MUTED  = new Color(0x7A, 0x78, 0x98);
    public static final Color SUCCESS     = new Color(0x81, 0xC9, 0x95);
    public static final Color DANGER      = new Color(0xE5, 0x73, 0x73);
    public static final Color WARNING     = new Color(0xF4, 0xA2, 0x61);

    public static void apply() {
        try {
            FlatDarkLaf.setup();
            UIManager.put("Button.arc",               8);
            UIManager.put("Component.arc",            8);
            UIManager.put("TextComponent.arc",        6);
            UIManager.put("Button.background",        ACCENT);
            UIManager.put("Button.foreground",        ACCENT_TEXT);
            UIManager.put("Button.hoverBackground",   new Color(0xC0, 0x94, 0x35));
            UIManager.put("Table.selectionBackground",BG_CARD);
            UIManager.put("Table.selectionForeground",ACCENT);
            UIManager.put("ScrollBar.width",          8);
            UIManager.put("defaultFont",              new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
