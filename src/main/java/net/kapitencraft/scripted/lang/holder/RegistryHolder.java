package net.kapitencraft.scripted.lang.holder;

import net.kapitencraft.scripted.lang.holder.class_ref.ClassReference;
import net.kapitencraft.scripted.lang.holder.token.Token;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record RegistryHolder(ClassReference reference, Token origin, Registry<?> registry, ResourceKey<?> key, ResourceLocation objLoc, Object entry) {
}
