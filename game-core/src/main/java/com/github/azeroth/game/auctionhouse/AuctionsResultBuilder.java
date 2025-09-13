package game;


import java.util.ArrayList;
import java.util.Comparator;

class AuctionsResultBuilder<T> {

    private final int offset;
    private final Comparator<T> sorter;
    private final AuctionHouseResultLimits maxResults;
    private final ArrayList<T> items = new ArrayList<>();
    private boolean hasMoreResults;


    public AuctionsResultBuilder(int offset, Comparator<T> sorter, AuctionHouseResultLimits maxResults) {
        offset = offset;
        sorter = sorter;
        maxResults = maxResults;
        hasMoreResults = false;
    }

    public final void addItem(T item) {
        var index = items.BinarySearch(item, sorter);

        if (index < 0) {
            index = ~index;
        }

        items.add(index, item);

        if (items.size() > maxResults.getValue() + offset) {
            items.remove(items.size() - 1);
            hasMoreResults = true;
        }
    }

    public final Span<T> getResultRange() {
        Span<T> h = items.toArray((T[]) new Object[0]);


        return h[(int) offset..];
    }

    public final boolean hasMoreResults() {
        return hasMoreResults;
    }
}
