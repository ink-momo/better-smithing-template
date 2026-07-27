package com.mo.better_smithing_template;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 模组自定义数据组件注册。 */
public class ModDataComponents {

    public static final DeferredRegister.DataComponents REGISTRAR =
            DeferredRegister.createDataComponents(BetterSmithingTemplate.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TEMPLATE_USES =
            REGISTRAR.registerComponentType(
                    "template_uses",
                    builder -> builder
                            .persistent(Codec.intRange(0, Integer.MAX_VALUE))
                            .networkSynchronized(ByteBufCodecs.INT)
            );

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
