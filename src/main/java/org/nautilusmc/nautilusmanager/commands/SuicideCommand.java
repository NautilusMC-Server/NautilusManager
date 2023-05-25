package org.nautilusmc.nautilusmanager.commands;

import net.kyori.adventure.text.Component;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SuicideCommand extends NautilusCommand {

    public static final String SUICIDE_TRANSLATION_KEY = "death.suicide";

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!").color(NautilusCommand.ERROR_COLOR));
            return true;
        }

        ServerPlayer nms = ((CraftPlayer) player).getHandle();
        nms.combatTracker.recordDamage(new DamageSource(nms.level.damageSources().outOfWorld().typeHolder(), null, null) {
            @Override
            public net.minecraft.network.chat.Component getLocalizedDeathMessage(LivingEntity killed) {
                return net.minecraft.network.chat.Component.translatable(SUICIDE_TRANSLATION_KEY);
            }
        }, (float) player.getHealth(), (float) player.getHealth());
        player.setHealth(0);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}
