package com.github.azeroth.game.entity.unit;


import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.spell.CastSpellExtraArgs;
import com.github.azeroth.game.spell.SpellInfo;

public class CharmInfo {
    private final Unit unit;
    private final UnitActionBarEntry[] petActionBar = new UnitActionBarEntry[SharedConst.ActionBarIndexMax];
    private final UnitActionBarEntry[] charmspells = new UnitActionBarEntry[4];
    private final ReactStates oldReactState;
    private CommandStates _CommandState = CommandStates.values()[0];
    private int petnumber;

    private boolean isCommandAttack;
    private boolean isCommandFollow;
    private boolean isAtStay;
    private boolean isFollowing;
    private boolean isReturning;
    private float stayX;
    private float stayY;
    private float stayZ;

    public CharmInfo(Unit unit) {
        unit = unit;
        _CommandState = CommandStates.Follow;
        petnumber = 0;
        oldReactState = ReactStates.Passive;

        for (byte i = 0; i < SharedConst.MaxSpellCharm; ++i) {
            _charmspells[i] = new UnitActionBarEntry();
            _charmspells[i].setActionAndType(0, ActiveStates.disabled);
        }

        for (var i = 0; i < SharedConst.ActionBarIndexMax; ++i) {
            PetActionBar[i] = new UnitActionBarEntry();
        }

        var creature = unit.toCreature();

        if (creature != null) {
            oldReactState = creature.getReactState();
            creature.setReactState(ReactStates.Passive);
        }
    }

    public final void restoreState() {
        if (unit.isTypeId(TypeId.UNIT)) {
            var creature = unit.toCreature();

            if (creature) {
                creature.setReactState(oldReactState);
            }
        }
    }

    public final void initPetActionBar() {
        // the first 3 SpellOrActions are attack, follow and stay
        for (byte i = 0; i < SharedConst.ActionBarIndexPetSpellStart - SharedConst.ActionBarIndexStart; ++i) {
            setActionBar((byte) (SharedConst.ActionBarIndexStart + i), (int) CommandStates.attack.getValue() - i, ActiveStates.command);
        }

        // middle 4 SpellOrActions are spells/special attacks/abilities
        for (byte i = 0; i < SharedConst.ActionBarIndexPetSpellEnd - SharedConst.ActionBarIndexPetSpellStart; ++i) {
            setActionBar((byte) (SharedConst.ActionBarIndexPetSpellStart + i), 0, ActiveStates.Passive);
        }

        // last 3 SpellOrActions are reactions
        for (byte i = 0; i < SharedConst.ActionBarIndexEnd - SharedConst.ActionBarIndexPetSpellEnd; ++i) {
            setActionBar((byte) (SharedConst.ActionBarIndexPetSpellEnd + i), (int) CommandStates.attack.getValue() - i, ActiveStates.reaction);
        }
    }


    public final void initEmptyActionBar() {
        initEmptyActionBar(true);
    }

    public final void initEmptyActionBar(boolean withAttack) {
        if (withAttack) {
            setActionBar((byte) SharedConst.ActionBarIndexStart, (int) CommandStates.attack.getValue(), ActiveStates.command);
        } else {
            setActionBar((byte) SharedConst.ActionBarIndexStart, 0, ActiveStates.Passive);
        }

        for (byte x = SharedConst.ActionBarIndexStart + 1; x < SharedConst.ActionBarIndexEnd; ++x) {
            setActionBar(x, 0, ActiveStates.Passive);
        }
    }

    public final void initPossessCreateSpells() {
        if (unit.isTypeId(TypeId.UNIT)) {
            // Adding switch until better way is found. Malcrom
            // Adding entrys to this switch will prevent COMMAND_ATTACK being added to pet bar.
            switch (unit.getEntry()) {
                case 23575: // Mindless Abomination
                case 24783: // Trained Rock Falcon
                case 27664: // Crashin' Thrashin' Racer
                case 40281: // Crashin' Thrashin' Racer
                case 28511: // Eye of Acherus
                    break;
                default:
                    initEmptyActionBar();

                    break;
            }

            for (byte i = 0; i < SharedConst.MaxCreatureSpells; ++i) {
                var spellId = unit.toCreature().getSpells()[i];
                var spellInfo = global.getSpellMgr().getSpellInfo(spellId, unit.getMap().getDifficultyID());

                if (spellInfo != null) {
                    if (spellInfo.hasAttribute(SpellAttr5.NotAvailableWhileCharmed)) {
                        continue;
                    }

                    if (spellInfo.isPassive()) {
                        unit.castSpell(unit, spellInfo.getId(), new CastSpellExtraArgs(true));
                    } else {
                        addSpellToActionBar(spellInfo, ActiveStates.Passive, i % SharedConst.ActionBarIndexMax);
                    }
                }
            }
        } else {
            initEmptyActionBar();
        }
    }

    public final void initCharmCreateSpells() {
        if (unit.isTypeId(TypeId.PLAYER)) // charmed players don't have spells
        {
            initEmptyActionBar();

            return;
        }

        initPetActionBar();

        for (int x = 0; x < SharedConst.MaxSpellCharm; ++x) {
            var spellId = unit.toCreature().getSpells()[x];
            var spellInfo = global.getSpellMgr().getSpellInfo(spellId, unit.getMap().getDifficultyID());

            if (spellInfo == null) {
                _charmspells[x].setActionAndType(spellId, ActiveStates.disabled);

                continue;
            }

            if (spellInfo.hasAttribute(SpellAttr5.NotAvailableWhileCharmed)) {
                continue;
            }

            if (spellInfo.isPassive()) {
                unit.castSpell(unit, spellInfo.getId(), new CastSpellExtraArgs(true));
                _charmspells[x].setActionAndType(spellId, ActiveStates.Passive);
            } else {
                _charmspells[x].setActionAndType(spellId, ActiveStates.disabled);

                ActiveStates newstate;

                if (!spellInfo.isAutocastable()) {
                    newstate = ActiveStates.Passive;
                } else {
                    if (spellInfo.getNeedsExplicitUnitTarget()) {
                        newstate = ActiveStates.enabled;
                        toggleCreatureAutocast(spellInfo, true);
                    } else {
                        newstate = ActiveStates.disabled;
                    }
                }

                addSpellToActionBar(spellInfo, newstate);
            }
        }
    }


    public final boolean addSpellToActionBar(SpellInfo spellInfo, ActiveStates newstate) {
        return addSpellToActionBar(spellInfo, newstate, 0);
    }

    public final boolean addSpellToActionBar(SpellInfo spellInfo) {
        return addSpellToActionBar(spellInfo, ActiveStates.Decide, 0);
    }

    public final boolean addSpellToActionBar(SpellInfo spellInfo, ActiveStates newstate, int preferredSlot) {
        var spell_id = spellInfo.getId();
        var first_id = spellInfo.getFirstRankSpell().getId();

        // new spell rank can be already listed
        for (byte i = 0; i < SharedConst.ActionBarIndexMax; ++i) {
            var action = PetActionBar[i].getAction();

            if (action != 0) {
                if (PetActionBar[i].isActionBarForSpell() && global.getSpellMgr().getFirstSpellInChain(action) == first_id) {
                    PetActionBar[i].setAction(spell_id);

                    return true;
                }
            }
        }

        // or use empty slot in other case
        for (byte i = 0; i < SharedConst.ActionBarIndexMax; ++i) {
            var j = (byte) ((preferredSlot + i) % SharedConst.ActionBarIndexMax);

            if (PetActionBar[j].getAction() == 0 && PetActionBar[j].isActionBarForSpell()) {
                setActionBar(j, spell_id, newstate == ActiveStates.Decide ? spellInfo.isAutocastable() ? ActiveStates.Disabled : ActiveStates.Passive : newstate);

                return true;
            }
        }

        return false;
    }

    public final boolean removeSpellFromActionBar(int spell_id) {
        var first_id = global.getSpellMgr().getFirstSpellInChain(spell_id);

        for (byte i = 0; i < SharedConst.ActionBarIndexMax; ++i) {
            var action = PetActionBar[i].getAction();

            if (action != 0) {
                if (PetActionBar[i].isActionBarForSpell() && global.getSpellMgr().getFirstSpellInChain(action) == first_id) {
                    setActionBar(i, 0, ActiveStates.Passive);

                    return true;
                }
            }
        }

        return false;
    }

    public final void toggleCreatureAutocast(SpellInfo spellInfo, boolean apply) {
        if (spellInfo.isPassive()) {
            return;
        }

        for (int x = 0; x < SharedConst.MaxSpellCharm; ++x) {
            if (spellInfo.getId() == _charmspells[x].getAction()) {
                _charmspells[x].setType(apply ? ActiveStates.Enabled : ActiveStates.disabled);
            }
        }
    }

    public final void setPetNumber(int petnumber, boolean statwindow) {
        petnumber = petnumber;

        if (statwindow) {
            unit.setPetNumberForClient(petnumber);
        } else {
            unit.setPetNumberForClient(0);
        }
    }

    public final void loadPetActionBar(String data) {
        initPetActionBar();

        var tokens = new LocalizedString();

        if (tokens.length != (SharedConst.ActionBarIndexEnd - SharedConst.ActionBarIndexStart) * 2) {
            return; // non critical, will reset to default
        }

        byte index = 0;

        for (byte i = 0; i < tokens.length && index < SharedConst.ActionBarIndexEnd; ++i, ++index) {
            var type = tokens.get(i++).<ActiveStates>ToEnum();
            int action;
            tangible.OutObject<Integer> tempOut_action = new tangible.OutObject<Integer>();
            tangible.TryParseHelper.tryParseInt(tokens.get(i), tempOut_action);
            action = tempOut_action.outArgValue;

            PetActionBar[index].setActionAndType(action, type);

            // check correctness
            if (PetActionBar[index].isActionBarForSpell()) {
                var spelInfo = global.getSpellMgr().getSpellInfo(PetActionBar[index].getAction(), unit.getMap().getDifficultyID());

                if (spelInfo == null) {
                    setActionBar(index, 0, ActiveStates.Passive);
                } else if (!spelInfo.isAutocastable()) {
                    setActionBar(index, PetActionBar[index].getAction(), ActiveStates.Passive);
                }
            }
        }
    }

    public final void buildActionBar(WorldPacket data) {
        for (var i = 0; i < SharedConst.ActionBarIndexMax; ++i) {
            data.writeInt32(PetActionBar[i].packedData);
        }
    }

    public final void setSpellAutocast(SpellInfo spellInfo, boolean state) {
        for (byte i = 0; i < SharedConst.ActionBarIndexMax; ++i) {
            if (spellInfo.getId() == PetActionBar[i].getAction() && PetActionBar[i].isActionBarForSpell()) {
                PetActionBar[i].setType(state ? ActiveStates.Enabled : ActiveStates.disabled);

                break;
            }
        }
    }

    public final void setIsCommandAttack(boolean val) {
        isCommandAttack = val;
    }

    public final boolean isCommandAttack() {
        return isCommandAttack;
    }

    public final void setIsCommandFollow(boolean val) {
        isCommandFollow = val;
    }

    public final boolean isCommandFollow() {
        return isCommandFollow;
    }

    public final void saveStayPosition() {
        //! At this point a new spline destination is enabled because of unit.stopMoving()
        var stayPos = new Position(unit.getMoveSpline().finalDestination());

        if (unit.getMoveSpline().onTransport) {
            var transport = unit.getDirectTransport();

            if (transport != null) {
                transport.calculatePassengerPosition(stayPos);
            }
        }

        stayX = stayPos.getX();
        stayY = stayPos.getY();
        stayZ = stayPos.getZ();
    }

    public final void getStayPosition(tangible.OutObject<Float> x, tangible.OutObject<Float> y, tangible.OutObject<Float> z) {
        x.outArgValue = stayX;
        y.outArgValue = stayY;
        z.outArgValue = stayZ;
    }

    public final void setIsAtStay(boolean val) {
        isAtStay = val;
    }

    public final boolean isAtStay() {
        return isAtStay;
    }

    public final void setIsFollowing(boolean val) {
        isFollowing = val;
    }

    public final boolean isFollowing() {
        return isFollowing;
    }

    public final void setIsReturning(boolean val) {
        isReturning = val;
    }

    public final boolean isReturning() {
        return isReturning;
    }

    public final int getPetNumber() {
        return petnumber;
    }

    public final CommandStates getCommandState() {
        return _CommandState;
    }

    public final void setCommandState(CommandStates st) {
        _CommandState = st;
    }

    public final boolean hasCommandState(CommandStates state) {
        return (_CommandState == state);
    }

    public final void setActionBar(byte index, int spellOrAction, ActiveStates type) {
        PetActionBar[index].setActionAndType(spellOrAction, type);
    }

    public final UnitActionBarEntry getActionBarEntry(byte index) {
        return PetActionBar[index];
    }

    public final UnitActionBarEntry getCharmSpell(byte index) {
        return _charmspells[index];
    }
}
