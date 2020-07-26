package plus.civ.uuidiamonds

import plus.civ.uuidiamonds.command.SpawnDiamondCommand
import plus.civ.uuidiamonds.database.DatabaseManager
import plus.civ.uuidiamonds.listener.DiamondToolBrokenListener
import plus.civ.uuidiamonds.listener.DiamondToolCraftModifier
import plus.civ.uuidiamonds.listener.DisableCraftDiamond
import plus.civ.uuidiamonds.listener.HiddenOreDiamondModifier
import vg.civcraft.mc.civmodcore.ACivMod

class UUIDiamonds: ACivMod() {
    companion object {
        val instance: UUIDiamonds
            get() = instanceStorage!!
        private var instanceStorage: UUIDiamonds? = null

        val configManager: ConfigManager
            get() = configManagerStorage!!
        private var configManagerStorage: ConfigManager? = null

        val databaseManager: DatabaseManager
            get() = databaseManagerStorage!!
        private var databaseManagerStorage: DatabaseManager? = null
    }

    override fun onEnable() {
        super.onEnable()
        instanceStorage = this
        configManagerStorage = ConfigManager(config)
        databaseManagerStorage = DatabaseManager()

        registerListener(DisableCraftDiamond())
        registerListener(DiamondToolCraftModifier())
        registerListener(DiamondToolBrokenListener())
        registerListener(HiddenOreDiamondModifier())

        getCommand("spawndiamond")!!.setExecutor(SpawnDiamondCommand())
    }
}
