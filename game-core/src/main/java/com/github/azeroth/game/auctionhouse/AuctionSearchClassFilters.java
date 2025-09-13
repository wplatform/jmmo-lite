package game;

public class AuctionSearchClassFilters {
    public SubclassFilter[] classes = new SubclassFilter[ItemClass.max.getValue()];

    public AuctionSearchClassFilters() {
        for (var i = 0; i < itemClass.max.getValue(); ++i) {
            Classes[i] = new SubclassFilter();
        }
    }


    public enum FilterType {
        SkipClass(0),
        SkipSubclass((int) 0xFFFFFFFF),
        SkipInvtype((int) 0xFFFFFFFF);

        public static final int SIZE = Integer.SIZE;
        private static java.util.HashMap<Integer, FilterType> mappings;
        private int intValue;

        private FilterType(int value) {
            intValue = value;
            getMappings().put(value, this);
        }

        private static java.util.HashMap<Integer, FilterType> getMappings() {
            if (mappings == null) {
                synchronized (FilterType.class) {
                    if (mappings == null) {
                        mappings = new java.util.HashMap<Integer, FilterType>();
                    }
                }
            }
            return mappings;
        }

        public static FilterType forValue(int value) {
            return getMappings().get(value);
        }

        public int getValue() {
            return intValue;
        }
    }

    public static class SubclassFilter {
        public FilterType subclassMask = FilterType.values()[0];

        public long[] invTypes = new long[ItemConst.MaxItemSubclassTotal];
    }
}
