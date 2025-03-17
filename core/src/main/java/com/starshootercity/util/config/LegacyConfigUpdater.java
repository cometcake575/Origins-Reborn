package com.starshootercity.util.config;

import com.starshootercity.OriginsReborn;
import com.starshootercity.abilities.AbilityRegister;
import com.starshootercity.abilities.AttributeModifierAbility;
import com.starshootercity.abilities.FreshAir;
import com.starshootercity.cooldowns.CooldownAbility;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.intellij.lang.annotations.Subst;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class LegacyConfigUpdater {

    // Nobody bother optimising this, it's run a single time if someone has just updated from a version before 2.6.0 and never again
    public static void updateConfig() {
        String version = getConfig().getString("config-version", "1.0.0");

        if (version.equals("1.0.0")) {
            File configFile = new File(OriginsReborn.getInstance().getDataFolder(), "config.yml");
            boolean ignored = configFile.delete();
            OriginsReborn.getInstance().saveDefaultConfig();
        }
        if (version.equals("2.0.0")) {
            getConfig().set("config-version", "2.0.3");
            getConfig().set("display.enable-prefixes", false);
            setComments("display", List.of("Miscellaneous display options"));
            setComments("display.enable-prefixes", List.of("Enable prefixes in tab and on display names with the names of origins"));
            saveConfig();
        }
        if (version.equals("2.0.3")) {
            getConfig().set("config-version", "2.1.7");
            getConfig().set("restrictions.reusing-origins", "NONE");
            getConfig().set("restrictions.prevent-same-origins", false);
            setComments("restrictions",
                    List.of(
                            "Restrictions placed on origin selection",
                            "These are designed for use with addon plugins that add many new origins",
                            "If you run out of origins that fit the restrictions you may experience issues"
                    )
            );
            setComments("restrictions.reusing-origins",
                    List.of(
                            "Rule for reusing origins",
                            "\"NONE\" allows origins to be reused",
                            "\"PERPLAYER\" means individual players can only use an origin once",
                            "\"ALL\" means no players can use an origin again after one has selected it"
                    )
            );
            setComments("restrictions.prevent-same-origins",
                    List.of(
                            "Prevent players from having the same origins as other players",
                            "This is locked on if reusing-origins is set to ALL"
                    )
            );
            saveConfig();
        }
        if ((version.equals("2.1.7") || version.equals("2.1.10"))) {
            getConfig().set("config-version", "2.1.11");
            getConfig().set("worlds.disabled-worlds", List.of("example_world"));
            setComments("worlds.disabled-worlds", List.of("Worlds to disable origins in"));
            saveConfig();
        }
        if (version.equals("2.1.14")) {
            getConfig().set("config-version", "2.1.11");
            setComments("origin-selection.show-initial-gui", null);
            getConfig().set("origin-selection.show-initial-gui", null);
            saveConfig();
        }
        if (version.equals("2.1.11")) {
            getConfig().set("config-version", "2.1.16");
            getConfig().set("origin-selection.auto-spawn-teleport", true);
            setComments("origin-selection.auto-spawn-teleport", List.of("Automatically teleport players to the world spawn when first selecting an origin"));
            saveConfig();
        }
        if (version.equals("2.1.16")) {
            getConfig().set("config-version", "2.1.17");
            getConfig().set("origin-selection.invulnerable-mode", "OFF");
            setComments("origin-selection.invulnerable-mode", List.of(
                    "OFF - you can take damage with the origin selection GUI open",
                    "ON - you cannot take damage with the origin selection GUI open",
                    "INITIAL - you cannot take damage if you do not have an origin (and therefore cannot close the screen)"
            ));
            saveConfig();
        }
        if (version.equals("2.1.17")) {
            getConfig().set("config-version", "2.1.18");
            getConfig().set("origin-selection.screen-title.prefix", "");
            getConfig().set("origin-selection.screen-title.suffix", "");
            setComments("origin-selection.screen-title.prefix", List.of("Prefix of GUI title"));
            setComments("origin-selection.screen-title.suffix", List.of("Suffix of GUI title"));
            setComments("origin-selection.screen-title", List.of("Prefixes and suffixes for the selection screen title", "This is an advanced setting - only use it if you know how"));
            saveConfig();
        }
        if (version.equals("2.1.18")) {
            getConfig().set("config-version", "2.1.19");
            getConfig().set("origin-selection.screen-title.background", "");
            setComments("origin-selection.screen-title.background", List.of("Background, between Origins-Reborn background and things like text"));
            saveConfig();
        }
        if (version.equals("2.1.19") || version.equals("2.1.20")) {
            getConfig().set("config-version", "2.2.3");
            getConfig().set("misc-settings.disable-flight-stuff", false);
            setComments("misc-settings", List.of("Miscellaneous settings"));
            setComments("misc-settings.disable-flight-stuff", List.of("Disable all flight-related features. This does not hide the abilities themselves, they must be removed from the .yml files in the ~/plugins/Origins-Reborn/origins/ folder"));
            saveConfig();
        }
        if (version.equals("2.2.3")) {
            getConfig().set("config-version", "2.2.5");
            getConfig().set("geyser.join-form-delay", 20);
            setComments("geyser", List.of("Settings for using GeyserMC"));
            setComments("geyser.join-form-delay", List.of("The delay in ticks to wait before showing a new Bedrock player the selection GUI"));
            saveConfig();
        }
        if (version.equals("2.2.5")) {
            getConfig().set("config-version", "2.2.18");
            getConfig().set("swap-command.vault.default-cost", getConfig().getInt("swap-command.vault.cost", 1000));
            setComments("swap-command.vault.cost", null);
            getConfig().set("swap-command.vault.cost", null);
            setComments("swap-command.vault.default-cost", List.of("Default cost of switching origin, if it hasn't been overriden in the origin file"));
            getConfig().set("origin-selection.default-origin", "NONE");
            setComments("origin-selection.default-origin", List.of("Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin", "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'", "Disabled if set to an invalid name such as \"NONE\""));
            saveConfig();
        }
        if (version.equals("2.2.18")) {
            getConfig().set("config-version", "2.2.20");
            getConfig().set("swap-command.vault.permanent-purchases", false);
            setComments("swap-command.vault.permanent-purchases", List.of("Allows the player to switch back to origins for free if they already had the origin before"));
            saveConfig();
        }
        if (version.equals("2.2.20")) {
            getConfig().set("config-version", "2.3.0");
            getConfig().set("cooldowns.disable-all-cooldowns", false);
            getConfig().set("cooldowns.show-cooldown-icons", true);
            setComments("cooldowns", List.of("Configuration for cooldowns"));
            setComments("cooldowns.disable-all-cooldowns", List.of("Disables every cooldown", " To modify specific cooldowns, edit the cooldown-config.yml file"));
            setComments("cooldowns.show-cooldown-icons", List.of("Use the actionbar to show cooldown icons", "You may want to disable this if using another plugin that requires the actionbar"));
            setComments("resource-pack.enabled", List.of("Whether to enable the resource pack", "If this is set to false you should send the pack to players either in server.properties or in another plugin", "You can find the packs for each version on the GitHub at https://github.com/cometcake575/Origins-Reborn/tree/main/packs/"));
            getConfig().set("resource-pack.link", null);
            saveConfig();
        }
        if (version.equals("2.3.0")) {
            getConfig().set("config-version", "2.3.14");
            getConfig().set("commands-on-origin.example", List.of("example %player%", "example %uuid%"));
            setComments("commands-on-origin.example", List.of("Example configuration for a command on origin switch"));
            setComments("commands-on-origin", List.of("Runs commands when the player switches to an origin", "Origins should be formatted as they are in the file names, but without the extension, e.g. \"human\"", "%player% is replaced with the player's username and %uuid% is replaced with their UUID"));
            saveConfig();
        }
        if (version.equals("2.3.14")) {
            getConfig().set("config-version", "2.3.17");
            setComments("commands-on-origin", List.of("Runs commands when the player switches to an origin", "Origins should be formatted as they are in the file names, but without the extension, e.g. \"human\"", "%player% is replaced with the player's username and %uuid% is replaced with their UUID", "Use \"default\" for commands that should be run regardless of origin"));
            saveConfig();
        }
        if (version.equals("2.3.15") || version.equals("2.3.17")) {
            getConfig().set("config-version", "2.3.18");
            getConfig().set("prevent-abilities-in.no_water_breathing", List.of("origins:water_breathing"));
            getConfig().set("prevent-abilities-in.no_abilities", List.of("all"));
            setComments("prevent-abilities-in.no_water_breathing", List.of("Example region in which the water breathing ability is disabled"));
            setComments("prevent-abilities-in.no_abilities", List.of("Example region where all abilities are disabled"));
            setComments("prevent-abilities-in", List.of("A list of WorldGuard regions in which to prevent the use of certain abilities, use 'all' for all abilities"));
            saveConfig();
        }
        if (version.equals("2.3.18")) {
            getConfig().set("config-version", "2.3.20");
            getConfig().set("orb-of-origin.random", false);
            setComments("orb-of-origin.random", List.of("Randomise origin instead of opening the selector upon using the orb"));
            saveConfig();
        }
        if (version.equals("2.3.20")) {
            getConfig().set("config-version", "2.4.0");
            getConfig().set("origin-selection.default-origin.origin", getConfig().get("origin-selection.default-origin"));
            getConfig().set("origin-selection.layer-orders.origin", 1);
            setComments("origin-selection.default-origin", List.of("Default origin, automatically gives players this origin rather than opening the GUI when the player has no origin", "Should be the name of the origin file without the ending, e.g. for 'origin_name.json' the value should be 'origin_name'", "Disabled if set to an invalid name such as \"NONE\""));
            setComments("origin-section.layer-orders", List.of("Priorities for different origin 'layers' to be selected in, higher priority layers are selected first."));
            saveConfig();
        }
        if (version.equals("2.4.0")) {
            getConfig().set("config-version", "2.4.1");
            getConfig().set("orb-of-origin.random.origin", getConfig().get("orb-of-origin.random"));
            getConfig().set("origin-selection.randomise.origin", getConfig().get("origin-selection.randomise"));
            setComments("orb-of-origin.random", List.of("Randomise origin instead of opening the selector upon using the orb"));
            setComments("origin-selection.randomise", List.of("Randomise origins instead of letting players pick"));
            saveConfig();
        }
        if (version.equals("2.4.1")) {
            getConfig().set("config-version", "2.4.2");
            getConfig().set("origin-selection.delay-before-required", 0);
            setComments("origin-selection.delay-before-required", List.of("The amount of time (in ticks, a tick is a 20th of a second) to wait between a player joining and when the GUI should open"));
            saveConfig();
        }
        if (version.equals("2.4.2")) {
            getConfig().set("config-version", "2.5.0");
            int i = getConfig().getInt("extra-settings.fresh-air-required-sleep-height", 86);
            AbilityRegister.registerConfigOption(OriginsReborn.getInstance(), OriginsReborn.freshAir, ConfigManager.SettingType.INTEGER, FreshAir.minHeight, Collections.singletonList("Minimum altitude the player can sleep at"), i);
            getConfig().set("extra-settings", null);
            saveConfig();
        }
        if (version.equals("2.5.0")) {
            getConfig().set("config-version", "2.5.5");
            getConfig().set("skinsrestorer-hook.enabled", false);
            getConfig().set("skinsrestorer-hook.ip", "UNSET");
            getConfig().set("skinsrestorer-hook.port", 80);

            setComments("skinsrestorer-hook", Collections.singletonList("Enables abilities that change the player's skin (requires SkinsRestorer to work)"));
            setComments("skinsrestorer-hook.enabled", Collections.singletonList("Whether to enable the SkinsRestorer hook"));
            setComments("skinsrestorer-hook.ip", Collections.singletonList("The IP of your server, set this to your server's public IP (without the port)"));
            setComments("skinsrestorer-hook.port", Collections.singletonList("Set this to any other open port on the server, this is used to host the image of the skin, so it can be used by SkinsRestorer"));

            saveConfig();
        }
        if (version.equals("2.5.5")) {

            // Relocate internal data storage files
            try {

                File internals = new File(OriginsReborn.getInstance().getDataFolder(), "internals");
                boolean ignored2 = internals.mkdirs();

                File inventories = new File(OriginsReborn.getInstance().getDataFolder(), "inventories.yml");
                if (inventories.exists()) {
                    Files.move(inventories.toPath(), Path.of(OriginsReborn.getInstance().getDataFolder().getPath(), "internals/inventories.yml"),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                File selectedOrigins = new File(OriginsReborn.getInstance().getDataFolder(), "selected-origins.yml");
                if (selectedOrigins.exists()) {
                    Files.move(selectedOrigins.toPath(), Path.of(OriginsReborn.getInstance().getDataFolder().getPath(), "internals/selected-origins.yml"),
                            StandardCopyOption.REPLACE_EXISTING);
                }
                File usedOrigins = new File(OriginsReborn.getInstance().getDataFolder(), "used-origins.yml");
                if (usedOrigins.exists()) {
                    Files.move(usedOrigins.toPath(), Path.of(OriginsReborn.getInstance().getDataFolder().getPath(), "internals/used-origins.yml"),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                File cooldownConfig = new File(OriginsReborn.getInstance().getDataFolder(), "cooldown-config.yml");
                File attributeModifierAbilityConfig = new File(OriginsReborn.getInstance().getDataFolder(), "attribute-modifier-ability-config.yml");

                if (cooldownConfig.exists()) {
                    FileConfiguration config = new YamlConfiguration();

                    config.load(cooldownConfig);

                    int i = config.getInt("origins-reborn:swap-command-cooldown", -1);
                    getConfig().set("swap-command.cooldown", i);
                    setComments("swap-command.cooldown", Collections.singletonList("Cooldown for the command (in ticks), -1 means no cooldown"));
                    config.set("origins-reborn:swap-command-cooldown", null);
                    saveConfig();

                    for (String s : config.getKeys(false)) {
                        int amount = config.getInt(s);
                        if (amount == -1) continue;

                        String data = s.split(":")[1];
                        @Subst("key:value") String string = data.replaceFirst("-", ":");
                        Key key = Key.key(string);

                        AbilityRegister.runOnRegisters.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(ability -> ability.registerConfigOption(CooldownAbility.COOLDOWN, Collections.singletonList("The duration of the cooldown (in ticks)"), ConfigManager.SettingType.INTEGER, amount));
                    }

                    boolean ignored = cooldownConfig.delete();
                }

                if (attributeModifierAbilityConfig.exists()) {
                    FileConfiguration config = new YamlConfiguration();

                    config.load(attributeModifierAbilityConfig);

                    for (@Subst("key:value") String s : config.getKeys(false)) {
                        String value = config.getString(s + "." + "value", "x");
                        String operation = config.getString(s + "." + "operation", "default");

                        Key key = Key.key(s);

                        AbilityRegister.runOnRegisters.computeIfAbsent(key, k -> new ArrayList<>())
                                .add(ability -> {
                                    if (!operation.equals("default")) {
                                        ability.registerConfigOption(AttributeModifierAbility.OPERATION, Collections.singletonList("The operation to use ('ADD_SCALAR', 'ADD_NUMBER' or 'MULTIPLY_SCALAR_1')"), ConfigManager.SettingType.STRING, operation);
                                    }
                                    ability.registerConfigOption(AttributeModifierAbility.EXP, List.of("A mathematical expression used to modify the attribute, where 'x' is a variable representing the default modifier.", "Example: 'x * 3' will triple the value used in the modifier."), ConfigManager.SettingType.STRING, value);
                                });
                    }

                    boolean ignored = attributeModifierAbilityConfig.delete();
                }

                File langFolder = new File(OriginsReborn.getInstance().getDataFolder(), "lang");
                if (langFolder.exists()) {
                    boolean ignored = langFolder.delete();
                }
                File characters = new File(OriginsReborn.getInstance().getDataFolder(), "characters.yml");
                if (characters.exists()) {
                    boolean ignored = characters.delete();
                }
                File toggleable = new File(OriginsReborn.getInstance().getDataFolder(), "toggleable-abilities.yml");
                if (toggleable.exists()) {
                    boolean ignored = toggleable.delete();
                }

                Map<String, Object> configBackup = new HashMap<>();
                for (ConfigManager.Option<?> option : ConfigManager.Option.getOptions().values()) {
                    configBackup.put(option.getPath(), option.getValue());
                }

                for (String s : getConfig().getKeys(false)) {
                    getConfig().set(s, null);
                }

                configBackup.remove("config-version");
                for (String s : ConfigManager.Option.getOptions().keySet()) {
                    if (configBackup.containsKey(s)) ConfigManager.Option.getOptions().get(s).setValue(configBackup.get(s));
                    else ConfigManager.Option.getOptions().get(s).resetValue();
                }
                saveConfig();

            } catch (IOException | InvalidConfigurationException ignored) {}
        }
    }

    private static ConfigurationSection getConfig() {
        return OriginsReborn.getInstance().getConfig();
    }

    private static void saveConfig() {
        OriginsReborn.getInstance().saveConfig();
    }

    private static void setComments(String path, List<String> comments) {
        OriginsReborn.getInstance().setComments(path, comments);
    }
}
