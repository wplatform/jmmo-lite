package com.github.azeroth.world.handler;

public class CollectionsHandler {

    void HandleCollectionItemSetFavorite(WorldPackets::Collections::CollectionItemSetFavorite& collectionItemSetFavorite)
    {
        switch (collectionItemSetFavorite.Type)
        {
            case WorldPackets::Collections::TOYBOX:
            GetCollectionMgr()->ToySetFavorite(collectionItemSetFavorite.ID, collectionItemSetFavorite.IsFavorite);
                break;
            case WorldPackets::Collections::APPEARANCE:
            {
                auto [hasAppearance, isTemporary] = GetCollectionMgr()->HasItemAppearance(collectionItemSetFavorite.ID);
                if (!hasAppearance || isTemporary)
                    return;

                GetCollectionMgr()->SetAppearanceIsFavorite(collectionItemSetFavorite.ID, collectionItemSetFavorite.IsFavorite);
                break;
            }
            case WorldPackets::Collections::TRANSMOG_SET:
            break;
            default:
                break;
        }
    }
}
