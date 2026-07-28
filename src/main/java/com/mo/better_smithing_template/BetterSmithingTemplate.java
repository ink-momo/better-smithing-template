package com.mo.better_smithing_template;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Better Smithing Template 模组主类。
 *
 * <p>1.20.1 Forge 入口：通过 {@link FMLJavaModLoadingContext#get()} 获取 Mod 事件总线，
 * 配置使用 {@link ModLoadingContext#registerConfig} 注册为 {@link ModConfig.Type#COMMON} 类型
 * （1.21 NeoForge 的 STARTUP 在 1.20.1 中不存在）。</p>
 *
 * <p>1.21 的 {@code ModifyDefaultComponentsEvent} 在 1.20.1 中不存在，因此无法在物品注册期
 * 注入 Data Component。改为在 {@link FMLCommonSetupEvent} 中遍历 {@link ForgeRegistries#ITEMS}
 * 一次性收集所有匹配的 Item 实例并缓存到 {@link #MATCHED_TEMPLATES}，供 Mixin 在
 * {@link net.minecraft.world.inventory.SmithingMenu#createResult} 阶段通过
 * {@link TemplateNbt#ensureInit} 惰性写入 NBT。</p>
 */
@Mod(BetterSmithingTemplate.MOD_ID)
public class BetterSmithingTemplate {

    public static final String MOD_ID = "better_smithing_template";
    public static final Logger LOGGER = LogUtils.getLogger();

    /** 在 FMLCommonSetupEvent 阶段被填充，所有应注入耐久的模板 Item 集合。 */
    private static volatile Set<Item> MATCHED_TEMPLATES = Set.of();

    public BetterSmithingTemplate() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册配置文件 config/better_smithing_template.toml
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.COMMON,
                TemplateDurabilityConfig.SPEC,
                "better_smithing_template.toml"
        );

        // 在 setup 阶段收集匹配的模板 Item
        modBus.addListener(BetterSmithingTemplate::onCommonSetup);
    }

    /**
     * 在 setup 阶段（注册阶段已完成、ForgeRegistries 已被填充）收集所有应注入耐久的模板 Item。
     *
     * <p>本方法不修改任何 Item 实例，仅做集合缓存；真正的 NBT 注入由
     * {@link TemplateNbt#ensureInit} 在玩家手持/放入锻造台时按需执行。</p>
     */
    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            int maxUses = TemplateDurabilityConfig.CONFIG.maxDamage.get();
            if (maxUses <= 0) {
                MATCHED_TEMPLATES = Set.of();
                LOGGER.info("[{}] max_damage = 0, 已禁用耐久功能（原版行为）", MOD_ID);
                return;
            }

            Set<ResourceLocation> targetIds = TemplateDurabilityConfig.CONFIG.targetItems.get().stream()
                    .map(ResourceLocation::tryParse)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());

            boolean autoDetect = TemplateDurabilityConfig.CONFIG.autoDetectTemplates.get();

            Set<Item> matched = ForgeRegistries.ITEMS.getValues().stream()
                    .filter(item -> matchesTarget(item, targetIds, autoDetect))
                    .collect(Collectors.toSet());

            MATCHED_TEMPLATES = Set.copyOf(matched);
            LOGGER.info("[{}] 已收集 {} 个锻造模板将注入耐久 (max_damage={})",
                    MOD_ID, matched.size(), maxUses);
        });
    }

    private static boolean matchesTarget(Item item, Set<ResourceLocation> targetIds, boolean autoDetect) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key != null && targetIds.contains(key)) {
            return true;
        }
        return autoDetect && item instanceof SmithingTemplateItem;
    }

    /**
     * 判断给定 Item 是否应当被注入耐久。
     *
     * <p>由 Mixin 在锻造台 {@code createResult} 阶段调用，配合
     * {@link TemplateNbt#ensureInit} 完成惰性 NBT 注入。</p>
     */
    public static boolean isTargetTemplate(Item item) {
        return MATCHED_TEMPLATES.contains(item);
    }

    /**
     * 读取配置的最大使用次数。Mix 层无法直接访问 ForgeConfigSpec 中的值（配置可能尚未加载），
     * 提供该访问器供 Mixin 在运行时按需读取。
     */
    public static int configuredMaxDamage() {
        return TemplateDurabilityConfig.CONFIG.maxDamage.get();
    }
}
