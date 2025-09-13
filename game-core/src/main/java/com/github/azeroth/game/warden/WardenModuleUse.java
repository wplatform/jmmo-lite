package game;


class WardenModuleUse {
    public WardenOpcodes command = WardenOpcodes.values()[0];
    public byte[] moduleId = new byte[16];
    public byte[] moduleKey = new byte[16];
    public int size;


//	public static implicit operator byte[](WardenModuleUse use)
//		{
//			var buffer = new byteBuffer();
//			buffer.writeInt8((byte)use.command);
//			buffer.writeBytes(use.moduleId, 16);
//			buffer.writeBytes(use.moduleKey, 16);
//			buffer.writeInt32(use.size);
//
//			return buffer.getData();
//		}
}
