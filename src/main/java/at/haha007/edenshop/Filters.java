package at.haha007.edenshop;

import com.destroystokyo.paper.MaterialTags;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

import static org.bukkit.Material.*;

public enum Filters {
    ALL(material -> true, "<green>Alle", CHEST),
    BUILDING_BLOCKS(material -> material.isBlock() && !material.isAir() && material.isCollidable(), "<green>Baumaterial", GRASS_BLOCK),
    MINERALS(material -> {
        if (material.isAir()) return false;
        if (MaterialTags.ORES.isTagged(material)) return true;
        if (MaterialTags.RAW_ORES.isTagged(material)) return true;
        if (MaterialTags.RAW_ORE_BLOCKS.isTagged(material)) return true;
        switch (material) {
            case COAL_BLOCK:
            case COAL:
            case IRON_BLOCK:
            case IRON_INGOT:
            case GOLD_BLOCK:
            case GOLD_INGOT:
            case DIAMOND_BLOCK:
            case DIAMOND:
            case EMERALD_BLOCK:
            case EMERALD:
            case LAPIS_BLOCK:
            case LAPIS_LAZULI:
            case REDSTONE_BLOCK:
            case REDSTONE:
            case NETHERITE_BLOCK:
            case NETHERITE_INGOT:
            case ANCIENT_DEBRIS:
            case QUARTZ:
            case QUARTZ_BLOCK:
            case COPPER_BLOCK:
            case COPPER_INGOT:
            case RAW_COPPER:
            case RAW_GOLD:
            case RAW_IRON:
            case RAW_COPPER_BLOCK:
            case RAW_GOLD_BLOCK:
            case RAW_IRON_BLOCK:
            case AMETHYST_BLOCK:
            case AMETHYST_SHARD:
            case BUDDING_AMETHYST:
            case AMETHYST_CLUSTER:
            case LARGE_AMETHYST_BUD:
            case MEDIUM_AMETHYST_BUD:
            case SMALL_AMETHYST_BUD:
                return true;
            default:
                return false;
        }
    }, "<green>Mineralien", IRON_INGOT),
    EQUIPMENT(material -> {
        if (!material.isItem()) return false;
        if (material.isAir()) return false;
        String string = material.toString().toLowerCase();
        if (string.endsWith("_helmet")) return true;
        if (string.endsWith("_chestplate")) return true;
        if (string.endsWith("_leggings")) return true;
        if (string.endsWith("_boots")) return true;
        if (string.endsWith("_sword")) return true;
        if (string.endsWith("_axe")) return true;
        if (string.endsWith("_pickaxe")) return true;
        if (string.endsWith("_shovel")) return true;
        if (string.endsWith("_hoe")) return true;
        if (string.endsWith("_boat")) return true;
        if (string.endsWith("_bucket")) return true;
        if (string.endsWith("horse_armor")) return true;
        switch (material) {
            case BOW:
            case CROSSBOW:
            case SHIELD:
            case TRIDENT:
            case FISHING_ROD:
            case SHEARS:
            case FLINT_AND_STEEL:
            case ELYTRA:
            case CARROT_ON_A_STICK:
            case WARPED_FUNGUS_ON_A_STICK:
            case LEAD:
            case SPYGLASS:
            case FIREWORK_ROCKET:
            case FIRE_CHARGE:
            case GOAT_HORN:
                return true;
            default:
                return false;
        }
    }, "<green>Ausrüstung", DIAMOND_SWORD),
    FOOD(Material::isEdible, "<green>Essen", PUMPKIN_PIE),
    MOB_DROPS(material -> {
        switch (material) {
            case PHANTOM_MEMBRANE:
            case GHAST_TEAR:
            case BLAZE_ROD:
            case MAGMA_CREAM:
            case SLIME_BALL:
            case SPIDER_EYE:
            case STRING:
            case GUNPOWDER:
            case RABBIT_FOOT:
            case RABBIT_HIDE:
            case BONE:
            case FEATHER:
            case LEATHER:
            case PORKCHOP:
            case BEEF:
            case CHICKEN:
            case MUTTON:
            case RABBIT:
            case COD:
            case SALMON:
            case TROPICAL_FISH:
            case PUFFERFISH:
            case GLOW_INK_SAC:
            case INK_SAC:
            case ENDER_PEARL:
            case ENDER_EYE:
            case SHULKER_SHELL:
            case PRISMARINE_SHARD:
            case PRISMARINE_CRYSTALS:
            case SCUTE:
            case NETHER_STAR:
            case WITHER_SKELETON_SKULL:
            case ARROW:
                return true;
            default:
                return false;
        }
    }, "<green>Monster- und Tierdrops", ROTTEN_FLESH),
    MAGIC(material -> {
        switch (material) {
            case ENCHANTED_BOOK:
            case ENCHANTING_TABLE:
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case DRAGON_BREATH:
            case TIPPED_ARROW:
            case EXPERIENCE_BOTTLE:
                return true;
            default:
                return false;
        }
    }, "<green>Magisches und Tränke", ENCHANTING_TABLE),
    MISC(material -> !(BUILDING_BLOCKS.test(material)
            || MINERALS.test(material)
            || EQUIPMENT.test(material)
            || FOOD.test(material)
            || MOB_DROPS.test(material)
            || MAGIC.test(material)),
            "<green>Sonstiges",
            TNT_MINECART);
    private final Predicate<Material> predicate;
    private final ItemStack itemStack;


    Filters(Predicate<Material> predicate, String displayName, Material displayMaterial) {
        this.predicate = predicate;
        ItemStack itemStack = new ItemStack(displayMaterial);
        itemStack.editMeta(meta -> meta.displayName(MiniMessage.miniMessage().deserialize("<!italic>" + displayName)));
        this.itemStack = itemStack;
    }

    public ItemStack displayItem() {
        return itemStack.clone();
    }

    public boolean test(Material material) {
        return predicate.test(material);
    }

    public Predicate<Material> filter() {
        return predicate;
    }
}
