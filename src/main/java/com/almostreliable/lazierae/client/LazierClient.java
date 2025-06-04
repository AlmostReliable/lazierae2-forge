package com.almostreliable.lazierae.client;

import com.almostreliable.lazierae.client.renderer.SideConfigRenderer;
import com.almostreliable.lazierae.client.screen.CrystalInjectorScreen;
import com.almostreliable.lazierae.registry.LazierBlockEntities;
import com.almostreliable.lazierae.registry.LazierMenus;
import com.almostreliable.lib.client.ClientInit;

public class LazierClient implements ClientInit {

    @Override
    public void onMenuScreensInit(ScreenFactoryInit event) {
        event.register(LazierMenus.INJECTOR.get(), CrystalInjectorScreen::new);
    }

    @Override
    public void onBlockEntityRendererInit(BlockEntityRendererInit event) {
        event.register(LazierBlockEntities.INJECTOR.get(), SideConfigRenderer::new);
    }
}
