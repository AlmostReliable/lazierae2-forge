package com.github.almostreliable.lazierae2.gui;

import com.github.almostreliable.lazierae2.component.SideConfiguration;
import com.github.almostreliable.lazierae2.core.TypeEnums.BLOCK_SIDE;
import com.github.almostreliable.lazierae2.core.TypeEnums.IO_SETTING;
import com.github.almostreliable.lazierae2.machine.MachineTile;
import com.github.almostreliable.lazierae2.network.PacketHandler;
import com.github.almostreliable.lazierae2.network.SideConfigPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.Collection;

final class IOControl {

    private static final int BUTTON_SIZE = 6;
    private static final int INNER_SIZE = 4;
    private static final String TEXTURE_ID = "io";

    private IOControl() {}

    @SuppressWarnings("SameParameterValue")
    static IOButton[] setup(MachineScreen screen, int x, int y) {
        Collection<IOButton> buttons = new ArrayList<>();
        buttons.add(new IOButton(screen, BLOCK_SIDE.TOP, x + getPosition(1), y + getPosition(0)));
        buttons.add(new IOButton(screen, BLOCK_SIDE.LEFT, x + getPosition(0), y + getPosition(1)));
        buttons.add(new IOButton(screen, BLOCK_SIDE.FRONT, x + getPosition(1), y + getPosition(1)));
        buttons.add(new IOButton(screen, BLOCK_SIDE.RIGHT, x + getPosition(2), y + getPosition(1)));
        buttons.add(new IOButton(screen, BLOCK_SIDE.BOTTOM, x + getPosition(1), y + getPosition(2)));
        buttons.add(new IOButton(screen, BLOCK_SIDE.BACK, x + getPosition(2), y + getPosition(2)));
        return buttons.toArray(new IOButton[0]);
    }

    private static int getPosition(int offset) {
        return offset * BUTTON_SIZE;
    }

    private static final class IOButton extends GenericButton {

        private final MachineTile tile;
        private final BLOCK_SIDE side;

        private IOButton(MachineScreen screen, BLOCK_SIDE side, int pX, int pY) {
            super(screen, pX, pY, BUTTON_SIZE, BUTTON_SIZE, TEXTURE_ID);
            tile = screen.getMenu().tile;
            this.side = side;
        }

        @Override
        public void renderButton(MatrixStack matrix, int mX, int mY, float partial) {
            Minecraft.getInstance().getTextureManager().bind(texture);
            blit(matrix, x, y, 0, 0, BUTTON_SIZE, BUTTON_SIZE, getTextureWidth(), height);
            blit(
                matrix,
                x + 1,
                y + 1,
                BUTTON_SIZE + INNER_SIZE * (float) tile.sideConfig.get(side).ordinal(),
                0,
                INNER_SIZE,
                INNER_SIZE,
                getTextureWidth(),
                height
            );
        }

        @Override
        protected void clickHandler() {
            changeMode();
            PacketHandler.CHANNEL.sendToServer(new SideConfigPacket(tile.sideConfig));
        }

        @Override
        protected int getTextureWidth() {
            return width + 4 * INNER_SIZE;
        }

        @Override
        protected int getTextureHeight() {
            return height;
        }

        @Override
        public void playDownSound(SoundHandler handler) {
            if (side == BLOCK_SIDE.FRONT && !Screen.hasShiftDown()) return;
            super.playDownSound(handler);
        }

        @Override
        public void renderToolTip(MatrixStack matrix, int mX, int mY) {
            // TODO
        }

        private void changeMode() {
            SideConfiguration config = tile.sideConfig;
            IO_SETTING setting = config.get(side);

            if (Screen.hasShiftDown()) {
                if (side == BLOCK_SIDE.FRONT) {
                    config.reset();
                    return;
                }
                setting = IO_SETTING.OFF;
            } else {
                if (side == BLOCK_SIDE.FRONT) return;
                switch (setting) {
                    case INPUT:
                        setting = IO_SETTING.OUTPUT;
                        break;
                    case OUTPUT:
                        setting = IO_SETTING.IO;
                        break;
                    case IO:
                        setting = IO_SETTING.OFF;
                        break;
                    case OFF:
                        setting = IO_SETTING.INPUT;
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + setting);
                }
            }

            config.set(side, setting);
        }
    }
}
