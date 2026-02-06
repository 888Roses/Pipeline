package dev.rosenoire.pipeline.common.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface TagHelper {
    class UniqueList<T> extends ArrayList<T> {
        @Override
        public boolean add(T t) {
            if (contains(t)) {
                return false;
            }

            return super.add(t);
        }
    }

    static Optional<List<Item>> getItemsFromTagItemStack(ItemStack itemStack) {
        if (itemStack.getCustomName() == null) {
            return Optional.empty();
        }

        String customName = itemStack.getCustomName().getString();
        return getItemsFromTagName(customName);
    }

    static Optional<List<Item>> getItemsFromTagName(String name) {
        if (name.startsWith("#")) name = name.substring(1);
        Identifier identifier = Identifier.tryParse(name);
        if (identifier == null) return Optional.empty();

        UniqueList<Item> tagContent = new UniqueList<>();

        TagKey<Item> itemTag = TagKey.of(RegistryKeys.ITEM, identifier);
        tagContent.addAll(collectTagEntries(Registries.ITEM, itemTag));

        TagKey<Block> blockTag = TagKey.of(RegistryKeys.BLOCK, identifier);
        collectTagEntries(Registries.BLOCK, blockTag)
                .stream()
                .map(Block::asItem)
                .filter(Objects::nonNull)
                .forEach(tagContent::add);

        return Optional.of(tagContent);
    }

    private static <T> List<T> collectTagEntries(DefaultedRegistry<T> registry, TagKey<T> tag) {
        return registry.getOptional(tag).map(named -> named.stream().map(RegistryEntry::value).toList()).orElse(List.of());
    }
}
