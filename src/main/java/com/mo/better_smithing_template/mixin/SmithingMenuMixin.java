package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.ModDataComponents;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Mixin 注入 SmithingMenu，改写锻造台的模板消耗机制，并拦截复制配方。 */
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {

    @Inject(method = "createResult", at = @At("RETURN"))
    private void betterSmithingTemplate$rejectUsedTemplateInCopyRecipe(CallbackInfo ci) {
        SmithingMenu self = (SmithingMenu) (Object) this;
        ItemStack template = self.getSlot(SmithingMenu.TEMPLATE_SLOT).getItem();
        if (template.isEmpty() || !template.has(ModDataComponents.TEMPLATE_USES.get())) {
            return;
        }
        int uses = template.getOrDefault(ModDataComponents.TEMPLATE_USES.get(), 0);
        int maxUses = template.getOrDefault(ModDataComponents.TEMPLATE_MAX_USES.get(), 1);
        if (uses >= maxUses) {
            return;
        }
        ItemStack result = self.getSlot(SmithingMenu.RESULT_SLOT).getItem();
        if (!result.isEmpty() && result.has(ModDataComponents.TEMPLATE_USES.get())) {
            self.getSlot(SmithingMenu.RESULT_SLOT).set(ItemStack.EMPTY);
        }
    }

    @Redirect(
            method = "onTake",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/SmithingMenu;shrinkStackInSlot(I)V",
                    ordinal = 0
            )
    )
    private void betterSmithingTemplate$damageTemplateInsteadOfShrink(SmithingMenu self, int index) {
        ItemStack template = self.getSlot(index).getItem();

        if (!template.isEmpty() && template.has(ModDataComponents.TEMPLATE_USES.get())) {
            int uses = template.getOrDefault(ModDataComponents.TEMPLATE_USES.get(), 0);
            int maxUses = template.getOrDefault(ModDataComponents.TEMPLATE_MAX_USES.get(), 1);
            int newUses = uses - 1;

            if (newUses > 0) {
                template.set(ModDataComponents.TEMPLATE_USES.get(), newUses);
                self.getSlot(index).set(template);
            } else if (template.getCount() > 1) {
                template.shrink(1);
                template.set(ModDataComponents.TEMPLATE_USES.get(), maxUses);
                self.getSlot(index).set(template);
            } else {
                self.getSlot(index).set(ItemStack.EMPTY);
            }
            return;
        }

        if (!template.isEmpty()) {
            template.shrink(1);
            self.getSlot(index).set(template);
        }
    }
}
