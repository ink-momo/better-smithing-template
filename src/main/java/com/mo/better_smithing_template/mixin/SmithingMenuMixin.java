package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.BetterSmithingTemplate;
import com.mo.better_smithing_template.TemplateNbt;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入 {@link SmithingMenu}，改写锻造台的模板消耗机制，并拦截复制配方。
 *
 * <p>1.20.1 Forge 中 {@link SmithingMenu} 的 {@code TEMPLATE_SLOT}、{@code RESULT_SLOT}、
 * {@code shrinkStackInSlot(int)}、{@code createResult()}、{@code onTake(...)} 与 1.21 一致
 * （通过 1.20-46.0.14 官方 Javadoc 验证）。仅持久化数据来源由 1.21 的 Data Component
 * 改为 NBT Tag（{@link TemplateNbt}）。</p>
 *
 * <p>由于 1.20.1 没有 {@code ModifyDefaultComponentsEvent}，无法在物品注册期注入耐久数据，
 * 故在 {@code createResult} 末尾通过 {@link TemplateNbt#ensureInit} 惰性初始化模板 NBT。</p>
 */
@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {

    /**
     * 在结果生成阶段：
     * <ol>
     *   <li>若模板槽中的模板尚未注入耐久 NBT 且属于匹配的目标 Item，则惰性写入默认耐久值。</li>
     *   <li>若模板已使用过（uses &lt; maxUses），则清空复制配方（如盔甲纹饰复制）的结果槽，
     *       防止用磨损模板复制物品。</li>
     * </ol>
     */
    @Inject(method = "createResult", at = @At("RETURN"))
    private void betterSmithingTemplate$initTemplateNbtAndRejectCopy(CallbackInfo ci) {
        SmithingMenu self = (SmithingMenu) (Object) this;
        ItemStack template = self.getSlot(SmithingMenu.TEMPLATE_SLOT).getItem();
        if (template.isEmpty()) {
            return;
        }

        // 惰性初始化：若该模板尚未注入 NBT 且属于目标 Item，则注入默认耐久。
        if (!TemplateNbt.hasTemplate(template)
                && BetterSmithingTemplate.isTargetTemplate(template.getItem())) {
            int maxUses = BetterSmithingTemplate.configuredMaxDamage();
            if (maxUses > 0) {
                TemplateNbt.ensureInit(template, maxUses);
                self.getSlot(SmithingMenu.TEMPLATE_SLOT).set(template);
            }
        }

        if (!TemplateNbt.hasTemplate(template)) {
            return;
        }

        int uses = TemplateNbt.getUses(template);
        int maxUses = TemplateNbt.getMaxUses(template);
        if (uses >= maxUses) {
            return;
        }

        // 模板已使用过：阻止复制配方（结果槽中含 template_uses NBT 的物品即视为复制产物）。
        ItemStack result = self.getSlot(SmithingMenu.RESULT_SLOT).getItem();
        if (!result.isEmpty() && TemplateNbt.hasTemplate(result)) {
            self.getSlot(SmithingMenu.RESULT_SLOT).set(ItemStack.EMPTY);
        }
    }

    /**
     * 拦截 {@code onTake} 中对模板槽的 {@code shrinkStackInSlot} 调用：
     * 若模板拥有耐久 NBT，则改为扣除 1 点耐久（仅在耐久耗尽时才真正消耗堆叠数）。
     * 否则回退到原版 {@code shrinkStackInSlot} 行为。
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

        if (!template.isEmpty() && TemplateNbt.hasTemplate(template)) {
            int uses = TemplateNbt.getUses(template);
            int maxUses = TemplateNbt.getMaxUses(template);
            int newUses = uses - 1;

            if (newUses > 0) {
                TemplateNbt.setUses(template, newUses);
                self.getSlot(index).set(template);
            } else if (template.getCount() > 1) {
                // 堆叠中剩余模板：消耗一个并重置耐久
                template.shrink(1);
                TemplateNbt.setBoth(template, maxUses, maxUses);
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
