package org.nautilusmc.nautilusmanager;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.nautilusmc.nautilusmanager.commands.AfkCommand;
import org.nautilusmc.nautilusmanager.commands.HomeBuyCommand;
import org.nautilusmc.nautilusmanager.commands.ReloadCommand;
import org.nautilusmc.nautilusmanager.cosmetics.NameColor;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.cosmetics.TabListManager;
import org.nautilusmc.nautilusmanager.cosmetics.commands.*;
import org.nautilusmc.nautilusmanager.events.*;
import org.nautilusmc.nautilusmanager.sql.SQL;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.teleport.Warps;
import org.nautilusmc.nautilusmanager.teleport.commands.BackCommand;
import org.nautilusmc.nautilusmanager.teleport.commands.SpawnCommand;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.DelHomeCommand;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.HomeCommand;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.HomesCommand;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.SetHomeCommand;
import org.nautilusmc.nautilusmanager.teleport.commands.tpa.*;
import org.nautilusmc.nautilusmanager.teleport.commands.warp.*;

public final class NautilusManager extends JavaPlugin {

    public static NautilusManager INSTANCE;
    public static LuckPerms LUCKPERMS;

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveDefaultConfig();
        SQL.init();

        NameColor.init();
        Nickname.init();
        Homes.init();
        Warps.init();
//        HomeBuyCommand.init();

        registerCommands();
        registerEvents();
        TabListManager.init();
//        registerAPIs();
    }

    private void registerCommands() {
        this.getCommand("cosmetics").setExecutor(new CosmeticsCommand());
        this.getCommand("nickname").setExecutor(new NicknameCommand());
        this.getCommand("formatting").setExecutor(new FormattingCommand());
        this.getCommand("msg").setExecutor(new MsgCommand());
        this.getCommand("chat").setExecutor(new ChatCommand());
        this.getCommand("reply").setExecutor(new ReplyCommand());
        this.getCommand("afk").setExecutor(new AfkCommand());
        this.getCommand("home").setExecutor(new HomeCommand());
        this.getCommand("sethome").setExecutor(new SetHomeCommand());
        this.getCommand("delhome").setExecutor(new DelHomeCommand());
        this.getCommand("homes").setExecutor(new HomesCommand());
        this.getCommand("spawn").setExecutor(new SpawnCommand());
        this.getCommand("back").setExecutor(new BackCommand());
        this.getCommand("buyhome").setExecutor(new HomeBuyCommand());
        this.getCommand("tpa").setExecutor(new TpaCommand());
        this.getCommand("tphere").setExecutor(new TpHereCommand());
        this.getCommand("tpaccept").setExecutor(new TpAcceptCommand());
        this.getCommand("tpdeny").setExecutor(new TpDenyCommand());
        this.getCommand("tpcancel").setExecutor(new TpCancel());
        this.getCommand("reloadnautilus").setExecutor(new ReloadCommand());
        this.getCommand("warp").setExecutor(new WarpCommand());
        this.getCommand("createwarp").setExecutor(new CreateWarpCommand());
        this.getCommand("delwarp").setExecutor(new DeleteWarpCommand());
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

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
