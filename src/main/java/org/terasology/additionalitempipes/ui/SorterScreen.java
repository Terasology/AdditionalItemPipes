// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.additionalitempipes.ui;

import org.terasology.additionalitempipes.components.SorterComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.module.inventory.ui.InventoryGrid;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.ActivateEventListener;
import org.terasology.nui.widgets.UICheckbox;

import java.util.LinkedList;

/**
 * Screen for Sorter. This class makes Sorter's screen "entity-aware" and lets user select the Sorter's side for not defined items in the filter.
 */
public class SorterScreen extends CoreScreenLayer {

    @In
    private LocalPlayer localPlayer;

    private LinkedList<InventoryGrid> inventories = new LinkedList<>();
    private LinkedList<UICheckbox> checkboxes = new LinkedList<>();

    @Override
    public void initialise() {
        InventoryGrid inventory = find("inventory", InventoryGrid.class);
        inventory.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                return localPlayer.getCharacterEntity();
            }
        });
        inventory.setCellOffset(10);

        inventories.add(find("redRow", InventoryGrid.class));
        inventories.add(find("yellowRow", InventoryGrid.class));
        inventories.add(find("greenRow", InventoryGrid.class));
        inventories.add(find("cyanRow", InventoryGrid.class));
        inventories.add(find("blueRow", InventoryGrid.class));
        inventories.add(find("pinkRow", InventoryGrid.class));

        for (InventoryGrid grid : inventories) {
            grid.bindTargetEntity(new ReadOnlyBinding<EntityRef>() {
                @Override
                public EntityRef get() {
                    EntityRef characterEntity = localPlayer.getCharacterEntity();
                    CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
                    return characterComponent.predictedInteractionTarget;
                }
            });
        }

        checkboxes.add(find("redBox", UICheckbox.class));
        checkboxes.add(find("yellowBox", UICheckbox.class));
        checkboxes.add(find("greenBox", UICheckbox.class));
        checkboxes.add(find("cyanBox", UICheckbox.class));
        checkboxes.add(find("blueBox", UICheckbox.class));
        checkboxes.add(find("pinkBox", UICheckbox.class));

        //this listener sets activated checkbox to true, other to false and sets Sorter's default side number to activated checkboxes' index.
        ActivateEventListener boxListener = new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget widget) {
                UICheckbox activatedBox = (UICheckbox)widget;
                activatedBox.setChecked(true);

                LinkedList<UICheckbox> otherBoxes = new LinkedList<>(checkboxes);
                int sideNum = otherBoxes.indexOf(activatedBox);
                otherBoxes.remove(activatedBox);

                for (UICheckbox otherBox : otherBoxes) {
                    otherBox.setChecked(false);
                }

                EntityRef characterEntity = localPlayer.getCharacterEntity();
                CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
                EntityRef sorter = characterComponent.predictedInteractionTarget;
                SorterComponent sComponent = sorter.getComponent(SorterComponent.class);
                sComponent.defaultSideNum = sideNum;
            }
        };

        for (UICheckbox checkbox : checkboxes) {
            checkbox.subscribe(boxListener);
        }
    }

    /**
     * Sets the screen's checkboxes according to Sorter's defaultSideNum.
     */
    @Override
    public void onScreenOpened() {
        EntityRef characterEntity = localPlayer.getCharacterEntity();
        CharacterComponent characterComponent = characterEntity.getComponent(CharacterComponent.class);
        EntityRef sorter = characterComponent.predictedInteractionTarget;
        SorterComponent sComponent = sorter.getComponent(SorterComponent.class);

        checkboxes.get(sComponent.defaultSideNum).setChecked(true);

        LinkedList<UICheckbox> otherBoxes = new LinkedList<>(checkboxes);
        otherBoxes.remove(checkboxes.get(sComponent.defaultSideNum));
        for (UICheckbox otherBox : otherBoxes) {
            otherBox.setChecked(false);
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
