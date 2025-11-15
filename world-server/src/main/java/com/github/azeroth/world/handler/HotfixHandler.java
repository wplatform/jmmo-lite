package com.github.azeroth.world.handler;

public class HotfixHandler {

    void HandleDBQueryBulk(WorldPackets::Hotfix::DBQueryBulk& dbQuery)
    {
        DB2StorageBase const* store = sDB2Manager.GetStorage(dbQuery.TableHash);
        for (WorldPackets::Hotfix::DBQueryBulk::DBQueryRecord const& record : dbQuery.Queries)
        {
            WorldPackets::Hotfix::DBReply dbReply;
            dbReply.TableHash = dbQuery.TableHash;
            dbReply.RecordID = record.RecordID;

            if (store && store->HasRecord(record.RecordID))
            {
                dbReply.Status = DB2Manager::HotfixRecord::Status::Valid;
                dbReply.Timestamp = GameTime::GetGameTime();
                store->WriteRecord(record.RecordID, GetSessionDbcLocale(), dbReply.Data);

                if (std::vector<DB2Manager::HotfixOptionalData> const* optionalDataEntries = sDB2Manager.GetHotfixOptionalData(dbQuery.TableHash, record.RecordID, GetSessionDbcLocale()))
                {
                    for (DB2Manager::HotfixOptionalData const& optionalData : *optionalDataEntries)
                    {
                        dbReply.Data << uint32(optionalData.Key);
                        dbReply.Data.append(optionalData.Data.data(), optionalData.Data.size());
                    }
                }
            }
            else
            {
                TC_LOG_TRACE("network", "CMSG_DB_QUERY_BULK: {} requested non-existing entry {} in datastore: {}", GetPlayerInfo(), record.RecordID, dbQuery.TableHash);
                dbReply.Timestamp = GameTime::GetGameTime();
            }

            SendPacket(dbReply.Write());
        }
    }

    void SendAvailableHotfixes()
    {
        WorldPackets::Hotfix::AvailableHotfixes availableHotfixes;
        availableHotfixes.VirtualRealmAddress = GetVirtualRealmAddress();

        for (auto const& [pushId, push] : sDB2Manager.GetHotfixData())
        {
            if (!(push.AvailableLocalesMask & (1 << GetSessionDbcLocale())))
                continue;

            availableHotfixes.Hotfixes.insert(push.Records.front().ID);
        }

        SendPacket(availableHotfixes.Write());
    }

    void HandleHotfixRequest(WorldPackets::Hotfix::HotfixRequest& hotfixQuery)
    {
        DB2Manager::HotfixContainer const& hotfixes = sDB2Manager.GetHotfixData();
        WorldPackets::Hotfix::HotfixConnect hotfixQueryResponse;
        hotfixQueryResponse.Hotfixes.reserve(hotfixQuery.Hotfixes.size());
        for (int32 hotfixId : hotfixQuery.Hotfixes)
        {
            if (DB2Manager::HotfixPush const* hotfixRecords = Trinity::Containers::MapGetValuePtr(hotfixes, hotfixId))
            {
                for (DB2Manager::HotfixRecord const& hotfixRecord : hotfixRecords->Records)
                {
                    if (!(hotfixRecord.AvailableLocalesMask & (1 << GetSessionDbcLocale())))
                        continue;

                    WorldPackets::Hotfix::HotfixConnect::HotfixData& hotfixData = hotfixQueryResponse.Hotfixes.emplace_back();
                    hotfixData.Record = hotfixRecord;
                    if (hotfixRecord.HotfixStatus == DB2Manager::HotfixRecord::Status::Valid)
                    {
                        DB2StorageBase const* storage = sDB2Manager.GetStorage(hotfixRecord.TableHash);
                        if (storage && storage->HasRecord(uint32(hotfixRecord.RecordID)))
                        {
                            std::size_t pos = hotfixQueryResponse.HotfixContent.size();
                            storage->WriteRecord(uint32(hotfixRecord.RecordID), GetSessionDbcLocale(), hotfixQueryResponse.HotfixContent);

                            if (std::vector<DB2Manager::HotfixOptionalData> const* optionalDataEntries = sDB2Manager.GetHotfixOptionalData(hotfixRecord.TableHash, hotfixRecord.RecordID, GetSessionDbcLocale()))
                            {
                                for (DB2Manager::HotfixOptionalData const& optionalData : *optionalDataEntries)
                                {
                                    hotfixQueryResponse.HotfixContent << uint32(optionalData.Key);
                                    hotfixQueryResponse.HotfixContent.append(optionalData.Data.data(), optionalData.Data.size());
                                }
                            }

                            hotfixData.Size = hotfixQueryResponse.HotfixContent.size() - pos;
                        }
                        else if (std::vector<uint8> const* blobData = sDB2Manager.GetHotfixBlobData(hotfixRecord.TableHash, hotfixRecord.RecordID, GetSessionDbcLocale()))
                        {
                            hotfixData.Size = blobData->size();
                            hotfixQueryResponse.HotfixContent.append(blobData->data(), blobData->size());
                        }
                    else
                        // Do not send Status::Valid when we don't have a hotfix blob for current locale
                        hotfixData.Record.HotfixStatus = storage ? DB2Manager::HotfixRecord::Status::RecordRemoved : DB2Manager::HotfixRecord::Status::Invalid;
                    }
                }
            }
        }

        SendPacket(hotfixQueryResponse.Write());
    }

}
