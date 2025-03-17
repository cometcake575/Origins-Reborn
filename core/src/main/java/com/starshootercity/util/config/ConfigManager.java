package com.starshootercity.util.config;

import com.starshootercity.OriginsReborn;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public class ConfigManager {

    public static String VERSION = "2.6.0";

    private static ConfigurationSection getConfig() {
        return OriginsReborn.getInstance().getConfig();
    }

    private static void saveConfig() {
        OriginsReborn.getInstance().saveConfig();
    }

    private static void setComments(String path, List<String> comments) {
        OriginsReborn.getInstance().setComments(path, comments);
    }

    public static <T> T getConfigValue(Option<T> option) {
        return option.getValue();
    }

    public static class Option<T> {
        private static boolean hasUpdated = false;

        private static final Map<String, Option<?>> options = new HashMap<>();

        public static Map<String, Option<?>> getOptions() {
            return options;
        }

        // Dimensions
        public static Option<String> OVERWORLD_DIMENSION;
        public static Option<String> NETHER_DIMENSION;
        public static Option<String> END_DIMENSION;
        public static Option<List<String>> DISABLED_WORLDS;

        // Commands on origin swap
        public static Option<Map<String, List<String>>> COMMANDS_ON_ORIGIN;

        // Ability blocking regions
        public static Option<Map<String, List<String>>> ABILITY_BLOCKING_REGIONS;

        // Cooldowns
        public static Option<Boolean> DISABLE_ALL_COOLDOWNS;
        public static Option<Boolean> SHOW_COOLDOWN_ICONS;

        // Misc
        public static Option<Boolean> DISABLE_FLIGHT_STUFF;

        // Swap command
        public static Option<Boolean> SWAP_COMMAND_ENABLED;
        public static Option<String> SWAP_COMMAND_PERMISSION;
        public static Option<Boolean> SWAP_COMMAND_RESETS_PLAYER;
        public static Option<Integer> SWAP_COMMAND_COOLDOWN;

        // Swap command vault integration
        public static Option<Boolean> SWAP_COMMAND_VAULT_ENABLED;
        public static Option<String> SWAP_COMMAND_VAULT_BYPASS_PERMISSION;
        public static Option<Integer> SWAP_COMMAND_VAULT_DEFAULT_COST;
        public static Option<Boolean> SWAP_COMMAND_VAULT_PERMANENT_PURCHASES;
        public static Option<String> SWAP_COMMAND_VAULT_CURRENCY_SYMBOL;

        // Origin selection
        public static Option<Integer> ORIGIN_SELECTION_DELAY_BEFORE_REQUIRED;
        public static Option<Map<String, Integer>> ORIGIN_SELECTION_LAYER_ORDERS;
        public static Option<Map<String, String>> ORIGIN_SELECTION_DEFAULT_ORIGINS;
        public static Option<String> ORIGIN_SELECTION_INVULNERABLE_MODE;
        public static Option<Boolean> ORIGIN_SELECTION_AUTO_SPAWN_TELEPORT;
        public static Option<Map<String, Boolean>> ORIGIN_SELECTION_RANDOMISE;
        public static Option<Boolean> ORIGIN_SELECTION_DEATH_ORIGIN_CHANGE;
        public static Option<Integer> ORIGIN_SELECTION_SCROLL_AMOUNT;

        // Randomisation option
        public static Option<Boolean> ORIGIN_SELECTION_RANDOM_OPTION_ENABLED;
        public static Option<List<String>> ORIGIN_SELECTION_RANDOM_OPTION_EXCLUDE;

        // Screen title
        public static Option<String> ORIGIN_SELECTION_SCREEN_TITLE_PREFIX;
        public static Option<String> ORIGIN_SELECTION_SCREEN_TITLE_BACKGROUND;
        public static Option<String> ORIGIN_SELECTION_SCREEN_TITLE_SUFFIX;

        // Orb of Origin
        public static Option<Boolean> ORB_OF_ORIGIN_RESET_PLAYER;
        public static Option<Boolean> ORB_OF_ORIGIN_CONSUME;
        public static Option<Map<String, Boolean>> ORB_OF_ORIGIN_RANDOM;
        public static Option<Boolean> ORB_OF_ORIGIN_ENABLE_RECIPE;
        public static Option<List<List<Material>>> ORB_OF_ORIGIN_RECIPE;

        // Resource pack
        public static Option<Boolean> RESOURCE_PACK_ENABLED;

        // Display
        public static Option<Boolean> DISPLAY_ENABLE_PREFIXES;

        // Restrictions
        public static Option<String> REUSING_ORIGINS;
        public static Option<Boolean> PREVENT_SAME_ORIGINS;

        // Geyser
        public static Option<Integer> GEYSER_JOIN_FORM_DELAY;

        // SkinsRestorer
        public static Option<Boolean> SKINSRESTORER_HOOK_ENABLED;
        public static Option<String> SKINSRESTORER_HOOK_IP;
        public static Option<Integer> SKINSRESTORER_HOOK_PORT;

        public static Option<String> CONFIG_VERSION;

        public static void initialize() {
            OVERWORLD_DIMENSION = new Option<>(SettingType.STRING, "worlds.world", "Overworld dimension", "world");
            NETHER_DIMENSION = new Option<>(SettingType.STRING, "worlds.world_nether", "Nether dimension", "world_nether");
            END_DIMENSION = new Option<>(SettingType.STRING, "worlds.world_the_end", "End dimension", "world_the_end");
            DISABLED_WORLDS = new Option<>(SettingType.STRING_LIST, "worlds.disabled_worlds", "Worlds to disable origins in", List.of("example_world"));

            COMMANDS_ON_ORIGIN = new Option<>(SettingType.MAP_STRING_TO_STRING_LIST, "commands-on-origin",
                    List.of(
                            "Runs commands when the player switches to an origin",
                            "Origins should be formatted as they are in the file names, but without the extension, e.g. \"human\"",
                            "%player% is replaced with the player's username and %uuid% is replaced with their UUID",
                            "Use \"default\" for commands that should be run regardless of origin"
                    ),
                    Map.of("example", List.of("example %player%", "example %uuid%")));

            ABILITY_BLOCKING_REGIONS = new Option<>(SettingType.MAP_STRING_TO_STRING_LIST, "prevent-abilities-in", Collections.singletonList("A list of WorldGuard regions in which to prevent the use of certain abilities, use 'all' for all abilities"),
                    Map.of(
                            "no_water_breathing", Collections.singletonList("origins:water_breathing"),
                            "no_abilities", Collections.singletonList("all")
                    ));

            DISABLE_ALL_COOLDOWNS = new Option<>(SettingType.BOOLEAN, "cooldowns.disable-all-cooldowns", Collections.singletonList("Disables every cooldown"), false);
            SHOW_COOLDOWN_ICONS = new Option<>(SettingType.BOOLEAN, "cooldowns.show-cooldown-icons", List.of("Use the actionbar to show cooldown icons", "You may want to disable this if using another plugin that requires the actionbar"), true);

            DISABLE_FLIGHT_STUFF = new Option<>(SettingType.BOOLEAN, "misc-settings.disable-flight-stuff", "Disable all flight-related features. This does not hide the abilities themselves, they must be removed from the .yml files in the ~/plugins/Origins-Reborn/origins/ folder", false);

            SWAP_COMMAND_ENABLED = new Option<>(SettingType.BOOLEAN, "swap-command.enabled", "Enable the swap command", true);
            SWAP_COMMAND_PERMISSION = new Option<>(SettingType.STRING, "swap-command.permission", "Permission required for origin swap command", "originsreborn.admin");
            SWAP_COMMAND_RESETS_PLAYER = new Option<>(SettingType.BOOLEAN, "swap-command.reset-player", "Reset player data like inventory and spawn point when switching origins using the /origin swap command", false);
            SWAP_COMMAND_COOLDOWN = new Option<>(SettingType.INTEGER, "swap-command.cooldown", "Cooldown for the command (in ticks), -1 means no cooldown", -1);

            SWAP_COMMAND_VAULT_ENABLED = new Option<>(SettingType.BOOLEAN, "swap-command.vault.enabled", "Enable charging players with Vault", false);
            SWAP_COMMAND_VAULT_BYPASS_PERMISSION = new Option<>(SettingType.STRING, "swap-command.vault.bypass-permission", "Permission to bypass the cost of the swap command", "originsreborn.costbypass");
            SWAP_COMMAND_VAULT_DEFAULT_COST = new Option<>(SettingType.INTEGER, "swap-command.vault.default-cost", "Default cost of switching origin, if it hasn't been overriden in the origin file", 1000);
            SWAP_COMMAND_VAULT_PERMANENT_PURCHASES = new Option<>(SettingType.BOOLEAN, "swap-command.vault.permanent-purchases", "Allows the player to switch back to origins for free if they already had the origin before", false);
            SWAP_COMMAND_VAULT_CURRENCY_SYMBOL = new Option<>(SettingType.STRING, "swap-command.vault.currency-symbol", "Currency symbol for the economy", "$");

            ORIGIN_SELECTION_DELAY_BEFORE_REQUIRED = new Option<>(SettingType.INTEGER, "origin-selection.delay-before-required", "The amount of time (in ticks, a tick is a 20th of a second) to wait between a player joining and when the GUI should open", 0);
            ORIGIN_SELECTION_LAYER_ORDERS = new Option<>(SettingType.MAP_STRING_TO_INTEGER, "origin-selection.layer-orders", "Priorities for different origin 'layers' to be selected in, higher priority layers are selected first.",
                    Map.of("origin", 1));
            ORIGIN_SELECTION_DEFAULT_ORIGINS = new Option<>(SettingType.MAP_STRING_TO_STRING, "origin-selection.default-origin",
                    List.of(
                            "Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin",
                            "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'",
                            "Disabled if set to an invalid name such as \"NONE\""
                    ), Map.of("origin", "NONE"));
            ORIGIN_SELECTION_INVULNERABLE_MODE = new Option<>(SettingType.STRING, "origin-selection.invulnerable-mode",
                    List.of(
                            "OFF - you can take damage with the origin selection GUI open",
                            "ON - you cannot take damage with the origin selection GUI open",
                            "INITIAL - you cannot take damage if you do not have an origin (and therefore cannot close the screen)"
                    ), "OFF");
            ORIGIN_SELECTION_AUTO_SPAWN_TELEPORT = new Option<>(SettingType.BOOLEAN, "origin-selection.auto-spawn-teleport", "Automatically teleport players to the world spawn when first selecting an origin", true);
            ORIGIN_SELECTION_RANDOMISE = new Option<>(SettingType.MAP_STRING_TO_BOOLEAN, "origin-selection.randomise", "Randomise origins instead of letting players pick",
                    Map.of("origin", false));
            ORIGIN_SELECTION_DEATH_ORIGIN_CHANGE = new Option<>(SettingType.BOOLEAN, "origin-selection.death-origin-change",
                    List.of("Allows players to choose a new origin when they die",
                            "If randomise is enabled this will reroll their origin to something random"),
                    false);
            ORIGIN_SELECTION_SCROLL_AMOUNT = new Option<>(SettingType.INTEGER, "origin-selection.scroll-amount", "Amount to scroll per scroll button click", 1);

            ORIGIN_SELECTION_RANDOM_OPTION_ENABLED = new Option<>(SettingType.BOOLEAN, "origin-selection.random-option.enabled", "Enable the random option choice - does nothing if randomise is enabled", true);
            ORIGIN_SELECTION_RANDOM_OPTION_EXCLUDE = new Option<>(SettingType.STRING_LIST, "origin-selection.random-option.exclude", "Origins to exclude from random options", List.of("human"));

            ORIGIN_SELECTION_SCREEN_TITLE_PREFIX = new Option<>(SettingType.STRING, "origin-selection.screen-title.prefix", "Prefix of GUI title", "");
            ORIGIN_SELECTION_SCREEN_TITLE_BACKGROUND = new Option<>(SettingType.STRING, "origin-selection.screen-title.background", "Background, between Origins-Reborn background and things like text", "");
            ORIGIN_SELECTION_SCREEN_TITLE_SUFFIX = new Option<>(SettingType.STRING, "origin-selection.screen-title.suffix", "Suffix of GUI title", "");

            ORB_OF_ORIGIN_RESET_PLAYER = new Option<>(SettingType.BOOLEAN, "orb-of-origin.reset-player", "Reset player data like inventory and spawn point when switching origins using the Orb of Origin", false);
            ORB_OF_ORIGIN_CONSUME = new Option<>(SettingType.BOOLEAN, "orb-of-origin.consume", "Consume the Orb of Origin upon use", true);
            ORB_OF_ORIGIN_RANDOM = new Option<>(SettingType.MAP_STRING_TO_BOOLEAN, "orb-of-origin.random", "Randomise origin instead of opening the selector upon using the orb",
                    Map.of("origin", false));
            ORB_OF_ORIGIN_ENABLE_RECIPE = new Option<>(SettingType.BOOLEAN, "orb-of-origin.enable-recipe", "Enable recipe for crafting the Orb of Origin", false);
            ORB_OF_ORIGIN_RECIPE = new Option<>(SettingType.MATERIAL_LIST_LIST, "orb-of-origin.recipe", "Crafting recipe for the Orb of Origin",
                    List.of(
                            List.of(Material.AIR, Material.DIAMOND, Material.AIR),
                            List.of(Material.DIAMOND, Material.NETHER_STAR, Material.DIAMOND),
                            List.of(Material.AIR, Material.DIAMOND, Material.AIR)
                    ));

            RESOURCE_PACK_ENABLED = new Option<>(SettingType.BOOLEAN, "resource-pack.enabled", List.of(
                    "Whether to enable the resource pack",
                    "If this is set to false you should send the pack to players either in server.properties or in another plugin",
                    "You can find the packs for each version on the GitHub at https://github.com/cometcake575/Origins-Reborn/tree/main/packs/"
            ), true);

            DISPLAY_ENABLE_PREFIXES = new Option<>(SettingType.BOOLEAN, "display.enable-prefixes", "Enable prefixes in tab and on display names with the names of origins", false);

            REUSING_ORIGINS = new Option<>(SettingType.STRING, "restrictions.reusing-origins", List.of(
                    "\"NONE\" allows origins to be reused",
                    "\"PERPLAYER\" means individual players can only use an origin once",
                    "\"ALL\" means no players can use an origin again after one has selected it"
            ), "NONE");
            PREVENT_SAME_ORIGINS = new Option<>(SettingType.BOOLEAN, "restrictions.prevent-same-origins", "Prevent players from having the same origins as other players, this is locked on if reusing-origins is set to ALL", false);

            GEYSER_JOIN_FORM_DELAY = new Option<>(SettingType.INTEGER, "geyser.join-form-delay", "The delay in ticks to wait before showing a new Bedrock player the selection GUI", 20);

            SKINSRESTORER_HOOK_ENABLED = new Option<>(SettingType.BOOLEAN, "skinsrestorer-hook.enabled", "Whether to enable the SkinsRestorer hook", false);
            SKINSRESTORER_HOOK_IP = new Option<>(SettingType.STRING, "skinsrestorer-hook.ip", "The IP of your server, set this to your server's public IP (without the port)", "UNSET");
            SKINSRESTORER_HOOK_PORT = new Option<>(SettingType.INTEGER, "skinsrestorer-hook.port", "Set this to any other open port on the server, this is used to host the image of the skin, so it can be used by SkinsRestorer", 80);

            CONFIG_VERSION = new Option<>(SettingType.STRING, "config-version", "Config version - do not touch this!", VERSION);

            try {
                LegacyConfigUpdater.updateConfig();
            } catch (Throwable ignored) {

            }

            makeOptionHeader("worlds", Collections.singletonList("Worlds used for some abilities"));
            makeOptionHeader("cooldowns", "Configuration for cooldowns");
            makeOptionHeader("misc-settings", "Miscellaneous settings");
            makeOptionHeader("swap-command", "The /origin swap command, allowing players to switch origin at will");
            makeOptionHeader("swap-command.vault", "Charge players using Vault to switch their origins");
            makeOptionHeader("origin-selection", "Settings for origin selection");
            makeOptionHeader("origin-selection.random-option", "Allows the player to pick a 'Random' option");
            makeOptionHeader("origin-selection.screen-title", "Prefixes and suffixes for the selection screen title");
            makeOptionHeader("orb-of-origin", "Settings for the Orb of Origin");
            makeOptionHeader("resource-pack", "Settings for the resource pack");
            makeOptionHeader("display", "Miscellaneous display options");
            makeOptionHeader("restrictions", List.of("Restrictions placed on origin selection", "These are designed for use with addon plugins that add many new origins", "If you run out of origins that fit the restrictions you may experience issues"));
            makeOptionHeader("geyser", "Settings for using GeyserMC");
            makeOptionHeader("skinsrestorer-hook", "Enables abilities that change the player's skin (requires SkinsRestorer to work)");

            if (hasUpdated) saveConfig();
        }

        private final SettingType<T> type;
        private final String path;
        private final T defaultValue;
        private final List<String> comments;

        Option(SettingType<T> settingType, String path, List<String> comments, T defaultValue) {
            options.put(path, this);
            this.type = settingType;
            this.path = path;
            this.comments = comments;
            this.defaultValue = defaultValue;

            if (!getConfig().contains(path)) {
                hasUpdated = true;
                settingType.set(getConfig(), path, defaultValue);
                setComments(path, comments);
            }
        }

        Option(SettingType<T> settingType, String path, String comment, T defaultValue) {
            this(settingType, path, Collections.singletonList(comment), defaultValue);
        }

        public T getValue() {
            return type.get(getConfig(), path);
        }

        public String getPath() {
            return path;
        }

        @ApiStatus.Internal
        @SuppressWarnings("unchecked")
        public void setValue(Object value) {
            hasUpdated = true;
            type.set(getConfig(), path, (T) value);
            setComments(path, comments);
        }

        public void resetValue() {
            hasUpdated = true;
            type.set(getConfig(), path, defaultValue);
            setComments(path, comments);
        }

        private static void makeOptionHeader(String path, List<String> comments) {
            if (!getConfig().contains(path)) {
                getConfig().set(path, "[Placeholder]");
                hasUpdated = true;
            }
            setComments(path, comments);
        }

        private static void makeOptionHeader(String path, String comment) {
            makeOptionHeader(path, Collections.singletonList(comment));
        }
    }

    public interface SettingType<T> {
        T get(ConfigurationSection config, String path);

        default void set(ConfigurationSection config, String path, T value) {
            config.set(path, value);
        }

        SettingType<String> STRING = ConfigurationSection::getString;

        SettingType<List<String>> STRING_LIST = ConfigurationSection::getStringList;

        SettingType<Map<String, List<String>>> MAP_STRING_TO_STRING_LIST = new SettingType<>() {
            @Override
            public Map<String, List<String>> get(ConfigurationSection config, String path) {
                Map<String, List<String>> result = new HashMap<>();
                ConfigurationSection section = config.getConfigurationSection(path);
                if (section == null) return Map.of();
                for (String s : section.getKeys(false)) {
                    result.put(s, section.getStringList(s));
                }
                return result;
            }

            @Override
            public void set(ConfigurationSection config, String path, Map<String, List<String>> value) {
                for (String s : value.keySet()) {
                    config.set(path + "." + s, value.get(s));
                }
            }
        };

        SettingType<Map<String, String>> MAP_STRING_TO_STRING = new SettingType<>() {
            @Override
            public Map<String, String> get(ConfigurationSection config, String path) {
                Map<String, String> result = new HashMap<>();
                ConfigurationSection section = config.getConfigurationSection(path);
                if (section == null) return Map.of();
                for (String s : section.getKeys(false)) {
                    result.put(s, section.getString(s));
                }
                return result;
            }

            @Override
            public void set(ConfigurationSection config, String path, Map<String, String> value) {
                for (String s : value.keySet()) {
                    config.set(path + "." + s, value.get(s));
                }
            }
        };

        SettingType<Map<String, Integer>> MAP_STRING_TO_INTEGER = new SettingType<>() {
            @Override
            public Map<String, Integer> get(ConfigurationSection config, String path) {
                Map<String, Integer> result = new HashMap<>();
                ConfigurationSection section = config.getConfigurationSection(path);
                if (section == null) return Map.of();
                for (String s : section.getKeys(false)) {
                    result.put(s, section.getInt(s));
                }
                return result;
            }

            @Override
            public void set(ConfigurationSection config, String path, Map<String, Integer> value) {
                for (String s : value.keySet()) {
                    config.set(path + "." + s, value.get(s));
                }
            }
        };

        SettingType<Map<String, Boolean>> MAP_STRING_TO_BOOLEAN = new SettingType<>() {
            @Override
            public Map<String, Boolean> get(ConfigurationSection config, String path) {
                Map<String, Boolean> result = new HashMap<>();
                ConfigurationSection section = config.getConfigurationSection(path);
                if (section == null) return Map.of();
                for (String s : section.getKeys(false)) {
                    result.put(s, section.getBoolean(s));
                }
                return result;
            }

            @Override
            public void set(ConfigurationSection config, String path, Map<String, Boolean> value) {
                for (String s : value.keySet()) {
                    config.set(path + "." + s, value.get(s));
                }
            }
        };

        SettingType<List<List<Material>>> MATERIAL_LIST_LIST = new SettingType<>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<List<Material>> get(ConfigurationSection config, String path) {
                List<List<Material>> result = new ArrayList<>();
                List<?> list = config.getList(path);
                if (list == null) return List.of();
                for (Object s : list) {
                    List<Material> la = new ArrayList<>();
                    for (String str : (List<String>) s) {
                        la.add(Material.matchMaterial(str));
                    }
                    result.add(la);
                }
                return result;
            }

            @Override
            public void set(ConfigurationSection config, String path, List<List<Material>> value) {
                List<List<String>> stringValues = new ArrayList<>();
                for (List<Material> materials : value) {
                    List<String> strings = new ArrayList<>();
                    for (Material material : materials) strings.add("minecraft:" + material.toString().toLowerCase());
                    stringValues.add(strings);
                }
                config.set(path, stringValues);
            }
        };

        SettingType<List<Material>> MATERIAL_LIST = new SettingType<>() {
            @Override
            public List<Material> get(ConfigurationSection config, String path) {
                List<Material> result = new ArrayList<>();
                for (String s : config.getStringList(path)) {
                    result.add(Material.matchMaterial(s));
                }
                return result;
            }

            @Override
            public void set(ConfigurationSection config, String path, List<Material> value) {
                List<String> stringValues = new ArrayList<>();
                for (Material material : value) stringValues.add("minecraft:" + material.toString().toLowerCase());
                config.set(path, stringValues);
            }
        };

        SettingType<Integer> INTEGER = ConfigurationSection::getInt;

        SettingType<Float> FLOAT = (config, path) -> (float) config.getDouble(path);

        SettingType<Double> DOUBLE = ConfigurationSection::getDouble;

        SettingType<Boolean> BOOLEAN = ConfigurationSection::getBoolean;
    }
}
