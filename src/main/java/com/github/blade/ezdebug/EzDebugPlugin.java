package com.github.blade.ezdebug;

import com.github.blade.ezdebug.model.DebugSettings;
import com.github.blade.hvac.api.EzvacApi;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

/**
 * EZdebug+: a Debug Potion that lets admins see EZvac at work, and the same
 * vision while holding EZvac's link tool. Built only on EZvac's public API
 * (1.2 or newer; the link tool needs 1.3).
 */
public final class EzDebugPlugin extends JavaPlugin {
    private EzvacApi ezvac;
    private DebugSettings settings;
    private DebugVision vision;
    private DebugPotion potion;

    @Override
    public void onEnable() {
        try {
            ezvac = EzvacApi.get();
        } catch (IllegalStateException missing) {
            getLogger().severe(missing.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!supported(ezvac.apiVersion())) {
            getLogger().severe("EZdebug+ needs EZvac API 1.2 or newer, but EZvac " + ezvac.pluginVersion()
                    + " offers API " + ezvac.apiVersion() + ". Update EZvac.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        saveDefaultConfig();
        loadSettings();
        vision = new DebugVision(this);
        potion = new DebugPotion(this);
        getServer().getPluginManager().registerEvents(potion, this);
        Commands commands = new Commands(this);
        PluginCommand command = Objects.requireNonNull(getCommand("ezdb"), "ezdb is missing from plugin.yml");
        command.setExecutor(commands);
        command.setTabCompleter(commands);
        for (Player player : getServer().getOnlinePlayers()) potion.resume(player);
        vision.start();
        getLogger().info("EZdebug+ enabled on EZvac " + ezvac.pluginVersion() + " (API " + ezvac.apiVersion() + ")"
                + (toolVision() ? "; holding the link tool shows debug vision." : "."));
    }

    @Override
    public void onDisable() {
        if (vision != null) vision.shutdown();
    }

    /** API 1.2 or newer, within major version 1. */
    static boolean supported(String apiVersion) {
        String[] parts = apiVersion.split("\\.");
        try {
            return parts.length >= 2 && Integer.parseInt(parts[0]) == 1 && Integer.parseInt(parts[1]) >= 2;
        } catch (NumberFormatException unknown) {
            return false;
        }
    }

    /** API 1.3 or newer, within major version 1: it has the link tool (EZvac 3.0). */
    static boolean hasLinkTool(String apiVersion) {
        String[] parts = apiVersion.split("\\.");
        try {
            return parts.length >= 2 && Integer.parseInt(parts[0]) == 1 && Integer.parseInt(parts[1]) >= 3;
        } catch (NumberFormatException unknown) {
            return false;
        }
    }

    /** Whether holding the link tool shows debug vision: switched on, and an EZvac that has one. */
    boolean toolVision() {
        return settings.linkTool() && hasLinkTool(ezvac.apiVersion());
    }

    void loadSettings() {
        reloadConfig();
        settings = DebugSettings.load(getConfig(), getLogger()::warning);
    }

    EzvacApi ezvac() { return ezvac; }
    DebugSettings settings() { return settings; }
    DebugVision vision() { return vision; }
    DebugPotion potion() { return potion; }
}
