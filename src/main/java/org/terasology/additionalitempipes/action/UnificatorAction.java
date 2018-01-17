/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.additionalitempipes.action;

import org.terasology.additionalitempipes.components.UnificatorComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.itempipes.controllers.PipeSystem;
import org.terasology.itempipes.event.PipeInsertEvent;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.common.lifespan.LifespanComponent;
import org.terasology.logic.inventory.PickupComponent;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RegisterSystem(RegisterMode.AUTHORITY)
public class UnificatorAction extends BaseComponentSystem {
    @In
    private BlockManager blockManager;
    @In
    PipeSystem pipeSystem;
    @In
    private WorldProvider world;

    @ReceiveEvent(components = {UnificatorComponent.class})
    public void detectSide(ActivateEvent event, EntityRef itemEntity) {
        UnificatorComponent unificatorComponent = itemEntity.getComponent(UnificatorComponent.class);
        if (!event.getTarget().exists()) {
            event.consume();
            return;
        }
        Vector3i posi = event.getTarget().getComponent(BlockComponent.class).getPosition();
        Side side = Side.inDirection(event.getHitNormal());
        unificatorComponent.outputside=side;

        switch (side) {
            case TOP:
                Block top = blockManager.getBlock("AdditionalItemPipes:unificatortop");
                world.setBlock(posi, top);
                break;
            case FRONT:
                Block front = blockManager.getBlock("AdditionalItemPipes:unificatorfront");
                world.setBlock(posi, front);
                break;
            case RIGHT:
                Block right = blockManager.getBlock("AdditionalItemPipes:unificatorright");
                world.setBlock(posi, right);
                break;
            case BACK:
                Block back = blockManager.getBlock("AdditionalItemPipes:unificatorback");
                world.setBlock(posi, back);
                break;
            case LEFT:
                Block left = blockManager.getBlock("AdditionalItemPipes:unificatorleft");
                world.setBlock(posi, left);
                break;
            case BOTTOM:
                Block bottom = blockManager.getBlock("AdditionalItemPipes:unificatorbottom");
                world.setBlock(posi, bottom);
                break;
        }
    }

    @ReceiveEvent(components = UnificatorComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void onItemInput(PipeInsertEvent event, EntityRef entity, BlockComponent block) {
        UnificatorComponent unificatorComponent = entity.getComponent(UnificatorComponent.class);
        EntityRef item = event.getActor();
        item.removeComponent(RigidBodyComponent.class);
        item.removeComponent(LifespanComponent.class);
        item.removeComponent(PickupComponent.class);
        Vector3i position = block.getPosition();
        inputOrDrop(event.getActor(), position, unificatorComponent.outputside);
        event.consume();
    }

    private void inputOrDrop(EntityRef item, Vector3i position, Side side) {
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
