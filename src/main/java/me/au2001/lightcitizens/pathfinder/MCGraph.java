package me.au2001.lightcitizens.pathfinder;

import me.au2001.lightcitizens.pathfinder.Node.Node3D;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.material.*;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Map;

public class MCGraph extends Graph {

    private MCSnapshot snapshot;
    private double height;
    private double maxJump;
    private double maxFall;

    public MCGraph(Location center, double distance) {
        this(center, distance, 2, 1, 3);
    }

    public MCGraph(Location center, double distance, double height, double maxJump, double maxFall) {
        this(new MCSnapshot(center, distance), height, maxJump, maxFall);
    }

    public MCGraph(MCSnapshot snapshot) {
        this(snapshot, 2, 1, 3);
    }

    public MCGraph(MCSnapshot snapshot, double height, double maxJump, double maxFall) {
        this.snapshot = snapshot;
        this.height = height;
        this.maxJump = maxJump;
        this.maxFall = maxFall;
    }

	public void close() {
		snapshot.close();
	}

    public MCSnapshot getSnapshot() {
        return snapshot;
    }

    public Node3D getNode(int x, int y, int z) {
        return new Node3D(x, y, z);
    }

    public boolean isInBound(Node3D node) {
        return snapshot.isInBound(node);
    }

    public boolean canMove(Node from, Node to) {
        if (!(from instanceof Node3D && to instanceof Node3D)) return false;
        if (from == null || to == null) return false;
        if (!isInBound((Node3D) to)) return false;

        int depth = (int) Math.ceil(Math.max(Math.max(maxJump, maxFall), 1));
        BlockFace face = getFace((Node3D) from, (Node3D) to);

        double upperFrom = getUpperHeight(snapshot, (Node3D) from, face, depth);
        if (upperFrom < ((Node3D) from).y) return to.equals(getRelative((Node3D) from, BlockFace.DOWN));

        double upperTo = getUpperHeight(snapshot, (Node3D) to, face.getOppositeFace(), depth);
        double lowerFrom = getLowerHeight(snapshot, (Node3D) from, face, (int) Math.ceil(height + maxJump));
        double lowerTo = getLowerHeight(snapshot, (Node3D) to, face.getOppositeFace(), (int) Math.ceil(height + maxJump));

        if (upperFrom < 0 || lowerFrom < 0 || upperTo < 0 || lowerTo < 0) return false; // An error occured

        // TODO: Horizontal check?

        boolean canMove = true;
        canMove = canMove && (upperTo <= upperFrom + maxJump && upperTo >= upperFrom - maxFall);
        canMove = canMove && (Math.max(upperFrom, upperTo) + height <= Math.min(lowerFrom, lowerTo));
        return canMove;
    }

    public Map<Node, Double> getNeighbors(Node node) {
        Map<Node, Double> neighbors = node.getNeighbors();
        if (!(node instanceof Node3D)) return neighbors;

        for (Node neighbor : new ArrayList<Node>(neighbors.keySet())) {
            if (!isInBound((Node3D) neighbor)) {
                neighbors.remove(neighbor);
                continue;
            }

            if (!canMove(node, neighbor)) {
                neighbors.remove(neighbor);

                for (int y = (int) -Math.ceil(maxFall); y <= maxJump; y++) {
                    if (y == 0) continue;

                    Node3D other = getRelative((Node3D) neighbor, BlockFace.UP, y);
                    if (canMove(node, other)) neighbors.put(other, y + 1.0);
                }
            }
        }

        return neighbors;
    }

    private static BlockFace getFace(Node3D from, Node3D to) {
        Vector vector = new Vector(Math.floor(to.x) - Math.floor(from.x), Math.floor(to.y) - Math.floor(from.y), Math.floor(to.z) - Math.floor(from.z));
        if (vector.lengthSquared() == 0) return BlockFace.SELF;
        vector.normalize();
        double minDist = Double.POSITIVE_INFINITY;
        BlockFace minFace = BlockFace.SELF;
        for (BlockFace face : BlockFace.values()) {
            Vector faceVector = new Vector(face.getModX(), face.getModY(), face.getModZ());
            double dist = faceVector.distanceSquared(vector);
            if (dist < minDist) {
                minDist = dist;
                minFace = face;
            }
        }
        return minFace;
    }

    private static Node3D getRelative(Node3D from, BlockFace face) {
        return getRelative(from, face, 1);
    }

    private static Node3D getRelative(Node3D from, BlockFace face, int distance) {
        return getRelative(from, face.getModX() * distance, face.getModY() * distance, face.getModZ() * distance);
    }

    private static Node3D getRelative(Node3D from, int x, int y, int z) {
        return new Node3D(from.x + x, from.y + y, from.z + z);
    }

    @SuppressWarnings("deprecation")
    public static double getUpperHeight(MCSnapshot snapshot, Node3D node, BlockFace side, int maxHeight) {
        if (!snapshot.isInBound(node)) return Double.POSITIVE_INFINITY;
        if (side == null) side = BlockFace.SELF;

        MaterialData data = snapshot.get(node);
        switch (data != null? data.getItemType() : Material.AIR) {
            // Full
            case STONE:
            case GRASS:
            case DIRT:
            case COBBLESTONE:
            case WOOD:
            case BEDROCK:
            case SAND:
            case GRAVEL:
            case GOLD_ORE:
            case IRON_ORE:
            case COAL_ORE:
            case LOG:
            case LEAVES:
            case SPONGE:
            case GLASS:
            case LAPIS_ORE:
            case LAPIS_BLOCK:
            case DISPENSER:
            case SANDSTONE:
            case NOTE_BLOCK:
            case PISTON_STICKY_BASE:
            case PISTON_BASE:
            case PISTON_EXTENSION:
            case WOOL:
            case PISTON_MOVING_PIECE:
            case GOLD_BLOCK:
            case IRON_BLOCK:
            case DOUBLE_STEP:
            case BRICK:
            case TNT:
            case BOOKSHELF:
            case MOSSY_COBBLESTONE:
            case OBSIDIAN:
            case MOB_SPAWNER:
            case DIAMOND_ORE:
            case DIAMOND_BLOCK:
            case WORKBENCH:
            case FURNACE:
            case BURNING_FURNACE:
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
            case ICE:
            case SNOW_BLOCK:
            case CACTUS:
            case CLAY:
            case JUKEBOX:
            case PUMPKIN:
            case NETHERRACK:
            case GLOWSTONE:
            case JACK_O_LANTERN:
            case STAINED_GLASS:
            case MONSTER_EGGS:
            case SMOOTH_BRICK:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
            case THIN_GLASS:
            case MELON_BLOCK:
            case MYCEL:
            case NETHER_BRICK:
            case CAULDRON:
            case ENDER_STONE:
            case DRAGON_EGG:
            case REDSTONE_LAMP_OFF:
            case REDSTONE_LAMP_ON:
            case WOOD_DOUBLE_STEP:
            case EMERALD_ORE:
            case EMERALD_BLOCK:
            case COMMAND:
            case BEACON:
            case ANVIL:
            case REDSTONE_BLOCK:
            case QUARTZ_ORE:
            case HOPPER:
            case QUARTZ_BLOCK:
            case DROPPER:
            case STAINED_CLAY:
            case STAINED_GLASS_PANE:
            case LEAVES_2:
            case LOG_2:
            case SLIME_BLOCK:
            case BARRIER:
            case PRISMARINE:
            case SEA_LANTERN:
            case HAY_BLOCK:
            case HARD_CLAY:
            case COAL_BLOCK:
            case PACKED_ICE:
            case DOUBLE_STONE_SLAB2:
            case RED_SANDSTONE:
                return node.y + 1;

            // Transparent
            case AIR:
            case SAPLING:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case WEB:
            case LONG_GRASS:
            case DEAD_BUSH:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case TORCH:
            case FIRE:
            case REDSTONE_WIRE:
            case CROPS:
            case SIGN_POST:
            case LADDER:
            case RAILS:
            case WALL_SIGN:
            case LEVER:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case STONE_BUTTON:
            case SUGAR_CANE_BLOCK:
            case PORTAL:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case VINE:
            case NETHER_WARTS:
            case ENDER_PORTAL:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case CARROT:
            case POTATO:
            case WOOD_BUTTON:
            case GOLD_PLATE:
            case IRON_PLATE:
            case ACTIVATOR_RAIL:
            case DOUBLE_PLANT:
            case STANDING_BANNER:
            case WALL_BANNER:
                return maxHeight >= 1? getUpperHeight(snapshot, getRelative(node, BlockFace.DOWN), side, maxHeight - 1) : node.y;

            // Fences/Walls
            case FENCE:
            case IRON_FENCE:
            case NETHER_FENCE:
            case COBBLE_WALL:
            case SPRUCE_FENCE:
            case BIRCH_FENCE:
            case JUNGLE_FENCE:
            case DARK_OAK_FENCE:
            case ACACIA_FENCE:
                return node.y + 1.5;

            // Gates
            case FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case ACACIA_FENCE_GATE:
                if (data instanceof Gate) {
                    Gate gate = (Gate) data;
                    if (gate.isOpen())
                        return maxHeight >= 1? getUpperHeight(snapshot, getRelative(node, BlockFace.DOWN), side, maxHeight - 1) : node.y;
                }
                return node.y + 1.5;

            // Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                // TODO
                if (data instanceof Door) {
                    Door door = (Door) data;
                    if (door.isTopHalf()) return node.y + 2;
                }
                return node.y + 1;

            // Slabs
            case STEP:
            case WOOD_STEP:
            case STONE_SLAB2:
                if (data instanceof Step) {
                    Step step = (Step) data;
                    if (step.isInverted()) return node.y + 1;
                }
                return node.y + 0.5;

            // Stairs
            case WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case RED_SANDSTONE_STAIRS:
                if (data instanceof Stairs) {
                    Stairs stairs = (Stairs) data;
                    if (!stairs.isInverted() && stairs.getDescendingDirection().equals(side)) return node.y + 0.5;
                }
                return node.y + 1;

            // Trapdoors
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
                if (data instanceof TrapDoor) {
                    TrapDoor trapdoor = (TrapDoor) data;
                    if (trapdoor.isInverted() || trapdoor.isOpen()) return node.y + 1;
                }
                return node.y + 0.1875;

            // Other
            case BED_BLOCK:
                return node.y + 0.5625;
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
                return node.y + 0.875;
            case SNOW:
                return node.y + data.getData() * 0.125;
            case SOIL:
                return node.y + 0.9375;
            case SOUL_SAND:
                return node.y + 0.875;
            case CAKE_BLOCK:
                return node.y + 0.5;
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
                return node.y + 0.125;
            case WATER_LILY:
                return node.y + 0.09375;
            case ENCHANTMENT_TABLE:
                return node.y + 0.75;
            case ENDER_PORTAL_FRAME:
                return node.y + 0.8125;
            case BREWING_STAND:
                return node.y + 0.875;
            case COCOA:
                return node.y + 0.75;
            case FLOWER_POT:
                return node.y + 0.375;
            case SKULL:
                if (data instanceof Skull) {
                    Skull skull = (Skull) data;
                    if (!skull.getFacing().equals(BlockFace.UP)) return node.y + 0.75;
                }
                return node.y + 0.5;
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
                return node.y + 0.375;
            case CARPET:
                return node.y + 0.0625;

            // Items
            case IRON_SPADE:
            case IRON_PICKAXE:
            case IRON_AXE:
            case FLINT_AND_STEEL:
            case APPLE:
            case BOW:
            case ARROW:
            case COAL:
            case DIAMOND:
            case IRON_INGOT:
            case GOLD_INGOT:
            case IRON_SWORD:
            case WOOD_SWORD:
            case WOOD_SPADE:
            case WOOD_PICKAXE:
            case WOOD_AXE:
            case STONE_SWORD:
            case STONE_SPADE:
            case STONE_PICKAXE:
            case STONE_AXE:
            case DIAMOND_SWORD:
            case DIAMOND_SPADE:
            case DIAMOND_PICKAXE:
            case DIAMOND_AXE:
            case STICK:
            case BOWL:
            case MUSHROOM_SOUP:
            case GOLD_SWORD:
            case GOLD_SPADE:
            case GOLD_PICKAXE:
            case GOLD_AXE:
            case STRING:
            case FEATHER:
            case SULPHUR:
            case WOOD_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case DIAMOND_HOE:
            case GOLD_HOE:
            case SEEDS:
            case WHEAT:
            case BREAD:
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLD_HELMET:
            case GOLD_CHESTPLATE:
            case GOLD_LEGGINGS:
            case GOLD_BOOTS:
            case FLINT:
            case PORK:
            case GRILLED_PORK:
            case PAINTING:
            case GOLDEN_APPLE:
            case SIGN:
            case WOOD_DOOR:
            case BUCKET:
            case WATER_BUCKET:
            case LAVA_BUCKET:
            case MINECART:
            case SADDLE:
            case IRON_DOOR:
            case REDSTONE:
            case SNOW_BALL:
            case BOAT:
            case LEATHER:
            case MILK_BUCKET:
            case CLAY_BRICK:
            case CLAY_BALL:
            case SUGAR_CANE:
            case PAPER:
            case BOOK:
            case SLIME_BALL:
            case STORAGE_MINECART:
            case POWERED_MINECART:
            case EGG:
            case COMPASS:
            case FISHING_ROD:
            case WATCH:
            case GLOWSTONE_DUST:
            case RAW_FISH:
            case COOKED_FISH:
            case INK_SACK:
            case BONE:
            case SUGAR:
            case CAKE:
            case BED:
            case DIODE:
            case COOKIE:
            case MAP:
            case SHEARS:
            case MELON:
            case PUMPKIN_SEEDS:
            case MELON_SEEDS:
            case RAW_BEEF:
            case COOKED_BEEF:
            case RAW_CHICKEN:
            case COOKED_CHICKEN:
            case ROTTEN_FLESH:
            case ENDER_PEARL:
            case BLAZE_ROD:
            case GHAST_TEAR:
            case GOLD_NUGGET:
            case NETHER_STALK:
            case POTION:
            case GLASS_BOTTLE:
            case SPIDER_EYE:
            case FERMENTED_SPIDER_EYE:
            case BLAZE_POWDER:
            case MAGMA_CREAM:
            case BREWING_STAND_ITEM:
            case CAULDRON_ITEM:
            case EYE_OF_ENDER:
            case SPECKLED_MELON:
            case MONSTER_EGG:
            case EXP_BOTTLE:
            case FIREBALL:
            case BOOK_AND_QUILL:
            case WRITTEN_BOOK:
            case EMERALD:
            case ITEM_FRAME:
            case FLOWER_POT_ITEM:
            case CARROT_ITEM:
            case POTATO_ITEM:
            case BAKED_POTATO:
            case POISONOUS_POTATO:
            case EMPTY_MAP:
            case GOLDEN_CARROT:
            case SKULL_ITEM:
            case CARROT_STICK:
            case NETHER_STAR:
            case PUMPKIN_PIE:
            case FIREWORK:
            case FIREWORK_CHARGE:
            case ENCHANTED_BOOK:
            case REDSTONE_COMPARATOR:
            case NETHER_BRICK_ITEM:
            case QUARTZ:
            case EXPLOSIVE_MINECART:
            case HOPPER_MINECART:
            case PRISMARINE_SHARD:
            case PRISMARINE_CRYSTALS:
            case RABBIT:
            case COOKED_RABBIT:
            case RABBIT_STEW:
            case RABBIT_FOOT:
            case RABBIT_HIDE:
            case ARMOR_STAND:
            case IRON_BARDING:
            case GOLD_BARDING:
            case DIAMOND_BARDING:
            case LEASH:
            case NAME_TAG:
            case COMMAND_MINECART:
            case MUTTON:
            case COOKED_MUTTON:
            case BANNER:
            case SPRUCE_DOOR_ITEM:
            case BIRCH_DOOR_ITEM:
            case JUNGLE_DOOR_ITEM:
            case ACACIA_DOOR_ITEM:
            case DARK_OAK_DOOR_ITEM:
            case GOLD_RECORD:
            case GREEN_RECORD:
            case RECORD_3:
            case RECORD_4:
            case RECORD_5:
            case RECORD_6:
            case RECORD_7:
            case RECORD_8:
            case RECORD_9:
            case RECORD_10:
            case RECORD_11:
            case RECORD_12:
            default:
                return -1;
        }
    }

    public static double getLowerHeight(MCSnapshot snapshot, Node3D node, BlockFace side, int maxHeight) {
        if (!snapshot.isInBound(node)) return Double.POSITIVE_INFINITY;
        if (side == null) side = BlockFace.SELF;

        MaterialData data = snapshot.get(node);
        switch (data != null? data.getItemType() : Material.AIR) {
            // Full
            case STONE:
            case GRASS:
            case DIRT:
            case COBBLESTONE:
            case WOOD:
            case BEDROCK:
            case SAND:
            case GRAVEL:
            case GOLD_ORE:
            case IRON_ORE:
            case COAL_ORE:
            case LOG:
            case LEAVES:
            case SPONGE:
            case GLASS:
            case LAPIS_ORE:
            case LAPIS_BLOCK:
            case DISPENSER:
            case SANDSTONE:
            case NOTE_BLOCK:
            case PISTON_STICKY_BASE:
            case PISTON_BASE:
            case PISTON_EXTENSION:
            case WOOL:
            case PISTON_MOVING_PIECE:
            case GOLD_BLOCK:
            case IRON_BLOCK:
            case DOUBLE_STEP:
            case BRICK:
            case TNT:
            case BOOKSHELF:
            case MOSSY_COBBLESTONE:
            case OBSIDIAN:
            case MOB_SPAWNER:
            case DIAMOND_ORE:
            case DIAMOND_BLOCK:
            case WORKBENCH:
            case FURNACE:
            case BURNING_FURNACE:
            case REDSTONE_ORE:
            case GLOWING_REDSTONE_ORE:
            case ICE:
            case SNOW_BLOCK:
            case CACTUS:
            case CLAY:
            case JUKEBOX:
            case PUMPKIN:
            case NETHERRACK:
            case GLOWSTONE:
            case JACK_O_LANTERN:
            case STAINED_GLASS:
            case MONSTER_EGGS:
            case SMOOTH_BRICK:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
            case THIN_GLASS:
            case MELON_BLOCK:
            case MYCEL:
            case NETHER_BRICK:
            case CAULDRON:
            case ENDER_STONE:
            case DRAGON_EGG:
            case REDSTONE_LAMP_OFF:
            case REDSTONE_LAMP_ON:
            case WOOD_DOUBLE_STEP:
            case EMERALD_ORE:
            case EMERALD_BLOCK:
            case COMMAND:
            case BEACON:
            case ANVIL:
            case REDSTONE_BLOCK:
            case QUARTZ_ORE:
            case HOPPER:
            case QUARTZ_BLOCK:
            case DROPPER:
            case STAINED_CLAY:
            case STAINED_GLASS_PANE:
            case LEAVES_2:
            case LOG_2:
            case SLIME_BLOCK:
            case BARRIER:
            case PRISMARINE:
            case SEA_LANTERN:
            case HAY_BLOCK:
            case HARD_CLAY:
            case COAL_BLOCK:
            case PACKED_ICE:
            case DOUBLE_STONE_SLAB2:
            case RED_SANDSTONE:
                return node.y;

            // Transparent
            case AIR:
            case SAPLING:
            case WATER:
            case STATIONARY_WATER:
            case LAVA:
            case STATIONARY_LAVA:
            case POWERED_RAIL:
            case DETECTOR_RAIL:
            case WEB:
            case LONG_GRASS:
            case DEAD_BUSH:
            case YELLOW_FLOWER:
            case RED_ROSE:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case TORCH:
            case FIRE:
            case REDSTONE_WIRE:
            case CROPS:
            case SIGN_POST:
            case LADDER:
            case RAILS:
            case WALL_SIGN:
            case LEVER:
            case STONE_PLATE:
            case WOOD_PLATE:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_TORCH_ON:
            case STONE_BUTTON:
            case SUGAR_CANE_BLOCK:
            case PORTAL:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case VINE:
            case NETHER_WARTS:
            case ENDER_PORTAL:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case CARROT:
            case POTATO:
            case WOOD_BUTTON:
            case GOLD_PLATE:
            case IRON_PLATE:
            case ACTIVATOR_RAIL:
            case DOUBLE_PLANT:
            case STANDING_BANNER:
            case WALL_BANNER:
                return maxHeight >= 1? getLowerHeight(snapshot, getRelative(node, BlockFace.UP), side, maxHeight - 1) : node.y + 1;

            // Fences/Walls
            case FENCE:
            case IRON_FENCE:
            case NETHER_FENCE:
            case COBBLE_WALL:
            case SPRUCE_FENCE:
            case BIRCH_FENCE:
            case JUNGLE_FENCE:
            case DARK_OAK_FENCE:
            case ACACIA_FENCE:
                return node.y;

            // Gates
            case FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case ACACIA_FENCE_GATE:
                if (data instanceof Gate) {
                    Gate gate = (Gate) data;
                    if (gate.isOpen())
                        return maxHeight >= 1? getLowerHeight(snapshot, getRelative(node, BlockFace.UP), side, maxHeight - 1) : node.y + 1;
                }
                return node.y;

            // Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                if (data instanceof Door) {
                    Door door = (Door) data;
                    BlockFace facing = door.getFacing();
                    if (door.isOpen()) {
                        switch (facing) {
                            case NORTH:
                                facing = door.getHinge()? BlockFace.WEST : BlockFace.EAST;
                                break;

                            case SOUTH:
                                facing = door.getHinge()? BlockFace.EAST : BlockFace.WEST;
                                break;

                            case EAST:
                                facing = door.getHinge()? BlockFace.NORTH : BlockFace.SOUTH;
                                break;

                            case WEST:
                                facing = door.getHinge()? BlockFace.SOUTH : BlockFace.NORTH;
                                break;
                        }
                    }
                    if (facing.equals(side)) return node.y;
                    if (!door.isTopHalf())
                        return maxHeight >= 2? getLowerHeight(snapshot, getRelative(node, BlockFace.UP, 2), side, maxHeight - 2) : node.y + 2;
                }
                return maxHeight >= 1? getLowerHeight(snapshot, getRelative(node, BlockFace.UP), side, maxHeight - 1) : node.y + 1;

            // Slabs
            case STEP:
            case WOOD_STEP:
            case STONE_SLAB2:
                if (data instanceof Step) {
                    Step step = (Step) data;
                    if (step.isInverted()) return node.y + 0.5;
                }
                return node.y;

            // Stairs
            case WOOD_STAIRS:
            case COBBLESTONE_STAIRS:
            case BRICK_STAIRS:
            case SMOOTH_STAIRS:
            case NETHER_BRICK_STAIRS:
            case SANDSTONE_STAIRS:
            case SPRUCE_WOOD_STAIRS:
            case BIRCH_WOOD_STAIRS:
            case JUNGLE_WOOD_STAIRS:
            case QUARTZ_STAIRS:
            case ACACIA_STAIRS:
            case DARK_OAK_STAIRS:
            case RED_SANDSTONE_STAIRS:
                if (data instanceof Stairs) {
                    Stairs stairs = (Stairs) data;
                    if (stairs.isInverted() && stairs.getDescendingDirection().equals(side)) return node.y + 0.5;
                }
                return node.y;

            // Trapdoors
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
                if (data instanceof TrapDoor) {
                    TrapDoor trapdoor = (TrapDoor) data;
                    if (trapdoor.isOpen()) {
                        if (trapdoor.getAttachedFace().equals(side)) return node.y;
                        return maxHeight >= 1? getLowerHeight(snapshot, getRelative(node, BlockFace.UP), side, maxHeight - 1) : node.y + 1;
                    }
                    if (trapdoor.isInverted()) return node.y + 0.8125;
                }
                return node.y;

            // Other
            case BED_BLOCK:
                return node.y;
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
                return node.y;
            case SNOW:
                return node.y;
            case SOIL:
                return node.y;
            case SOUL_SAND:
                return node.y;
            case CAKE_BLOCK:
                return node.y;
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
                return node.y;
            case WATER_LILY:
                return node.y;
            case ENCHANTMENT_TABLE:
                return node.y;
            case ENDER_PORTAL_FRAME:
                return node.y;
            case BREWING_STAND:
                return node.y;
            case COCOA:
                if (data instanceof CocoaPlant) {
                    CocoaPlant cocoa = (CocoaPlant) data;
                    if (cocoa.getFacing().equals(side))
                        return maxHeight >= 1? getLowerHeight(snapshot, getRelative(node, BlockFace.UP), side, maxHeight - 1) : node.y + 1;
                }
                return node.y + 0.25;
            case FLOWER_POT:
                return node.y;
            case SKULL:
                if (data instanceof Skull) {
                    Skull skull = (Skull) data;
                    if (skull.getFacing().equals(side))
                        return maxHeight >= 1? getLowerHeight(snapshot, getRelative(node, BlockFace.UP), side, maxHeight - 1) : node.y + 1;
                    if (!skull.getFacing().equals(BlockFace.UP)) return node.y + 0.25;
                }
                return node.y;
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
                return node.y;
            case CARPET:
                return node.y;

            // Items
            case IRON_SPADE:
            case IRON_PICKAXE:
            case IRON_AXE:
            case FLINT_AND_STEEL:
            case APPLE:
            case BOW:
            case ARROW:
            case COAL:
            case DIAMOND:
            case IRON_INGOT:
            case GOLD_INGOT:
            case IRON_SWORD:
            case WOOD_SWORD:
            case WOOD_SPADE:
            case WOOD_PICKAXE:
            case WOOD_AXE:
            case STONE_SWORD:
            case STONE_SPADE:
            case STONE_PICKAXE:
            case STONE_AXE:
            case DIAMOND_SWORD:
            case DIAMOND_SPADE:
            case DIAMOND_PICKAXE:
            case DIAMOND_AXE:
            case STICK:
            case BOWL:
            case MUSHROOM_SOUP:
            case GOLD_SWORD:
            case GOLD_SPADE:
            case GOLD_PICKAXE:
            case GOLD_AXE:
            case STRING:
            case FEATHER:
            case SULPHUR:
            case WOOD_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case DIAMOND_HOE:
            case GOLD_HOE:
            case SEEDS:
            case WHEAT:
            case BREAD:
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case CHAINMAIL_BOOTS:
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
            case GOLD_HELMET:
            case GOLD_CHESTPLATE:
            case GOLD_LEGGINGS:
            case GOLD_BOOTS:
            case FLINT:
            case PORK:
            case GRILLED_PORK:
            case PAINTING:
            case GOLDEN_APPLE:
            case SIGN:
            case WOOD_DOOR:
            case BUCKET:
            case WATER_BUCKET:
            case LAVA_BUCKET:
            case MINECART:
            case SADDLE:
            case IRON_DOOR:
            case REDSTONE:
            case SNOW_BALL:
            case BOAT:
            case LEATHER:
            case MILK_BUCKET:
            case CLAY_BRICK:
            case CLAY_BALL:
            case SUGAR_CANE:
            case PAPER:
            case BOOK:
            case SLIME_BALL:
            case STORAGE_MINECART:
            case POWERED_MINECART:
            case EGG:
            case COMPASS:
            case FISHING_ROD:
            case WATCH:
            case GLOWSTONE_DUST:
            case RAW_FISH:
            case COOKED_FISH:
            case INK_SACK:
            case BONE:
            case SUGAR:
            case CAKE:
            case BED:
            case DIODE:
            case COOKIE:
            case MAP:
            case SHEARS:
            case MELON:
            case PUMPKIN_SEEDS:
            case MELON_SEEDS:
            case RAW_BEEF:
            case COOKED_BEEF:
            case RAW_CHICKEN:
            case COOKED_CHICKEN:
            case ROTTEN_FLESH:
            case ENDER_PEARL:
            case BLAZE_ROD:
            case GHAST_TEAR:
            case GOLD_NUGGET:
            case NETHER_STALK:
            case POTION:
            case GLASS_BOTTLE:
            case SPIDER_EYE:
            case FERMENTED_SPIDER_EYE:
            case BLAZE_POWDER:
            case MAGMA_CREAM:
            case BREWING_STAND_ITEM:
            case CAULDRON_ITEM:
            case EYE_OF_ENDER:
            case SPECKLED_MELON:
            case MONSTER_EGG:
            case EXP_BOTTLE:
            case FIREBALL:
            case BOOK_AND_QUILL:
            case WRITTEN_BOOK:
            case EMERALD:
            case ITEM_FRAME:
            case FLOWER_POT_ITEM:
            case CARROT_ITEM:
            case POTATO_ITEM:
            case BAKED_POTATO:
            case POISONOUS_POTATO:
            case EMPTY_MAP:
            case GOLDEN_CARROT:
            case SKULL_ITEM:
            case CARROT_STICK:
            case NETHER_STAR:
            case PUMPKIN_PIE:
            case FIREWORK:
            case FIREWORK_CHARGE:
            case ENCHANTED_BOOK:
            case REDSTONE_COMPARATOR:
            case NETHER_BRICK_ITEM:
            case QUARTZ:
            case EXPLOSIVE_MINECART:
            case HOPPER_MINECART:
            case PRISMARINE_SHARD:
            case PRISMARINE_CRYSTALS:
            case RABBIT:
            case COOKED_RABBIT:
            case RABBIT_STEW:
            case RABBIT_FOOT:
            case RABBIT_HIDE:
            case ARMOR_STAND:
            case IRON_BARDING:
            case GOLD_BARDING:
            case DIAMOND_BARDING:
            case LEASH:
            case NAME_TAG:
            case COMMAND_MINECART:
            case MUTTON:
            case COOKED_MUTTON:
            case BANNER:
            case SPRUCE_DOOR_ITEM:
            case BIRCH_DOOR_ITEM:
            case JUNGLE_DOOR_ITEM:
            case ACACIA_DOOR_ITEM:
            case DARK_OAK_DOOR_ITEM:
            case GOLD_RECORD:
            case GREEN_RECORD:
            case RECORD_3:
            case RECORD_4:
            case RECORD_5:
            case RECORD_6:
            case RECORD_7:
            case RECORD_8:
            case RECORD_9:
            case RECORD_10:
            case RECORD_11:
            case RECORD_12:
            default:
                return -1;
        }
    }
}
