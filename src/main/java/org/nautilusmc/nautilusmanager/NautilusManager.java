package org.nautilusmc.nautilusmanager;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.nautilusmc.nautilusmanager.commands.AfkCommand;
import org.nautilusmc.nautilusmanager.commands.HomeBuyCommand;
import org.nautilusmc.nautilusmanager.cosmetics.NameColor;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.cosmetics.TabListManager;
import org.nautilusmc.nautilusmanager.cosmetics.commands.*;
import org.nautilusmc.nautilusmanager.events.*;
import org.nautilusmc.nautilusmanager.sql.SQL;

public final class NautilusManager extends JavaPlugin {

    public static NautilusManager INSTANCE;
    public static LuckPerms LUCKPERMS;

    @Override
    public void onEnable() {
        INSTANCE = this;

        initConfig();
        SQL.init();

        NameColor.init();
        Nickname.init();
        HomeBuyCommand.init();

        registerCommands();
        registerEvents();
        TabListManager.init();
        registerAPIs();
    }

    private void registerCommands() {
        this.getCommand("cosmetics").setExecutor(new CosmeticsCommand());
        this.getCommand("nickname").setExecutor(new NicknameCommand());
        this.getCommand("formatting").setExecutor(new FormattingCommand());
        this.getCommand("msg").setExecutor(new MsgCommand());
        this.getCommand("chat").setExecutor(new ChatCommand());
        this.getCommand("reply").setExecutor(new ReplyCommand());
        this.getCommand("afk").setExecutor(new AfkCommand());
        this.getCommand("buyhome").setExecutor(new HomeBuyCommand());
    }

    private void registerAPIs() {
        RegisteredServiceProvider<LuckPerms> luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (luckPerms != null) {
            LUCKPERMS = luckPerms.getProvider();
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new ChatCommand.ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new MessageStyler(), this);

        Bukkit.getPluginManager().registerEvents(new AfkManager(), this);
        Bukkit.getPluginManager().registerEvents(new TeleportHandler(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralEventManager(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnProtection(), this);
    }

    private void initConfig() {
        FileConfiguration config = this.getConfig();

        config.addDefault("sql.update_interval", -1);
        config.addDefault("sql.protocol", "");
        config.addDefault("sql.host", "");
        config.addDefault("sql.port", 0);
        config.addDefault("sql.database", "");
        config.addDefault("sql.username", "");
        config.addDefault("sql.password", "");

        config.addDefault("afk.timeToAfk", 300);
        config.addDefault("afk.timeToIgnoreSleep", 300);

        config.addDefault("death.itemDespawnSeconds", 300);

        config.addDefault("spawnProtection.loc1", new Location(Bukkit.getWorlds().get(0), 0, 0, 0));
        config.addDefault("spawnProtection.loc2", new Location(Bukkit.getWorlds().get(0), 0, 0, 0));

        config.options().copyDefaults(true);
        this.saveDefaultConfig();
    }

        @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
