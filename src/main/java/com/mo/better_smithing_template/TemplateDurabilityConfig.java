package com.mo.better_smithing_template;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/** 锻造模板耐久模组配置。 */
public class TemplateDurabilityConfig {

    public static final TemplateDurabilityConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        var pair = new ModConfigSpec.Builder().configure(TemplateDurabilityConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue autoDetectTemplates;
    public final ModConfigSpec.ConfigValue<List<? extends String>> targetItems;
    public final ModConfigSpec.IntValue maxDamage;

    private TemplateDurabilityConfig(ModConfigSpec.Builder builder) {

        autoDetectTemplates = builder
                .comment("是否启用智能识别",
                        "启用后，将自动识别游戏内所有锻造模板物品，无需手动配置（并非万能，可能会识别不到所有模板，此时请手动配置）")
                .define("auto_detect_templates", true);

        targetItems = builder
                .comment("需要添加耐久属性的模板注册名列表",
                        "格式: namespace:path，例如 cataclysm:cursium_upgrade_smithing_template",
                        "支持原版和模组添加的任何模板（理论支持）")
                .defineList("target_items",
                        () -> List.of(
                                "cataclysm:ignitium_upgrade_smithing_template",
                                "cataclysm:cursium_upgrade_smithing_template"
                        ),
                        () -> "minecraft:netherite_upgrade_smithing_template",   // 纯占位用，不实际使用
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

        maxDamage = builder
                .comment("每个锻造模板的耐久值上限（每次锻造消耗 1 点耐久）",
                        "设为 0 则不添加耐久（模板行为恢复为原版：每次锻造直接消耗）",
                        "例如设为 3，则一个模板可使用 3 次")
                .defineInRange("max_damage", 3, 0, 5);
    }
}