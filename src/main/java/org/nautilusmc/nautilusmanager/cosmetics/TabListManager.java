package org.nautilusmc.nautilusmanager.cosmetics;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.nautilusmc.nautilusmanager.NautilusManager;
import org.nautilusmc.nautilusmanager.events.AfkManager;
import org.nautilusmc.nautilusmanager.util.Emoji;

public class TabListManager {
    public static void init() {
        Bukkit.getScheduler().runTaskTimer(NautilusManager.INSTANCE, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Team team = p.getScoreboard().getEntityTeam(p);

                Component prefix = team == null ? Component.empty() : team.prefix().append(Component.space());
                Component afk = Component.empty();
                Component name = p.displayName();
                Component realName = Component.empty();
                Component health = Component.empty();

                if (AfkManager.isAfk(p)) {
                    afk = Component.text("AFK ").color(NamedTextColor.GRAY);
                }

                if (Nickname.getNickname(p) != null) {
                    realName = Component.text(" (" + p.getName() + ")").color(NamedTextColor.GRAY);;
                }

                if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                    if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        health = Component.text("20").color(TextColor.color(180, 180, 180))
                                .append(Component.text(Emoji.HEART.getRaw()).color(TextColor.color(210, 210, 210)));
                    } else {
                        TextColor healthValueColor = NamedTextColor.RED;
                        TextColor heartColor = NamedTextColor.DARK_RED;

                        if (p.hasPotionEffect(PotionEffectType.POISON)) {
                            healthValueColor = TextColor.color(187, 183, 66);
                            heartColor = TextColor.color(139, 135, 18);
                        } else if (p.hasPotionEffect(PotionEffectType.WITHER)) {
                            healthValueColor = NamedTextColor.DARK_GRAY;
                            heartColor = NamedTextColor.BLACK;
                        } else if (p.isFrozen()) {
                            healthValueColor = NamedTextColor.AQUA;
                            heartColor = NamedTextColor.DARK_AQUA;
                        }

                        health = Component.text(" " + Math.round(p.getHealth())).color(healthValueColor)
                                .append(Component.text(Emoji.HEART.getRaw()).color(heartColor));
                    }
                }

                p.playerListName(Component.empty().append(prefix).append(afk).append(name).append(realName).append(health));
            }
        }, 0, 20);
    }
}
