package com.hologramx.utils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    // Common color names for tab completion
    public static final List<String> COLOR_NAMES = Arrays.asList(
        "transparent", "black", "white", "red", "green", "blue", "yellow", "orange", 
        "purple", "pink", "cyan", "magenta", "lime", "gray", "grey", "brown",
        "dark_red", "dark_green", "dark_blue", "dark_purple", "dark_gray", "dark_grey",
        "light_blue", "light_green", "light_red", "light_purple", "light_gray", "light_grey"
    );
    
    // RGB pattern matching
    private static final Pattern RGB_PATTERN = Pattern.compile("rgb\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\)");
    private static final Pattern HEX_PATTERN = Pattern.compile("#?([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})");
    
    /**
     * Parse color string into hex format
     * Supports: transparent, named colors, #RRGGBB, #AARRGGBB, rgb(r,g,b)
     */
    public static String parseColor(String input) {
        if (input == null || input.isEmpty()) {
            return "transparent";
        }
        
        String color = input.toLowerCase().trim();
        
        // Handle transparent
        if ("transparent".equals(color)) {
            return "transparent";
        }
        
        // Handle RGB format: rgb(255, 0, 0)
        Matcher rgbMatcher = RGB_PATTERN.matcher(color);
        if (rgbMatcher.matches()) {
            try {
                int r = Integer.parseInt(rgbMatcher.group(1));
                int g = Integer.parseInt(rgbMatcher.group(2));
                int b = Integer.parseInt(rgbMatcher.group(3));
                
                if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                    return String.format("#%02X%02X%02X", r, g, b);
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        // Handle hex format: #RRGGBB or #AARRGGBB
        Matcher hexMatcher = HEX_PATTERN.matcher(color);
        if (hexMatcher.matches()) {
            String hex = hexMatcher.group(1);
            if (hex.length() == 6) {
                return "#" + hex.toUpperCase();
            } else if (hex.length() == 8) {
                return "#" + hex.toUpperCase();
            }
        }
        
        // Handle named colors
        String hexColor = getNamedColor(color);
        if (hexColor != null) {
            return hexColor;
        }
        
        return null;
    }
    
    /**
     * Get hex value for named colors
     */
    private static String getNamedColor(String name) {
        return switch (name.toLowerCase()) {
            case "black" -> "#000000";
            case "white" -> "#FFFFFF";
            case "red" -> "#FF0000";
            case "green" -> "#008000";
            case "blue" -> "#0000FF";
            case "yellow" -> "#FFFF00";
            case "orange" -> "#FFA500";
            case "purple" -> "#800080";
            case "pink" -> "#FFC0CB";
            case "cyan" -> "#00FFFF";
            case "magenta" -> "#FF00FF";
            case "lime" -> "#00FF00";
            case "gray", "grey" -> "#808080";
            case "brown" -> "#A52A2A";
            case "dark_red" -> "#8B0000";
            case "dark_green" -> "#006400";
            case "dark_blue" -> "#00008B";
            case "dark_purple" -> "#483D8B";
            case "dark_gray", "dark_grey" -> "#404040";
            case "light_blue" -> "#ADD8E6";
            case "light_green" -> "#90EE90";
            case "light_red" -> "#FFB6C1";
            case "light_purple" -> "#DDA0DD";
            case "light_gray", "light_grey" -> "#D3D3D3";
            default -> null;
        };
    }
    
    /**
     * Validate if a color string is valid
     */
    public static boolean isValidColor(String color) {
        return parseColor(color) != null;
    }
    
    /**
     * Get suggestions for tab completion based on input
     */
    public static List<String> getColorSuggestions(String input) {
        if (input == null || input.isEmpty()) {
            return COLOR_NAMES;
        }
        
        String lower = input.toLowerCase();
        List<String> suggestions = COLOR_NAMES.stream()
            .filter(color -> color.startsWith(lower))
            .collect(java.util.stream.Collectors.toList());
        
        // Add RGB format suggestion
        if ("rgb".startsWith(lower)) {
            suggestions.add("rgb(255,0,0)");
        }
        
        // Add hex format suggestions
        if ("#".startsWith(lower) || "hex".startsWith(lower)) {
            suggestions.add("#FF0000");
            suggestions.add("#00FF00");
            suggestions.add("#0000FF");
        }
        
        return suggestions;
    }
    
    /**
     * Convert hex color to ARGB integer (for Bukkit Color)
     */
    public static int hexToARGB(String hex) {
        if (hex == null || "transparent".equals(hex)) {
            return 0x00000000;
        }
        
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        
        try {
            if (hex.length() == 6) {
                // RGB format - add full opacity
                return (int) (0xFF000000L | Long.parseLong(hex, 16));
            } else if (hex.length() == 8) {
                // ARGB format
                return (int) Long.parseLong(hex, 16);
            }
        } catch (NumberFormatException e) {
            return 0x00000000;
        }
        
        return 0x00000000;
    }
    
    /**
     * Format color for display in messages
     */
    public static String formatColorForDisplay(String color) {
        if ("transparent".equals(color)) {
            return "transparent";
        }
        
        if (color != null && color.startsWith("#")) {
            return color.toUpperCase();
        }
        
        return color;
    }
}