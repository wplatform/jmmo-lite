package com.github.azeroth.game.networking.packet.petition;


import java.util.ArrayList;


public class ServerPetitionShowSignatures extends ServerPacket {
    public ObjectGuid item = ObjectGuid.EMPTY;
    public ObjectGuid owner = ObjectGuid.EMPTY;
    public ObjectGuid ownerAccountID = ObjectGuid.EMPTY;
    public int petitionID = 0;
    public ArrayList<PetitionSignature> signatures;

    public ServerPetitionShowSignatures() {
        super(ServerOpcode.PetitionShowSignatures);
        signatures = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeGuid(item);
        this.writeGuid(owner);
        this.writeGuid(ownerAccountID);
        this.writeInt32(petitionID);

        this.writeInt32(signatures.size());

        for (var signature : signatures) {
            this.writeGuid(signature.signer);
            this.writeInt32(signature.choice);
        }
    }

    public final static class PetitionSignature {
        public ObjectGuid signer = ObjectGuid.EMPTY;
        public int choice;

        public PetitionSignature clone() {
            PetitionSignature varCopy = new PetitionSignature();

            varCopy.signer = this.signer;
            varCopy.choice = this.choice;

            return varCopy;
        }
    }
}
