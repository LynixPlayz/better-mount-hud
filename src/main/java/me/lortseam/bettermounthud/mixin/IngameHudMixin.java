package me.lortseam.bettermounthud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(InGameHud.class)
public abstract class IngameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow
    private LivingEntity getRiddenEntity() {return null;}

    @Shadow
    private int getHeartCount(LivingEntity entity) {return 0;}

    @Shadow
    private int getHeartRows(int heartCount) {return 0;}

    @ModifyVariable(method = "renderMountHealth", at = @At(value = "STORE"), ordinal = 2)
    private int bettermounthud$moveMountHealthUp(int y) {
        if (client.interactionManager.hasStatusBars()) {
            y -= 10;
        }
        return y;
    }

    @Redirect(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;getHeartCount(Lnet/minecraft/entity/LivingEntity;)I"))
    private int bettermounthud$alwaysRenderFood(InGameHud inGameHud, LivingEntity entity) {
        return 0;
    }

    @ModifyVariable(method = "getAirBubbleY", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    private int bettermounthud$moveAirUp(int heartCount) {
        LivingEntity entity = getRiddenEntity();
        if (entity != null) {
            return getHeartCount(entity);
        }
        return heartCount;
    }

    @Redirect(method = "getCurrentBarType", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getJumpingMount()Lnet/minecraft/entity/JumpingMount;"))
    private JumpingMount bettermounthud$switchBar(ClientPlayerEntity player) {
        var jumpingMount = player.getJumpingMount();
        if (!client.interactionManager.hasExperienceBar() || client.options.jumpKey.isPressed()
                || player.getMountJumpStrength() > 0) return jumpingMount;
        return null;
    }

    @Redirect(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;hasExperienceBar()Z"))
    private boolean bettermounthud$renderExperienceLevel(ClientPlayerInteractionManager instance) {
        return client.interactionManager.hasExperienceBar() &&
                ((client.player.getJumpingMount() != null
                        && !client.options.jumpKey.isPressed()
                        && client.player.getMountJumpStrength() <= 0)
                || client.player.getJumpingMount() == null);
    }
}
