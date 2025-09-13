package com.github.azeroth.game.globals;


import com.github.azeroth.game.networking.packet.bpay.*;

import java.util.ArrayList;
import java.util.TreeMap;

public class BattlePayDataStoreMgr {
    private ArrayList<BpayGroup> productGroups = new ArrayList<>();
    private ArrayList<BpayShop> shopEntries = new ArrayList<>();
    private TreeMap<Integer, BpayProduct> products = new TreeMap<Integer, BpayProduct>();
    private TreeMap<Integer, BpayProductInfo> productInfos = new TreeMap<Integer, BpayProductInfo>();
    private TreeMap<Integer, BpayDisplayInfo> displayInfos = new TreeMap<Integer, BpayDisplayInfo>();
    private TreeMap<Integer, ProductAddon> productAddons = new TreeMap<Integer, ProductAddon>();

    public final ArrayList<BpayGroup> getProductGroups() {
        return productGroups;
    }

    private void setProductGroups(ArrayList<BpayGroup> value) {
        productGroups = value;
    }

    public final ArrayList<BpayShop> getShopEntries() {
        return shopEntries;
    }

    private void setShopEntries(ArrayList<BpayShop> value) {
        shopEntries = value;
    }

    public final TreeMap<Integer, BpayProduct> getProducts() {
        return products;
    }

    private void setProducts(TreeMap<Integer, BpayProduct> value) {
        products = value;
    }

    public final TreeMap<Integer, BpayProductInfo> getProductInfos() {
        return productInfos;
    }

    private void setProductInfos(TreeMap<Integer, BpayProductInfo> value) {
        productInfos = value;
    }

    public final TreeMap<Integer, BpayDisplayInfo> getDisplayInfos() {
        return displayInfos;
    }

    private void setDisplayInfos(TreeMap<Integer, BpayDisplayInfo> value) {
        displayInfos = value;
    }

    public final TreeMap<Integer, ProductAddon> getProductAddons() {
        return productAddons;
    }

    private void setProductAddons(TreeMap<Integer, ProductAddon> value) {
        productAddons = value;
    }

    public final void initialize() {
        loadProductAddons();
        loadDisplayInfos();
        loadProduct();
        loadProductGroups();
        loadShopEntries();
    }

    public final ArrayList<BpayProduct> getProductsOfProductInfo(int productInfoEntry) {
		/*std::vector<BattlePayData::Product> subproducts = {};

		for (auto productInfo : _productInfos)
			if (productInfo.second.entry == productInfoEntry)
				for (uint32 productid : productInfo.second.productIds)
				{
					//TC_LOG_INFO("server.BattlePay", "GetProductsOfProductInfo: found product [{}] at productInfo [{}]", productid, productInfoEntry);
					subproducts.push_back(*GetProduct(productid));
				}

		if (subproducts.size() > 0)
			return &subproducts; // warning*/

        Log.outInfo(LogFilter.BattlePay, "GetProductsOfProductInfo failed for productInfoEntry {}", productInfoEntry);

        return null;
    }

    public final ArrayList<BpayProductItem> getItemsOfProduct(int productID) {
        for (var product : getProducts().entrySet()) {
            if (product.getValue().productId == productID) {
                return product.getValue().items;
            }
        }

        Log.outInfo(LogFilter.BattlePay, "GetItemsOfProduct failed for productid {}", productID);

        return null;
    }

    public final BpayProduct getProduct(int productID) {
        return getProducts().GetValueOrDefault(productID);
    }


    // This awesome function returns back the productinfo for all the two types of productid!
    public final BpayProductInfo getProductInfoForProduct(int productID) {
        // Find product by subproduct id (_productInfos.productids) if not found find it by shop productid (_productInfos.productid)
        if (!(getProductInfos().containsKey(productID) && (var prod = getProductInfos().get(productID)) ==var prod))
        {
            for (var productInfo : getProductInfos().entrySet()) {
                if (productInfo.getValue().productId == productID) {
                    return productInfo.getValue();
                }
            }

            Log.outInfo(LogFilter.BattlePay, "GetProductInfoForProduct failed for productID {}", productID);

            return null;
        }

        return prod;
    }

    public final BpayDisplayInfo getDisplayInfo(int displayInfoEntry) {
        return getDisplayInfos().GetValueOrDefault(displayInfoEntry);
    }


    // Custom properties for each product (displayinfoEntry, productInfoEntry, shopEntry are the same)
    public final ProductAddon getProductAddon(int displayInfoEntry) {
        return getProductAddons().GetValueOrDefault(displayInfoEntry);
    }

    public final int getProductGroupId(int productId) {
        for (var shop : getShopEntries()) {
            if (shop.getProductID() == productId) {
                return shop.getGroupID();
            }
        }

        return 0;
    }

    public final boolean productExist(int productID) {
        if (getProducts().containsKey(productID)) {
            return true;
        }

        Log.outInfo(LogFilter.BattlePay, "ProductExist failed for productID {}", productID);

        return false;
    }

    public final boolean displayInfoExist(int displayInfoEntry) {
        if (getDisplayInfos().containsKey(displayInfoEntry)) {
            return true;
        }

        Log.outInfo(LogFilter.BattlePay, "DisplayInfoExist failed for displayInfoEntry {}", displayInfoEntry);

        return false;
    }

    private void loadProductAddons() {
        Log.outInfo(LogFilter.BattlePay, "Loading Battlepay display info addons ...");
        getProductAddons().clear();

        var result = DB.World.query("SELECT displayInfoEntry, disableListing, disableBuy, nameColorIndex, scriptName, Comment FROM battlepay_addon");

        if (result == null) {
            return;
        }

        do {
            var fields = result.GetFields();

            var productAddon = new ProductAddon();
            productAddon.setDisplayInfoEntry(fields.<Integer>Read(0));
            productAddon.setDisableListing(fields.<Byte>Read(1));
            productAddon.setDisableBuy(fields.<Byte>Read(2));
            productAddon.setNameColorIndex(fields.<Byte>Read(3));
            productAddon.setScriptName(fields.<String>Read(4));
            productAddon.setComment(fields.<String>Read(5));
            getProductAddons().put(fields.<Integer>Read(0), productAddon);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, ">> Loaded {} Battlepay product addons", (long) getProductAddons().size());
    }

    private void loadProductGroups() {
        Log.outInfo(LogFilter.ServerLoading, "Loading Battlepay product groups ...");
        getProductGroups().clear();

        var result = DB.World.query("SELECT entry, groupId, iconFileDataID, displayType, ordering, unk, name, Description FROM battlepay_group");

        if (result == null) {
            return;
        }

        do {
            var fields = result.GetFields();

            var productGroup = new BpayGroup();
            productGroup.setEntry(fields.<Integer>Read(0));
            productGroup.setGroupId(fields.<Integer>Read(1));
            productGroup.setIconFileDataID(fields.<Integer>Read(2));
            productGroup.setDisplayType(fields.<Byte>Read(3));
            productGroup.setOrdering(fields.<Integer>Read(4));
            productGroup.setUnk(fields.<Integer>Read(5));
            productGroup.setName(fields.<String>Read(6));
            productGroup.setDescription(fields.<String>Read(7));
            getProductGroups().add(productGroup);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, ">> Loaded {} Battlepay product groups", (long) getProductGroups().size());
    }

    private void loadProduct() {
        Log.outInfo(LogFilter.ServerLoading, "Loading Battlepay products ...");
        getProducts().clear();
        getProductInfos().clear();

        // Product Info
        var result = DB.World.query("SELECT entry, productId, normalPriceFixedPoint, currentPriceFixedPoint, productIds, unk1, unk2, unkInts, unk3, ChoiceType FROM battlepay_productinfo");

        if (result == null) {
            return;
        }

        do {
            var fields = result.GetFields();

            var productInfo = new BpayProductInfo();
            productInfo.setEntry(fields.<Integer>Read(0));
            productInfo.setProductId(fields.<Integer>Read(1));
            productInfo.setNormalPriceFixedPoint(fields.<Integer>Read(2));
            productInfo.setCurrentPriceFixedPoint(fields.<Integer>Read(3));
            var subproducts_stream = new LocalizedString();

            for (String subproduct : subproducts_stream) {
                int productId;
                tangible.OutObject<Integer> tempOut_productId = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(subproduct, tempOut_productId)) {
                    productId = tempOut_productId.outArgValue;
                    productInfo.getProductIds().add(productId); // another cool flux stuff: multiple subproducts can be added in one column
                } else {
                    productId = tempOut_productId.outArgValue;
                }
            }

            productInfo.setUnk1(fields.<Integer>Read(5));
            productInfo.setUnk2(fields.<Integer>Read(6));
            productInfo.getUnkInts().add(fields.<Integer>Read(7));
            productInfo.setUnk3(fields.<Integer>Read(8));
            productInfo.setChoiceType(fields.<Integer>Read(9));

            // we copy store the info for every product - some product info is the same for multiple products
            for (var subproductid : productInfo.getProductIds()) {
                getProductInfos().put(subproductid, productInfo);
            }
        } while (result.NextRow());

        // Product
        result = DB.World.query("SELECT entry, productId, type, flags, unk1, displayId, itemId, unk4, unk5, unk6, unk7, unk8, unk9, unkString, unkBit, unkBits, Name FROM battlepay_product");

        if (result == null) {
            return;
        }

        do {
            var fields = result.GetFields();

            var product = new BpayProduct();
            product.setEntry(fields.<Integer>Read(0));
            product.setProductId(fields.<Integer>Read(1));
            product.setType(fields.<Byte>Read(2));
            product.setFlags(fields.<Integer>Read(3));
            product.setUnk1(fields.<Integer>Read(4));
            product.setDisplayId(fields.<Integer>Read(5));
            product.setItemId(fields.<Integer>Read(6));
            product.setUnk4(fields.<Integer>Read(7));
            product.setUnk5(fields.<Integer>Read(8));
            product.setUnk6(fields.<Integer>Read(9));
            product.setUnk7(fields.<Integer>Read(10));
            product.setUnk8(fields.<Integer>Read(11));
            product.setUnk9(fields.<Integer>Read(12));
            product.setUnkString(fields.<String>Read(13));
            product.setUnkBit(fields.<Boolean>Read(14));
            product.setUnkBits(fields.<Integer>Read(15));
            product.setName(fields.<String>Read(16)); // unused in packets but useful in other ways

            getProducts().put(fields.<Integer>Read(1), product);
        } while (result.NextRow());

        // Product Items
        result = DB.World.query("SELECT ID, unkByte, itemID, quantity, unkInt1, unkInt2, isPet, petResult, Display FROM battlepay_item");

        if (result == null) {
            return;
        }

        do {
            var fields = result.GetFields();

            var productID = fields.<Integer>Read(1);

            if (!getProducts().containsKey(productID)) {
                continue;
            }

            var productItem = new BpayProductItem();

            productItem.setItemID(fields.<Integer>Read(2));

            if (global.getObjectMgr().getItemTemplate(productItem.getItemID()) != null) {
                continue;
            }

            productItem.setEntry(fields.<Integer>Read(0));
            productItem.setID(productID);
            productItem.setUnkByte(fields.<Byte>Read(2));
            productItem.setItemID(fields.<Integer>Read(3));
            productItem.setQuantity(fields.<Integer>Read(4));
            productItem.setUnkInt1(fields.<Integer>Read(5));
            productItem.setUnkInt2(fields.<Integer>Read(6));
            productItem.setPet(fields.<Boolean>Read(7));
            productItem.setPetResult(fields.<Integer>Read(8));
            getProducts().get(productID).getItems().add(productItem);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, ">> Loaded {} Battlepay product infos and {} Battlepay products", (long) getProductInfos().size(), (long) getProducts().size());
    }

    private void loadShopEntries() {
        Log.outInfo(LogFilter.ServerLoading, "Loading Battlepay shop entries ...");
        getShopEntries().clear();

        var result = DB.World.query("SELECT entry, entryID, groupID, productID, ordering, vasServiceType, StoreDeliveryType FROM battlepay_shop");

        if (result == null) {
            return;
        }

        do {
            var fields = result.GetFields();

            var shopEntry = new BpayShop();
            shopEntry.setEntry(fields.<Integer>Read(0));
            shopEntry.setEntryId(fields.<Integer>Read(1));
            shopEntry.setGroupID(fields.<Integer>Read(2));
            shopEntry.setProductID(fields.<Integer>Read(3));
            shopEntry.setOrdering(fields.<Integer>Read(4));
            shopEntry.setVasServiceType(fields.<Integer>Read(5));
            shopEntry.setStoreDeliveryType(fields.<Byte>Read(6));
            getShopEntries().add(shopEntry);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, ">> Loaded {} Battlepay shop entries", (long) getShopEntries().size());
    }

    private void loadDisplayInfos() {
        Log.outInfo(LogFilter.ServerLoading, "Loading Battlepay display info ...");
        getDisplayInfos().clear();

        var result = DB.World.query("SELECT entry, creatureDisplayID, visualID, name1, name2, name3, name4, name5, name6, name7, flags, unk1, unk2, unk3, unkInt1, unkInt2, UnkInt3 FROM battlepay_displayinfo");

        if (result == null) {
            return;
        }

        do {
            var fields = result.GetFields();

            var displayInfo = new BpayDisplayInfo();
            displayInfo.setEntry(fields.<Integer>Read(0));
            displayInfo.setCreatureDisplayID(fields.<Integer>Read(1));
            displayInfo.setVisualID(fields.<Integer>Read(2));
            displayInfo.setName1(fields.<String>Read(3));
            displayInfo.setName2(fields.<String>Read(4));
            displayInfo.setName3(fields.<String>Read(5));
            displayInfo.setName4(fields.<String>Read(6));
            displayInfo.setName5(fields.<String>Read(7));
            displayInfo.setName6(fields.<String>Read(8));
            displayInfo.setName7(fields.<String>Read(9));
            displayInfo.setFlags(fields.<Integer>Read(10));
            displayInfo.setUnk1(fields.<Integer>Read(11));
            displayInfo.setUnk2(fields.<Integer>Read(12));
            displayInfo.setUnk3(fields.<Integer>Read(13));
            displayInfo.setUnkInt1(fields.<Integer>Read(14));
            displayInfo.setUnkInt2(fields.<Integer>Read(15));
            displayInfo.setUnkInt3(fields.<Integer>Read(16));
            getDisplayInfos().put(fields.<Integer>Read(0), displayInfo);
        } while (result.NextRow());

        result = DB.World.query("SELECT entry, displayId, visualId, unk, name, DisplayInfoEntry FROM battlepay_visual");

        if (result == null) {
            return;
        }

        var visualCounter = 0;

        do {
            var fields = result.GetFields();

            visualCounter++;

            var visualInfo = new BpayVisual();
            visualInfo.setEntry(fields.<Integer>Read(0));
            visualInfo.setDisplayId(fields.<Integer>Read(1));
            visualInfo.setVisualId(fields.<Integer>Read(2));
            visualInfo.setUnk(fields.<Integer>Read(3));
            visualInfo.setName(fields.<String>Read(4));
            visualInfo.setDisplayInfoEntry(fields.<Integer>Read(5));

            if (!(getDisplayInfos().containsKey(visualInfo.getDisplayInfoEntry()) && (var
            bpayDisplayInfo = getDisplayInfos().get(visualInfo.getDisplayInfoEntry())) ==var bpayDisplayInfo))
            {
                continue;
            }

            bpayDisplayInfo.visuals.add(visualInfo);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, ">> Loaded {} Battlepay display info with {} visual.", (long) getDisplayInfos().size(), visualCounter);
    }
}
