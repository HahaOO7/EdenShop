package at.haha007.edenshop;

import org.bukkit.Material;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.ItemStack;

public class MaterialFilter {

    enum Filter {
        ALL, BUILDING, GEAR, ORES, FOOD, LOOT, OTHER, MAGIC
    }

    public boolean check(ItemStack i) {

        if (check(i, Filter.GEAR)) return false;
        if (check(i, Filter.BUILDING)) return false;
        if (check(i, Filter.ORES)) return false;
        if (check(i, Filter.FOOD)) return false;
        if (check(i, Filter.LOOT)) return false;
        if (check(i, Filter.MAGIC)) return false;

        return true;

    }

    public boolean check(ItemStack i, Filter filter) {

        Material mat = i.getType();

        switch (filter) {
            case ALL:
                return true;

            case ORES:
                switch (mat) {
                    case IRON_ORE:

                    case LAPIS_LAZULI:
                    case EMERALD_BLOCK:
                    case EMERALD_ORE:
                    case EMERALD:
                    case NETHER_QUARTZ_ORE:
                    case QUARTZ:
                    case DIAMOND_BLOCK:
                    case COAL_BLOCK:
                    case REDSTONE_BLOCK:
                    case GOLD_BLOCK:
                    case IRON_BLOCK:
                    case LAPIS_BLOCK:
                    case LAPIS_ORE:
                    case REDSTONE_ORE:
                    case REDSTONE:
                    case COAL_ORE:
                    case COAL:
                    case DIAMOND_ORE:
                    case DIAMOND:
                    case GOLD_INGOT:
                    case GOLD_NUGGET:
                    case GOLD_ORE:
                    case RAW_GOLD:
                    case RAW_GOLD_BLOCK:
                    case RAW_IRON:
                    case RAW_IRON_BLOCK:
                    case IRON_INGOT:
                        return true;

                    default:
                        if (mat.toString().contains("COPPER"))
                            return true;
                        return false;
                }

            case GEAR:
                switch (mat) {
                    case SHIELD:
                    case ELYTRA:
                    case TIPPED_ARROW:
                    case SHEARS:
                    case FISHING_ROD:
                    case ARROW:
                    case BOW:
                        return true;
                    default:
                        if (mat.toString().contains("SWORD")) return true;
                        if (mat.toString().contains("HELMET")) return true;
                        if (mat.toString().contains("LEGGINGS")) return true;
                        if (mat.toString().contains("CHESTPLATE")) return true;
                        if (mat.toString().contains("BOOTS")) return true;
                        if (mat.toString().endsWith("AXE")) return true;
                        if (mat.toString().contains("HOE")) return true;
                        if (mat.toString().contains("SPADE")) return true;
                        return false;
                }

            case FOOD:
                if (mat.getCreativeCategory() == CreativeCategory.FOOD) return true;
                return false;

            case LOOT:
                switch (mat) {
                    case BONE:
                    case INK_SAC:
                    case BONE_MEAL:
                    case ZOMBIE_HEAD:
                    case DRAGON_HEAD:
                    case CREEPER_HEAD:
                    case WITHER_SKELETON_SKULL:
                    case SKELETON_SKULL:
                    case RABBIT_HIDE:
                    case RABBIT_FOOT:
                    case FEATHER:
                    case LEATHER:
                    case SLIME_BALL:
                    case ENDER_EYE:
                    case ENDER_PEARL:
                    case MAGMA_CREAM:
                    case STRING:
                    case GHAST_TEAR:
                    case BLAZE_POWDER:
                    case BLAZE_ROD:
                    case GUNPOWDER:
                    case ROTTEN_FLESH:
                    case FERMENTED_SPIDER_EYE:
                    case SPIDER_EYE:
                        return true;

                    default:
                        return false;
                }

            case MAGIC: {
                switch (mat) {
                    case POTION:
                    case DRAGON_BREATH:
                    case BREWING_STAND:
                    case ENCHANTING_TABLE:
                    case ENCHANTED_BOOK:
                    case SPLASH_POTION:
                    case LINGERING_POTION:
                        return true;
                    default:
                        return false;
                }
            }

            case BUILDING:
                if (mat.getCreativeCategory() == CreativeCategory.BUILDING_BLOCKS) return true;
                if (mat.toString().contains("DOOR")) return true;
                if (mat.toString().contains("FENCE")) return true;
                if (mat.toString().contains("LEAVES")) return true;
                return false;
            default:
                return false;
        }

    }

}
