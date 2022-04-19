package com.almostreliable.lazierae2.network.packets;

import com.almostreliable.lazierae2.network.ServerToClientPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MaintainerSyncPacket extends ServerToClientPacket<MaintainerSyncPacket> {

    private int slot;
    private int flags;
    private boolean state;
    private ItemStack stack;
    private long count;
    private long batch;

    public MaintainerSyncPacket(int slot, int flags, boolean state, ItemStack stack, long count, long batch) {
        this.slot = slot;
        this.flags = flags;
        this.state = state;
        this.stack = stack;
        this.count = count;
        this.batch = batch;
    }

    public MaintainerSyncPacket() {}

    @Override
    public void encode(MaintainerSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.slot);
        buffer.writeInt(packet.flags);
        if ((packet.flags & SYNC_FLAGS.STATE) != 0) buffer.writeBoolean(packet.state);
        if ((packet.flags & SYNC_FLAGS.STACK) != 0) buffer.writeItemStack(packet.stack, false);
        if ((packet.flags & SYNC_FLAGS.COUNT) != 0) buffer.writeLong(packet.count);
        if ((packet.flags & SYNC_FLAGS.BATCH) != 0) buffer.writeLong(packet.batch);
    }

    @Override
    public MaintainerSyncPacket decode(FriendlyByteBuf buffer) {
        var packet = new MaintainerSyncPacket();
        packet.slot = buffer.readInt();
        packet.flags = buffer.readInt();
        if ((packet.flags & SYNC_FLAGS.STATE) != 0) packet.state = buffer.readBoolean();
        if ((packet.flags & SYNC_FLAGS.STACK) != 0) packet.stack = buffer.readItem();
        if ((packet.flags & SYNC_FLAGS.COUNT) != 0) packet.count = buffer.readLong();
        if ((packet.flags & SYNC_FLAGS.BATCH) != 0) packet.batch = buffer.readLong();
        return packet;
    }

    public int getSlot() {
        return slot;
    }

    public int getFlags() {
        return flags;
    }

    public boolean getState() {
        return state;
    }

    public ItemStack getStack() {
        return stack;
    }

    public long getCount() {
        return count;
    }

    public long getBatch() {
        return batch;
    }

    public enum SYNC_FLAGS {
        ;
        public static final int STATE = 1;
        public static final int STACK = 1 << 1;
        public static final int COUNT = 1 << 2;
        public static final int BATCH = 1 << 3;
    }
}
