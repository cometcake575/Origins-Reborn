package com.starshootercity;

import com.starshootercity.abilities.*;
import com.starshootercity.commands.FlightToggleCommand;
import com.starshootercity.commands.OriginCommand;
import com.starshootercity.cooldowns.Cooldowns;
import com.starshootercity.events.PlayerLeftClickEvent;
import com.starshootercity.packetsenders.*;
import com.starshootercity.skript.SkriptInitializer;
import com.starshootercity.util.Metrics;
import com.starshootercity.util.config.ConfigManager;
import com.starshootercity.util.SkinManager;
import com.starshootercity.util.WorldGuardHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OriginsReborn extends OriginsAddon {

    private static OriginsReborn instance;

    // Used for the legacy updater
    public static FreshAir freshAir;
    public static boolean skinManagerEnabled = false;

    public static OriginsReborn getInstance() {
        return instance;
    }

    private Economy economy;

    public static boolean isSkinManagerEnabled() {
        return skinManagerEnabled;
    }

    public Economy getEconomy() {
        return economy;
    }

    private static NMSInvoker nmsInvoker;

    private static Cooldowns cooldowns;

    public static Cooldowns getCooldowns() {
        return cooldowns;
    }

    public static NMSInvoker getNMSInvoker() {
        return nmsInvoker;
    }

    private boolean setupEconomy() {
        try {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
            return (economy != null);
        } catch (NoClassDefFoundError e) {
            return false;
        }
    }

    private static void initializeNMSInvoker(OriginsReborn instance) {
        String version = Bukkit.getBukkitVersion().split("-")[0];
        nmsInvoker = switch (version) {
            case "1.18.1" -> new NMSInvokerV1_18_1();
            case "1.18.2" -> new NMSInvokerV1_18_2();
            case "1.19" -> new NMSInvokerV1_19();
            case "1.19.1" -> new NMSInvokerV1_19_1();
            case "1.19.2" -> new NMSInvokerV1_19_2();
            case "1.19.3" -> new NMSInvokerV1_19_3();
            case "1.19.4" -> new NMSInvokerV1_19_4();
            case "1.20" -> new NMSInvokerV1_20();
            case "1.20.1" -> new NMSInvokerV1_20_1();
            case "1.20.2" -> new NMSInvokerV1_20_2();
            case "1.20.3" -> new NMSInvokerV1_20_3();
            case "1.20.4" -> new NMSInvokerV1_20_4();
            case "1.20.5", "1.20.6" -> new NMSInvokerV1_20_6();
            case "1.21" -> new NMSInvokerV1_21();
            case "1.21.1" -> new NMSInvokerV1_21_1();
            case "1.21.2", "1.21.3" -> new NMSInvokerV1_21_3();
            case "1.21.4" -> new NMSInvokerV1_21_4();
            default -> throw new IllegalStateException("Unsupported version: " + version);
        };
        Bukkit.getPluginManager().registerEvents(nmsInvoker, instance);
        nmsInvoker.initialize();
    }

    private boolean vaultEnabled;

    public boolean isVaultEnabled() {
        return vaultEnabled;
    }

    public void setComments(String path, List<String> comments) {
        getNMSInvoker().setComments(getConfig(), path, comments);
    }

    private static boolean worldGuardHookInitialized;

    public static boolean isWorldGuardHookInitialized() {
        return worldGuardHookInitialized;
    }

    @Override
    public void onLoad() {
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                worldGuardHookInitialized = WorldGuardHook.tryInitialize();
            }
        }
        catch (Throwable t) {
            worldGuardHookInitialized = false;
        }
    }

    @Override
    public void onDisable() {
        if (!skinManagerEnabled) return;
        SkinManager.unload();
    }

    @Override
    public void onRegister() {
        instance = this;

        int pluginId = 25114;
        new Metrics(this, pluginId);

        // Used for legacy updater
        freshAir = new FreshAir();

        initializeNMSInvoker(this);
        saveDefaultConfig();
        ConfigManager.Option.initialize();

        if (ConfigManager.getConfigValue(ConfigManager.Option.SKINSRESTORER_HOOK_ENABLED)) {
            if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer")) {
                skinManagerEnabled = true;
                Bukkit.getPluginManager().registerEvents(new SkinManager(), this);
            } else {
                getLogger().warning("The SkinsRestorer hook is enabled in the config, however SkinsRestorer is not installed.");
            }
        }

        if (worldGuardHookInitialized) WorldGuardHook.completeInitialize();

        Translator.initialize(this);

        AbilityRegister.setupAbilityConfig();

        if (getConfig().getBoolean("swap-command.vault.enabled")) {
            vaultEnabled = setupEconomy();
            if (!vaultEnabled) {
                getLogger().warning("Vault is missing, origin swaps will not cost currency");
            }
        } else vaultEnabled = false;
        cooldowns = new Cooldowns();
        if (!getConfig().getBoolean("cooldowns.disable-all-cooldowns") && getConfig().getBoolean("cooldowns.show-cooldown-icons")) {
            Bukkit.getPluginManager().registerEvents(cooldowns, this);
        }
        SkriptInitializer.initialize(this);
        Bukkit.getPluginManager().registerEvents(new OriginSwapper(), this);
        Bukkit.getPluginManager().registerEvents(new OrbOfOrigin(), this);
        Bukkit.getPluginManager().registerEvents(new PackApplier(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLeftClickEvent.PlayerLeftClickEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new ParticleAbility.ParticleAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new BreakSpeedModifierAbility.BreakSpeedModifierAbilityListener(), this);

        PluginCommand flightCommand = getCommand("fly");
        if (flightCommand != null) flightCommand.setExecutor(new FlightToggleCommand());

        File export = new File(getDataFolder(), "export");
        if (!export.exists()) {
            boolean ignored = export.mkdir();
        }
        File imports = new File(getDataFolder(), "import");
        if (!imports.exists()) {
            boolean ignored = imports.mkdir();
        }

        PluginCommand command = getCommand("origin");
        if (command != null) command.setExecutor(new OriginCommand());
    }

    @Override
    public @NotNull String getNamespace() {
        return "origins";
    }

    @Override
    public @NotNull List<Ability> getAbilities() {
        List<Ability> abilities = new ArrayList<>(List.of(
                new PumpkinHate(),
                new FallImmunity(),
                new WeakArms(),
                new Fragile(),
                new SlowFalling(),
                freshAir,
                new Vegetarian(),
                new LayEggs(),
                new NoShield(),
                new MasterOfWebs(),
                new Tailwind(),
                new Arthropod(),
                new Climbing(),
                new Carnivore(),
                new WaterBreathing(),
                new WaterVision(),
                new CatVision(),
                new NineLives(),
                new BurnInDaylight(),
                new WaterVulnerability(),
                new Phantomize(),
                new Invisibility(),
                new ThrowEnderPearl(),
                new PhantomizeOverlay(),
                new FireImmunity(),
                new AirFromPotions(),
                new SwimSpeed(),
                new LikeWater(),
                new LightArmor(),
                new MoreKineticDamage(),
                new DamageFromPotions(),
                new DamageFromSnowballs(),
                new Hotblooded(),
                new BurningWrath(),
                new SprintJump(),
                new AerialCombatant(),
                new Elytra(),
                new LaunchIntoAir(),
                new HungerOverTime(),
                new MoreExhaustion(),
                new Aquatic(),
                new NetherSpawn(),
                new Claustrophobia(),
                new VelvetPaws(),
                new AquaAffinity(),
                new FlameParticles(),
                new EnderParticles(),
                new Phasing(),
                new ScareCreepers(),
                new StrongArms(),
                StrongArms.StrongArmsBreakSpeed.strongArmsBreakSpeed,
                StrongArms.StrongArmsDrops.strongArmsDrops,
                new ShulkerInventory(),
                new NaturalArmor(),
                new BurnInDaylightWithoutHelmet(),
                new ConduitPowerOnLand(),
                new Grayscale()
        ));
        if (nmsInvoker.getBlockInteractionRangeAttribute() != null && nmsInvoker.getEntityInteractionRangeAttribute() != null) {
            abilities.add(new ExtraReach(nmsInvoker.getBlockInteractionRangeAttribute(), nmsInvoker.getEntityInteractionRangeAttribute()));
            abilities.add(ExtraReach.ExtraReachBlocks.extraReachBlocks);
            abilities.add(ExtraReach.ExtraReachEntities.extraReachEntities);
        }
        return abilities;
    }
}