package com.almostreliable.lazierae2.client;

import appeng.init.client.InitScreens;
import com.almostreliable.lazierae2.registry.LazierMenus;
import com.almostreliable.lib.client.ClientInit;

public class ClientSetup implements ClientInit {
    @Override
    public void onMenuScreensInit(ScreenFactoryInit event) {
        InitScreens.register(LazierMenus.INJECTOR.get(), CrystalInjectorScreen::new, "/screens/crystal_injector.json");
    }
}
