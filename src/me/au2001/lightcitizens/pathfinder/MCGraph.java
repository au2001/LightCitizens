package me.au2001.lightcitizens.pathfinder;

import me.au2001.lightcitizens.pathfinder.Node.Node3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.*;

import java.util.ArrayList;
import java.util.Map;

public class MCGraph extends Graph {

    private World world;
    private Location center;
    private int distanceSquared;
    private double height;
    private double maxJump;
    private double maxFall;

    public MCGraph(World world) {
        this(world, 2, 1, 3);
    }

    public MCGraph(World world, double height, double maxJump, double maxFall) {
        this(world.getSpawnLocation(), -1, height, maxJump, maxFall);
    }

    public MCGraph(Location center, int distanceSquared) {
        this(center, distanceSquared, 2, 1, 3);
    }

    public MCGraph(Location center, int distanceSquared, double height, double maxJump, double maxFall) {
        this.world = center.getWorld();
        this.center = center;
        this.distanceSquared = distanceSquared;
        this.height = height;
        this.maxJump = maxJump;
        this.maxFall = maxFall;
    }

    public World getWorld() {
        return world;
    }

    public Node3D getNode(Location location) {
        return getNode(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public Node3D getNode(Block block) {
        return getNode(block.getX(), block.getY(), block.getZ());
    }

    public Node3D getNode(int x, int y, int z) {
        return new Node3D(x, y, z);
    }

    public Block getBlock(Node3D node) {
        int x = (int) Math.floor(node.x);
        int y = (int) Math.floor(node.y);
        int z = (int) Math.floor(node.z);
        if (!world.isChunkLoaded(Math.floorDiv(x, 16), Math.floorDiv(z, 16)))
            return null;

        return world.getBlockAt(x, y, z);
//        if (Bukkit.isPrimaryThread()) return world.getBlockAt(x, y, z);
//
//        AtomicReference<Block> block = new AtomicReference<Block>(null);
//        AtomicBoolean available = new AtomicBoolean(false);
//        new BukkitRunnable() {
//            public void run() {
//                block.set(world.getBlockAt(x, y, z));
//                available.set(true);
//            }
//        }.runTask(LightCitizens.getInstance());
//
//        while (!available.get()) {
//            try {
//                Thread.sleep(1);
//            } catch (InterruptedException e) {}
//        }
//        return block.get();
    }

    public Location getLocation(Node3D node) {
        return new Location(world, node.x + 0.5, node.y, node.z + 0.5);
    }

    public boolean isInBound(Node3D node) {
        if (distanceSquared < 0) return true;
        int distX = (int) Math.pow(node.x - center.getBlockX(), 2);
        int distZ = (int) Math.pow(node.z - center.getBlockZ(), 2);
        return distX + distZ <= distanceSquared && getLocation(node).getChunk().isLoaded();
    }

    public boolean isInBound(Block block) {
        if (block == null) return false;
        if (distanceSquared < 0) return true;
        int distX = (int) Math.pow(block.getX() - center.getBlockX(), 2);
        int distZ = (int) Math.pow(block.getZ() - center.getBlockZ(), 2);
        return distX + distZ <= distanceSquared && block.getChunk().isLoaded();
    }

    public boolean canMove(Node from, Node to) {
        if (!(from instanceof Node3D && to instanceof Node3D)) return false;
        return canMove(getBlock((Node3D) from), getBlock((Node3D) to));
    }

    public boolean canMove(Block from, Block to) {
        if (from == null || to == null) return false;
        if (!isInBound(to)) return false;

        int depth = (int) Math.ceil(Math.max(Math.max(maxJump, maxFall), 1));

        double upperFrom = getUpperHeight(from, from.getFace(to), depth);
        if (upperFrom < from.getY()) return to.equals(from.getRelative(BlockFace.DOWN));

        double upperTo = getUpperHeight(to, to.getFace(from), depth);
        double lowerFrom = getLowerHeight(from, from.getFace(to), (int) Math.ceil(height + maxJump));
        double lowerTo = getLowerHeight(to, to.getFace(from), (int) Math.ceil(height + maxJump));

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

        Block from = getBlock((Node3D) node);
        if (from == null) return neighbors;
//        neighbors.put(new Node3D(((Node3D) node).x - 1, ((Node3D) node).y, ((Node3D) node).z), 1.0);
//        neighbors.put(new Node3D(((Node3D) node).x + 1, ((Node3D) node).y, ((Node3D) node).z), 1.0);
//        neighbors.put(new Node3D(((Node3D) node).x, ((Node3D) node).y, ((Node3D) node).z - 1), 1.0);
//        neighbors.put(new Node3D(((Node3D) node).x, ((Node3D) node).y, ((Node3D) node).z + 1), 1.0);
//        neighbors.put(new Node3D(((Node3D) node).x, ((Node3D) node).y - 1, ((Node3D) node).z), 1.0);
//        neighbors.put(new Node3D(((Node3D) node).x, ((Node3D) node).y + 1, ((Node3D) node).z), 1.0);

        for (Node neighbor : new ArrayList<Node>(neighbors.keySet())) {
            Block block = getBlock((Node3D) neighbor);
            if (block == null) continue;

            if (!isInBound(block)) {
                neighbors.remove(neighbor);
                continue;
            }

            if (!canMove(from, block)) {
                neighbors.remove(neighbor);

                for (int y = (int) -Math.ceil(maxFall); y <= maxJump; y++) {
                    if (y == 0) continue;

                    Block other = block.getRelative(0, y, 0);
                    if (canMove(from, other)) neighbors.put(getNode(other), y + 1.0);
                }
            }
        }

        return neighbors;
    }

    @SuppressWarnings("deprecation")
    private double getUpperHeight(Block block, BlockFace side, int maxHeight) {
        if (block == null || block.getY() > world.getMaxHeight() || block.getY() < 0) return Double.POSITIVE_INFINITY;
        if (side == null) side = BlockFace.SELF;

        double y = block.getY();
        switch (block.getType()) {
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
                return y + 1;

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
                return maxHeight >= 1? getUpperHeight(block.getRelative(BlockFace.DOWN), side, maxHeight - 1) : y;

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
                return y + 1.5;

            // Gates
            case FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case ACACIA_FENCE_GATE:
                if (block.getState().getData() instanceof Gate) {
                    Gate gate = (Gate) block.getState().getData();
                    if (gate.isOpen())
                        return maxHeight >= 1? getUpperHeight(block.getRelative(BlockFace.DOWN), side, maxHeight - 1) : y;
                }
                return y + 1.5;

            // Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                // TODO
                if (block.getState().getData() instanceof Door) {
                    Door door = (Door) block.getState().getData();
                    if (door.isTopHalf()) return y + 2;
                }
                return y + 1;

            // Slabs
            case STEP:
            case WOOD_STEP:
            case STONE_SLAB2:
                if (block.getState().getData() instanceof Step) {
                    Step step = (Step) block.getState().getData();
                    if (step.isInverted()) return y + 1;
                }
                return y + 0.5;

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
                if (block.getState().getData() instanceof Stairs) {
                    Stairs stairs = (Stairs) block.getState().getData();
                    if (!stairs.isInverted() && stairs.getDescendingDirection().equals(side)) return y + 0.5;
                }
                return y + 1;

            // Trapdoors
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
                if (block.getState().getData() instanceof TrapDoor) {
                    TrapDoor trapdoor = (TrapDoor) block.getState().getData();
                    if (trapdoor.isInverted() || trapdoor.isOpen()) return y + 1;
                }
                return y + 0.1875;

            // Other
            case BED_BLOCK:
                return y + 0.5625;
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
                return y + 0.875;
            case SNOW:
                return y + block.getState().getRawData() * 0.125;
            case SOIL:
                return y + 0.9375;
            case SOUL_SAND:
                return y + 0.875;
            case CAKE_BLOCK:
                return y + 0.5;
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
                return y + 0.125;
            case WATER_LILY:
                return y + 0.09375;
            case ENCHANTMENT_TABLE:
                return y + 0.75;
            case ENDER_PORTAL_FRAME:
                return y + 0.8125;
            case BREWING_STAND:
                return y + 0.875;
            case COCOA:
                return y + 0.75;
            case FLOWER_POT:
                return y + 0.375;
            case SKULL:
                if (block.getState().getData() instanceof Skull) {
                    Skull skull = (Skull) block.getState().getData();
                    if (!skull.getFacing().equals(BlockFace.UP)) return y + 0.75;
                }
                return y + 0.5;
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
                return y + 0.375;
            case CARPET:
                return y + 0.0625;

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

    private double getLowerHeight(Block block, BlockFace side, int maxHeight) {
        if (block == null || block.getY() > world.getMaxHeight() || block.getY() < 0) return Double.NEGATIVE_INFINITY;
        if (side == null) side = BlockFace.SELF;
        double y = block.getY();

        switch (block.getType()) {
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
                return y;

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
                return maxHeight >= 1? getLowerHeight(block.getRelative(BlockFace.UP), side, maxHeight - 1) : y + 1;

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
                return y;

            // Gates
            case FENCE_GATE:
            case SPRUCE_FENCE_GATE:
            case BIRCH_FENCE_GATE:
            case JUNGLE_FENCE_GATE:
            case DARK_OAK_FENCE_GATE:
            case ACACIA_FENCE_GATE:
                if (block.getState().getData() instanceof Gate) {
                    Gate gate = (Gate) block.getState().getData();
                    if (gate.isOpen())
                        return maxHeight >= 1? getLowerHeight(block.getRelative(BlockFace.UP), side, maxHeight - 1) : y + 1;
                }
                return y;

            // Doors
            case WOODEN_DOOR:
            case IRON_DOOR_BLOCK:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
                if (block.getState().getData() instanceof Door) {
                    Door door = (Door) block.getState().getData();
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
                    if (facing.equals(side)) return y;
                    if (!door.isTopHalf())
                        return maxHeight >= 2? getLowerHeight(block.getRelative(BlockFace.UP, 2), side, maxHeight - 2) : y + 2;
                }
                return maxHeight >= 1? getLowerHeight(block.getRelative(BlockFace.UP), side, maxHeight - 1) : y + 1;

            // Slabs
            case STEP:
            case WOOD_STEP:
            case STONE_SLAB2:
                if (block.getState().getData() instanceof Step) {
                    Step step = (Step) block.getState().getData();
                    if (step.isInverted()) return y + 0.5;
                }
                return y;

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
                if (block.getState().getData() instanceof Stairs) {
                    Stairs stairs = (Stairs) block.getState().getData();
                    if (stairs.isInverted() && stairs.getDescendingDirection().equals(side)) return y + 0.5;
                }
                return y;

            // Trapdoors
            case TRAP_DOOR:
            case IRON_TRAPDOOR:
                if (block.getState().getData() instanceof TrapDoor) {
                    TrapDoor trapdoor = (TrapDoor) block.getState().getData();
                    if (trapdoor.isOpen()) {
                        if (trapdoor.getAttachedFace().equals(side)) return y;
                        return maxHeight >= 1? getLowerHeight(block.getRelative(BlockFace.UP), side, maxHeight - 1) : y + 1;
                    }
                    if (trapdoor.isInverted()) return y + 0.8125;
                }
                return y;

            // Other
            case BED_BLOCK:
                return y;
            case CHEST:
            case ENDER_CHEST:
            case TRAPPED_CHEST:
                return y;
            case SNOW:
                return y;
            case SOIL:
                return y;
            case SOUL_SAND:
                return y;
            case CAKE_BLOCK:
                return y;
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case REDSTONE_COMPARATOR_OFF:
            case REDSTONE_COMPARATOR_ON:
                return y;
            case WATER_LILY:
                return y;
            case ENCHANTMENT_TABLE:
                return y;
            case ENDER_PORTAL_FRAME:
                return y;
            case BREWING_STAND:
                return y;
            case COCOA:
                if (block.getState().getData() instanceof CocoaPlant) {
                    CocoaPlant cocoa = (CocoaPlant) block.getState().getData();
                    if (cocoa.getFacing().equals(side))
                        return maxHeight >= 1? getLowerHeight(block.getRelative(BlockFace.UP), side, maxHeight - 1) : y + 1;
                }
                return y + 0.25;
            case FLOWER_POT:
                return y;
            case SKULL:
                if (block.getState().getData() instanceof Skull) {
                    Skull skull = (Skull) block.getState().getData();
                    if (skull.getFacing().equals(side))
                        return maxHeight >= 1? getLowerHeight(block.getRelative(BlockFace.UP), side, maxHeight - 1) : y + 1;
                    if (!skull.getFacing().equals(BlockFace.UP)) return y + 0.25;
                }
                return y;
            case DAYLIGHT_DETECTOR:
            case DAYLIGHT_DETECTOR_INVERTED:
                return y;
            case CARPET:
                return y;

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
