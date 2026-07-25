package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.ModDataComponents;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Mixin 注入 {@link SmithingMenu}，改写锻造台的模板消耗机制。
 *
 * 原版行为：玩家取出锻造结果时，{@code onTake} 调用 {@code shrinkStackInSlot(0)}
 * 直接将模板物品数量减 1（即整张模板被消耗）。
 *
 * 模组行为：通过 {@code @Redirect} 拦截对 {@code shrinkStackInSlot(0)} 的调用，
 * 改为对自定义组件 {@link ModDataComponents#TEMPLATE_USES} 减 1：
 * - 若模板拥有 TEMPLATE_USES 组件，则使用次数 -1
 * - 当使用次数归零时，模板损坏并消失（set EMPTY）
 * - 若模板未拥有该组件（未配置耐久），则回退到原版消耗逻辑（shrink 1）
 *
 * 注意：仅拦截 ordinal = 0 的调用（即模板槽），基础物品槽和材料槽仍按原版逻辑消耗。
 *
 * 实现细节：不直接 @Shadow 父类 ItemCombinerMenu 的 inputSlots 字段
 * （Mixin 无法在子类目标中定位父类字段），而是通过 public getSlot(int) 访问槽位。
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {

    /**
     * 拦截 {@link SmithingMenu#onTake} 中对 {@code shrinkStackInSlot(0)} 的调用。
     * ordinal = 0 确保只拦截模板槽（slot 0）的消耗，不影响基础物品和材料。
     *
     * @param self  被调用方法的实例（即 SmithingMenu 本身）
     * @param index 槽位索引，此处固定为 0（模板槽）
     */
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

        // 仅当模板拥有自定义使用次数组件时，使用次数扣减逻辑
        if (!template.isEmpty() && template.has(ModDataComponents.TEMPLATE_USES.get())) {
            int uses = template.getOrDefault(ModDataComponents.TEMPLATE_USES.get(), 0);
            int newUses = uses - 1;

            if (newUses <= 0) {
                // 使用次数耗尽，模板损坏消失
                self.getSlot(index).set(ItemStack.EMPTY);
            } else {
                // 扣减 1 次使用
                template.set(ModDataComponents.TEMPLATE_USES.get(), newUses);
                self.getSlot(index).set(template);
            }
            return;
        }

        // 模板未配置使用次数组件，回退到原版消耗逻辑（数量减 1）
        if (!template.isEmpty()) {
            template.shrink(1);
            self.getSlot(index).set(template);
        }
    }
}
