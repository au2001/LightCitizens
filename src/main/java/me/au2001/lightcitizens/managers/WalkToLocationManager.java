package me.au2001.lightcitizens.managers;

import me.au2001.lightcitizens.FakeEntity;
import me.au2001.lightcitizens.LightCitizens;
import me.au2001.lightcitizens.pathfinder.MCGoal;
import me.au2001.lightcitizens.pathfinder.MCSnapshot;
import me.au2001.lightcitizens.pathfinder.Node.Node3D;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class WalkToLocationManager extends Manager {

    protected static final AStarMachine<VectorNode, Path> ASTAR = AStarMachine.createWithDefaultStorage();

    protected Location destination = null;
    protected NavigatorParameters params;
    protected MCSnapshot blockSource;
    protected List<Node3D> plan;
    protected boolean loading = false;
    protected MCGoal.Step step = null;

    public WalkToLocationManager(FakeEntity entity) {
        super(entity);

        params = new NavigatorParameters();
        params.examiner(new MinecraftBlockExaminer() {

            private final Vector UP = new Vector(0, 1, 0), DOWN = new Vector(0, -1, 0);

            public float getCost(BlockSource source, PathPoint point) {
                Vector pos = point.getVector();
                Material above = source.getMaterialAt(pos.clone().add(UP));
                if (above == null) return Float.POSITIVE_INFINITY;
                Material below = source.getMaterialAt(pos.clone().add(DOWN));
                if (below == null) return Float.POSITIVE_INFINITY;
                Material in = source.getMaterialAt(pos);
                if (in == null) return Float.POSITIVE_INFINITY;

                return super.getCost(source, point);
            }

            public PassableState isPassable(BlockSource source, PathPoint point) {
                Vector pos = point.getVector();
                Material above = source.getMaterialAt(pos.clone().add(UP));
                if (above == null) return PassableState.UNPASSABLE;
                Material below = source.getMaterialAt(pos.clone().add(DOWN));
                if (below == null) return PassableState.UNPASSABLE;
                Material in = source.getMaterialAt(pos);
                if (in == null) return PassableState.UNPASSABLE;

                return super.isPassable(source, point);
            }

        });
        params.pathDistanceMargin(0);
        params.distanceMargin(0);
    }

    public void onManagerRemoved() {
        walkToLocation(null);
    }

    public void tick() {
        if (destination == null) return;

        if (blockSource == null) {
            if (!loading) {
                loading = true;
                Location location = entity.getLocation();
                final Location center = location.clone().add(destination.toVector().subtract(location.toVector()).multiply(0.5));

                new BukkitRunnable() {
                    public void run() {
                        if (!loading) return;
                        MCSnapshot blockSource = new MCSnapshot(center, params.range());
                        if (!loading) {
                            blockSource.close();
                            return;
                        }
                        WalkToLocationManager.this.blockSource = blockSource;
                        loading = false;
                    }
                }.runTask(LightCitizens.getInstance());
            }
            return;
        }

        if (plan == null) {
            final Location location = entity.getLocation();
            final VectorGoal goal = new VectorGoal(destination, (float) params.pathDistanceMargin());

            VectorNode start = new VectorNode(goal, location, blockSource, params.examiners());
            Path path = ASTAR.runFully(goal, start, 50000);
            if (path != null) {
                if (!path.isComplete() && params.debug()) path.debug();
                plan = exportPath(path, blockSource);
            }
        }

        if (plan == null || plan.isEmpty()) return;

        // TODO: Fix moving sideways and arriving at the end.

        while (step == null || !step.hasNext()) {
            if (plan.isEmpty()) return;
            Location location = entity.getLocation();
            Node3D to = plan.remove(0);
            Node3D from = new Node3D(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            if (from.equals(to)) continue;

            step = new MCGoal.Step(location.getWorld(), from, to, (int) (20 / params.baseSpeed() / 3));
            params.run();
        }

        Location location = step.next();
        location.setDirection(location.clone().subtract(entity.getLocation()).toVector());
        entity.setLocation(location);
    }

    protected List<Node3D> exportPath(Path path, MCSnapshot snapshot) {
        List<Node3D> plan = new ArrayList<Node3D>();
        while (!path.isComplete()) {
            Vector vector = path.getCurrentVector();
            Node3D to = new Node3D(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
            // TODO: Walk on top of slabs etc. instead of inside them
            // to.y = MCGraph.getUpperHeight(snapshot, to, null, 1);
            plan.add(to);
            path.update(null);
        }
        return plan;
    }

    @SuppressWarnings("unchecked")
    public void walkToLocation(Location location) {
        this.destination = location != null? location.clone() : null;
        if (blockSource != null) blockSource.close();
        blockSource = null;
        loading = false;
        plan = null;
    }

    public boolean isWalking() {
        return destination != null;
    }

    public double getSpeed() {
        return params.baseSpeed();
    }

    public void setSpeed(double speed) {
        if (speed <= 0) throw new IllegalArgumentException("Speed must be greater than zero (strictly)!");
        params.baseSpeed((float) speed);
        if (blockSource != null) blockSource.close();
        blockSource = null;
        loading = false;
        plan = null;
    }

    public double getAcceptableDistance() {
        return params.pathDistanceMargin();
    }

    public void setAcceptableDistance(double distance) {
        if (distance < 0) throw new IllegalArgumentException("Distance must be greater or equal to zero!");
        params.pathDistanceMargin((float) distance);
        if (blockSource != null) blockSource.close();
        blockSource = null;
        loading = false;
        plan = null;
    }

}
