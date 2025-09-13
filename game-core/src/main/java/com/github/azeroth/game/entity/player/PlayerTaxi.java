package com.github.azeroth.game.entity.player;


import java.util.ArrayList;


public class PlayerTaxi {
    private final ArrayList<Integer> taxiDestinations = new ArrayList<>();
    private final object taxiLock = new object();
    public byte[] taximask;
    private int flightMasterFactionId;

    public final Object getTaxiLock() {
        return taxiLock;
    }

    public final void initTaxiNodesForLevel(Race race, PlayerClass chrClass, int level) {
        synchronized (getTaxiLock()) {
            taximask = new byte[((CliDB.TaxiNodesStorage.GetNumRows() - 1) / 8) + 1];

            // class specific initial known nodes
            if (chrClass == playerClass.Deathknight) {
                var factionMask = player.teamForRace(race) == Team.Horde ? CliDB.HordeTaxiNodesMask : CliDB.AllianceTaxiNodesMask;
                taximask = new byte[factionMask.length];

                for (var i = 0; i < factionMask.length; ++i) {
                    Taximask[i] |= (byte) (CliDB.OldContinentsNodesMask[i] & factionMask[i]);
                }
            }

            // race specific initial known nodes: capital and taxi hub masks
            switch (race) {
                case Human:
                case Dwarf:
                case NightElf:
                case Gnome:
                case Draenei:
                case Worgen:
                case PandarenAlliance:
                    setTaximaskNode(2); // Stormwind, Elwynn
                    setTaximaskNode(6); // Ironforge, Dun Morogh
                    setTaximaskNode(26); // Lor'danel, Darkshore
                    setTaximaskNode(27); // Rut'theran Village, Teldrassil
                    setTaximaskNode(49); // Moonglade (Alliance)
                    setTaximaskNode(94); // The Exodar
                    setTaximaskNode(456); // Dolanaar, Teldrassil
                    setTaximaskNode(457); // Darnassus, Teldrassil
                    setTaximaskNode(582); // Goldshire, Elwynn
                    setTaximaskNode(589); // Eastvale Logging Camp, Elwynn
                    setTaximaskNode(619); // Kharanos, Dun Morogh
                    setTaximaskNode(620); // Gol'Bolar quarry, Dun Morogh
                    setTaximaskNode(624); // Azure Watch, Azuremyst Isle

                    break;
                case Orc:
                case Undead:
                case Tauren:
                case Troll:
                case BloodElf:
                case Goblin:
                case PandarenHorde:
                    setTaximaskNode(11); // Undercity, Tirisfal
                    setTaximaskNode(22); // Thunder Bluff, Mulgore
                    setTaximaskNode(23); // Orgrimmar, Durotar
                    setTaximaskNode(69); // Moonglade (Horde)
                    setTaximaskNode(82); // Silvermoon City
                    setTaximaskNode(384); // The Bulwark, Tirisfal
                    setTaximaskNode(402); // Bloodhoof Village, Mulgore
                    setTaximaskNode(460); // Brill, Tirisfal Glades
                    setTaximaskNode(536); // Sen'jin Village, Durotar
                    setTaximaskNode(537); // Razor Hill, Durotar
                    setTaximaskNode(625); // Fairbreeze Village, Eversong Woods
                    setTaximaskNode(631); // Falconwing Square, Eversong Woods

                    break;
            }

            // new continent starting masks (It will be accessible only at new map)
            switch (player.teamForRace(race)) {
                case Alliance:
                    setTaximaskNode(100);

                    break;
                case Horde:
                    setTaximaskNode(99);

                    break;
            }

            // level dependent taxi hubs
            if (level >= 68) {
                setTaximaskNode(213); //Shattered Sun Staging Area
            }
        }
    }

    public final void loadTaxiMask(String data) {
        synchronized (getTaxiLock()) {
            taximask = new byte[((CliDB.TaxiNodesStorage.GetNumRows() - 1) / 8) + 1];

            var split = new LocalizedString();

            var index = 0;

            for (var i = 0; index < taximask.length && i != split.length; ++i, ++index) {
                // load and set bits only for existing taxi nodes
                int id;
                tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
                if (tangible.TryParseHelper.tryParseInt(split.get(i), tempOut_id)) {
                    id = tempOut_id.outArgValue;
                    Taximask[index] = (byte) (CliDB.TaxiNodesMask[index] & id);
                } else {
                    id = tempOut_id.outArgValue;
                }
            }
        }
    }

    public final void appendTaximaskTo(ShowTaxiNodes data, boolean all) {
        data.canLandNodes = new byte[CliDB.TaxiNodesMask.length];
        data.canUseNodes = new byte[CliDB.TaxiNodesMask.length];

        if (all) {
            Buffer.BlockCopy(CliDB.TaxiNodesMask, 0, data.canLandNodes, 0, data.canLandNodes.length); // all existed nodes
            Buffer.BlockCopy(CliDB.TaxiNodesMask, 0, data.canUseNodes, 0, data.canUseNodes.length);
        } else {
            synchronized (getTaxiLock()) {
                Buffer.BlockCopy(taximask, 0, data.canLandNodes, 0, data.canLandNodes.length); // known nodes
                Buffer.BlockCopy(taximask, 0, data.canUseNodes, 0, data.canUseNodes.length);
            }
        }
    }

    public final boolean loadTaxiDestinationsFromString(String values, Team team) {
        clearTaxiDestinations();

        var LocalizedString = new LocalizedString();

        if (LocalizedString.length > 0) {
            tangible.OutObject<Integer> tempOut__flightMasterFactionId = new tangible.OutObject<Integer>();
            tangible.TryParseHelper.tryParseInt(LocalizedString.get(0), tempOut__flightMasterFactionId);
            flightMasterFactionId = tempOut__flightMasterFactionId.outArgValue;
        }

        for (var i = 1; i < LocalizedString.length; ++i) {
            int node;
            tangible.OutObject<Integer> tempOut_node = new tangible.OutObject<Integer>();
            if (tangible.TryParseHelper.tryParseInt(LocalizedString.get(i), tempOut_node)) {
                node = tempOut_node.outArgValue;
                addTaxiDestination(node);
            } else {
                node = tempOut_node.outArgValue;
            }
        }

        if (taxiDestinations.isEmpty()) {
            return true;
        }

        // Check integrity
        if (taxiDestinations.size() < 2) {
            return false;
        }

        for (var i = 1; i < taxiDestinations.size(); ++i) {
            int path;
            tangible.OutObject<Integer> tempOut_path = new tangible.OutObject<Integer>();
            tangible.OutObject<Integer> tempOut__ = new tangible.OutObject<Integer>();
            global.getObjectMgr().getTaxiPath(taxiDestinations.get(i - 1), taxiDestinations.get(i), tempOut_path, tempOut__);
            _ = tempOut__.outArgValue;
            path = tempOut_path.outArgValue;

            if (path == 0) {
                return false;
            }
        }

        // can't load taxi path without mount set (quest taxi path?)
        if (global.getObjectMgr().getTaxiMountDisplayId(getTaxiSource(), team, true) == 0) {
            return false;
        }

        return true;
    }

    public final String saveTaxiDestinationsToString() {
        if (taxiDestinations.isEmpty()) {
            return "";
        }

        StringBuilder ss = new StringBuilder();
        ss.append(String.format("%1$s ", flightMasterFactionId));

        for (var i = 0; i < taxiDestinations.size(); ++i) {
            ss.append(String.format("%1$s ", taxiDestinations.get(i)));
        }

        return ss.toString();
    }

    public final int getCurrentTaxiPath() {
        if (taxiDestinations.size() < 2) {
            return 0;
        }


        int path;
        tangible.OutObject<Integer> tempOut_path = new tangible.OutObject<Integer>();
        tangible.OutObject<Integer> tempOut__ = new tangible.OutObject<Integer>();
        global.getObjectMgr().getTaxiPath(taxiDestinations.get(0), taxiDestinations.get(1), tempOut_path, tempOut__);
        _ = tempOut__.outArgValue;
        path = tempOut_path.outArgValue;

        return path;
    }

    public final boolean requestEarlyLanding() {
        if (taxiDestinations.size() <= 2) {
            return false;
        }

        // start from first destination - m_TaxiDestinations[0] is the current starting node
        for (var i = 1; i < taxiDestinations.size(); ++i) {
            if (isTaximaskNodeKnown(taxiDestinations.get(i))) {
                if (++i == taxiDestinations.size() - 1) {
                    return false; // if we are left with only 1 known node on the path don't change the spline, its our final destination anyway
                }

                taxiDestinations.subList(i, taxiDestinations.size()).clear();

                return true;
            }
        }

        return false;
    }

    public final FactionTemplateRecord getFlightMasterFactionTemplate() {
        return CliDB.FactionTemplateStorage.get(flightMasterFactionId);
    }

    public final void setFlightMasterFactionTemplateId(int factionTemplateId) {
        flightMasterFactionId = factionTemplateId;
    }

    public final boolean isTaximaskNodeKnown(int nodeidx) {
        var field = (nodeidx - 1) / 8;
        var submask = (int) (1 << (int) ((nodeidx - 1) % 8));

        synchronized (getTaxiLock()) {
            return (Taximask[field] & submask) == submask;
        }
    }

    public final boolean setTaximaskNode(int nodeidx) {
        var field = (nodeidx - 1) / 8;
        var submask = (int) (1 << (int) ((nodeidx - 1) % 8));

        synchronized (getTaxiLock()) {
            if ((Taximask[field] & submask) != submask) {
                Taximask[field] |= (byte) submask;

                return true;
            } else {
                return false;
            }
        }
    }

    public final void clearTaxiDestinations() {
        taxiDestinations.clear();
    }

    public final void addTaxiDestination(int dest) {
        taxiDestinations.add(dest);
    }

    public final int getTaxiSource() {
        return taxiDestinations.isEmpty() ? 0 : taxiDestinations.get(0);
    }

    public final int getTaxiDestination() {
        return taxiDestinations.size() < 2 ? 0 : taxiDestinations.get(1);
    }

    private void setTaxiDestination(ArrayList<Integer> nodes) {
        taxiDestinations.clear();
        taxiDestinations.addAll(nodes);
    }

    public final int nextTaxiDestination() {
        taxiDestinations.remove(0);

        return getTaxiDestination();
    }

    public final ArrayList<Integer> getPath() {
        return taxiDestinations;
    }

    public final boolean empty() {
        return taxiDestinations.isEmpty();
    }
}
