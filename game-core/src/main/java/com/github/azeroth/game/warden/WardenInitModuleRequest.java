package game;


class WardenInitModuleRequest {
    public WardenOpcodes command1 = WardenOpcodes.values()[0];
    public short size1;
    public int checkSumm1;
    public byte unk1;
    public byte unk2;
    public byte type;
    public byte string_library1;
    public int[] function1 = new int[4];

    public WardenOpcodes command2 = WardenOpcodes.values()[0];
    public short size2;
    public int checkSumm2;
    public byte unk3;
    public byte unk4;
    public byte string_library2;
    public int function2;
    public byte function2_set;

    public WardenOpcodes command3 = WardenOpcodes.values()[0];
    public short size3;
    public int checkSumm3;
    public byte unk5;
    public byte unk6;
    public byte string_library3;
    public int function3;
    public byte function3_set;


//	public static implicit operator byte[](WardenInitModuleRequest request)
//		{
//			var buffer = new byteBuffer();
//			buffer.writeInt8((byte)request.command1);
//			buffer.writeInt16(request.size1);
//			buffer.writeInt32(request.checkSumm1);
//			buffer.writeInt8(request.unk1);
//			buffer.writeInt8(request.unk2);
//			buffer.writeInt8(request.type);
//			buffer.writeInt8(request.string_library1);
//
//			foreach (var function in request.function1)
//				buffer.writeInt32(function);
//
//			buffer.writeInt8((byte)request.command2);
//			buffer.writeInt16(request.size2);
//			buffer.writeInt32(request.checkSumm2);
//			buffer.writeInt8(request.unk3);
//			buffer.writeInt8(request.unk4);
//			buffer.writeInt8(request.string_library2);
//			buffer.writeInt32(request.function2);
//			buffer.writeInt8(request.function2_set);
//
//			buffer.writeInt8((byte)request.command3);
//			buffer.writeInt16(request.size3);
//			buffer.writeInt32(request.checkSumm3);
//			buffer.writeInt8(request.unk5);
//			buffer.writeInt8(request.unk6);
//			buffer.writeInt8(request.string_library3);
//			buffer.writeInt32(request.function3);
//			buffer.writeInt8(request.function3_set);
//
//			return buffer.getData();
//		}
}
