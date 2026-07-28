package com.mo.better_smithing_template.mixin;

import com.mo.better_smithing_template.SmithingTemplateSlotTracker;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SmithingMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin 注入 {@link ItemCombinerMenu}，在 {@link SmithingMenu} 构造完成时注册 inputSlots 到 Tracker。
 *
 * <p>1.20.1 Forge 中 {@code ItemCombinerMenu} 的构造器签名为
 * {@code (MenuType<?>, int, Inventory, ContainerLevelAccess)}，与 1.21 一致；
 * {@code protected final Container inputSlots} 字段也保持兼容。</p>
 */
@Mixin(ItemCombinerMenu.class)
public abstract class ItemCombinerMenuMixin {

    @Shadow
    @Final
    protected Container inputSlots;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void betterSmithingTemplate$registerInputSlots(MenuType<?> type, int containerId,
                                                           Inventory playerInventory, ContainerLevelAccess access,
                                                           CallbackInfo ci) {
        if ((Object) this instanceof SmithingMenu) {
            SmithingTemplateSlotTracker.register(this.inputSlots);
        }
    }
}
