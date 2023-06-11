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
    public static final TextColor NORMAL_HEALTH_COLOR = NamedTextColor.RED;
    public static final TextColor NORMAL_HEART_COLOR = NamedTextColor.DARK_RED;
    public static final TextColor GOLDEN_HEALTH_COLOR = NamedTextColor.YELLOW;
    public static final TextColor GOLDEN_HEART_COLOR = NamedTextColor.GOLD;
    public static final TextColor POISON_HEALTH_COLOR = TextColor.color(187, 183, 66);
    public static final TextColor POISON_HEART_COLOR = TextColor.color(139, 135, 18);
    public static final TextColor WITHER_HEALTH_COLOR = NamedTextColor.DARK_GRAY;
    public static final TextColor WITHER_HEART_COLOR = NamedTextColor.BLACK;
    public static final TextColor FROZEN_HEALTH_COLOR = NamedTextColor.AQUA;
    public static final TextColor FROZEN_HEART_COLOR = NamedTextColor.DARK_AQUA;
    public static final TextColor INVIS_HEALTH_COLOR = NORMAL_HEALTH_COLOR; // TextColor.color(180, 180, 180);
    public static final TextColor INVIS_HEART_COLOR = NORMAL_HEART_COLOR; // TextColor.color(210, 210, 210);

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(NautilusManager.INSTANCE, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                Component tabEntry = Component.empty();

                Team team = p.getScoreboard().getEntityTeam(p);
                if (team != null) {
                    tabEntry = tabEntry.append(team.prefix()).appendSpace();
                }

                if (AfkManager.isAfk(p)) {
                    tabEntry = tabEntry.append(Component.text("AFK").color(NamedTextColor.GRAY)).appendSpace();
                }

                tabEntry = tabEntry.append(p.displayName());

                if (Nickname.getNickname(p) != null) {
                    tabEntry = tabEntry.appendSpace().append(Component.text("(" + p.getName() + ")").color(NamedTextColor.GRAY));
                }

                if (p.getGameMode() != GameMode.CREATIVE && p.getGameMode() != GameMode.SPECTATOR) {
                    tabEntry = tabEntry.appendSpace();

                    if (p.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        // don't show the real health if they are invisible to preserve anonymity
                        tabEntry = tabEntry.append(Component.text("20").color(INVIS_HEALTH_COLOR)
                                .append(Component.text(Emoji.HEART.toString()).color(INVIS_HEART_COLOR)));
                    } else {
                        TextColor healthColor = NORMAL_HEALTH_COLOR;
                        TextColor heartColor = NORMAL_HEART_COLOR;

                        if (p.hasPotionEffect(PotionEffectType.ABSORPTION)) {
                            healthColor = GOLDEN_HEALTH_COLOR;
                            heartColor = GOLDEN_HEART_COLOR;
                        } else if (p.hasPotionEffect(PotionEffectType.POISON)) {
                            healthColor = POISON_HEALTH_COLOR;
                            heartColor = POISON_HEART_COLOR;
                        } else if (p.hasPotionEffect(PotionEffectType.WITHER)) {
                            healthColor = WITHER_HEALTH_COLOR;
                            heartColor = WITHER_HEART_COLOR;
                        } else if (p.isFrozen()) {
                            healthColor = FROZEN_HEALTH_COLOR;
                            heartColor = FROZEN_HEART_COLOR;
                        }

                        tabEntry = tabEntry.append(Component.text(Math.round(p.getHealth())).color(healthColor)
                                .append(Component.text(Emoji.HEART.toString()).color(heartColor)));
                    }
                }

                p.playerListName(tabEntry);
            }
        }, 0, 20);
    }
}
