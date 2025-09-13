package com.github.azeroth.game.battlepay;


import com.github.azeroth.game.networking.packet.bpay.*;
import game.BattlePayDataStoreMgr;
import game.WorldConfig;
import game.WorldSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;


public class BattlepayManager {
    private final TreeMap<Integer, BpayProduct> existProducts = new TreeMap<Integer, BpayProduct>();

    private final WorldSession session;
    private final String walletName = "";
    private purchase actualTransaction = new purchase();
    private long purchaseIDCount;
    private long distributionIDCount;

    public BattlepayManager(WorldSession session) {
        session = session;
        purchaseIDCount = 0;
        distributionIDCount = 0;
        walletName = "Credits";
    }

    public final int getBattlePayCredits() {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.LOGIN_SEL_BATTLE_PAY_ACCOUNT_CREDITS);

        stmt.AddValue(0, session.getBattlenetAccountId());

        var result_don = DB.Login.query(stmt);

        if (result_don == null) {
            return 0;
        }

        var fields = result_don.GetFields();
        var credits = fields.<Integer>Read(0);

        return credits * 10000; // currency precision .. in retail it like gold and copper .. 10 usd is 100000 battlepay credit
    }

    public final boolean hasBattlePayCredits(int count) {
        if (getBattlePayCredits() >= count) {
            return true;
        }

        session.getPlayer().sendSysMessage(20000, count);

        return false;
    }

    public final boolean updateBattlePayCredits(long price) {
        //TC_LOG_INFO("server.BattlePay", "UpdateBattlePayCredits: getBattlePayCredits(): {} - price: {}", getBattlePayCredits(), price);
        var calcCredit = (getBattlePayCredits() - price) / 10000;
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.LOGIN_UPD_BATTLE_PAY_ACCOUNT_CREDITS);
        stmt.AddValue(0, calcCredit);
        stmt.AddValue(1, session.getBattlenetAccountId());
        DB.Login.execute(stmt);

        return true;
    }


    public final boolean modifyBattlePayCredits(int credits) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.LOGIN_UPD_BATTLE_PAY_ACCOUNT_CREDITS);
        stmt.AddValue(0, credits);
        stmt.AddValue(1, session.getBattlenetAccountId());
        DB.Login.execute(stmt);
        sendBattlePayMessage(3, "", credits);

        return true;
    }


    public final void sendBattlePayMessage(int bpaymessageID, String name) {
        sendBattlePayMessage(bpaymessageID, name, 0);
    }

    public final void sendBattlePayMessage(int bpaymessageID, String name, int value) {
        var msg = "";

        if (bpaymessageID == 1) {
            msg += "The purchase '" + name + "' was successful!";
        }

        if (bpaymessageID == 2) {
            msg += "Remaining credits: " + getBattlePayCredits() / 10000 + " .";
        }

        if (bpaymessageID == 10) {
            msg += "You cannot purchase '" + name + "' . Contact a game master to find out more.";
        }

        if (bpaymessageID == 11) {
            msg += "Your bags are too full to add : " + name + " .";
        }

        if (bpaymessageID == 12) {
            msg += "You have already purchased : " + name + " .";
        }

        if (bpaymessageID == 20) {
            msg += "The battle pay credits have been updated for the character '" + name + "' ! Available credits:" + value + " .";
        }

        if (bpaymessageID == 21) {
            msg += "You must enter an amount !";
        }

        if (bpaymessageID == 3) {
            msg += "You have now '" + value + "' credits.";
        }

        session.getCommandHandler().sendSysMessage(msg);
    }

    public final void sendBattlePayBattlePetDelivered(ObjectGuid petguid, int creatureID) {
        var response = new BattlePayBattlePetDelivered();
        response.setDisplayID(creatureID);
        response.setBattlePetGuid(petguid);
        session.sendPacket(response);
        Log.outError(LogFilter.BattlePay, "Send BattlePayBattlePetDelivered guid: {} && creatureID: {}", petguid.getCounter(), creatureID);
    }

    public final int getShopCurrency() {
        return (int) ConfigMgr.GetDefaultValue("FeatureSystem.BpayStore.Currency", 1);
    }

    public final boolean isAvailable() {
        return WorldConfig.getBoolValue(WorldCfg.FeatureSystemBpayStoreEnabled);
    }

    public final boolean alreadyOwnProduct(int itemId) {
        var player = session.getPlayer();

        if (player) {
            var itemTemplate = global.getObjectMgr().getItemTemplate(itemId);

            if (itemTemplate == null) {
                return true;
            }

            for (var itr : itemTemplate.getEffects()) {
                if (itr.triggerType == ItemSpelltriggerType.OnLearn && player.hasSpell((int) itr.spellID)) {
                    return true;
                }
            }

            if (player.getItemCount(itemId) != 0) {
                return true;
            }
        }

        return false;
    }

    public final void savePurchase(Purchase purchase) {
        var productInfo = BattlePayDataStoreMgr.getInstance().getProductInfoForProduct(purchase.productID);
        var displayInfo = BattlePayDataStoreMgr.getInstance().getDisplayInfo(productInfo.getEntry());
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.LOGIN_INS_PURCHASE);
        stmt.AddValue(0, session.getAccountId());
        stmt.AddValue(1, global.getWorldMgr().getVirtualRealmAddress());
        stmt.AddValue(2, session.getPlayer() ? session.getPlayer().getGUID().getCounter() : 0);
        stmt.AddValue(3, purchase.productID);
        stmt.AddValue(4, displayInfo.getName1());
        stmt.AddValue(5, purchase.currentPrice);
        stmt.AddValue(6, session.getRemoteAddress());
        DB.Login.execute(stmt);
    }

    public final void processDelivery(Purchase purchase) {
        var player = session.getPlayer();

        if (!player) {
            return;
        }

        var productInfo = BattlePayDataStoreMgr.getInstance().getProductInfoForProduct(purchase.productID);
        var itemstosendinmail = new ArrayList<>();

        for (var productId : productInfo.getProductIds()) {
            var product = BattlePayDataStoreMgr.getInstance().getProduct(productId);
            var item = global.getObjectMgr().getItemTemplate(product.getFlags());
            var itemsToSendIfInventoryFull = new ArrayList<>();

            switch (ProductType.forValue(product.getType())) {
                case Item_: // 0
                    itemsToSendIfInventoryFull.clear();

                    if (item != null && player) {
                        if (player.getFreeInventorySpace() > product.getUnk1()) {
                            player.addItemWithToast(product.getFlags(), (short) product.getUnk1(), 0);
                        } else {
                            player.sendABunchOfItemsInMail(new ArrayList<Integer>(Arrays.asList(product.getFlags())), "Ingame Shop item delivery");
                        }
                    } else {
                        session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);
                    }

                    for (var _item : BattlePayDataStoreMgr.getInstance().getItemsOfProduct(product.getProductId())) {
                        if (global.getObjectMgr().getItemTemplate(_item.getItemID()) != null) {
                            if (player.getFreeInventorySpace() > _item.getQuantity()) {
                                player.addItemWithToast(_item.getItemID(), (short) _item.getQuantity(), 0);
                            } else {
                                itemsToSendIfInventoryFull.add(_item.getItemID()); // problem if the quantity > 0
                            }
                        }
                    }

                    if (!itemsToSendIfInventoryFull.isEmpty()) {
                        player.sendABunchOfItemsInMail(itemsToSendIfInventoryFull, "Ingame Shop Item Delivery");
                    }

                    break;

                case LevelBoost: // 1
                    if (product.getProductId() == 572) // level 50 boost
                    {
                        player.setLevel(50);

                        player.gearUpByLoadout(9, new ArrayList<Integer>(Arrays.asList(6771)));

                        player.initTalentForLevel();
                        player.initStatsForLevel();
                        player.updateSkillsForLevel();
                        player.learnDefaultSkills();
                        player.learnSpecializationSpells();
                        player.updateAllStats();
                        player.setFullHealth();
                        player.setFullPower(powerType.mana);
                    }

                    if (product.getProductId() == 630) // level 60 boost
                    {
                        player.setLevel(60);

                        player.gearUpByLoadout(9, new ArrayList<Integer>(Arrays.asList(6771)));

                        player.initTalentForLevel();
                        player.initStatsForLevel();
                        player.updateSkillsForLevel();
                        player.learnDefaultSkills();
                        player.learnSpecializationSpells();
                        player.updateAllStats();
                        player.setFullHealth();
                        player.setFullPower(powerType.mana);
                    }

                    break;

                case Pet: // 2
                    if (player) // if logged in
                    {
                        player.getSession().getBattlePayMgr().addBattlePetFromBpayShop(product.getItemId());
                    } else {
                        session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);
                    }

                    break;

                case Mount: // 3
                    session.getCollectionMgr().addMount(product.getDisplayId(), MountStatusFlags.NONE);

                    break;

                case WoWToken: // 4
                    if (item != null && player) {
                        if (player.getFreeInventorySpace() > product.getUnk1()) {
                            player.addItemWithToast(product.getFlags(), (short) product.getUnk1(), 0);
                        } else {
                            player.sendABunchOfItemsInMail(new ArrayList<Integer>(Arrays.asList(product.getFlags())), "Ingame Shop - WoW Token Delivery");
                        }
                    } else {
                        session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);
                    }

                    break;

                case NameChange: // 5
                    if (player) // if logged in
                    {
                        player.setAtLoginFlag(AtLoginFlags.Rename);
                    } else {
                        session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);
                    }

                    break;

                case FactionChange: // 6
                    if (player) // if logged in
                    {
                        player.setAtLoginFlag(AtLoginFlags.ChangeFaction); // not ok for 6 or 3 faction change - only does once yet
                    } else {
                        session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);
                    }

                    break;

                case RaceChange: // 8
                    if (player) // if logged in
                    {
                        player.setAtLoginFlag(AtLoginFlags.ChangeRace); // not ok for 6 or 3 faction change - only does once yet
                    } else {
                        session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);
                    }

                    break;

                case CharacterTransfer: // 11
                    // if u have multiple realms u have to implement this xD otherwise it sends error
                    session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);

                    break;

                case Toy: // 14
                    boolean fan;
                    tangible.OutObject<Boolean> tempOut_fan = new tangible.OutObject<Boolean>();
                    if (Boolean.tryParse(String.valueOf(product.getUnk1()), tempOut_fan)) {
                        fan = tempOut_fan.outArgValue;
                        session.getCollectionMgr().addToy(product.getFlags(), false, fan);
                    } else {
                        fan = tempOut_fan.outArgValue;
                    }

                    break;

                case Expansion: // 18
                    if (player) // if logged in
                    {
                        //player->SendMovieStart(936); // Play SL Intro - xD what else in a private server we don't sell expansions
                        player.sendMovieStart(957); // Play SL Outro - we are preparing for dragonflight xD
                    }

                    break;

                case GameTime: // 20
                    if (item != null && player) {
                        if (player.getFreeInventorySpace() > product.getUnk1()) {
                            player.addItemWithToast(product.getFlags(), (short) product.getUnk1(), 0);
                        } else {
                            player.sendABunchOfItemsInMail(new ArrayList<Integer>(Arrays.asList(product.getFlags())), "Ingame Shop - WoW Token Delivery");
                        }
                    } else {
                        session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);
                    }

                    break;

                case GuildNameChange: // 21
                case GuildFactionChange: // 22
                case GuildTransfer: // 23
                case GuildFactionTranfer: // 24
                    // Not implemented yet - need some more guild functions e.g.: getmembers
                    session.sendStartPurchaseResponse(session, getPurchase(), BpayError.PurchaseDenied);

                    break;

                case TransmogAppearance: // 26
                    session.getCollectionMgr().addTransmogSet(product.getUnk7());

                    break;

                /** Customs:
                 */
                case ItemSet: {
                    var its = global.getObjectMgr().getItemTemplates();

                    //C++ TO C# CONVERTER NOTE: 'auto' variable declarations are not supported in C#:
                    //ORIGINAL LINE: for (auto const& itemTemplatePair : its)
                    for (var itemTemplatePair : its.entrySet()) {
                        if (itemTemplatePair.getValue().ItemSet != product.getFlags()) {
                            continue;
                        }

                        var dest = new ArrayList<>();
                        var msg = player.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, itemTemplatePair.getKey(), 1);

                        if (msg == InventoryResult.Ok) {
                            var newItem = player.storeNewItem(dest, itemTemplatePair.getKey(), true);

                            player.sendNewItem(newItem, 1, true, false);
                        } else {
                            itemstosendinmail.add(itemTemplatePair.getValue().id);
                        }
                    }

                    if (!itemstosendinmail.isEmpty()) {
                        player.sendABunchOfItemsInMail(itemstosendinmail, "Ingame Shop - You bought an item set!");
                    }
                }

                break;

                case Gold: // 30
                    if (player) {
                        player.modifyMoney(product.getUnk7());
                    }

                    break;

                case Currency: // 31
                    if (player) {
                        player.modifyCurrency(product.getFlags(), (int) product.getUnk1()); // implement currencyID in DB
                    }

                    break;
				/*
								case Battlepay::CharacterCustomization:
									if (player)
										player->SetAtLoginFlag(AT_LOGIN_CUSTOMIZE);
									break;

								// Script by Legolast++
								case Battlepay::ProfPriAlchemy:

									player->HasSkill(SKILL_ALCHEMY);
									player->HasSkill(SKILL_SHADOWLANDS_ALCHEMY);
									LearnAllRecipesInProfession(player, SKILL_ALCHEMY);
									break;

								case Battlepay::ProfPriSastre:

									player->HasSkill(SKILL_TAILORING);
									player->HasSkill(SKILL_SHADOWLANDS_TAILORING);
									LearnAllRecipesInProfession(player, SKILL_TAILORING);
									break;
								case Battlepay::ProfPriJoye:

									player->HasSkill(SKILL_JEWELCRAFTING);
									player->HasSkill(SKILL_SHADOWLANDS_JEWELCRAFTING);
									LearnAllRecipesInProfession(player, SKILL_JEWELCRAFTING);
									break;
								case Battlepay::ProfPriHerre:

									player->HasSkill(SKILL_BLACKSMITHING);
									player->HasSkill(SKILL_SHADOWLANDS_BLACKSMITHING);
									LearnAllRecipesInProfession(player, SKILL_BLACKSMITHING);
									break;
								case Battlepay::ProfPriPele:

									player->HasSkill(SKILL_LEATHERWORKING);
									player->HasSkill(SKILL_SHADOWLANDS_LEATHERWORKING);
									LearnAllRecipesInProfession(player, SKILL_LEATHERWORKING);
									break;
								case Battlepay::ProfPriInge:

									player->HasSkill(SKILL_ENGINEERING);
									player->HasSkill(SKILL_SHADOWLANDS_ENGINEERING);
									LearnAllRecipesInProfession(player, SKILL_ENGINEERING);
									break;
								case Battlepay::ProfPriInsc:

									player->HasSkill(SKILL_INSCRIPTION);
									player->HasSkill(SKILL_SHADOWLANDS_INSCRIPTION);
									LearnAllRecipesInProfession(player, SKILL_INSCRIPTION);
									break;
								case Battlepay::ProfPriEncha:

									player->HasSkill(SKILL_ENCHANTING);
									player->HasSkill(SKILL_SHADOWLANDS_ENCHANTING);
									LearnAllRecipesInProfession(player, SKILL_ENCHANTING);
									break;
								case Battlepay::ProfPriDesu:

									player->HasSkill(SKILL_SKINNING);
									player->HasSkill(SKILL_SHADOWLANDS_SKINNING);
									LearnAllRecipesInProfession(player, SKILL_SKINNING);
									break;
								case Battlepay::ProfPriMing:

									player->HasSkill(SKILL_MINING);
									player->HasSkill(SKILL_SHADOWLANDS_MINING);
									LearnAllRecipesInProfession(player, SKILL_MINING);
									break;
								case Battlepay::ProfPriHerb:

									player->HasSkill(SKILL_HERBALISM);
									player->HasSkill(SKILL_SHADOWLANDS_HERBALISM);
									LearnAllRecipesInProfession(player, SKILL_HERBALISM);
									break;

								case Battlepay::ProfSecCoci:

									player->HasSkill(SKILL_COOKING);
									player->HasSkill(SKILL_SHADOWLANDS_COOKING);
									LearnAllRecipesInProfession(player, SKILL_COOKING);
									break;

								case Battlepay::Promo:
									if (!player)
										// Ridding
									player->LearnSpell(33388, true);
									player->LearnSpell(33391, true);
									player->LearnSpell(34090, true);
									player->LearnSpell(34091, true);
									player->LearnSpell(90265, true);
									player->LearnSpell(54197, true);
									player->LearnSpell(90267, true);
									// Mounts
									player->LearnSpell(63956, true);

									break;
								case Battlepay::RepClassic:
									if (!player)
										player->SetReputation(21, 42000);
									player->SetReputation(576, 42000);
									player->SetReputation(87, 42000);
									player->SetReputation(92, 42000);
									player->SetReputation(93, 42000);
									player->SetReputation(609, 42000);
									player->SetReputation(529, 42000);
									player->SetReputation(909, 42000);
									player->SetReputation(369, 42000);
									player->SetReputation(59, 42000);
									player->SetReputation(910, 42000);
									player->SetReputation(349, 42000);
									player->SetReputation(809, 42000);
									player->SetReputation(749, 42000);
									player->SetReputation(270, 42000);
									player->SetReputation(470, 42000);
									player->SetReputation(577, 42000);
									player->SetReputation(70, 42000);
									player->SetReputation(1357, 42000);
									player->SetReputation(1975, 42000);

									if (player->GetTeam() == ALLIANCE)
									{
										player->SetReputation(890, 42000);
										player->SetReputation(1691, 42000);
										player->SetReputation(1419, 42000);
										player->SetReputation(69, 42000);
										player->SetReputation(930, 42000);
										player->SetReputation(47, 42000);
										player->SetReputation(1134, 42000);
										player->SetReputation(54, 42000);
										player->SetReputation(730, 42000);
										player->SetReputation(509, 42000);
										player->SetReputation(1353, 42000);
										player->SetReputation(72, 42000);
										player->SetReputation(589, 42000);
									}
									else // Repu Horda
									{
										player->SetReputation(1690, 42000);
										player->SetReputation(1374, 42000);
										player->SetReputation(1133, 42000);
										player->SetReputation(81, 42000);
										player->SetReputation(729, 42000);
										player->SetReputation(68, 42000);
										player->SetReputation(889, 42000);
										player->SetReputation(510, 42000);
										player->SetReputation(911, 42000);
										player->SetReputation(76, 42000);
										player->SetReputation(1352, 42000);
										player->SetReputation(530, 42000);
									}
									player->GetSession()->SendNotification("|cff00FF00Se ha aumentado todas las Reputaciones Clasicas!");
									return;
									break;
								case Battlepay::RepBurnig:
									if (!player)
										player->SetReputation(1015, 42000);
									player->SetReputation(1011, 42000);
									player->SetReputation(933, 42000);
									player->SetReputation(967, 42000);
									player->SetReputation(970, 42000);
									player->SetReputation(942, 42000);
									player->SetReputation(1031, 42000);
									player->SetReputation(1012, 42000);
									player->SetReputation(990, 42000);
									player->SetReputation(932, 42000);
									player->SetReputation(934, 42000);
									player->SetReputation(935, 42000);
									player->SetReputation(1077, 42000);
									player->SetReputation(1038, 42000);
									player->SetReputation(989, 42000);

									if (player->GetTeam() == ALLIANCE)
									{
										player->SetReputation(946, 42000);
										player->SetReputation(978, 42000);
									}
									else // Repu Horda
									{
										player->SetReputation(941, 42000);
										player->SetReputation(947, 42000);
										player->SetReputation(922, 42000);
									}
									player->GetSession()->SendNotification("|cff00FF00Se ha aumentado todas las Reputaciones Burning Crusade!");
									return;
									break;
								case Battlepay::RepTLK:
									if (!player)
										player->SetReputation(1242, 42000);
									player->SetReputation(1376, 42000);
									player->SetReputation(1387, 42000);
									player->SetReputation(1135, 42000);
									player->SetReputation(1158, 42000);
									player->SetReputation(1173, 42000);
									player->SetReputation(1171, 42000);
									player->SetReputation(1204, 42000);
									if (player->GetTeam() == ALLIANCE)
									{
										player->SetReputation(1177, 42000);
										player->SetReputation(1174, 42000);
									}
									else // Repu Horda
									{
										player->SetReputation(1172, 42000);
										player->SetReputation(1178, 42000);
									}
									player->SetReputation(529, 42000);
									player->GetSession()->SendNotification("|cff00FF00Se ha aumentado todas las Reputaciones The Lich King!");
									return;
									break;
								case Battlepay::RepCata:
									if (!player)
										player->SetReputation(1091, 42000);
									player->SetReputation(1098, 42000);
									player->SetReputation(1106, 42000);
									player->SetReputation(1156, 42000);
									player->SetReputation(1090, 42000);
									player->SetReputation(1119, 42000);
									player->SetReputation(1073, 42000);
									player->SetReputation(1105, 42000);
									player->SetReputation(1104, 42000);

									if (player->GetTeam() == ALLIANCE)
									{
										player->SetReputation(1094, 42000);
										player->SetReputation(1050, 42000);
										player->SetReputation(1068, 42000);
										player->SetReputation(1126, 42000);
										player->SetReputation(1037, 42000);
									}
									else // Repu Horda
									{
										player->SetReputation(1052, 42000);
										player->SetReputation(1067, 42000);
										player->SetReputation(1124, 42000);
										player->SetReputation(1064, 42000);
										player->SetReputation(1085, 42000);
									}
									player->GetSession()->SendNotification("|cff00FF00Se ha aumentado todas las Reputaciones Cataclismo!");
									return;
									break;
								case Battlepay::RepPanda:
									if (!player)
										player->SetReputation(1216, 42000);
									player->SetReputation(1435, 42000);
									player->SetReputation(1277, 42000);
									player->SetReputation(1359, 42000);
									player->SetReputation(1275, 42000);
									player->SetReputation(1492, 42000);
									player->SetReputation(1281, 42000);
									player->SetReputation(1283, 42000);
									player->SetReputation(1279, 42000);
									player->SetReputation(1273, 42000);
									player->SetReputation(1341, 42000);
									player->SetReputation(1345, 42000);
									player->SetReputation(1337, 42000);
									player->SetReputation(1272, 42000);
									player->SetReputation(1351, 42000);
									player->SetReputation(1302, 42000);
									player->SetReputation(1269, 42000);
									player->SetReputation(1358, 42000);
									player->SetReputation(1271, 42000);
									player->SetReputation(1282, 42000);
									player->SetReputation(1440, 42000);
									player->SetReputation(1270, 42000);
									player->SetReputation(1278, 42000);
									player->SetReputation(1280, 42000);
									player->SetReputation(1276, 42000);

									if (player->GetTeam() == ALLIANCE)
									{
										player->SetReputation(1242, 42000);
										player->SetReputation(1376, 42000);
										player->SetReputation(1387, 42000);

									}
									else // Repu Horda
									{
										player->SetReputation(1388, 42000);
										player->SetReputation(1228, 42000);
										player->SetReputation(1375, 42000);
									}
									player->GetSession()->SendNotification("|cff00FF00Se ha aumentado todas las Reputaciones de Pandaria!");
									return;
									break;
								case Battlepay::RepDraenor:
									if (!player)
										player->SetReputation(1850, 42000);
									player->SetReputation(1515, 42000);
									player->SetReputation(1520, 42000);
									player->SetReputation(1732, 42000);
									player->SetReputation(1735, 42000);
									player->SetReputation(1741, 42000);
									player->SetReputation(1849, 42000);
									player->SetReputation(1737, 42000);
									player->SetReputation(1711, 42000);
									player->SetReputation(1736, 42000);
									// Repu Alianza
									if (player->GetTeam() == ALLIANCE)
									{
										player->SetReputation(1731, 42000);
										player->SetReputation(1710, 42000);
										player->SetReputation(1738, 42000);
										player->SetReputation(1733, 42000);
										player->SetReputation(1847, 42000);
										player->SetReputation(1682, 42000);
									}
									else // Repu Horda
									{
										player->SetReputation(1740, 42000);
										player->SetReputation(1681, 42000);
										player->SetReputation(1445, 42000);
										player->SetReputation(1708, 42000);
										player->SetReputation(1848, 42000);
										player->SetReputation(1739, 42000);
									}
									player->GetSession()->SendNotification("|cff00FF00Se ha aumentado todas las Reputaciones de Draenor!");
									return;
									break;
								case Battlepay::RepLegion:
									if (!player)
										player->SetReputation(1919, 42000);
									player->SetReputation(1859, 42000);
									player->SetReputation(1900, 42000);
									player->SetReputation(1899, 42000);
									player->SetReputation(1989, 42000);
									player->SetReputation(1947, 42000);
									player->SetReputation(1894, 42000);
									player->SetReputation(1984, 42000);
									player->SetReputation(1862, 42000);
									player->SetReputation(1861, 42000);
									player->SetReputation(1860, 42000);
									player->SetReputation(1815, 42000);
									player->SetReputation(1883, 42000);
									player->SetReputation(1828, 42000);
									player->SetReputation(1948, 42000);
									player->SetReputation(2018, 42000);
									player->SetReputation(1888, 42000);
									player->SetReputation(2045, 42000);
									player->SetReputation(2170, 42000);
									player->SetReputation(2165, 42000);
									player->GetSession()->SendNotification("|cff00FF00Se ha aumentado todas las Reputaciones de Legion!");
									return;
									break;
				*/
                case PremadePve:
                    if (!player) // Bags
                    {
                        for (var slot = InventorySlots.BagStart; slot < InventorySlots.BagEnd; slot++) {
                            player.equipNewItem(slot, 142075, itemContext.NONE, true);
                        }
                    }

                    player.giveLevel(60);
                    player.initTalentForLevel();
                    player.modifyMoney(200000000);
                    player.learnSpell(33388, true); // Equitacion
                    player.learnSpell(33391, true);
                    player.learnSpell(34090, true);
                    player.learnSpell(34091, true);
                    player.learnSpell(90265, true);
                    player.learnSpell(54197, true);
                    player.learnSpell(90267, true);
                    player.learnSpell(115913, true);
                    player.learnSpell(110406, true);
                    player.learnSpell(104381, true);

                    if (player.getClass() == playerClass.Shaman) {
                        player.equipNewItem(EquipmentSlot.Head, 199444, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199448, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199447, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199443, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199441, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199445, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 199446, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199442, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Hunter) {
                        player.equipNewItem(EquipmentSlot.Head, 198592, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 198596, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 198595, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 198591, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 198589, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 198593, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 198594, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 198590, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Mage) {
                        player.equipNewItem(EquipmentSlot.Head, 198568, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 198571, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 198570, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 198567, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 198565, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 198569, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 198572, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 198566, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Priest) {
                        player.equipNewItem(EquipmentSlot.Head, 199420, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199423, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199422, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199419, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199417, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199421, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 19942, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199418, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Warlock) {
                        player.equipNewItem(EquipmentSlot.Head, 199420, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199423, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199422, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199419, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199417, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199421, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 19942, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199418, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.DemonHunter) {
                        player.equipNewItem(EquipmentSlot.Head, 198575, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 198578, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 198577, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 198574, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 198579, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 198576, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 198580, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 198573, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Rogue) {
                        player.equipNewItem(EquipmentSlot.Head, 199427, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199430, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199429, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199426, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199431, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199428, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 199432, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199425, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Monk) {
                        player.equipNewItem(EquipmentSlot.Head, 198575, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 198578, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 198577, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 198574, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 198579, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 198576, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 198580, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 198573, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Druid) {
                        player.equipNewItem(EquipmentSlot.Head, 199427, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199430, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199429, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199426, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199431, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199428, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 199432, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199425, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Warrior) {
                        player.equipNewItem(EquipmentSlot.Head, 199433, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199440, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199439, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199436, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199434, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199437, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 199438, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199435, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Paladin) {
                        player.equipNewItem(EquipmentSlot.Head, 199433, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199440, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199439, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199436, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199434, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199437, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 199438, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199435, itemContext.NONE, true);
                    }

                    if (player.getClass() == playerClass.Deathknight) {
                        var quest = global.getObjectMgr().getQuestTemplate(12801);

                        if (global.getObjectMgr().getQuestTemplate(12801) != null) {
                            player.addQuest(quest, null);
                            player.completeQuest(quest.id);
                            player.rewardQuest(quest, lootItemType.item, 0, null, false);
                        }

                        if (player.getTeamId() == TeamId.ALLIANCE) {
                            player.teleportTo(0, -8829.8710f, 625.3872f, 94.1712f, 3.808243f);
                        } else {
                            player.teleportTo(1, 1570.6693f, -4399.3388f, 16.0058f, 3.382241f);
                        }

                        player.learnSpell(53428, true); // runeforging
                        player.learnSpell(53441, true); // runeforging
                        player.learnSpell(54586, true); // runeforging credit
                        player.learnSpell(48778, true); //acherus deathcharger
                        player.learnSkillRewardedSpells(776, 375, race.NONE);
                        player.learnSkillRewardedSpells(960, 375, race.NONE);

                        player.equipNewItem(EquipmentSlot.Head, 198581, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 198587, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 198588, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 198584, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 198582, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 198585, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 198586, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 198583, itemContext.NONE, true);
                    }

                    // DRACTHYR DF
                    if (player.getClass() == playerClass.Evoker) {
                        player.equipNewItem(EquipmentSlot.Head, 199444, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Wrist, 199448, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Waist, 199447, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Hands, 199443, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.chest, 199441, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Legs, 199445, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Shoulders, 199446, itemContext.NONE, true);
                        player.equipNewItem(EquipmentSlot.Feet, 199442, itemContext.NONE, true);
                    }

                    break;
				/*
			case Battlepay::VueloDL:
				if (!player)
					player->AddItem(128706, 1);
				player->CompletedAchievement(sAchievementStore.LookupEntry(10018));
				player->CompletedAchievement(sAchievementStore.LookupEntry(11190));
				player->CompletedAchievement(sAchievementStore.LookupEntry(11446));
				player->GetSession()->SendNotification("|cff00FF00Has aprendido poder volar en las Islas Abruptas, Costas Abruptas y Draenor");
				break;
				//default:
					//break;
					*/
            }
        }
		/*
		if (!product->scriptName.empty())
			sScriptMgr->OnBattlePayProductDelivery(session, product);
			*/
    }

    public final void registerStartPurchase(Purchase purchase) {
        actualTransaction = purchase;
    }

    public final long generateNewPurchaseID() {
        return (0x1E77800000000000 | ++purchaseIDCount);
    }

    public final long generateNewDistributionId() {
        return (0x1E77800000000000 | ++distributionIDCount);
    }

    public final Purchase getPurchase() {
        return actualTransaction;
    }

    //C++ TO C# CONVERTER WARNING: 'const' methods are not available in C#:
    //ORIGINAL LINE: string const& getDefaultWalletName() const
    public final String getDefaultWalletName() {
        return walletName;
    }


    public final Tuple<Boolean, BpayDisplayInfo> writeDisplayInfo(int displayInfoEntry) {
        return writeDisplayInfo(displayInfoEntry, 0);
    }

    public final Tuple<Boolean, BpayDisplayInfo> writeDisplayInfo(int displayInfoEntry, int productId) {
        //C++ TO C# CONVERTER TASK: Lambda expressions cannot be assigned to 'var':
        var qualityColor = (int displayInfoOrProductInfoEntry) ->
        {
            var productAddon = BattlePayDataStoreMgr.getInstance().getProductAddon(displayInfoOrProductInfoEntry);

            if (productAddon == null) {
                return "|cffffffff";
            }

            switch (BattlePayDataStoreMgr.getInstance().getProductAddon(displayInfoOrProductInfoEntry).getNameColorIndex()) {
                case 0:
                    return "|cffffffff";
                case 1:
                    return "|cff1eff00";
                case 2:
                    return "|cff0070dd";
                case 3:
                    return "|cffa335ee";
                case 4:
                    return "|cffff8000";
                case 5:
                    return "|cffe5cc80";
                case 6:
                    return "|cffe5cc80";
                default:
                    return "|cffffffff";
            }
        };

        var info = new BpayDisplayInfo();

        var displayInfo = BattlePayDataStoreMgr.getInstance().getDisplayInfo(displayInfoEntry);

        if (displayInfo == null) {
            return Tuple.create(false, info);
        }

        info.setCreatureDisplayID(displayInfo.getCreatureDisplayID());
        info.setVisualID(displayInfo.getVisualID());
        info.setName1(qualityColor(displayInfoEntry) + displayInfo.getName1());
        info.setName2(displayInfo.getName2());
        info.setName3(displayInfo.getName3());
        info.setName4(displayInfo.getName4());
        info.setName5(displayInfo.getName5());
        info.setName6(displayInfo.getName6());
        info.setName7(displayInfo.getName7());
        info.setFlags(displayInfo.getFlags());
        info.setUnk1(displayInfo.getUnk1());
        info.setUnk2(displayInfo.getUnk2());
        info.setUnk3(displayInfo.getUnk3());
        info.setUnkInt1(displayInfo.getUnkInt1());
        info.setUnkInt2(displayInfo.getUnkInt2());
        info.setUnkInt3(displayInfo.getUnkInt3());

        for (var v = 0; v < displayInfo.getVisuals().size(); v++) {
            var visual = displayInfo.getVisuals().get(v);

            var _Visual = new BpayVisual();
            _Visual.setName(visual.getName());
            _Visual.setDisplayId(visual.getDisplayId());
            _Visual.setVisualId(visual.getVisualId());
            _Visual.setUnk(visual.getUnk());

            info.getVisuals().add(_Visual);
        }

        if (displayInfo.getFlags() != 0) {
            info.setFlags(displayInfo.getFlags());
        }

        return Tuple.create(true, info);
    }

    //C++ TO C# CONVERTER TASK: There is no C# equivalent to C++ suffix return type syntax:
    //ORIGINAL LINE: auto ProductFilter(WorldPackets::BattlePay::Product product)->bool;
    //C++ TO C# CONVERTER TASK: The return type of the following function could not be determined:
    //C++ TO C# CONVERTER TASK: The implementation of the following method could not be found:
    //	auto ProductFilter(WorldPackets::BattlePay::Product product);
    public final void sendProductList() {
        var response = new ProductListResponse();
        var player = session.getPlayer(); // it's a false value if player is in character screen

        if (!isAvailable()) {
            response.setResult((int) ProductListResult.LockUnk1.getValue());
            session.sendPacket(response);

            return;
        }

        response.setResult((int) ProductListResult.Available.getValue());
        response.setCurrencyID(getShopCurrency() > 0 ? getShopCurrency() : 1);

        // BATTLEPAY GROUP
        for (var itr : BattlePayDataStoreMgr.getInstance().getProductGroups()) {
            var group = new BpayGroup();
            group.setGroupId(itr.getGroupId());
            group.setIconFileDataID(itr.getIconFileDataID());
            group.setDisplayType(itr.getDisplayType());
            group.setOrdering(itr.getOrdering());
            group.setUnk(itr.getUnk());
            group.setName(itr.getName());
            group.setDescription(itr.getDescription());

            response.getProductGroups().add(group);
        }

        // BATTLEPAY SHOP
        for (var itr : BattlePayDataStoreMgr.getInstance().getShopEntries()) {
            var shop = new BpayShop();
            shop.setEntryId(itr.getEntryId());
            shop.setGroupID(itr.getGroupID());
            shop.setProductID(itr.getProductID());
            shop.setOrdering(itr.getOrdering());
            shop.setVasServiceType(itr.getVasServiceType());
            shop.setStoreDeliveryType(itr.getStoreDeliveryType());

            // shop entry and display entry must be the same
            var data = writeDisplayInfo(itr.getEntry());

            if (data.Item1) {
                shop.setDisplay(data.item2);
            }

            // when logged out don't show everything
            if (player == null && shop.getStoreDeliveryType() != 2) {
                continue;
            }

            var productAddon = BattlePayDataStoreMgr.getInstance().getProductAddon(itr.getEntry());

            if (productAddon != null) {
                if (productAddon.getDisableListing() > 0) {
                    continue;
                }
            }

            response.getShops().add(shop);
        }

        // BATTLEPAY PRODUCT INFO
        for (var itr : BattlePayDataStoreMgr.getInstance().getProductInfos().entrySet()) {
            var productInfo = itr.getValue();

            var productAddon = BattlePayDataStoreMgr.getInstance().getProductAddon(productInfo.entry);

            if (productAddon != null) {
                if (productAddon.getDisableListing() > 0) {
                    continue;
                }
            }

            var productinfo = new BpayProductInfo();
            productinfo.setProductId(productInfo.productId);
            productinfo.setNormalPriceFixedPoint(productInfo.normalPriceFixedPoint);
            productinfo.setCurrentPriceFixedPoint(productInfo.currentPriceFixedPoint);
            productinfo.setProductIds(productInfo.productIds);
            productinfo.setUnk1(productInfo.unk1);
            productinfo.setUnk2(productInfo.unk2);
            productinfo.setUnkInts(productInfo.unkInts);
            productinfo.setUnk3(productInfo.unk3);
            productinfo.setChoiceType(productInfo.choiceType);

            // productinfo entry and display entry must be the same
            var data = writeDisplayInfo(productInfo.entry);

            if (data.Item1) {
                productinfo.setDisplay(data.item2);
            }

            response.getProductInfos().add(productinfo);
        }

        for (var itr : BattlePayDataStoreMgr.getInstance().getProducts().entrySet()) {
            var product = itr.getValue();
            var productInfo = BattlePayDataStoreMgr.getInstance().getProductInfoForProduct(product.productId);

            var productAddon = BattlePayDataStoreMgr.getInstance().getProductAddon(productInfo.getEntry());

            if (productAddon != null) {
                if (productAddon.getDisableListing() > 0) {
                    continue;
                }
            }

            // BATTLEPAY PRODUCTS
            var pProduct = new BpayProduct();
            pProduct.setProductId(product.productId);
            pProduct.setType(product.type);
            pProduct.setFlags(product.flags);
            pProduct.setUnk1(product.unk1);
            pProduct.setDisplayId(product.displayId);
            pProduct.setItemId(product.itemId);
            pProduct.setUnk4(product.unk4);
            pProduct.setUnk5(product.unk5);
            pProduct.setUnk6(product.unk6);
            pProduct.setUnk7(product.unk7);
            pProduct.setUnk8(product.unk8);
            pProduct.setUnk9(product.unk9);
            pProduct.setUnkString(product.unkString);
            pProduct.setUnkBit(product.unkBit);
            pProduct.setUnkBits(product.unkBits);

            // BATTLEPAY ITEM
            if (product.items.count > 0) {
                for (var item : BattlePayDataStoreMgr.getInstance().getItemsOfProduct(product.productId)) {
                    var pItem = new BpayProductItem();
                    pItem.setID(item.getID());
                    pItem.setUnkByte(item.getUnkByte());
                    pItem.setItemID(item.getItemID());
                    pItem.setQuantity(item.getQuantity());
                    pItem.setUnkInt1(item.getUnkInt1());
                    pItem.setUnkInt2(item.getUnkInt2());
                    pItem.setPet(item.isPet());
                    pItem.setPetResult(item.getPetResult());

                    if (BattlePayDataStoreMgr.getInstance().displayInfoExist(productInfo.getEntry())) {
                        // productinfo entry and display entry must be the same
                        var disInfo = writeDisplayInfo(productInfo.getEntry());

                        if (disInfo.Item1) {
                            pItem.setDisplay(disInfo.item2);
                        }
                    }

                    pProduct.getItems().add(pItem);
                }
            }

            // productinfo entry and display entry must be the same
            var data = writeDisplayInfo(productInfo.getEntry());

            if (data.Item1) {
                pProduct.setDisplay(data.item2);
            }

            response.getProducts().add(pProduct);
        }

		/*
		// debug
		TC_LOG_INFO("server.BattlePay", "SendProductList with {} productInfos, {} products, {} shops. CurrencyID: {}.", response.productInfos.size(), response.products.size(), response.shops.size(), response.currencyID);
		for (int i = 0; i != response.productInfos.size(); i++)
		{
			TC_LOG_INFO("server.BattlePay", "({}) ProductInfo: ProductId [{}], First SubProductId [{}], CurrentPriceFixedPoint [{}]", i, response.ProductInfos[i].productId, response.ProductInfos[i].ProductIds[0], response.ProductInfos[i].currentPriceFixedPoint);
			TC_LOG_INFO("server.BattlePay", "({}) Products: ProductId [{}], UnkString [{}]", i, response.Products[i].productId, response.Products[i].unkString);
			TC_LOG_INFO("server.BattlePay", "({}) Shops: ProductId [{}]", i, response.Shops[i].productID);
		}
		*/

        session.sendPacket(response);
    }

    public final void sendAccountCredits() {
        //    auto sessionId = _session->GetAccountId();
        //
        //    LoginDatabasePreparedStatement* stmt = DB.Login.GetPreparedStatement(LOGIN_SEL_BATTLE_PAY_ACCOUNT_CREDITS);
        //    stmt->setUInt32(0, _session->GetAccountId());
        //    PreparedQueryResult result = DB.Login.query(stmt);
        //
        //    auto sSession = sWorld->FindSession(sessionId);
        //    if (!sSession)
        //        return;
        //
        //    uint64 balance = 0;
        //    if (result)
        //    {
        //        auto fields = result->Fetch();
        //        if (auto balanceStr = fields[0].GetCString())
        //            balance = atoi(balanceStr);
        //    }
        //
        //    auto player = sSession->GetPlayer();
        //    if (!player)
        //        return;
        //
        //    sendBattlePayMessage(2, "");
    }

    public final void sendBattlePayDistribution(int productId, short status, long distributionId, ObjectGuid targetGuid) {
        var distributionBattlePay = new DistributionUpdate();
        var product = BattlePayDataStoreMgr.getInstance().getProduct(productId);

        var productInfo = BattlePayDataStoreMgr.getInstance().getProductInfoForProduct(productId);

        distributionBattlePay.getDistributionObject().setDistributionID(distributionId);
        distributionBattlePay.getDistributionObject().setStatus(status);
        distributionBattlePay.getDistributionObject().setProductID(productId);
        distributionBattlePay.getDistributionObject().setRevoked(false); // not needed for us

        if (!targetGuid.isEmpty()) {
            distributionBattlePay.getDistributionObject().setTargetPlayer(targetGuid);
            distributionBattlePay.getDistributionObject().setTargetVirtualRealm(global.getWorldMgr().getVirtualRealmAddress());
            distributionBattlePay.getDistributionObject().setTargetNativeRealm(global.getWorldMgr().getVirtualRealmAddress());
        }

        var productData = new BpayProduct();

        productData.setProductId(product.getProductId());
        productData.setType(product.getType());
        productData.setFlags(product.getFlags());
        productData.setUnk1(product.getUnk1());
        productData.setDisplayId(product.getDisplayId());
        productData.setItemId(product.getItemId());
        productData.setUnk4(product.getUnk4());
        productData.setUnk5(product.getUnk5());
        productData.setUnk6(product.getUnk6());
        productData.setUnk7(product.getUnk7());
        productData.setUnk8(product.getUnk8());
        productData.setUnk9(product.getUnk9());
        productData.setUnkString(product.getUnkString());
        productData.setUnkBit(product.getUnkBit());
        productData.setUnkBits(product.getUnkBits());

        for (var item : BattlePayDataStoreMgr.getInstance().getItemsOfProduct(product.getProductId())) {
            var productItem = new BpayProductItem();

            productItem.setID(item.getID());
            productItem.setUnkByte(item.getUnkByte());
            productItem.setItemID(item.getItemID());
            productItem.setQuantity(item.getQuantity());
            productItem.setUnkInt1(item.getUnkInt1());
            productItem.setUnkInt2(item.getUnkInt2());
            productItem.setPet(item.isPet());
            productItem.setPetResult(item.getPetResult());

            var dInfo = writeDisplayInfo(productInfo.getEntry());

            if (dInfo.Item1) {
                productItem.setDisplay(dInfo.item2);
            }
        }

        var data = writeDisplayInfo(productInfo.getEntry());

        if (data.Item1) {
            productData.setDisplay(data.item2);
        }

        distributionBattlePay.getDistributionObject().setProduct(productData);
        session.sendPacket(distributionBattlePay);
    }

    public final void assignDistributionToCharacter(final ObjectGuid targetCharGuid, long distributionId, int productId, short specialization_id, short choice_id) {
        var upgrade = new UpgradeStarted();
        upgrade.setCharacterGUID(targetCharGuid);
        session.sendPacket(upgrade);

        var assignResponse = new BattlePayStartDistributionAssignToTargetResponse();
        assignResponse.setDistributionID(distributionId);
        assignResponse.setUnkint1(0);
        assignResponse.setUnkint2(0);
        session.sendPacket(upgrade);

        var purchase = getPurchase();
        purchase.status = (short) BpayDistributionStatus.ADD_TO_PROCESS.getValue(); // DistributionStatus.Globals.BATTLE_PAY_DIST_STATUS_ADD_TO_PROCESS;

        sendBattlePayDistribution(productId, purchase.status, distributionId, targetCharGuid);
    }

    public final void update(int diff) {
        Log.outInfo(LogFilter.BattlePay, "BattlepayManager::Update");
		/*
		auto& data = actualTransaction;
		auto product = sBattlePayDataStore->GetProduct(data.productID);

		switch (data.status)
		{
		case Battlepay::Properties::DistributionStatus::BATTLE_PAY_DIST_STATUS_ADD_TO_PROCESS:
		{

			switch (product->type)
			{
			case CharacterBoost:
			{
				auto const& player = data.targetCharacter;
				if (!player)
					break;

				WorldPackets::BattlePay::BattlePayCharacterUpgradeQueued responseQueued;
				responseQueued.EquipmentItems = sDB2Manager.GetItemLoadOutItemsByClassID(player->getClass(), 3)[0];
				responseQueued.Character = data.targetCharacter;
				_session->SendPacket(responseQueued.write());

				data.status = DistributionStatus::BATTLE_PAY_DIST_STATUS_PROCESS_COMPLETE;
				sendBattlePayDistribution(data.productID, data.status, data.distributionId, data.targetCharacter);
				break;
			}
			default:
				break;
			}
			break;

		}
		case Battlepay::Properties::DistributionStatus::BATTLE_PAY_DIST_STATUS_PROCESS_COMPLETE: //send SMSG_BATTLE_PAY_VAS_PURCHASE_STARTED
		{
			switch (product->WebsiteType)
			{
			case CharacterBoost:
			{
				data.status = DistributionStatus::BATTLE_PAY_DIST_STATUS_FINISHED;
				sendBattlePayDistribution(data.productID, data.status, data.distributionId, data.targetCharacter);
				break;
			}
			default:
				break;
			}
			break;
		}
		case Battlepay::Properties::DistributionStatus::BATTLE_PAY_DIST_STATUS_FINISHED:
		{
			switch (product->WebsiteType)
			{
			case CharacterBoost:
				sendBattlePayDistribution(data.productID, data.status, data.distributionId, data.targetCharacter);
				break;
			default:
				break;
			}
			break;
		}
		case Battlepay::Properties::DistributionStatus::BATTLE_PAY_DIST_STATUS_AVAILABLE:
		case Battlepay::Properties::DistributionStatus::BATTLE_PAY_DIST_STATUS_NONE:
		default:
			break;
		}
		*/
    }

    //C++ TO C# CONVERTER WARNING: 'const' methods are not available in C#:
    //ORIGINAL LINE: void addBattlePetFromBpayShop(uint battlePetCreatureID) const
    public final void addBattlePetFromBpayShop(int battlePetCreatureID) {
        var speciesEntry = battlepets.BattlePetMgr.getBattlePetSpeciesByCreature(battlePetCreatureID);

        if (battlepets.BattlePetMgr.getBattlePetSpeciesByCreature(battlePetCreatureID) != null) {
            session.getBattlePetMgr().addPet(speciesEntry.id, battlepets.BattlePetMgr.selectPetDisplay(speciesEntry), battlepets.BattlePetMgr.rollPetBreed(speciesEntry.id), battlepets.BattlePetMgr.getDefaultPetQuality(speciesEntry.id));

            //it gives back false information need to get the pet guid from the add pet method somehow
            sendBattlePayBattlePetDelivered(ObjectGuid.create(HighGuid.BattlePet, global.getObjectMgr().getGenerator(HighGuid.BattlePet).generate()), speciesEntry.creatureID);
        }
    }
}
