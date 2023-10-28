package net.kav.infinity_mob.sensors;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.tslat.smartbrainlib.api.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.api.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.api.core.sensor.vanilla.NearbyLivingEntitySensor;
import net.tslat.smartbrainlib.object.SquareRadius;
import net.tslat.smartbrainlib.registry.SBLSensors;
import net.tslat.smartbrainlib.util.BrainUtils;
import net.tslat.smartbrainlib.util.EntityRetrievalUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NearbyLivingEntityEyeSightSensor <E extends LivingEntity> extends PredicateSensor<LivingEntity, E> {
    private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS);

    @Nullable
    protected SquareRadius radius = null;

    public NearbyLivingEntityEyeSightSensor() {
        super((target, entity) -> target != entity && target.isAlive());
    }

    /**
     * Set the radius for the sensor to scan.
     *
     * @param radius The coordinate radius, in blocks
     * @return this
     */
    public NearbyLivingEntityEyeSightSensor<E> setRadius(double radius) {
        return setRadius(radius, radius);
    }

    /**
     * Set the radius for the sensor to scan.
     *
     * @param xz The X/Z coordinate radius, in blocks
     * @param y  The Y coordinate radius, in blocks
     * @return this
     */
    public NearbyLivingEntityEyeSightSensor<E> setRadius(double xz, double y) {
        this.radius = new SquareRadius(xz, y);

        return this;
    }

    @Override
    public List<MemoryModuleType<?>> memoriesUsed() {
        return MEMORIES;
    }

    @Override
    public SensorType<? extends ExtendedSensor<?>> type() {
        return SBLSensors.NEARBY_LIVING_ENTITY.get();
    }

    @Override
    protected void sense(ServerWorld level, E entity) {
        SquareRadius radius = this.radius;

        if (radius == null) {
            double dist = entity.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);

            radius = new SquareRadius(dist, dist);
        }
        List<LivingEntity> entity_seen=new ArrayList<>();
        List<LivingEntity> entities = EntityRetrievalUtil.getEntities(level, entity.getBoundingBox().expand(radius.xzRadius(), radius.yRadius(), radius.xzRadius()), obj -> obj instanceof LivingEntity livingEntity && predicate().test(livingEntity, entity));

        for(LivingEntity entity1:entities)
        {
            if(entity.canSee(entity))
            {
                entity_seen.add(entity1);
            }

        }



        entity_seen.sort(Comparator.comparingDouble(entity::squaredDistanceTo));

        BrainUtils.setMemory(entity, MemoryModuleType.MOBS, entity_seen);
        BrainUtils.setMemory(entity, MemoryModuleType.VISIBLE_MOBS, new LivingTargetCache(entity, entity_seen));
    }
}
