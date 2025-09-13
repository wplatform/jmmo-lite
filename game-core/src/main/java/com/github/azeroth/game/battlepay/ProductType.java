package com.github.azeroth.game.battlepay;

public enum ProductType {
    // retail values:
    Item_(0),
    LevelBoost(1),
    pet(2),
    mount(3),
    WoWToken(4),
    NameChange(5),
    factionChange(6),
    RaceChange(8),
    CharacterTransfer(11),
    Toy(14),
    expansion(18),
    gameTime(20),
    GuildNameChange(21),
    GuildFactionChange(22),
    GuildTransfer(23),
    GuildFactionTranfer(24),
    TransmogAppearance(26),
    gold(30),
    currency(31),

    // custom values:
    ItemSet(100),
    Heirloom(101),
    ProfPriAlchemy(118),
    ProfPriSastre(119),
    ProfPriJoye(120),
    ProfPriHerre(121),
    ProfPriPele(122),
    ProfPriInge(123),
    ProfPriInsc(124),
    ProfPriEncha(125),
    ProfPriDesu(126),
    ProfPriMing(127),
    ProfPriHerb(128),
    ProfSecCoci(129),
    Promo(140),
    RepClassic(141),
    RepBurnig(142),
    RepTLK(143),
    RepCata(144),
    RepPanda(145),
    RepDraenor(146),
    RepLegion(147),
    PremadePve(149),
    VueloDL(150);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, ProductType> mappings;
    private int intValue;

    private ProductType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, ProductType> getMappings() {
        if (mappings == null) {
            synchronized (ProductType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, ProductType>();
                }
            }
        }
        return mappings;
    }

    public static ProductType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
