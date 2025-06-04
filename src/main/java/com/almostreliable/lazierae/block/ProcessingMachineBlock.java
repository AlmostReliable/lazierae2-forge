package com.almostreliable.lazierae.block;

import com.almostreliable.lazierae.blockentity.ProcessingMachineBlockEntity;
import com.almostreliable.lazierae.registry.ProcessingMachineType;

public class ProcessingMachineBlock extends MachineBlock<ProcessingMachineBlockEntity> {

    private final ProcessingMachineType processingMachineType;

    public ProcessingMachineBlock(Properties props, ProcessingMachineType processingMachineType) {
        super(props);
        this.processingMachineType = processingMachineType;
    }

    public ProcessingMachineType getProcessingMachineType() {
        return processingMachineType;
    }
}
