package com.github.azeroth.game.entity.player;


import com.github.azeroth.dbc.domain.FactionTemplate;
import com.github.azeroth.dbc.model.TaxiMask;
import com.github.azeroth.defines.Race;
import com.github.azeroth.defines.Team;
import com.github.azeroth.defines.UnitClass;
import com.github.azeroth.game.networking.packet.taxi.ShowTaxiNodes;
import com.github.azeroth.utils.StringUtil;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class PlayerTaxi {
    private final ArrayList<Integer> taxiDestinations = new ArrayList<>();
    private final Player player;
    public TaxiMask taxiMask;
    private int flightMasterFactionId;


    public final void initTaxiNodesForLevel() {
        Race race = player.getRace();
        var chrClass = player.getUnitClass();
        var dbcObjectManager = player.getWorldContext().getDbcObjectManager();
        TaxiMask factionMask = player.teamForRace(race) == Team.HORDE ? dbcObjectManager.getHordeTaxiNodesMask() : dbcObjectManager.getAllianceTaxiNodesMask();
        TaxiMask oldContinentsNodesMask = dbcObjectManager.getOldContinentsNodesMask();
        if (Objects.requireNonNull(chrClass) == UnitClass.DEATH_KNIGHT) {
            for (var i = 0; i < taxiMask.size(); ++i)
                taxiMask.set(i, (byte) (taxiMask.get(i) | (oldContinentsNodesMask.get(i) & factionMask.get(i))));
        }

        // race specific initial known nodes: capital and taxi hub masks
        switch (race) {
            case HUMAN:
            case DWARF:
            case NIGHT_ELF:
            case GNOME:
            case DRAENEI:
            case WORGEN:
            case PANDAREN_ALLIANCE:
                setTaxiMaskNode(2); // Stormwind, Elwynn
                setTaxiMaskNode(6); // Ironforge, Dun Morogh
                setTaxiMaskNode(26); // Lor'danel, Darkshore
                setTaxiMaskNode(27); // Rut'theran Village, Teldrassil
                setTaxiMaskNode(49); // Moonglade (Alliance)
                setTaxiMaskNode(94); // The Exodar
                setTaxiMaskNode(456); // Dolanaar, Teldrassil
                setTaxiMaskNode(457); // Darnassus, Teldrassil
                setTaxiMaskNode(582); // Goldshire, Elwynn
                setTaxiMaskNode(589); // Eastvale Logging Camp, Elwynn
                setTaxiMaskNode(619); // Kharanos, Dun Morogh
                setTaxiMaskNode(620); // Gol'Bolar quarry, Dun Morogh
                setTaxiMaskNode(624); // Azure Watch, Azuremyst Isle

                break;
            case ORC:
            case UNDEAD:
            case TAUREN:
            case TROLL:
            case BLOOD_ELF:
            case GOBLIN:
            case PANDAREN_HORDE:
                setTaxiMaskNode(11); // Undercity, Tirisfal
                setTaxiMaskNode(22); // Thunder Bluff, Mulgore
                setTaxiMaskNode(23); // Orgrimmar, Durotar
                setTaxiMaskNode(69); // Moonglade (Horde)
                setTaxiMaskNode(82); // Silvermoon City
                setTaxiMaskNode(384); // The Bulwark, Tirisfal
                setTaxiMaskNode(402); // Bloodhoof Village, Mulgore
                setTaxiMaskNode(460); // Brill, Tirisfal Glades
                setTaxiMaskNode(536); // Sen'jin Village, Durotar
                setTaxiMaskNode(537); // Razor Hill, Durotar
                setTaxiMaskNode(625); // Fairbreeze Village, Eversong Woods
                setTaxiMaskNode(631); // Falconwing Square, Eversong Woods

                break;
        }

        // new continent starting masks (It will be accessible only at new map)
        switch (player.teamForRace(race)) {
            case ALLIANCE:
                setTaxiMaskNode(100);

                break;
            case HORDE:
                setTaxiMaskNode(99);

                break;
        }

        // level dependent taxi hubs
        var level = player.getLevel();
        if (level >= 68) {
            setTaxiMaskNode(213); //Shattered Sun Staging Area
        }
    }

    public final boolean loadTaxiMask(String data) {
        boolean warn = false;
        int maskSize = ((player.getWorldContext().getDbcObjectManager().taxiNode().size() - 1) / 8) + 1;
        taxiMask = new TaxiMask(maskSize);
        var sTaxiNodesMask = player.getWorldContext().getDbcObjectManager().getTaxiNodesMask();
        var tokens = StringUtil.tokenizeInts(data, " ");

        for (int index = 0; (index < taxiMask.size()) && (index < tokens.length); ++index) {
            int mask = tokens[index];
            // load and set bits only for existing taxi nodes
            taxiMask.set(index, (byte) (sTaxiNodesMask.get(index) & mask));
            if (taxiMask.get(index) != mask)
                warn = true;
        }

        return !warn;
    }

    public final void appendTaxiMaskTo(ShowTaxiNodes data, boolean all) {
        if (all) {
            byte[] allNodes = player.getWorldContext().getDbcObjectManager().getTaxiNodesMask().data();
            data.canLandNodes = allNodes;              // all existed nodes
            data.canUseNodes = allNodes;
        } else {
            data.canLandNodes = taxiMask.data();                  // known nodes
            data.canUseNodes = taxiMask.data();
        }
    }

    public final boolean loadTaxiDestinationsFromString(String values, Team team) {
        clearTaxiDestinations();

        var tokens = StringUtil.tokenizeInts(values, " ");
        if (tokens.length > 0) {
            flightMasterFactionId = tokens[0];
        }

        for (var i = 1; i < tokens.length; ++i) {
            int node = tokens[i];
            addTaxiDestination(node);
        }

        if (taxiDestinations.isEmpty()) {
            return true;
        }

        // Check integrity
        if (taxiDestinations.size() < 2) {
            return false;
        }

        var it = taxiDestinations.iterator();
        int first = it.next();
        while (it.hasNext()) {
            int next = it.next();
            var taxiPath = player.getWorldContext().getDbcObjectManager().getTaxiPath(first, next);
            if (taxiPath == null) {
                return false;
            }
            first = next;
        }
        // can't load taxi path without mount set (quest taxi path?)
        return player.getWorldContext().getObjectManager().getTaxiMountDisplayId(getTaxiSource(), team, true) != 0;
    }

    public final String saveTaxiDestinationsToString() {
        if (taxiDestinations.isEmpty()) {
            return "";
        }
        String taxiDestinationsStr = String.join(" ", taxiDestinations.stream().map(Object::toString).toArray(String[]::new));
        return flightMasterFactionId + " " + taxiDestinationsStr;
    }

    public final int getCurrentTaxiPath() {
        if (taxiDestinations.size() < 2) {
            return 0;
        }
        var iterator = taxiDestinations.iterator();
        var first = iterator.next();
        var next = iterator.next();
        var taxiPath = player.getWorldContext().getDbcObjectManager().getTaxiPath(first, next);
        if (taxiPath == null) {
            return 0;
        }
        return taxiPath.getId();
    }

    public final boolean requestEarlyLanding() {
        if (taxiDestinations.size() <= 2) {
            return false;
        }

        // start from first destination - m_TaxiDestinations[0] is the current starting node
        for (var i = 1; i < taxiDestinations.size(); ++i) {
            if (isTaxiMaskNodeKnown(taxiDestinations.get(i))) {
                if (++i == taxiDestinations.size() - 1) {
                    return false; // if we are left with only 1 known node on the path don't change the spline, its our final destination anyway
                }

                taxiDestinations.subList(i, taxiDestinations.size()).clear();

                return true;
            }
        }

        return false;
    }

    public final FactionTemplate getFlightMasterFactionTemplate() {
        return player.getWorldContext().getDbcObjectManager().factionTemplate(flightMasterFactionId);
    }

    public final void setFlightMasterFactionTemplateId(int factionTemplateId) {
        flightMasterFactionId = factionTemplateId;
    }

    public final boolean isTaxiMaskNodeKnown(int nodeidx) {
        var field = (nodeidx - 1) / 8;
        var submask = 1 << ((nodeidx - 1) % 8);

        return (taxiMask.get(field) & submask) == submask;
    }

    public final boolean setTaxiMaskNode(int nodeIdx) {
        var field = (nodeIdx - 1) / 8;
        var subMask = 1 << ((nodeIdx - 1) % 8);

        if ((taxiMask.get(field) & subMask) != subMask) {
            taxiMask.set(field, (byte) (taxiMask.get(field) | subMask));

            return true;
        } else {
            return false;
        }
    }

    public final void clearTaxiDestinations() {
        taxiDestinations.clear();
    }

    public final void addTaxiDestination(int dest) {
        taxiDestinations.add(dest);
    }

    public final int getTaxiSource() {
        return taxiDestinations.isEmpty() ? 0 : taxiDestinations.getFirst();
    }

    public final int getTaxiDestination() {
        return taxiDestinations.size() < 2 ? 0 : taxiDestinations.get(1);
    }

    private void setTaxiDestination(ArrayList<Integer> nodes) {
        taxiDestinations.clear();
        taxiDestinations.addAll(nodes);
    }

    public final int nextTaxiDestination() {
        taxiDestinations.removeFirst();
        return getTaxiDestination();
    }

    public final ArrayList<Integer> getPath() {
        return taxiDestinations;
    }

    public final boolean empty() {
        return taxiDestinations.isEmpty();
    }
}
