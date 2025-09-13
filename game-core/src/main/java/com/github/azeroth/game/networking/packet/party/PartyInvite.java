package com.github.azeroth.game.networking.packet.party;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class PartyInvite extends ServerPacket {
    public boolean mightCRZYou;
    public boolean mustBeBNetFriend;
    public boolean allowMultipleRoles;
    public boolean questSessionActive;
    public short unk1;

    public boolean canAccept;

    // Inviter
    public virtualRealmInfo inviterRealm = new virtualRealmInfo();
    public ObjectGuid inviterGUID = ObjectGuid.EMPTY;
    public ObjectGuid inviterBNetAccountId = ObjectGuid.EMPTY;
    public String inviterName;

    // Realm
    public boolean isXRealm;

    // Lfg
    public int proposedRoles;
    public int lfgCompletedMask;
    public ArrayList<Integer> lfgSlots = new ArrayList<>();

    public PartyInvite() {
        super(ServerOpcode.PartyInvite);
    }

    public final void initialize(Player inviter, int proposedRoles, boolean canAccept) {
        canAccept = canAccept;

        inviterName = inviter.getName();
        inviterGUID = inviter.getGUID();
        inviterBNetAccountId = inviter.getSession().getAccountGUID();

        proposedRoles = proposedRoles;

        var realm = global.getWorldMgr().getRealm();
        inviterRealm = new virtualRealmInfo(realm.id.GetAddress(), true, false, realm.name, realm.NormalizedName);
    }

    @Override
    public void write() {
        this.writeBit(canAccept);
        this.writeBit(mightCRZYou);
        this.writeBit(isXRealm);
        this.writeBit(mustBeBNetFriend);
        this.writeBit(allowMultipleRoles);
        this.writeBit(questSessionActive);
        this.writeBits(inviterName.getBytes().length, 6);

        inviterRealm.write(this);

        this.writeGuid(inviterGUID);
        this.writeGuid(inviterBNetAccountId);
        this.writeInt16(unk1);
        this.writeInt32(proposedRoles);
        this.writeInt32(lfgSlots.size());
        this.writeInt32(lfgCompletedMask);

        this.writeString(inviterName);

        for (var LfgSlot : lfgSlots) {
            this.writeInt32(LfgSlot);
        }
    }
}
