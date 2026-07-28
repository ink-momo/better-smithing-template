package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.SmithingTemplateSlotTracker;
import com.mo.better_smithing_template.TemplateNbt;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin 注入 {@link Slot}：限制锻造台模板槽堆叠为 1，并阻止使用过的模板放入合成网格。
 *
 * <p>1.20.1 Forge 中 {@link Slot#getMaxStackSize(ItemStack)} 与
 * {@link Slot#mayPlace(ItemStack)} 签名与 1.21 一致（通过官方 Mojang 映射验证）；
 * {@link CraftingContainer} 也保持兼容。仅持久化数据来源由 1.21 的 Data Component 改为 NBT。</p>
 */
@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow
    public Container container;

    @Shadow
    public int index;

    @Inject(
            method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void betterSmithingTemplate$limitTemplateStackSize(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (this.index == 0
                && this.container != null
                && SmithingTemplateSlotTracker.isSmithingMenuInputSlots(this.container)
                && TemplateNbt.hasTemplate(stack)) {
            cir.setReturnValue(1);
        }
    }

    @Inject(
            method = "mayPlace(Lnet/minecraft/world/item/ItemStack;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void betterSmithingTemplate$rejectUsedTemplateInCraftingGrid(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.container instanceof CraftingContainer
                && TemplateNbt.hasTemplate(stack)) {
            int uses = TemplateNbt.getUses(stack);
            int maxUses = TemplateNbt.getMaxUses(stack);
            if (uses < maxUses) {
                cir.setReturnValue(false);
            }
        }
    }
}
