package com.mo.better_smithing_template;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * 锻造模板耐久模组配置。
 *
 * <p>使用 Forge 1.20.1 的 {@link ForgeConfigSpec}（替代 1.21 NeoForge 的 ModConfigSpec）。
 * 配置类型为 {@link net.minecraftforge.fml.config.ModConfig.Type#COMMON}，意味着该配置在客户端
 * 与服务端都会加载但不会自动同步 —— 服务端需自行同步给客户端。</p>
 *
 * <p>本模组的耐久逻辑完全在服务端 Mixin 中执行，因此 COMMON 类型已足够。</p>
 */
public class TemplateDurabilityConfig {

    public static final TemplateDurabilityConfig CONFIG;
    public static final ForgeConfigSpec SPEC;

    static {
        var pair = new ForgeConfigSpec.Builder().configure(TemplateDurabilityConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ForgeConfigSpec.BooleanValue autoDetectTemplates;
    public final ForgeConfigSpec.ConfigValue<List<? extends String>> targetItems;
    public final ForgeConfigSpec.IntValue maxDamage;

    private TemplateDurabilityConfig(ForgeConfigSpec.Builder builder) {

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
                        o -> o instanceof String s && ResourceLocation.tryParse(s) != null);

        maxDamage = builder
                .comment("每个锻造模板的耐久值上限（每次锻造消耗 1 点耐久）",
                        "设为 0 则不添加耐久（模板行为恢复为原版：每次锻造直接消耗）",
                        "例如设为 3，则一个模板可使用 3 次")
                .defineInRange("max_damage", 3, 0, 5);
    }
}
