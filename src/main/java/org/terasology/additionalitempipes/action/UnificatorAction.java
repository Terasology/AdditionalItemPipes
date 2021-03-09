// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.additionalitempipes.action;

import org.joml.Vector3ic;
import org.terasology.additionalitempipes.components.UnificatorComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.common.lifespan.LifespanComponent;
import org.terasology.engine.logic.inventory.PickupComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.physics.components.RigidBodyComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.itempipes.controllers.PipeSystem;
import org.terasology.itempipes.event.PipeInsertEvent;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class UnificatorAction extends BaseComponentSystem {
    private Side outputside;
    @In
    private BlockManager blockManager;
    @In
    PipeSystem pipeSystem;
    @In
    private WorldProvider world;
    @In
    UnificatorComponent unificatorComponent;

    @ReceiveEvent(components = {UnificatorComponent.class})
    public void detectSide(ActivateEvent event, EntityRef itemEntity) {
        if (!event.getTarget().exists()) {
            event.consume();
            return;
        }
        Vector3ic posi = event.getTarget().getComponent(BlockComponent.class).getPosition();
        Side side = Side.inDirection(event.getHitNormal());

        switch (side) {
            case TOP:
                outputside = Side.TOP;
                Block top = blockManager.getBlock("AdditionalItemPipes:unificatortop");
                world.setBlock(posi, top);
                break;
            case FRONT:
                outputside = Side.FRONT;
                Block front = blockManager.getBlock("AdditionalItemPipes:unificatorfront");
                world.setBlock(posi, front);
                break;
            case RIGHT:
                outputside = Side.RIGHT;
                Block right = blockManager.getBlock("AdditionalItemPipes:unificatorright");
                world.setBlock(posi, right);
                break;
            case BACK:
                outputside = Side.BACK;
                Block back = blockManager.getBlock("AdditionalItemPipes:unificatorback");
                world.setBlock(posi, back);
                break;
            case LEFT:
                outputside = Side.LEFT;
                Block left = blockManager.getBlock("AdditionalItemPipes:unificatorleft");
                world.setBlock(posi, left);
                break;
            case BOTTOM:
                outputside = Side.BOTTOM;
                Block bottom = blockManager.getBlock("AdditionalItemPipes:unificatorbottom");
                world.setBlock(posi, bottom);
                break;
        }
    }

    @ReceiveEvent(components = UnificatorComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onItemInput(PipeInsertEvent event, EntityRef entity, UnificatorComponent unificator, BlockComponent block) {
        EntityRef item = event.getActor();
        item.removeComponent(RigidBodyComponent.class);
        item.removeComponent(LifespanComponent.class);
        item.removeComponent(PickupComponent.class);
        Vector3ic position = block.getPosition();
        if (outputside != null) {
            int num = 0;
            switch (outputside) {
                case TOP:
                    num = 0;
                    break;
                case FRONT:
                    num = 4;
                    break;
                case RIGHT:
                    num = 3;
                    break;
                case BACK:
                    num = 5;
                    break;
                case LEFT:
                    num = 2;
                    break;
                case BOTTOM:
                    num = 1;
                    break;
            }
            inputOrDrop(event.getActor(), position, Side.values()[num]);
            event.consume();
        }
    }

    private void inputOrDrop(EntityRef item, Vector3ic position, Side side) {
        Map<Side, EntityRef> pipes = pipeSystem.findPipes(position);
        EntityRef pipe = pipes.get(side);
        if (pipe != null) {
            Set<Prefab> prefabs = pipeSystem.findingMatchingPathPrefab(pipe, side.reverse());
            Optional<Prefab> pick = prefabs.stream().skip((int) (prefabs.size() * Math.random())).findFirst();
            if (pipeSystem.insertIntoPipe(item, pipe, side.reverse(), pick.get(), 1f)) {
                return;
            }
        }
        pipeSystem.dropItem(item);
    }
}
