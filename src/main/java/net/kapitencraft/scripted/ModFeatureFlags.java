package net.kapitencraft.scripted;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;

public interface ModFeatureFlags {
    FeatureFlag CONTENT = FeatureFlags.REGISTRY.getFlag(Scripted.res("content"));
}
