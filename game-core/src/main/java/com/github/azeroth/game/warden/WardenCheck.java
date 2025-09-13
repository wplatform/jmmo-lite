package game;

public class WardenCheck {
    public short checkId;
    public WardenChecktype type = WardenCheckType.values()[0];
    public byte[] data;
    public int address; // PROC_CHECK, MEM_CHECK, PAGE_CHECK
    public byte length; // PROC_CHECK, MEM_CHECK, PAGE_CHECK
    public string str; // LUA, MPQ, DRIVER
    public String comment;
    public char[] idStr = new char[4]; // LUA
    public Wardenactions action = WardenActions.values()[0];
}
