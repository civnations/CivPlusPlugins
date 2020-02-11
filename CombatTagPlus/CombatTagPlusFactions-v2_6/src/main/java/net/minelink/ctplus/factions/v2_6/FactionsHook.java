package net.minelink.ctplus.factions.v2_6;

import com.massivecraft.factions.FFlag;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.massivecore.ps.PS;
import net.minelink.ctplus.hook.Hook;
import org.bukkit.Location;

public class FactionsHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location location) {
        PS ps = PS.valueOf(location);
        Faction faction = BoardColls.get().getFactionAt(ps);
        return faction.getFlag(FFlag.PVP);
    }

}
