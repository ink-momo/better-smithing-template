package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.ModDataComponents;
import com.mo.better_smithing_template.SmithingTemplateSlotTracker;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Mixin 注入 Slot：限制锻造台模板槽堆叠为 1，并阻止使用过的模板放入合成网格。 */
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
                && stack.has(ModDataComponents.TEMPLATE_USES.get())) {
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
                && stack.has(ModDataComponents.TEMPLATE_USES.get())) {
            int uses = stack.getOrDefault(ModDataComponents.TEMPLATE_USES.get(), 0);
            int maxUses = stack.getOrDefault(ModDataComponents.TEMPLATE_MAX_USES.get(), 1);
            if (uses < maxUses) {
                cir.setReturnValue(false);
            }
        }
    }
}
