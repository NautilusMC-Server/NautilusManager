package org.nautilusmc.nautilusmanager.commands;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SuicideCommand extends Command {
    public static final String SUICIDE_TRANSLATION_KEY = "death.suicide";

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(NOT_PLAYER_ERROR);
            return true;
        }

        ServerPlayer nms = ((CraftPlayer) player).getHandle();
        nms.combatTracker.recordDamage(new DamageSource(nms.level().damageSources().generic().typeHolder(), null, null) {
            @Override
            public @NotNull net.minecraft.network.chat.Component getLocalizedDeathMessage(@NotNull LivingEntity killed) {
                return net.minecraft.network.chat.Component.translatable(SUICIDE_TRANSLATION_KEY);
            }
        }, (float) player.getHealth());
        player.setHealth(0);

        return true;
    }
}
