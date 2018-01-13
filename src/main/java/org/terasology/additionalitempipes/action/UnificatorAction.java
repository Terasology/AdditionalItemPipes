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
        Vector3i posi = event.getTarget().getComponent(BlockComponent.class).getPosition();
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
        Vector3i position = block.getPosition();
        if(outputside != null) {
            int num = 0;
            switch(outputside){
                case TOP:
                    num=0;
                    break;
                case FRONT:
                    num=4;
                    break;
                case RIGHT:
                    num=3;
                    break;
                case BACK:
                    num=5;
                    break;
                case LEFT:
                    num=2;
                    break;
                case BOTTOM:
                    num=1;
                    break;
            }
            inputOrDrop(event.getActor(), position, Side.values()[num]);
            event.consume();
        }
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
