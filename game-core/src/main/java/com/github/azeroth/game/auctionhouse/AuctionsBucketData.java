package game;


import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

public class AuctionsBucketData {
    public AuctionsBucketkey key;

    // filter helpers
    public byte itemClass;
    public byte itemSubClass;
    public byte inventoryType;
    public AuctionHouseFilterMask qualityMask = AuctionHouseFilterMask.values()[0];
    public int[] qualityCounts = new int[ItemQuality.max.getValue()];
    public long minPrice; // for sort

    public (
        public byte requiredLevel = 0; // for usable search,     public byte sortLevel = 0;)[]itemModifiedAppearanceId =new(
        public byte minBattlePetLevel = 0;,     public byte maxBattlePetLevel = 0;)[4]; // for uncollected search
    public String[] fullName = new String[Locale.Total.getValue()];
    public ArrayList<AuctionPosting> auctions = new ArrayList<>();
int id
int count
int id
int count

    public final void buildBucketInfo(BucketInfo bucketInfo, Player player) {
        bucketInfo.key = new auctionBucketKey(key);
        bucketInfo.minPrice = minPrice;
        bucketInfo.requiredLevel = requiredLevel;
        bucketInfo.totalQuantity = 0;

        for (var auction : auctions) {
            for (var item : auction.items) {
                bucketInfo.totalQuantity += (int) item.getCount();

                if (key.getBattlePetSpeciesId() != 0) {
                    var breedData = item.getModifier(ItemModifier.BattlePetBreedData);
                    var breedId = breedData & 0xFFFFFF;
                    var quality = (byte) ((breedData >>> 24) & 0xFF);
                    var level = (byte) (item.getModifier(ItemModifier.battlePetLevel));

                    bucketInfo.maxBattlePetQuality = bucketInfo.maxBattlePetQuality != null ? Math.max(bucketInfo.maxBattlePetQuality.byteValue(), quality) : quality;
                    bucketInfo.maxBattlePetLevel = bucketInfo.maxBattlePetLevel != null ? Math.max(bucketInfo.maxBattlePetLevel.byteValue(), level) : level;
                    bucketInfo.battlePetBreedID = (byte) breedId;
                }
            }

            bucketInfo.containsOwnerItem = bucketInfo.containsOwnerItem || Objects.equals(auction.owner, player.getGUID());
        }

        bucketInfo.containsOnlyCollectedAppearances = true;

        for (var appearance : itemModifiedAppearanceId) {
            if (appearance.Item1 != 0) {
                bucketInfo.itemModifiedAppearanceIDs.add(appearance.Item1);

                if (!player.getSession().getCollectionMgr().hasItemAppearance(appearance.Item1).PermAppearance) {
                    bucketInfo.containsOnlyCollectedAppearances = false;
                }
            }
        }
    }

    public static class Sorter implements Comparator<AuctionsBucketData> {
        private final Locale locale;
        private final AuctionSortDef[] sorts;
        private final int sortCount;

        public Sorter(Locale locale, AuctionSortDef[] sorts, int sortCount) {
            locale = locale;
            sorts = sorts;
            sortCount = sortCount;
        }

        public final int compare(AuctionsBucketData left, AuctionsBucketData right) {
            for (var i = 0; i < sortCount; ++i) {
                var ordering = compareColumns(_sorts[i].sortOrder, left, right);

                if (ordering != 0) {
                    return (ordering < 0).CompareTo(!_sorts[i].reverseSort);
                }
            }

            return AuctionsBucketKey.opNotEquals(left.key, right.key) ? 1 : 0;
        }

        private long compareColumns(AuctionHouseSortOrder column, AuctionsBucketData left, AuctionsBucketData right) {
            switch (column) {
                case Price:
                case Bid:
                case Buyout:
                    return (long) (left.MinPrice - right.minPrice);
                case Name:

                    return left.FullName[_locale.getValue()].CompareTo(right.FullName[_locale.getValue()]);
                case Level:
                    return left.SortLevel - right.sortLevel;
                default:
                    break;
            }

            return 0;
        }
    }
}
