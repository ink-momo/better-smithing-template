package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.ModDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入 {@link Item}，为拥有自定义组件 {@link ModDataComponents#TEMPLATE_USES} 的物品
 * 渲染类原版耐久条。
 *
 * 原版耐久条依赖 {@link net.minecraft.core.component.DataComponents#DAMAGE} /
 * {@link net.minecraft.core.component.DataComponents#MAX_DAMAGE}，而本模组改用自定义组件
 * 存储使用次数，原版耐久条不会显示。本 Mixin 拦截三个渲染相关方法：
 * - {@link Item#isBarVisible}：当剩余次数 < 上限时显示条
 * - {@link Item#getBarWidth}：按剩余比例计算条宽（0-13）
 * - {@link Item#getBarColor}：按剩余比例计算颜色（绿→红，同原版公式）
 *
 * 仅对拥有 TEMPLATE_USES 组件的物品生效，其余物品保持原版行为。
 * 客户端专用（耐久条仅在客户端渲染）。
 */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void betterSmithingTemplate$customBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Integer uses = stack.get(ModDataComponents.TEMPLATE_USES.get());
        if (uses == null) {
            return; // 非本模组管理的物品，走原版逻辑
        }
        int maxUses = stack.getOrDefault(ModDataComponents.TEMPLATE_MAX_USES.get(), 1);
        cir.setReturnValue(uses < maxUses);
    }

    @Inject(method = "getBarWidth", at = @At("HEAD"), cancellable = true)
    private void betterSmithingTemplate$customBarWidth(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Integer uses = stack.get(ModDataComponents.TEMPLATE_USES.get());
        if (uses == null) {
            return;
        }
        int maxUses = stack.getOrDefault(ModDataComponents.TEMPLATE_MAX_USES.get(), 1);
        int used = maxUses - uses;
        cir.setReturnValue(Math.round(13.0F - (float) used * 13.0F / (float) maxUses));
    }

    @Inject(method = "getBarColor", at = @At("HEAD"), cancellable = true)
    private void betterSmithingTemplate$customBarColor(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        Integer uses = stack.get(ModDataComponents.TEMPLATE_USES.get());
        if (uses == null) {
            return;
        }
        int maxUses = stack.getOrDefault(ModDataComponents.TEMPLATE_MAX_USES.get(), 1);
        int used = maxUses - uses;
        float remainingRatio = Math.max(0.0F, (float) (maxUses - used) / (float) maxUses);
        cir.setReturnValue(Mth.hsvToRgb(remainingRatio / 3.0F, 1.0F, 1.0F));
    }
}
