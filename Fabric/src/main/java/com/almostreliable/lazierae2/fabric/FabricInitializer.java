package com.almostreliable.lazierae2.fabric;

import appeng.api.IAEAddonEntrypoint;
import com.almostreliable.almostlib.AlmostLib;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lazierae2.registry.LazierMenus;

import static com.almostreliable.lazierae2.LazierAE2.MANAGER;

public class FabricInitializer implements IAEAddonEntrypoint {
    @Override
    public void onAe2Initialized() {
        LazierAE2.init();
        MANAGER.initRegistriesToLoader();
        AlmostLib.PLATFORM.initRegistration(LazierMenus.REGISTRY);
        LazierAE2.initPost();
    }
}
