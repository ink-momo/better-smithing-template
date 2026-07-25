package com.mo.better_smithing_template;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组自定义数据组件注册。
 *
 * 设计原因：原版 {@link net.minecraft.core.component.DataComponents#MAX_DAMAGE} 与
 * {@link net.minecraft.core.component.DataComponents#MAX_STACK_SIZE} > 1 互斥
 * （见 {@link net.minecraft.world.item.ItemStack#validateComponents}）。
 * 直接给可堆叠的锻造模板加 MAX_DAMAGE 会导致所有引用该模板的配方 result 被
 * {@link net.minecraft.world.item.ItemStack#STRICT_CODEC} 拒绝，配方加载失败。
 *
 * 解决方案：用自定义组件存储"剩余使用次数"，绕开原版耐久互斥校验，
 * 模板保持可堆叠，所有原版配方正常加载。耐久条由 {@code ItemMixin} 自行渲染。
 */
public class ModDataComponents {

    public static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(BetterSmithingTemplate.MOD_ID);

    /** 剩余使用次数，每次锻造 -1，归零时模板消失。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TEMPLATE_USES =
            REGISTRAR.registerComponentType(
                    "template_uses",
                    builder -> builder
                            .persistent(Codec.intRange(0, Integer.MAX_VALUE))
                            .networkSynchronized(ByteBufCodecs.INT)
            );

    /** 最大使用次数，用于耐久条比例计算，设置后不变。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TEMPLATE_MAX_USES =
            REGISTRAR.registerComponentType(
                    "template_max_uses",
                    builder -> builder
                            .persistent(Codec.intRange(1, Integer.MAX_VALUE))
                            .networkSynchronized(ByteBufCodecs.INT)
            );

    public static void register(IEventBus modBus) {
        REGISTRAR.register(modBus);
    }
}
