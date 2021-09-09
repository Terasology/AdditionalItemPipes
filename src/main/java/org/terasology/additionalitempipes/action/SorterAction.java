// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.additionalitempipes.action;

import org.joml.Vector3ic;
import org.terasology.additionalitempipes.components.SorterComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.common.lifespan.LifespanComponent;
import org.terasology.engine.logic.inventory.PickupComponent;
import org.terasology.engine.math.Side;
import org.terasology.engine.physics.components.RigidBodyComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.itempipes.controllers.PipeSystem;
import org.terasology.itempipes.event.PipeInsertEvent;
import org.terasology.module.inventory.components.InventoryComponent;
import org.terasology.module.inventory.events.InventorySlotChangedEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * System covering Sorter's behavior.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SorterAction extends BaseComponentSystem {

    @In
    PipeSystem pipeSystem;

    /**
     * Called when an item is input to the Sorter by pipe.
     *
     * @param event PipeInsertEvent called by {@link org.terasology.itempipes.controllers.BlockMotionSystem}.
     * @param entity EntityRef to the Sorter.
     * @param sorter Sorter's SorterComponent.
     * @param block Sorter's BlockComponent.
     */
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = SorterComponent.class)
    public void onItemInput(PipeInsertEvent event, EntityRef entity, SorterComponent sorter, BlockComponent block) {
        EntityRef item = event.getActor();

        item.removeComponent(RigidBodyComponent.class);
        item.removeComponent(LifespanComponent.class);
        item.removeComponent(PickupComponent.class);

        Vector3ic sorterPos = block.getPosition();

        //look for the item in the filter - if found, send the item to side according to the filter, if not - to default side set by a checkbox.
        int sideNum = 0;
        for (List<String> list : sorter.filter) {
            for (String compareString : list) {
                if (compareString.equalsIgnoreCase(getCompareString(event.getActor()))) {
                    inputOrDrop(event.getActor(), sorterPos, Side.values()[sideNum]);
                    event.consume();
                    return;
                }
            }
            sideNum++;
        }

        inputOrDrop(event.getActor(), sorterPos, Side.values()[sorter.defaultSideNum]);
        //consume the event to prevent inserting items into Sorter's inventory (used as a base for the filter)
        event.consume();
    }

    /**
     * Handles the item which came to the Sorter. Inserts the item into the according pipe, if available, otherwise -
     * drops it.
     *
     * @param item the item which came to the Sorter.
     * @param sorterPos position of the Sorter.
     * @param side side to which a pipe is connected.
     */
    private void inputOrDrop(EntityRef item, Vector3ic sorterPos, Side side) {
        Map<Side, EntityRef> pipes = pipeSystem.findPipes(sorterPos);
        EntityRef pipe = pipes.get(side);
        if (pipe != null) {
            Set<Prefab> prefabs = pipeSystem.findingMatchingPathPrefab(pipe, side.reverse());
            Optional<Prefab> pick = prefabs.stream().skip((int) (prefabs.size() * Math.random())).findFirst();
            if (pick.isPresent()) {
                if (pipeSystem.insertIntoPipe(item, pipe, side.reverse(), pick.get(), 1f)) {
                    return;
                }
            }
        }
        pipeSystem.dropItem(item);
    }

    /**
     * Called when an item is added/removed into the Sorter's inventory - filter base.
     *
     * @param event Event triggered when items are added/removed
     * @param entity EntityRef to the Sorter.
     * @param sortComp SorterComponent of the Sorter.
     * @param inv InventoryComponent of the Sorter.
     */
    @ReceiveEvent(components = {SorterComponent.class, InventoryComponent.class})
    public void onFilterChange(InventorySlotChangedEvent event, EntityRef entity, SorterComponent sortComp, InventoryComponent inv) {
        List<List<String>> newFilter = new LinkedList<>();
        newFilter.add(new ArrayList<>());
        newFilter.add(new ArrayList<>());
        newFilter.add(new ArrayList<>());
        newFilter.add(new ArrayList<>());
        newFilter.add(new ArrayList<>());
        newFilter.add(new ArrayList<>());

        int slotNum = 0;
        for (EntityRef slot : inv.itemSlots) {
            if (slot != EntityRef.NULL) {
                int sideNumber = slotNum / 5;
                newFilter.get(sideNumber).add(getCompareString(slot));
            }
            slotNum++;
        }

        sortComp.filter = newFilter;
        entity.saveComponent(sortComp);
    }

    /**
     * Gets a string off the entity used for filtering. The items are differentiated by their's prefab name, block - by
     * their block family's name.
     *
     * @param item Item used to generate the string for filtering purposes.
     * @return String generated for filtering purposes.
     */
    private String getCompareString(EntityRef item) {
        BlockItemComponent biComponent = item.getComponent(BlockItemComponent.class);
        if (biComponent != null) {
            return biComponent.blockFamily.getURI().toString();
        } else {
            return item.getParentPrefab().getName();
        }
    }
}
