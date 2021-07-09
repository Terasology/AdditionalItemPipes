// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.additionalitempipes.components;

import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This Component differentiates the Sorter from other blocks. It contains the Sorter's filter and index od the side to which items not listed in the filter will be put.
 */
public class SorterComponent implements Component<SorterComponent> {
    //A list of 6 lists (one per side) of strings (strings for filtering purposes based on the item's data)
    public List<List<String>> filter = new LinkedList<List<String>>() {{
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
        add(new ArrayList<>());
    }};

    public int defaultSideNum = 0;

    @Override
    public void copy(SorterComponent other) {
        this.filter = other.filter.stream()
                .map(Lists::newArrayList)
                .collect(Collectors.toList());
        this.defaultSideNum = other.defaultSideNum;
    }
}
