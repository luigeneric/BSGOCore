package io.github.luigeneric.core.protocols.player;


import io.github.luigeneric.core.player.container.IContainerID;

record RepairSystemRecord(IContainerID containerID, int serverID, float repairValue, boolean useCubits){}
