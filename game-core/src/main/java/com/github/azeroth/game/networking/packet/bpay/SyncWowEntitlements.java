package com.github.azeroth.game.networking.packet.bpay;


import java.util.ArrayList;


public class SyncWowEntitlements extends ServerPacket {
    private ArrayList<Integer> purchaseCount = new ArrayList<>();
    private ArrayList<Integer> productCount = new ArrayList<>();
    private ArrayList<Bpayproduct> product = new ArrayList<>();

    public SyncWowEntitlements() {
        super(ServerOpcode.SyncWowEntitlements);
    }

    public final ArrayList<Integer> getPurchaseCount() {
        return purchaseCount;
    }

    public final void setPurchaseCount(ArrayList<Integer> value) {
        purchaseCount = value;
    }

    public final ArrayList<Integer> getProductCount() {
        return productCount;
    }

    public final void setProductCount(ArrayList<Integer> value) {
        productCount = value;
    }

    public final ArrayList<BpayProduct> getProduct() {
        return product;
    }

    public final void setProduct(ArrayList<BpayProduct> value) {
        product = value;
    }


	/*void WorldPackets::BattlePay::PurchaseProduct::Read()
	{
		this >> clientToken;
		this >> productID;
		this >> targetCharacter;

		uint32 strlen1 = this.readBit(6);
		uint32 strlen2 = this.readBit(12);
		WowSytem = this.readString(strlen1);
		PublicKey = this.readString(strlen2);
	}*/

    @Override
    public void write() {
        Log.outInfo(LogFilter.BattlePay, "SyncWowEntitlements");
        this.writeInt32((int) getPurchaseCount().size());
        this.writeInt32((int) getProduct().size());

        for (var purchases : getPurchaseCount()) {
            this.writeInt32(0); // productID ?
            this.writeInt32(0); // flags?
            this.writeInt32(0); // idem to flags?
            this.writeInt32(0); // always 0
            this.writeBits(0, 7); // always 0
            this.writeBit(false); // always false
        }

        for (var product : getProduct()) {
            this.write(product.getProductId());
            this.write(product.getType());
            this.write(product.getFlags());
            this.write(product.getUnk1());
            this.write(product.getDisplayId());
            this.write(product.getItemId());
            this.writeInt32(0);
            this.writeInt32(2);
            this.writeInt32(0);
            this.writeInt32(0);
            this.writeInt32(0);
            this.writeInt32(0);

            this.writeBits((int) product.getUnkString().length(), 8);
            this.writeBit(product.getUnkBits() != 0);
            this.writeBit(product.getUnkBit());
            this.writeBits((int) product.getItems().size(), 7);
            this.writeBit(product.getDisplay() != null);
            this.writeBit(false); // unk

            if (product.getUnkBits() != 0) {
                this.writeBits(product.getUnkBits(), 4);
            }

            this.flushBits();

            for (var productItem : product.getItems()) {
                this.writeInt32(productItem.getID());
                this.writeInt8(productItem.getUnkByte());
                this.writeInt32(productItem.getItemID());
                this.writeInt32(productItem.getQuantity());
                this.writeInt32(productItem.getUnkInt1());
                this.writeInt32(productItem.getUnkInt2());

                this.writeBit(productItem.isPet());
                this.writeBit(productItem.getPetResult() != 0);
                this.writeBit(productItem.getDisplay() != null);

                if (productItem.getPetResult() != 0) {
                    this.writeBits(productItem.getPetResult(), 4);
                }

                this.flushBits();

                if (productItem.getDisplay() != null) {
                    productItem.getDisplay().write(this);
                }
            }

            this.writeString(product.getUnkString());

            if (product.getDisplay() != null) {
                product.getDisplay().write(this);
            }
        }
    }
}
