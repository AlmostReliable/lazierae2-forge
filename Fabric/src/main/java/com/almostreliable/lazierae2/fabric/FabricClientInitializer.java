package com.almostreliable.lazierae2.fabric;

import appeng.api.IAEAddonEntrypoint;
import com.almostreliable.almostlib.fabric.client.ClientInitRunner;
import com.almostreliable.lazierae2.LazierAE2;
import com.almostreliable.lazierae2.client.ClientSetup;

public class FabricClientInitializer implements IAEAddonEntrypoint {
    @Override
    public void onAe2Initialized() {
        ClientInitRunner.run(ClientSetup::new, LazierAE2.MANAGER);
    }
}
