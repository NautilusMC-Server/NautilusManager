package org.nautilusmc.nautilusmanager.commands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SuicideCommand extends NautilusCommand {
    public static final String SUICIDE_TRANSLATION_KEY = "death.suicide";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ErrorMessage.NOT_PLAYER);
            return true;
        }

        ServerPlayer nms = ((CraftPlayer) player).getHandle();
        nms.combatTracker.recordDamage(new DamageSource(nms.level.damageSources().outOfWorld().typeHolder(), null, null) {
            @Override
            public @NotNull net.minecraft.network.chat.Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                return net.minecraft.network.chat.Component.translatable(SUICIDE_TRANSLATION_KEY);
            }
        }, (float) player.getHealth(), (float) player.getHealth());
        player.setHealth(0);
        return true;
    }
}
