package com.almostreliable.lazierae2.content.maintainer;

import appeng.api.networking.crafting.*;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.helpers.MultiCraftingTracker;
import appeng.helpers.NonNullArrayIterator;
import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Modified version from AE2's {@link MultiCraftingTracker}.
 */
public class MaintainerCraftTracker implements INBTSerializable<CompoundTag> {

    private final int size;
    private final ICraftingRequester owner;
    private Future<ICraftingPlan>[] jobs;
    private ICraftingLink[] links;

    MaintainerCraftTracker(ICraftingRequester owner, int size) {
        this.owner = owner;
        this.size = size;
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        for (var i = 0; i < size; i++) {
            var link = getLink(i);
            if (link != null) {
                var l = new CompoundTag();
                link.writeToNBT(l);
                tag.put("links-" + i, l);
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        for (var i = 0; i < size; i++) {
            var link = tag.getCompound("links-" + i);
            if (!link.isEmpty()) {
                setLink(i, StorageHelper.loadCraftingLink(link, owner));
            }
        }
    }

    void jobStateChange(ICraftingLink link) {
        if (links == null) return;
        for (var i = 0; i < links.length; i++) {
            if (Objects.equals(links[i], link)) {
                setLink(i, null);
                return;
            }
        }
    }

    @SuppressWarnings("java:S2142")
    boolean requestCraft(
        int slot, AEKey what, long amount, Level level, ICraftingService crafting, IActionSource source
    ) {
        if (getLink(slot) != null) return false;
        var craftingJob = getJob(slot);
        if (craftingJob != null) {
            try {
                ICraftingPlan job = null;
                if (craftingJob.isDone()) job = craftingJob.get();
                if (job != null) {
                    var link = crafting.submitJob(job, owner, null, false, source);
                    setJob(slot, null);
                    if (link != null) {
                        setLink(slot, link);
                        return true;
                    }
                }
            } catch (InterruptedException | ExecutionException ignored) {
                // ignore
            }
        } else {
            setJob(
                slot,
                crafting.beginCraftingCalculation(level, () -> source, what, amount, CalculationStrategy.CRAFT_LESS)
            );
        }
        return false;
    }

    int getSlot(ICraftingLink link) {
        if (links == null) return -1;
        for (var i = 0; i < links.length; i++) {
            if (Objects.equals(links[i], link)) {
                return i;
            }
        }
        return -1;
    }

    boolean isBusy(int slot) {
        return getLink(slot) != null;
    }

    @Nullable
    private ICraftingLink getLink(int slot) {
        if (links == null) return null;
        return links[slot];
    }

    private void setLink(int slot, @Nullable ICraftingLink link) {
        if (links == null) links = new ICraftingLink[size];
        links[slot] = link;

        var hasStuff = false;
        for (var i = 0; i < links.length; i++) {
            var l = links[i];
            if (l == null || l.isCanceled() || l.isDone()) {
                links[i] = null;
            } else {
                hasStuff = true;
            }
        }
        if (!hasStuff) links = null;
    }

    @Nullable
    private Future<ICraftingPlan> getJob(int slot) {
        if (jobs == null) return null;
        return jobs[slot];
    }

    @SuppressWarnings({"VariableNotUsedInsideIf", "unchecked"})
    private void setJob(int slot, @Nullable Future<ICraftingPlan> job) {
        if (jobs == null) jobs = new Future[size];
        jobs[slot] = job;

        var hasStuff = false;
        for (var j : jobs) {
            if (j != null) {
                hasStuff = true;
                break;
            }
        }
        if (!hasStuff) jobs = null;
    }

    @SuppressWarnings("java:S4738")
    ImmutableSet<ICraftingLink> getRequestedJobs() {
        if (links == null) return ImmutableSet.of();
        return ImmutableSet.copyOf(new NonNullArrayIterator<>(links));
    }
}
