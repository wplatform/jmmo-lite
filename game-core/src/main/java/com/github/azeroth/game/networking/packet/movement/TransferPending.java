package com.github.azeroth.game.networking.packet.movement;


public class TransferPending extends ServerPacket {
    public int mapID = -1;
    public Position oldMapPosition;
    public shipTransferPending ship = null;
    public Integer transferSpellID = null;

    public TransferPending() {
        super(ServerOpcode.TransferPending);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
        this.writeXYZ(oldMapPosition);
        this.writeBit(ship != null);
        this.writeBit(transferSpellID != null);

        if (ship != null) {
            this.writeInt32(ship.getValue().id);
            this.writeInt32(ship.getValue().originMapID);
        }

        if (transferSpellID != null) {
            this.writeInt32(transferSpellID.intValue());
        }

        this.flushBits();
    }

    public final static class ShipTransferPending {
        public int id; // gameobject_template.entry of the transport the player is teleporting on
        public int originMapID; // Map id the player is currently on (before teleport)

        public ShipTransferPending clone() {
            ShipTransferPending varCopy = new ShipTransferPending();

            varCopy.id = this.id;
            varCopy.originMapID = this.originMapID;

            return varCopy;
        }
    }
}
