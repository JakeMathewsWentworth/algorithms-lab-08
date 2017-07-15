package edu.wit.cs.comp3370;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/* Provides a solution to the 0-1 knapsack problem 
 * 
 * Wentworth Institute of Technology
 * COMP 3370
 * Lab Assignment 8
 * 
 */

public class LAB8 {

    /**
     * Finds a solution to the binary knapsack problem using dynamic programming
     * 
     * @param table
     *            An array of items available to choose from
     * @param weight
     *            The maximum weight of the knapsack
     * @return An array of items with the most value that can be taken given the
     *         weight
     */
    public static Item[] FindDynamic(Item[] table, int weight) {
        ResolvedItem[][] itemMatrix = new ResolvedItem[table.length][weight + 1];

        for (int index = 0; index < table.length; index++) {
            Item item = table[index];
            for (int currentWeight = 0; currentWeight <= weight; currentWeight++) {
                ResolvedItem resolved = null;
                ResolvedItem excluded = lookForValue(itemMatrix, index, currentWeight, 0);
                if (currentWeight - item.weight >= 0) {
                    ResolvedItem found = lookForValue(itemMatrix, index, currentWeight, item.weight);
                    if (found != null) {
                        int included = found.value + item.value;
                        int newValue = Math.max(included, excluded.value);
                        if (newValue == included) {
                            resolved = new ResolvedItem(newValue, item, found);
                        } else {
                            resolved = excluded;
                        }
                    } else {
                        resolved = new ResolvedItem(item.value, item, null);
                    }
                } else if (excluded != null) {
                    resolved = excluded;
                } else {
                    resolved = new ResolvedItem(0, null, null);
                }
                itemMatrix[index][currentWeight] = resolved;
            }
        }

        ResolvedItem resultingResolvedItem = itemMatrix[table.length - 1][weight];
        List<Item> resolvedItems = new ArrayList<>();
        getResolvedItems(resultingResolvedItem, resolvedItems);

        best_value = resultingResolvedItem.value;
        Item[] finalItems = new Item[resolvedItems.size()];
        finalItems = resolvedItems.toArray(finalItems);
        return finalItems;
    }

    private static ResolvedItem lookForValue(ResolvedItem[][] itemMatrix, int index, int currentWeight, int offset) {
        ResolvedItem bestValue = null;
        try {
            bestValue = itemMatrix[index - 1][currentWeight - offset];
        } catch (ArrayIndexOutOfBoundsException ignore) {

        }

        return bestValue;
    }

    private static void getResolvedItems(ResolvedItem currentResolvedItem, List<Item> items) {
        if (currentResolvedItem == null) {
            return;
        }
        if (currentResolvedItem.selectedItem != null) {
            items.add(currentResolvedItem.selectedItem);
        }
        getResolvedItems(currentResolvedItem.includedItem, items);
    }

    private static class ResolvedItem {
        public int value;
        public Item selectedItem;
        public ResolvedItem includedItem;

        public ResolvedItem(int value, Item selectedItem, ResolvedItem includedItem) {
            this.value = value;
            this.selectedItem = selectedItem;
            this.includedItem = includedItem;
        }

        @Override
        public String toString() {
            return Integer.toString(value);
        }
    }

    /********************************************
     * 
     * You shouldn't modify anything past here
     * 
     ********************************************/

    // set by calls to Find* methods
    private static int best_value = 0;

    public static class Item {
        public int weight;
        public int value;
        public int index;

        public Item(int w, int v, int i) {
            weight = w;
            value = v;
            index = i;
        }

        @Override
        public String toString() {
            return "(" + weight + "#, $" + value + ")";
        }
    }

    // enumerates all subsets of items to find maximum value that fits in
    // knapsack
    public static Item[] FindEnumerate(Item[] table, int weight) {

        if (table.length > 31) { // bitshift fails for larger sizes
            System.err.println("Problem size too large. Exiting");
            System.exit(0);
        }

        int nCr = 1 << table.length; // bitmask for included items
        int bestSum = -1;
        boolean[] bestUsed = {};
        boolean[] used = new boolean[table.length];

        for (int i = 0; i < nCr; i++) { // test all combinations
            int temp = i;

            for (int j = 0; j < table.length; j++) {
                used[j] = (temp % 2 == 1);
                temp = temp >> 1;
            }

            if (TotalWeight(table, used) <= weight) {
                if (TotalValue(table, used) > bestSum) {
                    bestUsed = Arrays.copyOf(used, used.length);
                    bestSum = TotalValue(table, used);
                }
            }
        }

        int itemCount = 0; // count number of items in best result
        for (int i = 0; i < bestUsed.length; i++)
            if (bestUsed[i])
                itemCount++;

        Item[] ret = new Item[itemCount];
        int retIndex = 0;

        for (int i = 0; i < bestUsed.length; i++) { // construct item list
            if (bestUsed[i]) {
                ret[retIndex] = table[i];
                retIndex++;
            }
        }
        best_value = bestSum;
        return ret;

    }

    // returns total value of all items that are marked true in used array
    private static int TotalValue(Item[] table, boolean[] used) {
        int ret = 0;
        for (int i = 0; i < table.length; i++)
            if (used[i])
                ret += table[i].value;

        return ret;
    }

    // returns total weight of all items that are marked true in used array
    private static int TotalWeight(Item[] table, boolean[] used) {
        int ret = 0;
        for (int i = 0; i < table.length; i++) {
            if (used[i])
                ret += table[i].weight;
        }

        return ret;
    }

    // adds items to the knapsack by picking the next item with the highest
    // value:weight ratio. This could use a max-heap of ratios to run faster,
    // but
    // it runs in n^2 time wrt items because it has to scan every item each time
    // an item is added
    public static Item[] FindGreedy(Item[] table, int weight) {
        boolean[] used = new boolean[table.length];
        int itemCount = 0;

        while (weight > 0) { // while the knapsack has space
            int bestIndex = GetGreedyBest(table, used, weight);
            if (bestIndex < 0)
                break;
            weight -= table[bestIndex].weight;
            best_value += table[bestIndex].value;
            used[bestIndex] = true;
            itemCount++;
        }

        Item[] ret = new Item[itemCount];
        int retIndex = 0;

        for (int i = 0; i < used.length; i++) { // construct item list
            if (used[i]) {
                ret[retIndex] = table[i];
                retIndex++;
            }
        }

        return ret;
    }

    // finds the available item with the best value:weight ratio that fits in
    // the knapsack
    private static int GetGreedyBest(Item[] table, boolean[] used, int weight) {

        double bestVal = -1;
        int bestIndex = -1;
        for (int i = 0; i < table.length; i++) {
            double ratio = (table[i].value * 1.0) / table[i].weight;
            if (!used[i] && (ratio > bestVal) && (weight >= table[i].weight)) {
                bestVal = ratio;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    public static int getBest() {
        return best_value;
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String file1;
        int weight = 0;
        System.out.printf(
                "Enter <objects file> <knapsack weight> <algorithm>, ([d]ynamic programming, [e]numerate, [g]reedy).\n");
        System.out.printf("(e.g: objects/small 10 g)\n");
        file1 = s.next();
        weight = s.nextInt();

        ArrayList<Item> tableList = new ArrayList<Item>();

        try (Scanner f = new Scanner(new File(file1))) {
            int i = 0;
            while (f.hasNextInt())
                tableList.add(new Item(f.nextInt(), f.nextInt(), i++));
        } catch (IOException e) {
            System.err.println("Cannot open file " + file1 + ". Exiting.");
            System.exit(0);
        }

        Item[] table = new Item[tableList.size()];
        for (int i = 0; i < tableList.size(); i++)
            table[i] = tableList.get(i);

        String algo = s.next();
        Item[] result = {};

        switch (algo.charAt(0)) {
        case 'd':
            result = FindDynamic(table, weight);
            break;
        case 'e':
            result = FindEnumerate(table, weight);
            break;
        case 'g':
            result = FindGreedy(table, weight);
            break;
        default:
            System.out.println("Invalid algorithm");
            System.exit(0);
            break;
        }

        s.close();

        System.out.printf("Index of included items: ");
        for (int i = 0; i < result.length; i++)
            System.out.printf("%d ", result[i].index);
        System.out.printf("\nBest value: %d\n", best_value);
    }

}
