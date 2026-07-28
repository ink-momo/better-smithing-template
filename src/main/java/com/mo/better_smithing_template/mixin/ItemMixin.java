package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.TemplateNbt;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入 {@link Item}，为拥有 {@code template_uses} NBT 的物品渲染类原版耐久条。客户端专用。
 *
 * <p>1.20.1 Forge 中 {@link Item#isBarVisible(ItemStack)}、{@link Item#getBarWidth(ItemStack)}、
 * {@link Item#getBarColor(ItemStack)} 三个方法签名与 1.21 完全一致（通过官方 Mojang 映射验证），
 * 仅持久化数据来源由 1.21 的 Data Component 改为 NBT Tag（{@link TemplateNbt}）。</p>
 */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void betterSmithingTemplate$customBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!TemplateNbt.hasTemplate(stack)) {
            return;
        }
        int uses = TemplateNbt.getUses(stack);
        int maxUses = TemplateNbt.getMaxUses(stack);
        cir.setReturnValue(uses < maxUses);
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void betterSmithingTemplate$customBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!TemplateNbt.hasTemplate(stack)) {
            return;
        }
        int uses = TemplateNbt.getUses(stack);
        int maxUses = TemplateNbt.getMaxUses(stack);
        int used = maxUses - uses;
        cir.setReturnValue(Math.round(13.0F - (float) used * 13.0F / (float) maxUses));
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void betterSmithingTemplate$customBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (!TemplateNbt.hasTemplate(stack)) {
            return;
        }
        int uses = TemplateNbt.getUses(stack);
        int maxUses = TemplateNbt.getMaxUses(stack);
        int used = maxUses - uses;
        float remainingRatio = Math.max(0.0F, (float) (maxUses - used) / (float) maxUses);
        cir.setReturnValue(Mth.hsvToRgb(remainingRatio / 3.0F, 1.0F, 1.0F));
    }
}
