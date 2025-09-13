package game;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

public class AuctionPosting {
    public int id;
    public AuctionsbucketData bucket;

    public ArrayList<item> items = new ArrayList<>();
    public ObjectGuid owner = ObjectGuid.EMPTY;
    public ObjectGuid ownerAccount = ObjectGuid.EMPTY;
    public ObjectGuid bidder = ObjectGuid.EMPTY;
    public long minBid;
    public long buyoutOrUnitPrice;
    public long deposit;
    public long bidAmount;
    public LocalDateTime startTime = LocalDateTime.MIN;
    public LocalDateTime endTime = LocalDateTime.MIN;
    public AuctionPostingServerFlag serverFlags = AuctionPostingServerFlag.values()[0];

    public ArrayList<ObjectGuid> bidderHistory = new ArrayList<>();

    public static long calculateMinIncrement(long bidAmount) {
        return MathUtil.CalculatePct(bidAmount / MoneyConstants.Silver, 5) * MoneyConstants.Silver;
    }

    public final boolean isCommodity() {
        return items.size() > 1 || items.get(0).getTemplate().getMaxStackSize() > 1;
    }

    public final int getTotalItemCount() {
        return (int) items.Sum(item -> item.count);
    }

    public final void buildAuctionItem(AuctionItem auctionItem, boolean alwaysSendItem, boolean sendKey, boolean censorServerInfo, boolean censorBidInfo) {
        // SMSG_AUCTION_LIST_BIDDER_ITEMS_RESULT, SMSG_AUCTION_LIST_ITEMS_RESULT (if not commodity), SMSG_AUCTION_LIST_OWNER_ITEMS_RESULT, SMSG_AUCTION_REPLICATE_RESPONSE (if not commodity)
        //auctionItem.Item - here to unify comment

        // all (not optional<>)
        auctionItem.count = (int) getTotalItemCount();
        auctionItem.flags = items.get(0).getItemData().dynamicFlags;
        auctionItem.auctionID = id;
        auctionItem.owner = owner;

        // prices set when filled
        if (isCommodity()) {
            if (alwaysSendItem) {
                auctionItem.item = new itemInstance(items.get(0));
            }

            auctionItem.unitPrice = buyoutOrUnitPrice;
        } else {
            auctionItem.item = new itemInstance(items.get(0));

            auctionItem.charges = new int[]{Items.get(0).getSpellCharges(0), items.get(0).getSpellCharges(1), items.get(0).getSpellCharges(2), items.get(0).getSpellCharges(3), items.get(0).getSpellCharges(4)}.max();

            for (EnchantmentSlot enchantmentSlot = 0; enchantmentSlot.getValue() < EnchantmentSlot.MaxInspected.getValue(); enchantmentSlot++) {
                var enchantId = items.get(0).getEnchantmentId(enchantmentSlot);

                if (enchantId == 0) {
                    continue;
                }

                auctionItem.enchantments.add(new ItemEnchantData(enchantId, items.get(0).getEnchantmentDuration(enchantmentSlot), items.get(0).getEnchantmentCharges(enchantmentSlot), (byte) enchantmentSlot.getValue()));
            }

            for (byte i = 0; i < items.get(0).getItemData().gems.size(); ++i) {
                var gemData = items.get(0).getItemData().gems.get(i);

                if (gemData.itemId != 0) {
                    ItemGemData gem = new ItemGemData();
                    gem.slot = i;
                    gem.item = new itemInstance(gemData);
                    auctionItem.gems.add(gem);
                }
            }

            if (minBid != 0) {
                auctionItem.minBid = minBid;
            }

            var minIncrement = calculateMinIncrement();

            if (minIncrement != 0) {
                auctionItem.minIncrement = minIncrement;
            }

            if (buyoutOrUnitPrice != 0) {
                auctionItem.buyoutPrice = buyoutOrUnitPrice;
            }
        }

        // all (not optional<>)
        auctionItem.durationLeft = (int) Math.max((EndTime - gameTime.GetSystemTime()).TotalMilliseconds, 0L);
        auctionItem.deleteReason = 0;

        // SMSG_AUCTION_LIST_ITEMS_RESULT (only if owned)
        auctionItem.censorServerSideInfo = censorServerInfo;
        auctionItem.itemGuid = isCommodity() ? ObjectGuid.Empty : items.get(0).getGUID();
        auctionItem.ownerAccountID = ownerAccount;
        auctionItem.endTime = (int) time.DateTimeToUnixTime(endTime);

        // SMSG_AUCTION_LIST_BIDDER_ITEMS_RESULT, SMSG_AUCTION_LIST_ITEMS_RESULT (if has bid), SMSG_AUCTION_LIST_OWNER_ITEMS_RESULT, SMSG_AUCTION_REPLICATE_RESPONSE (if has bid)
        auctionItem.censorBidInfo = censorBidInfo;

        if (!bidder.isEmpty()) {
            auctionItem.bidder = bidder;
            auctionItem.bidAmount = bidAmount;
        }

        // SMSG_AUCTION_LIST_BIDDER_ITEMS_RESULT, SMSG_AUCTION_LIST_OWNER_ITEMS_RESULT, SMSG_AUCTION_REPLICATE_RESPONSE (if commodity)
        if (sendKey) {
            auctionItem.auctionBucketKey = new auctionBucketKey(AuctionsBucketKey.forItem(items.get(0)));
        }

        // all
        if (!items.get(0).getItemData().creator.getValue().isEmpty()) {
            auctionItem.creator = items.get(0).getItemData().creator;
        }
    }

    public final long calculateMinIncrement() {
        return calculateMinIncrement(bidAmount);
    }

    public static class Sorter implements Comparator<AuctionPosting> {
        private final Locale locale;
        private final AuctionSortDef[] sorts;
        private final int sortCount;

        public Sorter(Locale locale, AuctionSortDef[] sorts, int sortCount) {
            locale = locale;
            sorts = sorts;
            sortCount = sortCount;
        }

        public final int compare(AuctionPosting left, AuctionPosting right) {
            for (var i = 0; i < sortCount; ++i) {
                var ordering = compareColumns(_sorts[i].sortOrder, left, right);

                if (ordering != 0) {
                    return (ordering < 0).CompareTo(!_sorts[i].reverseSort);
                }
            }

            // Auctions are processed in LIFO order
            if (!left.startTime.equals(right.startTime)) {
                return left.startTime.CompareTo(right.startTime);
            }

            return (new integer(left.id)).compareTo(right.id);
        }

        private long compareColumns(AuctionHouseSortOrder column, AuctionPosting left, AuctionPosting right) {
            switch (column) {
                case Price: {
                    var leftPrice = left.buyoutOrUnitPrice != 0 ? left.BuyoutOrUnitPrice : (left.bidAmount != 0 ? left.BidAmount : left.minBid);
                    var rightPrice = right.buyoutOrUnitPrice != 0 ? right.BuyoutOrUnitPrice : (right.bidAmount != 0 ? right.BidAmount : right.minBid);

                    return (long) (leftPrice - rightPrice);
                }
                case Name:

                    return left.bucket.FullName[_locale.getValue()].CompareTo(right.bucket.FullName[_locale.getValue()]);
                case Level: {
                    var leftLevel = left.items.get(0).getModifier(ItemModifier.battlePetSpeciesId) == 0 ? left.bucket.SortLevel : (int) left.items.get(0).getModifier(ItemModifier.battlePetLevel);
                    var rightLevel = right.items.get(0).getModifier(ItemModifier.battlePetSpeciesId) == 0 ? right.bucket.SortLevel : (int) right.items.get(0).getModifier(ItemModifier.battlePetLevel);

                    return leftLevel - rightLevel;
                }
                case Bid:
                    return (long) (left.BidAmount - right.bidAmount);
                case Buyout:
                    return (long) (left.BuyoutOrUnitPrice - right.buyoutOrUnitPrice);
                default:
                    break;
            }

            return 0;
        }
    }
}
