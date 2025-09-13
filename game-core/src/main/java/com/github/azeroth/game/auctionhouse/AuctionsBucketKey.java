package game;


import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemTemplate;

public class AuctionsBucketKey implements Comparable<AuctionsBucketKey> {
    private int itemId;
    private short itemLevel;
    private short battlePetSpeciesId;
    private short suffixItemNameDescriptionId;

    public AuctionsBucketKey(int itemId, short itemLevel, short battlePetSpeciesId, short suffixItemNameDescriptionId) {
        setItemId(itemId);
        setItemLevel(itemLevel);
        setBattlePetSpeciesId(battlePetSpeciesId);
        setSuffixItemNameDescriptionId(suffixItemNameDescriptionId);
    }

    public AuctionsBucketKey(AuctionBucketKey key) {
        setItemId(key.itemID);
        setItemLevel(key.itemLevel);
        setBattlePetSpeciesId((short) (key.battlePetSpeciesID != null ? key.battlePetSpeciesID.shortValue() : 0));
        setSuffixItemNameDescriptionId((short) (key.suffixItemNameDescriptionID != null ? key.suffixItemNameDescriptionID.shortValue() : 0));
    }

    public static boolean opEquals(AuctionsBucketKey right, AuctionsBucketKey left) {
        return right.getItemId() == left.getItemId() && right.getItemLevel() == left.getItemLevel() && right.getBattlePetSpeciesId() == left.getBattlePetSpeciesId() && right.getSuffixItemNameDescriptionId() == left.getSuffixItemNameDescriptionId();
    }

    public static boolean opNotEquals(AuctionsBucketKey right, AuctionsBucketKey left) {
        return !(AuctionsBucketKey.opEquals(right, left));
    }

    public static AuctionsBucketKey forItem(Item item) {
        var itemTemplate = item.getTemplate();

        if (itemTemplate.getMaxStackSize() == 1) {
            return new AuctionsBucketKey(item.getEntry(), (short) item.getItemLevel(itemTemplate, item.getBonusData(), 0, (int) item.getRequiredLevel(), 0, 0, 0, false, 0), (short) item.getModifier(ItemModifier.battlePetSpeciesId), (short) item.getBonusData().suffix);
        } else {
            return forCommodity(itemTemplate);
        }
    }

    public static AuctionsBucketKey forCommodity(ItemTemplate itemTemplate) {
        return new AuctionsBucketKey(itemTemplate.getId(), (short) itemTemplate.getBaseItemLevel(), (short) 0, (short) 0);
    }

    public final int getItemId() {
        return itemId;
    }

    public final void setItemId(int value) {
        itemId = value;
    }

    public final short getItemLevel() {
        return itemLevel;
    }

    public final void setItemLevel(short value) {
        itemLevel = value;
    }

    public final short getBattlePetSpeciesId() {
        return battlePetSpeciesId;
    }

    public final void setBattlePetSpeciesId(short value) {
        battlePetSpeciesId = value;
    }

    public final short getSuffixItemNameDescriptionId() {
        return suffixItemNameDescriptionId;
    }

    public final void setSuffixItemNameDescriptionId(short value) {
        suffixItemNameDescriptionId = value;
    }

    public final int compareTo(AuctionsBucketKey other) {
        return (new integer(getItemId())).compareTo(other.getItemId());
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (new integer(getItemId())).hashCode() ^ (new SHORT(getItemLevel())).hashCode() ^ (new SHORT(getBattlePetSpeciesId())).hashCode() ^ (new SHORT(getSuffixItemNameDescriptionId())).hashCode();
    }
}
