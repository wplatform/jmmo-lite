package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;


class AhBotCommands {

    private static boolean handleAHBotRebuildCommand(CommandHandler handler, StringArguments args) {
		/*char* arg = strtok((char*)args, " ");

		bool all = false;
		if (arg && strcmp(arg, "all") == 0)
			all = true;

		sAuctionBot->Rebuild(all);*/
        return true;
    }


    private static boolean handleAHBotReloadCommand(CommandHandler handler, StringArguments args) {
        //sAuctionBot->ReloadAllConfig();
        //handler->SendSysMessage(LANG_AHBOT_RELOAD_OK);
        return true;
    }


    private static boolean handleAHBotStatusCommand(CommandHandler handler, StringArguments args) {
		/*   char* arg = strtok((char*)args, " ");
		if (!arg)
			return false;

		bool all = false;
		if (strcmp(arg, "all") == 0)
			all = true;

		AuctionHouseBotStatusInfo statusInfo;
		sAuctionBot->PrepareStatusInfos(statusInfo);

		WorldSession* session = handler->GetSession();

		if (!session)
		{
			handler->SendSysMessage(LANG_AHBOT_STATUS_BAR_CONSOLE);
			handler->SendSysMessage(LANG_AHBOT_STATUS_TITLE1_CONSOLE);
			handler->SendSysMessage(LANG_AHBOT_STATUS_MIDBAR_CONSOLE);
		}
		else
			handler->SendSysMessage(LANG_AHBOT_STATUS_TITLE1_CHAT);

		public uint fmtId = session ? LANG_AHBOT_STATUS_FORMAT_CHAT : LANG_AHBOT_STATUS_FORMAT_CONSOLE;

		handler->PSendSysMessage(fmtId, handler->getSysMessage(LANG_AHBOT_STATUS_ITEM_COUNT),
			statusInfo[AUCTION_HOUSE_ALLIANCE].itemsCount,
			statusInfo[AUCTION_HOUSE_HORDE].itemsCount,
			statusInfo[AUCTION_HOUSE_NEUTRAL].itemsCount,
			statusInfo[AUCTION_HOUSE_ALLIANCE].itemsCount +
			statusInfo[AUCTION_HOUSE_HORDE].itemsCount +
			statusInfo[AUCTION_HOUSE_NEUTRAL].itemsCount);

		if (all)
		{
			handler->PSendSysMessage(fmtId, handler->getSysMessage(LANG_AHBOT_STATUS_ITEM_RATIO),
				sAuctionBotConfig->GetConfig(CONFIG_AHBOT_ALLIANCE_ITEM_AMOUNT_RATIO),
				sAuctionBotConfig->GetConfig(CONFIG_AHBOT_HORDE_ITEM_AMOUNT_RATIO),
				sAuctionBotConfig->GetConfig(CONFIG_AHBOT_NEUTRAL_ITEM_AMOUNT_RATIO),
				sAuctionBotConfig->GetConfig(CONFIG_AHBOT_ALLIANCE_ITEM_AMOUNT_RATIO) +
				sAuctionBotConfig->GetConfig(CONFIG_AHBOT_HORDE_ITEM_AMOUNT_RATIO) +
				sAuctionBotConfig->GetConfig(CONFIG_AHBOT_NEUTRAL_ITEM_AMOUNT_RATIO));

			if (!session)
			{
				handler->SendSysMessage(LANG_AHBOT_STATUS_BAR_CONSOLE);
				handler->SendSysMessage(LANG_AHBOT_STATUS_TITLE2_CONSOLE);
				handler->SendSysMessage(LANG_AHBOT_STATUS_MIDBAR_CONSOLE);
			}
			else
				handler->SendSysMessage(LANG_AHBOT_STATUS_TITLE2_CHAT);

			for (int i = 0; i < MAX_AUCTION_QUALITY; ++i)
				handler->PSendSysMessage(fmtId, handler->getSysMessage(ahbotQualityIds[i]),
					statusInfo[AUCTION_HOUSE_ALLIANCE].QualityInfo[i],
					statusInfo[AUCTION_HOUSE_HORDE].QualityInfo[i],
					statusInfo[AUCTION_HOUSE_NEUTRAL].QualityInfo[i],
					sAuctionBotConfig->GetConfigItemQualityAmount(AuctionQuality(i)));
		}

		if (!session)
			handler->SendSysMessage(LANG_AHBOT_STATUS_BAR_CONSOLE);
		*/
        return true;
    }


    private static class ItemsCommands {

        private static boolean handleAHBotItemsAmountCommand(CommandHandler handler, StringArguments args) {
			/*public uint qVals[MAX_AUCTION_QUALITY];
			char* arg = strtok((char*)args, " ");
			for (int i = 0; i < MAX_AUCTION_QUALITY; ++i)
			{
				if (!arg)
					return false;
				qVals[i] = atoi(arg);
				arg = strtok(NULL, " ");
			}

			sAuctionBot->SetItemsAmount(qVals);

			for (int i = 0; i < MAX_AUCTION_QUALITY; ++i)
				handler->PSendSysMessage(LANG_AHBOT_ITEMS_AMOUNT, handler->getSysMessage(ahbotQualityIds[i]), sAuctionBotConfig->GetConfigItemQualityAmount(AuctionQuality(i)));
			*/
            return true;
        }


        private static boolean handleAHBotItemsAmountQualityBlue(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsAmountQualityGray(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsAmountQualityGreen(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsAmountQualityOrange(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsAmountQualityPurple(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsAmountQualityWhite(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsAmountQualityYellow(CommandHandler handler, StringArguments args) {
            return true;
        }

        private static boolean handleAHBotItemsAmountQualityCommand(CommandHandler handler, StringArguments args) {
			/*
			char* arg = strtok((char*)args, " ");
			if (!arg)
				return false;
			public uint qualityVal = atoi(arg);

			sAuctionBot->SetItemsAmountForQuality(Q, qualityVal);
			handler->PSendSysMessage(LANG_AHBOT_ITEMS_AMOUNT, handler->getSysMessage(ahbotQualityIds[Q]),
				sAuctionBotConfig->GetConfigItemQualityAmount(Q));
			*/
            return true;
        }
    }


    private static class RatioCommands {

        private static boolean handleAHBotItemsRatioCommand(CommandHandler handler, StringArguments args) {
			/*public uint rVal[MAX_AUCTION_QUALITY];
			char* arg = strtok((char*)args, " ");
			for (int i = 0; i < MAX_AUCTION_QUALITY; ++i)
			{
				if (!arg)
					return false;
				rVal[i] = atoi(arg);
				arg = strtok(NULL, " ");
			}

			sAuctionBot->SetItemsRatio(rVal[0], rVal[1], rVal[2]);

			for (int i = 0; i < MAX_AUCTION_HOUSE_TYPE; ++i)
				handler->PSendSysMessage(LANG_AHBOT_ITEMS_RATIO, AuctionBotConfig::GetHouseTypeName(AuctionHouseType(i)), sAuctionBotConfig->GetConfigItemAmountRatio(AuctionHouseType(i)));
			*/
            return true;
        }


        private static boolean handleAHBotItemsRatioHouseAlliance(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsRatioHouseHorde(CommandHandler handler, StringArguments args) {
            return true;
        }


        private static boolean handleAHBotItemsRatioHouseNeutral(CommandHandler handler, StringArguments args) {
            return true;
        }

        private static boolean handleAHBotItemsRatioHouseCommand(CommandHandler handler, StringArguments args) {
			/*char* arg = strtok((char*)args, " ");
			if (!arg)
				return false;
			public uint ratioVal = atoi(arg);

			sAuctionBot->SetItemsRatioForHouse(H, ratioVal);
			handler->PSendSysMessage(LANG_AHBOT_ITEMS_RATIO, AuctionBotConfig::GetHouseTypeName(H), sAuctionBotConfig->GetConfigItemAmountRatio(H));
			*/
            return true;
        }
    }
}
