package org.nautilusmc.nautilusmanager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.nautilusmc.nautilusmanager.commands.*;
import org.nautilusmc.nautilusmanager.cosmetics.MuteManager;
import org.nautilusmc.nautilusmanager.crews.CrewHandler;
import org.nautilusmc.nautilusmanager.crews.commands.*;
import org.nautilusmc.nautilusmanager.discord.DiscordBot;
import org.nautilusmc.nautilusmanager.teleport.TpaManager;
import org.nautilusmc.nautilusmanager.cosmetics.NameColor;
import org.nautilusmc.nautilusmanager.cosmetics.Nickname;
import org.nautilusmc.nautilusmanager.cosmetics.TabListManager;
import org.nautilusmc.nautilusmanager.cosmetics.commands.*;
import org.nautilusmc.nautilusmanager.events.*;
import org.nautilusmc.nautilusmanager.sql.SQL;
import org.nautilusmc.nautilusmanager.teleport.Homes;
import org.nautilusmc.nautilusmanager.teleport.Warps;
import org.nautilusmc.nautilusmanager.teleport.commands.*;
import org.nautilusmc.nautilusmanager.teleport.commands.homes.*;
import org.nautilusmc.nautilusmanager.teleport.commands.tpa.*;
import org.nautilusmc.nautilusmanager.teleport.commands.warp.*;
import org.nautilusmc.nautilusmanager.util.Permission;

public final class NautilusManager extends JavaPlugin {
    public static final TextColor WORDMARK_DARK_COLOR = NamedTextColor.DARK_AQUA;
    public static final TextColor WORDMARK_LIGHT_COLOR = NamedTextColor.AQUA;

    public static final Component WORDMARK = Component.empty()
            .append(Component.text("Nautilus", WORDMARK_DARK_COLOR).decoration(TextDecoration.BOLD, false))
            .append(Component.text("MC", WORDMARK_LIGHT_COLOR).decoration(TextDecoration.BOLD, true));

    private static NautilusManager plugin;

    public static NautilusManager getPlugin() {
        return plugin;
    }

    public static String getVersion() {
        return "Survival " + Bukkit.getMinecraftVersion();
    }

    @Override
    public void onEnable() {
        plugin = this;

        saveDefaultConfig();
        SQL.init();

        NameColor.init();
        Nickname.init();
        Homes.init();
//        Portals.init();
        Warps.init();
        TpaManager.init();
        MuteManager.init();
        Permission.init();
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
        this.getCommand("emoji").setExecutor(new EmojiCommand());
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
        this.getCommand("tpcancel").setExecutor(new TpCancelCommand());
        this.getCommand("reloadnautilus").setExecutor(new ReloadCommand());
        this.getCommand("warp").setExecutor(new WarpCommand());
        this.getCommand("createwarp").setExecutor(new CreateWarpCommand());
        this.getCommand("delwarp").setExecutor(new DelWarpCommand());
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
        this.getCommand("timetop").setExecutor(new TimeTopCommand());
        this.getCommand("traveltop").setExecutor(new TravelTopCommand());
        this.getCommand("language").setExecutor(new LanguageCommand());

        this.getCommand("ban").setExecutor(new BanCommand());
        Bukkit.getCommandMap().getKnownCommands().put("ban", this.getCommand("ban"));
        this.getCommand("pardon").setExecutor(new PardonCommand());
        Bukkit.getCommandMap().getKnownCommands().put("pardon", this.getCommand("pardon"));
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new ChatCommand.ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new MessageStyler(), this);

        Bukkit.getPluginManager().registerEvents(new AFKManager(), this);
        Bukkit.getPluginManager().registerEvents(new VanishManager(), this);
//        Bukkit.getPluginManager().registerEvents(new PortalHandler(), this);
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
