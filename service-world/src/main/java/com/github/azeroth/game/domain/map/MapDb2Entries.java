package com.github.azeroth.game.domain.map;


import com.github.azeroth.common.Pair;
import com.github.azeroth.dbc.domain.MapDifficulty;
import com.github.azeroth.dbc.domain.MapEntry;


// ====================================================================================================
// Produced by the Free Edition of C# to Java Converter.
// To produce customized conversions, purchase a Premium Edition license:
// https://www.tangiblesoftwaresolutions.com/product-details/csharp-to-java-converter.html
// ====================================================================================================







// C# TO JAVA CONVERTER WARNING: Java does not allow user-defined second types. The behavior of this class may differ from the original:
// ORIGINAL LINE: public struct MapDb2Entries
public final class MapDb2Entries
{
	public MapEntry map;
	public MapDifficulty mapDifficulty;

	public MapDb2Entries(MapEntry map, MapDifficulty mapDifficulty)
	{
		this.map = map;
		this.mapDifficulty = mapDifficulty;
	}

// C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
// ORIGINAL LINE: public Tuple<uint, uint> GetKey()
	public Pair<Short, Short> GetKey()
	{
// C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
// ORIGINAL LINE: return Tuple.Create(MapDifficulty.MapID, (uint)MapDifficulty.LockID);
		return Pair.of(mapDifficulty.getMapID(), mapDifficulty.getLockID());
	}

	public boolean IsInstanceIdBound()
	{
		return !map.isFlexLocking() && !mapDifficulty.isUsingEncounterLocks();
	}

}
