package org.nautilusmc.nautilusmanager.cosmetics;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.events.AfkManager;

public class TabListManager {

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(NautilusManager.INSTANCE, () -> {
            for(Player p : Bukkit.getOnlinePlayers()) {
                Component afk = Component.empty();
                Component name = p.displayName();
                Component realName = Component.empty();
                Component health = Component.empty();

                if (AfkManager.isAfk(p)) {
                    afk = Component.text("AFK ").color(NamedTextColor.GRAY);
                }

                if (Nickname.getNickname(p) != null) {
                    realName = Component.text(" ("+p.getName()+")").color(NamedTextColor.GRAY);;
                }

                if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                    if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        health = Component.text("20").color(TextColor.color(180, 180, 180))
                                .append(Component.text("♥").color(TextColor.color(210, 210, 210)));
                    } else {
                        TextColor color1 = NamedTextColor.RED;
                        TextColor color2 = NamedTextColor.DARK_RED;

                        if (p.hasPotionEffect(PotionEffectType.POISON)) {
                            color1 = TextColor.color(187, 183, 66);
                            color2 = TextColor.color(139, 135, 18);
                        } else if (p.hasPotionEffect(PotionEffectType.WITHER)) {
                            color1 = NamedTextColor.DARK_GRAY;
                            color2 = NamedTextColor.BLACK;
                        }  else if (p.isFrozen()) {
                            color1 = NamedTextColor.AQUA;
                            color2 = NamedTextColor.DARK_AQUA;
                        }

                        health = Component.text(" "+ Math.round(p.getHealth())).color(color1)
                                .append(Component.text("♥").color(color2));
                    }
                }

                p.playerListName(Component.empty().append(afk).append(name).append(realName).append(health));
            }
        }, 0, 20);
    }
}
