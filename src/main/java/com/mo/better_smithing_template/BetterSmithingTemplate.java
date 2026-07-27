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

/** Better Smithing Template 模组主类。 */
@Mod(BetterSmithingTemplate.MOD_ID)
public class BetterSmithingTemplate {

    public static final String MOD_ID = "better_smithing_template";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BetterSmithingTemplate(IEventBus modBus, ModContainer container) {
        ModDataComponents.register(modBus);
        container.registerConfig(ModConfig.Type.STARTUP, TemplateDurabilityConfig.SPEC, "better_smithing_template.toml");
        modBus.addListener(BetterSmithingTemplate::onModifyComponents);
    }

    private static void onModifyComponents(ModifyDefaultComponentsEvent event) {
        int maxUses = TemplateDurabilityConfig.CONFIG.maxDamage.get();
        if (maxUses <= 0) {
            return;
        }

        Set<ResourceLocation> targetIds = TemplateDurabilityConfig.CONFIG.targetItems.get().stream()
                .map(ResourceLocation::parse)
                .collect(Collectors.toSet());

        boolean autoDetect = TemplateDurabilityConfig.CONFIG.autoDetectTemplates.get();

        event.modifyMatching(
                item -> matchesTarget(item, targetIds, autoDetect),
                builder -> {
                    builder.set(ModDataComponents.TEMPLATE_USES.get(), maxUses);
                    builder.set(ModDataComponents.TEMPLATE_MAX_USES.get(), maxUses);
                }
        );

        LOGGER.info("[{}] 已为匹配的锻造模板设置使用次数上限: {}", MOD_ID, maxUses);
    }

    private static boolean matchesTarget(Item item, Set<ResourceLocation> targetIds, boolean autoDetect) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        if (targetIds.contains(key)) {
            return true;
        }
        return autoDetect && item instanceof SmithingTemplateItem;
    }
}
