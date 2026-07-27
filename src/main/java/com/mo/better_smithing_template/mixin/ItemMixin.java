package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.ModDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mixin 注入 Item，为拥有 TEMPLATE_USES 组件的物品渲染类原版耐久条。客户端专用。 */
@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "isBarVisible", at = @At("HEAD"), cancellable = true)
    private void betterSmithingTemplate$customBarVisible(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Integer uses = stack.get(ModDataComponents.TEMPLATE_USES.get());
        if (uses == null) {
            return;
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
