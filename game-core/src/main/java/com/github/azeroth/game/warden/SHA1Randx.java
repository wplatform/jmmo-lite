package game;

class SHA1Randx {
    private final byte[] o1 = new byte[20];
    private final byte[] o2 = new byte[20];

    private SHA1 sh;
    private int taken;
    private byte[] o0 = new byte[20];

    public SHA1Randx(byte[] buff) {
        var halfSize = buff.length / 2;
        Span<Byte> span = buff;

        sh = SHA1.create();
        o1 = sh.ComputeHash(buff, 0, halfSize);

        sh = SHA1.create();

        o2 = sh.ComputeHash(span[halfSize..].ToArray(), 0, buff.length - halfSize);

        fillUp();
    }

    public final void generate(byte[] buf, int sz) {
        for (int i = 0; i < sz; ++i) {
            if (taken == 20) {
                fillUp();
            }

            buf[i] = _o0[_taken];
            taken++;
        }
    }


    private void fillUp() {
        sh = SHA1.create();
        sh.ComputeHash(o1, 0, 20);
        sh.ComputeHash(o0, 0, 20);
        o0 = sh.ComputeHash(o2, 0, 20);

        taken = 0;
    }
}
