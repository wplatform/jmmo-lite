package com.github.azeroth.game.chat;



class PdumpCommand {


    private static boolean handlePDumpCopyCommand(CommandHandler handler, PlayerIdentifier player, AccountIdentifier account, String characterName, Long characterGUID) {
		/*
			std::string name;
			if (!ValidatePDumpTarget(handler, name, characterName, characterGUID))
				return false;

			std::string dump;
			switch (PlayerDumpWriter().WriteDumpToString(dump, player.getGUID().GetCounter()))
			{
				case DUMP_SUCCESS:
					break;
				case DUMP_CHARACTER_DELETED:
					handler->PSendSysMessage(LANG_COMMAND_EXPORT_DELETED_CHAR);
					handler->SetSentErrorMessage(true);
					return false;
				case DUMP_FILE_OPEN_ERROR: // this error code should not happen
				default:
					handler->PSendSysMessage(LANG_COMMAND_EXPORT_FAILED);
					handler->SetSentErrorMessage(true);
					return false;
			}

			switch (PlayerDumpReader().LoadDumpFromString(dump, account, name, characterGUID.value_or(0)))
			{
				case DUMP_SUCCESS:
					break;
				case DUMP_TOO_MANY_CHARS:
					handler->PSendSysMessage(LANG_ACCOUNT_CHARACTER_LIST_FULL, account.getName().c_str(), account.getID());
					handler->SetSentErrorMessage(true);
					return false;
				case DUMP_FILE_OPEN_ERROR: // this error code should not happen
				case DUMP_FILE_BROKEN: // this error code should not happen
				default:
					handler->PSendSysMessage(LANG_COMMAND_IMPORT_FAILED);
					handler->SetSentErrorMessage(true);
					return false;
			}

			// ToDo: use a new trinity_string for this commands
			handler->PSendSysMessage(LANG_COMMAND_IMPORT_SUCCESS);
			*/
        return true;
    }



    private static boolean handlePDumpLoadCommand(CommandHandler handler, String fileName, AccountIdentifier account, String characterName, Long characterGuid) {
		/*
			if (!AccountMgr.normalizeString(accountName))
			{
				handler.sendSysMessage(LANG_ACCOUNT_NOT_EXIST, accountName);
				handler.setSentErrorMessage(true);
				return false;
			}

			public uint accountId = AccountMgr.getId(accountName);
			if (!accountId)
			{
				accountId = atoi(accountStr);                             // use original string
				if (!accountId)
				{
					handler.sendSysMessage(LANG_ACCOUNT_NOT_EXIST, accountName);

					return false;
				}
			}

			if (!AccountMgr.getName(accountId, accountName))
			{
				handler.sendSysMessage(LANG_ACCOUNT_NOT_EXIST, accountName);
				handler.setSentErrorMessage(true);
				return false;
			}

			string name;
			if (nameStr)
			{
				name = nameStr;
				// normalize the name if specified and check if it exists
				if (!ObjectManager.normalizePlayerName(name))
				{
					handler.sendSysMessage(LANG_INVALID_CHARACTER_NAME);

					return false;
				}

				if (ObjectMgr.checkPlayerName(name, true) != CHAR_NAME_SUCCESS)
				{
					handler.sendSysMessage(LANG_INVALID_CHARACTER_NAME);

					return false;
				}

				guidStr = strtok(NULL, " ");
			}

			public uint guid = 0;

			if (guidStr)
			{
				guid = uint32(atoi(guidStr));
				if (!guid)
				{
					handler.sendSysMessage(LANG_INVALID_CHARACTER_GUID);

					return false;
				}

				if (global.ObjectMgr.GetPlayerAccountIdByGUID(guid))
				{
					handler.sendSysMessage(LANG_CHARACTER_GUID_IN_USE, guid);

					return false;
				}
			}

			switch (PlayerDumpReader().LoadDump(fileStr, accountId, name, guid))
			{
				case DUMP_SUCCESS:
					handler.sendSysMessage(LANG_COMMAND_IMPORT_SUCCESS);
					break;
				case DUMP_FILE_OPEN_ERROR:
					handler.sendSysMessage(LANG_FILE_OPEN_FAIL, fileStr);

					return false;
				case DUMP_FILE_BROKEN:
					handler.sendSysMessage(LANG_DUMP_BROKEN, fileStr);

					return false;
				case DUMP_TOO_MANY_CHARS:
					handler.sendSysMessage(LANG_ACCOUNT_CHARACTER_LIST_FULL, accountName, accountId);

					return false;
				default:
					handler.sendSysMessage(LANG_COMMAND_IMPORT_FAILED);

					return false;
			}
			*/
        return true;
    }


    private static boolean handlePDumpWriteCommand(CommandHandler handler, String fileName, String playerName) {
		/*
			switch (PlayerDumpWriter().WriteDump(fileName, player.getGUID().GetCounter()))
			{
				case DUMP_SUCCESS:
					handler.sendSysMessage(LANG_COMMAND_EXPORT_SUCCESS);
					break;
				case DUMP_FILE_OPEN_ERROR:
					handler.sendSysMessage(LANG_FILE_OPEN_FAIL, fileName);

					return false;
				case DUMP_CHARACTER_DELETED:
					handler.sendSysMessage(LANG_COMMAND_EXPORT_DELETED_CHAR);

					return false;
				default:
					handler.sendSysMessage(LANG_COMMAND_EXPORT_FAILED);

					return false;
			}
			*/
        return true;
    }
}
