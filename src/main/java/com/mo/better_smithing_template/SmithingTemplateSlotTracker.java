package com.mo.better_smithing_template;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.Container;

/** 追踪所有 SmithingMenu 的 inputSlots 容器实例，供 SlotMixin 识别模板槽。 */
public final class SmithingTemplateSlotTracker {

    private static final Set<Container> TRACKED_INPUT_SLOTS =
            Collections.newSetFromMap(new WeakHashMap<>());

    private SmithingTemplateSlotTracker() {
    }

    public static void register(Container inputSlots) {
        TRACKED_INPUT_SLOTS.add(inputSlots);
    }

    public static boolean isSmithingMenuInputSlots(Container container) {
        return TRACKED_INPUT_SLOTS.contains(container);
    }
}
