package com.starshootercity.commands;

import com.starshootercity.*;
import com.starshootercity.abilities.AbilityRegister;
import com.starshootercity.cooldowns.Cooldowns;
import com.starshootercity.events.PlayerSwapOriginEvent;
import com.starshootercity.util.CompressionUtils;
import com.starshootercity.util.ShortcutUtils;
import com.starshootercity.util.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class OriginCommand implements CommandExecutor, TabCompleter {

    public OriginCommand() {
        Translator.registerTranslation("command.no_swap_permission", "§cYou don't have permission to do this!");
    }

    public static NamespacedKey key = OriginsReborn.getCooldowns().registerCooldown(OriginsReborn.getInstance(), new NamespacedKey(OriginsReborn.getInstance(), "swap-command-cooldown"), new Cooldowns.CooldownInfo(0));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Invalid command. Usage: /origin <command>").color(NamedTextColor.RED));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "swap" -> {
                if (sender instanceof Player player) {
                    if (OriginsReborn.getCooldowns().hasCooldown(player, key)) {
                        player.sendMessage(Component.text("You are on cooldown.").color(NamedTextColor.RED));
                        return true;
                    }
                    if (!ConfigManager.getConfigValue(ConfigManager.Option.SWAP_COMMAND_ENABLED)) {
                        sender.sendMessage(Component.text("This command has been disabled in the configuration").color(NamedTextColor.RED));
                        return true;
                    }
                    if (AddonLoader.allowOriginSwapCommand(player)) {
                        String layer;
                        if (args.length == 2) layer = args[1];
                        else layer = "origin";
                        OriginSwapper.openOriginSwapper(player, PlayerSwapOriginEvent.SwapReason.COMMAND, 0, 0, OriginsReborn.getInstance().isVaultEnabled(), layer);
                    } else {
                        sender.sendMessage(Component.text(Translator.translate("command.no_swap_permission")));
                    }
                } else {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                }
                return true;
            }
            case "reload" -> {
                if (sender instanceof Player player) {
                    if (!player.hasPermission("originsreborn.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                        return true;
                    }
                }
                AddonLoader.reloadAddons();
                OriginsReborn.getInstance().reloadConfig();
                AbilityRegister.reloadAbilityConfig();
                Translator.reloadTranslations();
                return true;
            }
            case "exchange" -> {
                if (sender instanceof Player player) {
                    if (sender.hasPermission("originsreborn.exchange")) {
                        if (args.length < 2) {
                            sender.sendMessage(Component.text("Usage: /origin exchange <player> [<layer>]").color(NamedTextColor.RED));
                            return true;
                        }
                        Player target = Bukkit.getPlayer(args[1]);
                        if (target == null) {
                            sender.sendMessage(Component.text("Usage: /origin exchange <player> [<layer>]").color(NamedTextColor.RED));
                            return true;
                        }
                        if (target.equals(player)) {
                            sender.sendMessage(Component.text("You must specify another player.").color(NamedTextColor.RED));
                            return true;
                        }
                        for (ExchangeRequest request : exchangeRequests.getOrDefault(player, List.of())) {
                            if (request.expireTime > ShortcutUtils.getCurrentTick()) continue;
                            String l = request.layer.substring(0, 0).toUpperCase() + request.layer.substring(1);
                            String layer = request.layer;
                            if (request.p2.equals(player) && request.p1.equals(target)) {
                                target.sendMessage(Component.text("%s swapped with %s.".formatted(l, player.getName())).color(NamedTextColor.AQUA));
                                player.sendMessage(Component.text("%s swapped with %s.".formatted(l, target.getName())).color(NamedTextColor.AQUA));

                                Origin pOrigin = OriginSwapper.getOrigin(player, layer);
                                Origin tOrigin = OriginSwapper.getOrigin(target, layer);

                                OriginSwapper.setOrigin(player, tOrigin, PlayerSwapOriginEvent.SwapReason.COMMAND, false, layer);
                                OriginSwapper.setOrigin(target, pOrigin, PlayerSwapOriginEvent.SwapReason.COMMAND, false, layer);
                                return true;
                            }
                        }
                        if (!exchangeRequests.containsKey(target)) {
                            exchangeRequests.put(target, new ArrayList<>());
                        }
                        exchangeRequests.get(target).removeIf(request -> request.p1.equals(player) && request.p2.equals(player));
                        String layer;
                        if (args.length != 3) layer = "origin";
                        else layer = args[2];

                        exchangeRequests.get(target).add(new ExchangeRequest(player, target, ShortcutUtils.getCurrentTick() + 6000, layer));
                        target.sendMessage(Component.text("%s is requesting to swap %s with you, type /origin exchange %s to accept. The request will expire in 5 minutes.".formatted(layer, player.getName(), player.getName())).color(NamedTextColor.AQUA));
                        player.sendMessage(Component.text("Requesting to swap %s with %s. The request will expire in 5 minutes.".formatted(layer, target.getName())).color(NamedTextColor.AQUA));
                    } else {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("Only players can switch origins with others!").color(NamedTextColor.RED));
                }
                return true;
            }
            case "set" -> {
                if (sender instanceof Player player) {
                    if (!player.hasPermission("originsreborn.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                        return true;
                    }
                }
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin set <player> <layer> <origin>").color(NamedTextColor.RED));
                    return true;
                }
                String layer = args[2];
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin set <player> <layer> <origin>").color(NamedTextColor.RED));
                    return true;
                }
                Origin origin = AddonLoader.getOrigin(args[3].replace("_", " "));
                if (origin == null || !origin.getLayer().equals(layer)) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin set <player> <layer> <origin>").color(NamedTextColor.RED));
                    return true;
                }
                OriginSwapper.setOrigin(player, origin, PlayerSwapOriginEvent.SwapReason.COMMAND, false, layer);
                return true;
            }
            case "clear" -> {
                if (sender instanceof Player player) {
                    if (!player.hasPermission("originsreborn.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                        return true;
                    }
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin clear <player> <layer>").color(NamedTextColor.RED));
                    return true;
                }
                String layer = args[2];
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin clear <player> <layer>").color(NamedTextColor.RED));
                    return true;
                }
                OriginSwapper.setOrigin(player, null, PlayerSwapOriginEvent.SwapReason.COMMAND, false, layer);
                return true;
            }
            case "orb" -> {
                Player player;
                if (sender instanceof Player p) {
                    player = p;
                    if (!player.hasPermission("originsreborn.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to do this!").color(NamedTextColor.RED));
                        return true;
                    }
                } else if (!(args.length == 2 && (player = Bukkit.getPlayer(args[1])) != null)) {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                    return true;
                }
                player.getInventory().addItem(OrbOfOrigin.orb);
                return true;
            }
            case "check" -> {
                if (sender instanceof Player player) {
                    String layer;
                    if (args.length == 2) layer = args[1];
                    else layer = "origin";
                    OriginSwapper.openOriginSwapper(player, PlayerSwapOriginEvent.SwapReason.COMMAND, AddonLoader.getOrigins(layer).indexOf(OriginSwapper.getOrigin(player, layer)), 0, false, true, layer);
                } else {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                }
                return true;
            }
            case "pack" -> {
                if (sender instanceof Player player) {
                    PackApplier.sendPacks(player);
                } else {
                    sender.sendMessage(Component.text("This command can only be run by a player").color(NamedTextColor.RED));
                }
                return true;
            }
            case "export" -> {
                if (args.length != 3) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin export <addon id> <path>").color(NamedTextColor.RED));
                    return true;
                }
                File output = new File(OriginsReborn.getInstance().getDataFolder(), "export/" + args[2] + ".orbarch");
                List<File> files = AddonLoader.originFiles.get(args[1]);
                if (files == null) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin export <addon id> <path>").color(NamedTextColor.RED));
                    return true;
                }
                try {
                    CompressionUtils.compressFiles(files, output);
                    sender.sendMessage(Component.text("Exported origins to '~/plugins/Origins-Reborn/export/%s.orbarch'".formatted(args[2])).color(NamedTextColor.AQUA));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            case "import" -> {
                if (args.length != 2) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin import <path>").color(NamedTextColor.RED));
                    return true;
                }
                File input = new File(OriginsReborn.getInstance().getDataFolder(), "import/" + args[1]);
                File output = new File(OriginsReborn.getInstance().getDataFolder(), "origins");
                if (!input.exists() || !output.exists()) {
                    sender.sendMessage(Component.text("Invalid command. Usage: /origin import <path>").color(NamedTextColor.RED));
                    return true;
                }
                try {
                    CompressionUtils.decompressFiles(input, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            default -> {
                sender.sendMessage(Component.text("Invalid command. Usage: /origin <command>").color(NamedTextColor.RED));
                return true;
            }
        }
    }

    private final Map<Player, List<ExchangeRequest>> exchangeRequests = new HashMap<>();

    public record ExchangeRequest(Player p1, Player p2, int expireTime, String layer) {

    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        List<String> result = new ArrayList<>();
        List<String> data = switch (args.length) {
            case 1 -> {
                List<String> r = new ArrayList<>();
                r.add("check");
                if (sender instanceof Player player && AddonLoader.allowOriginSwapCommand(player)) {
                    r.add("swap");
                }
                if (sender.hasPermission("originsreborn.exchange")) {
                    r.add("exchange");
                }
                if (!sender.hasPermission("originsreborn.admin")) yield r;
                r.add("reload");
                r.add("set");
                r.add("orb");
                r.add("export");
                r.add("import");
                r.add("pack");
                r.add("clear");
                yield r;
            }
            case 2 -> {
                switch (args[0]) {
                    case "set", "orb", "exchange", "clear" -> {
                        yield new ArrayList<>() {{
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                add(player.getName());
                            }
                        }};
                    }
                    case "export" -> {
                        yield new ArrayList<>(AddonLoader.originFiles.keySet());
                    }
                    case "check", "swap" -> {
                        yield new ArrayList<>(AddonLoader.layers);
                    }
                    case "import" -> {
                        File input = new File(OriginsReborn.getInstance().getDataFolder(), "import");
                        File[] files = input.listFiles();

                        if (files == null) yield List.of();
                        List<String> fileNames = new ArrayList<>();
                        for (File file : files) {
                            fileNames.add(file.getName());
                        }
                        yield fileNames;
                    }
                    default -> {
                        yield List.of();
                    }
                }
            }
            case 3 -> {
                if (Set.of("set", "clear").contains(args[0])) {
                    yield AddonLoader.layers;
                } else yield List.of();
            }
            case 4 -> {
                if (args[0].equals("set")) {
                    String layer = args[2];
                    yield new ArrayList<>() {{
                        for (Origin origin : AddonLoader.getOrigins(layer)) {
                            add(origin.getName().toLowerCase().replace(" ", "_"));
                        }
                    }};
                } else yield List.of();
            }
            default -> List.of();
        };
        StringUtil.copyPartialMatches(args[args.length - 1], data, result);
        return result;
    }
}