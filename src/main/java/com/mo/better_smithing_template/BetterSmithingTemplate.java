package com.mo.better_smithing_template;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Better Smithing Template 模组主类。
 *
 * 设计思路：
 * 1. 通过 {@link ModifyDefaultComponentsEvent} 为锻造模板添加自定义数据组件
 *    {@link ModDataComponents#TEMPLATE_USES} 和 {@link ModDataComponents#TEMPLATE_MAX_USES}，
 *    使其拥有"使用次数"（可配置上限），替代原版 MAX_DAMAGE。
 *    原因：原版 MAX_DAMAGE 与 MAX_STACK_SIZE > 1 互斥，会导致锻造模板复制配方加载失败。
 * 2. 通过 Mixin 改写 {@link net.minecraft.world.inventory.SmithingMenu} 的锻造取出逻辑，
 *    锻造时不再直接消耗模板，而是对 TEMPLATE_USES -1；归零时模板才会消失。
 * 3. 通过 Mixin 改写 {@link Item} 的耐久条渲染方法，让自定义组件也能显示类原版耐久条。
 *
 * 配置策略：混合匹配模式
 * - target_items：精确控制哪些物品获得耐久，支持任意注册名
 * - auto_detect_templates：自动匹配所有 SmithingTemplateItem 子类（含其他模组）
 * 两者取并集，兼顾灵活性和开箱即用。
 *
 * 注意：必须使用 {@link ModConfig.Type#STARTUP} 类型加载配置，
 * 否则 ModifyDefaultComponentsEvent 触发时配置尚未加载。
 */
@Mod(BetterSmithingTemplate.MOD_ID)
public class BetterSmithingTemplate {

    public static final String MOD_ID = "better_smithing_template";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterSmithingTemplate(IEventBus modBus, ModContainer container) {
        // 注册自定义数据组件，必须在 ModifyDefaultComponentsEvent 触发前完成
        ModDataComponents.register(modBus);

        // 必须使用 STARTUP 类型：COMMON 类型在 FMLCommonSetupEvent 之前才加载，
        // 此时 ModifyDefaultComponentsEvent 已经过去
        container.registerConfig(ModConfig.Type.STARTUP, TemplateDurabilityConfig.SPEC, "better_smithing_template.toml");

        modBus.addListener(BetterSmithingTemplate::onModifyComponents);
    }

    private static void onModifyComponents(ModifyDefaultComponentsEvent event) {
        int maxUses = TemplateDurabilityConfig.CONFIG.maxDamage.get();
        if (maxUses <= 0) {
            return;
        }

        // 从配置文件读取目标物品注册名列表，转为 Set 以提高查询效率
        Set<ResourceLocation> targetIds = TemplateDurabilityConfig.CONFIG.targetItems.get().stream()
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());

        boolean autoDetect = TemplateDurabilityConfig.CONFIG.autoDetectTemplates.get();

        // 混合匹配：注册名列表 OR instanceof SmithingTemplateItem
        // 设置 TEMPLATE_USES（剩余次数，初始等于上限）和 TEMPLATE_MAX_USES（上限，用于耐久条比例）
        // 不使用 MAX_DAMAGE，避免与 MAX_STACK_SIZE > 1 互斥导致配方加载失败
        event.modifyMatching(
                item -> matchesTarget(item, targetIds, autoDetect),
                builder -> {
                    builder.set(ModDataComponents.TEMPLATE_USES.get(), maxUses);
                    builder.set(ModDataComponents.TEMPLATE_MAX_USES.get(), maxUses);
                }
        );

        LOGGER.info("[{}] 已为匹配的锻造模板设置使用次数上限: {}", MOD_ID, maxUses);
    }

    /**
     * 判断物品是否应获得耐久属性。两个条件取并集：
     * 1. 注册名在配置文件列表中（精确匹配，支持任意物品）
     * 2. autoDetectTemplates 为 true 且物品是 SmithingTemplateItem 实例（自动覆盖模组模板）
     */
    private static boolean matchesTarget(Item item, Set<ResourceLocation> targetIds, boolean autoDetect) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        if (targetIds.contains(key)) {
            return true;
        }
        return autoDetect && item instanceof SmithingTemplateItem;
    }
}
