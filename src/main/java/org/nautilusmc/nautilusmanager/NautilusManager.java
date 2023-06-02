package org.nautilusmc.nautilusmanager;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nautilusmc.nautilusmanager.commands.*;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;
import org.nautilusmc.nautilusmanager.crews.commands.CrewCommand;
import org.nautilusmc.nautilusmanager.crews.commands.CrewsCommand;
import org.nautilusmc.nautilusmanager.crews.commands.InviteCommand;
import org.nautilusmc.nautilusmanager.crews.commands.WarCommand;
import org.nautilusmc.nautilusmanager.discord.DiscordBot;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.BuyHomeCommand;
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
import org.nautilusmc.nautilusmanager.util.PermsUtil;

import java.util.List;

public final class NautilusManager extends JavaPlugin {

    public static NautilusManager INSTANCE;

    public static final TextColor DEFAULT_CHAT_TEXT_COLOR = TextColor.color(200, 200, 200);

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveDefaultConfig();
        SQL.init();

        NameColor.init();
        Nickname.init();
        Homes.init();
        Warps.init();
        TpaManager.init();
        MuteManager.init();
        PermsUtil.init();
        CrewHandler.init();

        registerCommands();
        registerEvents();
        TabListManager.init();

        DiscordBot.init();
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
        this.getCommand("buyhome").setExecutor(new BuyHomeCommand());
        this.getCommand("tpa").setExecutor(new TpaCommand());
        this.getCommand("tpahere").setExecutor(new TpaHereCommand());
        this.getCommand("tptrust").setExecutor(new TpTrustCommand());
        this.getCommand("tptrustlist").setExecutor(new TpTrustListCommand());
        this.getCommand("tpaccept").setExecutor(new TpAcceptCommand());
        this.getCommand("tpdeny").setExecutor(new TpDenyCommand());
        this.getCommand("tpcancel").setExecutor(new TpCancel());
        this.getCommand("reloadnautilus").setExecutor(new ReloadCommand());
        this.getCommand("warp").setExecutor(new WarpCommand());
        this.getCommand("createwarp").setExecutor(new CreateWarpCommand());
        this.getCommand("delwarp").setExecutor(new DeleteWarpCommand());
        this.getCommand("vanish").setExecutor(new VanishCommand());
        this.getCommand("chatmsg").setExecutor(new ChatMsgCommand());
        this.getCommand("suicide").setExecutor(new SuicideCommand());
        this.getCommand("crews").setExecutor(new CrewsCommand());
        this.getCommand("crew").setExecutor(new CrewCommand());
        this.getCommand("confirm").setExecutor(new ConfirmCommand());
        this.getCommand("deny").setExecutor(new DenyCommand());
        this.getCommand("invite").setExecutor(new InviteCommand());
        this.getCommand("war").setExecutor(new WarCommand());
        this.getCommand("sponsor").setExecutor(new SponsorCommand());
        this.getCommand("itemname").setExecutor(new ItemNameCommand());
        this.getCommand("toggleinvisible").setExecutor(new ToggleInvisibleCommand());
        this.getCommand("mute").setExecutor(new MuteCommand());
        this.getCommand("mutelist").setExecutor(new MuteListCommand());

        this.getCommand("ban").setExecutor(new BanCommand());
        Bukkit.getCommandMap().getKnownCommands().put("ban", this.getCommand("ban"));
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new ChatCommand.ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new MessageStyler(), this);

        Bukkit.getPluginManager().registerEvents(new AfkManager(), this);
        Bukkit.getPluginManager().registerEvents(new VanishManager(), this);
        Bukkit.getPluginManager().registerEvents(new TeleportHandler(), this);
        Bukkit.getPluginManager().registerEvents(new GeneralEventManager(), this);
        Bukkit.getPluginManager().registerEvents(new SpawnProtection(), this);


        Bukkit.getPluginManager().registerEvents(new CrewHandler(), this);
        Bukkit.getPluginManager().registerEvents(new DiscordBot(), this);
    }

    // I do this separately from onDisable so that I can call it before thread death in the event of a reload
    public static void unload() {
        VanishManager.unload();
        DiscordBot.unload();
    }
}
