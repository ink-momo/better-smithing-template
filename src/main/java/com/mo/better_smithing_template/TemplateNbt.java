package com.mo.better_smithing_template;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 替代 1.21 的 Data Components，使用 NBT Tag 存储模板耐久信息。
 *
 * <p>1.20.1 的 ItemStack 没有持久化数据组件系统，所有自定义持久化数据通过 NBT Tag 存储。
 * 该工具类封装了模板耐久的 NBT 读写逻辑，集中管理 tag 名称与初始化策略。</p>
 *
 * <p>所有方法均不会主动初始化 NBT tag —— 仅 {@link #ensureInit(ItemStack, int)} 在需要时
 * 为 {@link ItemStack} 创建并写入默认耐久值，从而最大限度避免对原版逻辑的副作用。</p>
 */
public final class TemplateNbt {

    /** 当前剩余使用次数的 NBT key（int，范围 0 ~ Integer.MAX_VALUE）。 */
    public static final String TAG_USES = "template_uses";

    /** 最大使用次数上限的 NBT key（int，范围 1 ~ Integer.MAX_VALUE）。 */
    public static final String TAG_MAX_USES = "template_max_uses";

    private TemplateNbt() {
    }

    /** 判断该 ItemStack 是否已注入模板耐久 NBT。 */
    public static boolean hasTemplate(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.hasTag()
                && stack.getTag().contains(TAG_USES);
    }

    /** 读取剩余使用次数；若未注入则返回 0。 */
    public static int getUses(ItemStack stack) {
        if (!hasTemplate(stack)) {
            return 0;
        }
        return stack.getTag().getInt(TAG_USES);
    }

    /** 读取最大使用次数；若未注入则返回 1（避免除零）。 */
    public static int getMaxUses(ItemStack stack) {
        if (!hasTemplate(stack) || !stack.getTag().contains(TAG_MAX_USES)) {
            return 1;
        }
        return stack.getTag().getInt(TAG_MAX_USES);
    }

    /** 设置剩余使用次数（不修改 maxUses）。若不存在 tag 会自动创建。 */
    public static void setUses(ItemStack stack, int uses) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_USES, Math.max(0, uses));
        if (!tag.contains(TAG_MAX_USES)) {
            tag.putInt(TAG_MAX_USES, Math.max(1, uses));
        }
    }

    /** 同时设置 uses 与 maxUses。 */
    public static void setBoth(ItemStack stack, int uses, int maxUses) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_USES, Math.max(0, uses));
        tag.putInt(TAG_MAX_USES, Math.max(1, maxUses));
    }

    /**
     * 惰性初始化：若该 ItemStack 没有模板耐久 NBT，则写入默认的 uses = maxUses = maxUses。
     *
     * <p>用于替代 1.21 的 {@code ModifyDefaultComponentsEvent}：在锻造台生成结果阶段
     * （{@link net.minecraft.world.inventory.SmithingMenu#createResult}）或玩家手持物品时按需注入。</p>
     *
     * @return true 表示本次调用写入了 NBT；false 表示 ItemStack 已有耐久 NBT，未做修改。
     */
    public static boolean ensureInit(ItemStack stack, int maxUses) {
        if (stack == null || stack.isEmpty() || hasTemplate(stack)) {
            return false;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_USES, maxUses);
        tag.putInt(TAG_MAX_USES, maxUses);
        return true;
    }
}
