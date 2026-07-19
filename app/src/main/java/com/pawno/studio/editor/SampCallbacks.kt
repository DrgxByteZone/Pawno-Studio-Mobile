package com.pawno.studio.editor

data class SampCallbackDoc(
    val name: String,
    val signature: String,
    val snippet: String,
    val description: String = ""
)

object SampCallbacks {

    val CALLBACKS: List<SampCallbackDoc> = listOf(
        SampCallbackDoc(
            name = "OnGameModeInit",
            signature = "public OnGameModeInit()",
            snippet = "public OnGameModeInit() {\n    SetGameModeText(\"Gamemode v1.0\");\n    return 1;\n}",
            description = "Called when the gamemode starts up"
        ),
        SampCallbackDoc(
            name = "OnGameModeExit",
            signature = "public OnGameModeExit()",
            snippet = "public OnGameModeExit() {\n    return 1;\n}",
            description = "Called when the gamemode shuts down"
        ),
        SampCallbackDoc(
            name = "OnFilterScriptInit",
            signature = "public OnFilterScriptInit()",
            snippet = "public OnFilterScriptInit() {\n    print(\"Filterscript Loaded\");\n    return 1;\n}",
            description = "Called when a filterscript is loaded"
        ),
        SampCallbackDoc(
            name = "OnFilterScriptExit",
            signature = "public OnFilterScriptExit()",
            snippet = "public OnFilterScriptExit() {\n    return 1;\n}",
            description = "Called when a filterscript is unloaded"
        ),
        SampCallbackDoc(
            name = "OnPlayerConnect",
            signature = "public OnPlayerConnect(playerid)",
            snippet = "public OnPlayerConnect(playerid) {\n    return 1;\n}",
            description = "Called when a player connects to the server"
        ),
        SampCallbackDoc(
            name = "OnPlayerDisconnect",
            signature = "public OnPlayerDisconnect(playerid, reason)",
            snippet = "public OnPlayerDisconnect(playerid, reason) {\n    return 1;\n}",
            description = "Called when a player leaves the server"
        ),
        SampCallbackDoc(
            name = "OnPlayerSpawn",
            signature = "public OnPlayerSpawn(playerid)",
            snippet = "public OnPlayerSpawn(playerid) {\n    return 1;\n}",
            description = "Called when a player spawns"
        ),
        SampCallbackDoc(
            name = "OnPlayerDeath",
            signature = "public OnPlayerDeath(playerid, killerid, reason)",
            snippet = "public OnPlayerDeath(playerid, killerid, reason) {\n    return 1;\n}",
            description = "Called when a player dies or is killed"
        ),
        SampCallbackDoc(
            name = "OnVehicleSpawn",
            signature = "public OnVehicleSpawn(vehicleid)",
            snippet = "public OnVehicleSpawn(vehicleid) {\n    return 1;\n}",
            description = "Called when a vehicle spawns or respawns"
        ),
        SampCallbackDoc(
            name = "OnVehicleDeath",
            signature = "public OnVehicleDeath(vehicleid, killerid)",
            snippet = "public OnVehicleDeath(vehicleid, killerid) {\n    return 1;\n}",
            description = "Called when a vehicle is destroyed"
        ),
        SampCallbackDoc(
            name = "OnPlayerText",
            signature = "public OnPlayerText(playerid, text[])",
            snippet = "public OnPlayerText(playerid, text[]) {\n    return 1;\n}",
            description = "Called when a player sends a chat message"
        ),
        SampCallbackDoc(
            name = "OnPlayerCommandText",
            signature = "public OnPlayerCommandText(playerid, cmdtext[])",
            snippet = "public OnPlayerCommandText(playerid, cmdtext[]) {\n    return 0;\n}",
            description = "Called when a player types a slash command"
        ),
        SampCallbackDoc(
            name = "OnDialogResponse",
            signature = "public OnDialogResponse(playerid, dialogid, response, listitem, inputtext[])",
            snippet = "public OnDialogResponse(playerid, dialogid, response, listitem, inputtext[]) {\n    return 1;\n}",
            description = "Called when a player interacts with a dialog"
        ),
        SampCallbackDoc(
            name = "OnPlayerEnterVehicle",
            signature = "public OnPlayerEnterVehicle(playerid, vehicleid, ispassenger)",
            snippet = "public OnPlayerEnterVehicle(playerid, vehicleid, ispassenger) {\n    return 1;\n}",
            description = "Called when a player starts entering a vehicle"
        ),
        SampCallbackDoc(
            name = "OnPlayerExitVehicle",
            signature = "public OnPlayerExitVehicle(playerid, vehicleid)",
            snippet = "public OnPlayerExitVehicle(playerid, vehicleid) {\n    return 1;\n}",
            description = "Called when a player starts exiting a vehicle"
        ),
        SampCallbackDoc(
            name = "OnPlayerStateChange",
            signature = "public OnPlayerStateChange(playerid, newstate, oldstate)",
            snippet = "public OnPlayerStateChange(playerid, newstate, oldstate) {\n    return 1;\n}",
            description = "Called when a player state changes (driver, passenger, onfoot)"
        ),
        SampCallbackDoc(
            name = "OnPlayerClickMap",
            signature = "public OnPlayerClickMap(playerid, Float:fX, Float:fY, Float:fZ)",
            snippet = "public OnPlayerClickMap(playerid, Float:fX, Float:fY, Float:fZ) {\n    return 1;\n}",
            description = "Called when a player clicks on the pause menu map blip"
        )
    )

    private val CALLBACK_MAP: Map<String, SampCallbackDoc> = CALLBACKS.associateBy { it.name }

    fun isCallback(token: String): Boolean = CALLBACK_MAP.containsKey(token)

    fun findMatching(query: String): List<SampCallbackDoc> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return CALLBACKS.filter { it.name.lowercase().startsWith(q) || it.name.lowercase().contains(q) }
    }
}
