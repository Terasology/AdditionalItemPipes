package org.terasology.additionalitempipes.components;

import org.terasology.entitySystem.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This Component differentiates the Sorter from other blocks. It contains the Sorter's filter and index od the side to which items not listed in the filter will be put.
 */
public class SorterComponent implements Component {
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
}
