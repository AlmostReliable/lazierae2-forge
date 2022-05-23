package com.almostreliable.lazierae2.mixin;

import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.entity.AEBaseItemEntity;
import appeng.entity.SingularityEntity;
import com.almostreliable.lazierae2.core.Setup.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import static net.minecraft.world.item.Items.DIAMOND;

@Mixin(SingularityEntity.class)
@SuppressWarnings("java:S2160")
public class SingularityEntityMixin extends AEBaseItemEntity {

    @Unique
    private int delay;
    @Unique
    private int transformTime;

    protected SingularityEntityMixin(
        EntityType<? extends AEBaseItemEntity> entityType, Level level
    ) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();
        if (!getItem().is(AEItems.ENDER_DUST.asItem())) return;
        if (isRemoved() || !AEConfig.instance().isInWorldFluixEnabled()) return;

        if (level.isClientSide() && delay > 30 && AEConfig.instance().isEnableEffects()) {
            AppEng.instance().spawnEffect(EffectType.Lightning, level, getX(), getY(), getZ(), null);
            delay = 0;
        }
        delay++;

        var j = Mth.floor(getX());
        var i = Mth.floor((getBoundingBox().minY + getBoundingBox().maxY) / 2.0D);
        var k = Mth.floor(getZ());

        var state = level.getBlockState(new BlockPos(j, i, k));
        var mat = state.getMaterial();

        if (!level.isClientSide() && mat.isLiquid()) {
            transformTime++;
            if (transformTime > 60 && !transform()) {
                transformTime = 0;
            }
        } else {
            transformTime = 0;
        }
    }

    @Unique
    private boolean transform() {
        var item = getItem();
        if (!item.is(AEItems.ENDER_DUST.asItem())) return false;

        var region = new AABB(getX() - 1, getY() - 1, getZ() - 1, getX() + 1, getY() + 1, getZ() + 1);
        var entitiesInRange = getCheckedEntitiesWithinAABBExcludingEntity(region);

        ItemEntity diamond = null;
        ItemEntity skyDust = null;

        for (var entity : entitiesInRange) {
            if (entity instanceof ItemEntity e && !e.isRemoved()) {
                var other = e.getItem();
                if (!other.isEmpty()) {
                    if (other.is(DIAMOND)) {
                        diamond = (ItemEntity) entity;
                    }
                    if (other.is(AEItems.SKY_DUST.asItem())) {
                        skyDust = (ItemEntity) entity;
                    }
                }
            }
        }

        if (diamond != null && skyDust != null) {
            getItem().shrink(1);
            diamond.getItem().shrink(1);
            skyDust.getItem().shrink(1);

            if (getItem().getCount() <= 0) discard();
            if (diamond.getItem().getCount() <= 0) diamond.discard();
            if (skyDust.getItem().getCount() <= 0) skyDust.discard();

            var x = Math.floor(getX()) + .25d + getLevel().random.nextDouble() * .5;
            var y = Math.floor(getY()) + .25d + getLevel().random.nextDouble() * .5;
            var z = Math.floor(getZ()) + .25d + getLevel().random.nextDouble() * .5;
            var xSpeed = getLevel().random.nextDouble() * .25 - 0.125;
            var ySpeed = getLevel().random.nextDouble() * .25 - 0.125;
            var zSpeed = getLevel().random.nextDouble() * .25 - 0.125;

            var entity = new ItemEntity(level, x, y, z, new ItemStack(Items.RESONATING_DUST.get(), 2));
            entity.setDeltaMovement(xSpeed, ySpeed, zSpeed);
            level.addFreshEntity(entity);

            return true;
        }

        return false;
    }
}
