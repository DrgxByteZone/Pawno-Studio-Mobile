package com.pawno.studio.editor

data class SampFunctionDoc(
    val name: String,
    val signature: String,
    val returnType: String = "int",
    val description: String = ""
)

object SampNatives {

    val NATIVES: List<SampFunctionDoc> = listOf(
        // Player info & stats
        SampFunctionDoc("SetPlayerPos", "SetPlayerPos(playerid, Float:x, Float:y, Float:z)", "bool", "Sets player coordinates in 3D space"),
        SampFunctionDoc("GetPlayerPos", "GetPlayerPos(playerid, &Float:x, &Float:y, &Float:z)", "bool", "Gets player 3D coordinates"),
        SampFunctionDoc("SetPlayerFacingAngle", "SetPlayerFacingAngle(playerid, Float:angle)", "bool", "Sets the facing angle (rotation) of a player"),
        SampFunctionDoc("GetPlayerFacingAngle", "GetPlayerFacingAngle(playerid, &Float:angle)", "bool", "Gets the facing angle of a player"),
        SampFunctionDoc("SetPlayerHealth", "SetPlayerHealth(playerid, Float:health)", "bool", "Sets player health value"),
        SampFunctionDoc("GetPlayerHealth", "GetPlayerHealth(playerid, &Float:health)", "bool", "Gets player current health"),
        SampFunctionDoc("SetPlayerArmour", "SetPlayerArmour(playerid, Float:armour)", "bool", "Sets player body armour value"),
        SampFunctionDoc("GetPlayerArmour", "GetPlayerArmour(playerid, &Float:armour)", "bool", "Gets player armour value"),
        SampFunctionDoc("GivePlayerMoney", "GivePlayerMoney(playerid, money)", "bool", "Gives or subtracts money to player"),
        SampFunctionDoc("GetPlayerMoney", "GetPlayerMoney(playerid)", "int", "Returns the amount of money player currently has"),
        SampFunctionDoc("ResetPlayerMoney", "ResetPlayerMoney(playerid)", "bool", "Resets player money to zero"),
        SampFunctionDoc("SetPlayerScore", "SetPlayerScore(playerid, score)", "bool", "Sets score displayed on scoreboard"),
        SampFunctionDoc("GetPlayerScore", "GetPlayerScore(playerid)", "int", "Gets player current score"),
        SampFunctionDoc("GetPlayerName", "GetPlayerName(playerid, const name[], len)", "int", "Stores player nickname into string"),
        SampFunctionDoc("SetPlayerName", "SetPlayerName(playerid, const name[])", "int", "Changes player nickname"),
        SampFunctionDoc("SetPlayerSkin", "SetPlayerSkin(playerid, skinid)", "bool", "Changes player character model/skin"),
        SampFunctionDoc("GetPlayerSkin", "GetPlayerSkin(playerid)", "int", "Returns player character model ID"),
        SampFunctionDoc("SetPlayerColor", "SetPlayerColor(playerid, color)", "bool", "Sets player radar blip and nametag color"),
        SampFunctionDoc("GetPlayerColor", "GetPlayerColor(playerid)", "int", "Returns player color"),
        SampFunctionDoc("SetPlayerInterior", "SetPlayerInterior(playerid, interiorid)", "bool", "Sets player current interior ID"),
        SampFunctionDoc("GetPlayerInterior", "GetPlayerInterior(playerid)", "int", "Gets player interior ID"),
        SampFunctionDoc("SetPlayerVirtualWorld", "SetPlayerVirtualWorld(playerid, worldid)", "bool", "Sets player virtual world"),
        SampFunctionDoc("GetPlayerVirtualWorld", "GetPlayerVirtualWorld(playerid)", "int", "Gets player virtual world ID"),
        SampFunctionDoc("IsPlayerConnected", "IsPlayerConnected(playerid)", "bool", "Checks if player is active on the server"),
        SampFunctionDoc("IsPlayerInAnyVehicle", "IsPlayerInAnyVehicle(playerid)", "bool", "Checks if player is inside a vehicle"),
        SampFunctionDoc("IsPlayerInVehicle", "IsPlayerInVehicle(playerid, vehicleid)", "bool", "Checks if player is inside a specific vehicle"),
        SampFunctionDoc("GetPlayerVehicleID", "GetPlayerVehicleID(playerid)", "int", "Gets vehicle ID the player is in"),
        SampFunctionDoc("GetPlayerVehicleSeat", "GetPlayerVehicleSeat(playerid)", "int", "Gets seat index player occupies"),
        SampFunctionDoc("PutPlayerInVehicle", "PutPlayerInVehicle(playerid, vehicleid, seatid)", "bool", "Puts player into specified vehicle seat"),
        SampFunctionDoc("RemovePlayerFromVehicle", "RemovePlayerFromVehicle(playerid)", "bool", "Removes player from their current vehicle"),
        SampFunctionDoc("SetPlayerCameraPos", "SetPlayerCameraPos(playerid, Float:x, Float:y, Float:z)", "bool", "Positions the player camera"),
        SampFunctionDoc("SetPlayerCameraLookAt", "SetPlayerCameraLookAt(playerid, Float:x, Float:y, Float:z, cut = CAMERA_CUT)", "bool", "Points camera at coordinates"),
        SampFunctionDoc("SetCameraBehindPlayer", "SetCameraBehindPlayer(playerid)", "bool", "Restores normal gameplay camera behind player"),

        // Weapons
        SampFunctionDoc("GivePlayerWeapon", "GivePlayerWeapon(playerid, weaponid, ammo)", "bool", "Gives weapon and ammo to player"),
        SampFunctionDoc("ResetPlayerWeapons", "ResetPlayerWeapons(playerid)", "bool", "Removes all weapons from player"),
        SampFunctionDoc("GetPlayerWeapon", "GetPlayerWeapon(playerid)", "int", "Returns currently held weapon ID"),
        SampFunctionDoc("GetPlayerWeaponData", "GetPlayerWeaponData(playerid, slot, &weapons, &ammo)", "bool", "Gets weapon and ammo by slot"),

        // Messaging & Dialogs
        SampFunctionDoc("SendClientMessage", "SendClientMessage(playerid, color, const message[])", "bool", "Sends colored chat message to player"),
        SampFunctionDoc("SendClientMessageToAll", "SendClientMessageToAll(color, const message[])", "bool", "Sends colored chat message to all players"),
        SampFunctionDoc("ShowPlayerDialog", "ShowPlayerDialog(playerid, dialogid, style, const caption[], const info[], const button1[], const button2[])", "bool", "Displays interactive GUI dialog"),
        SampFunctionDoc("GameTextForPlayer", "GameTextForPlayer(playerid, const string[], time, style)", "bool", "Displays onscreen GTA game text"),
        SampFunctionDoc("GameTextForAll", "GameTextForAll(const string[], time, style)", "bool", "Displays onscreen GTA game text to all"),

        // Vehicles
        SampFunctionDoc("CreateVehicle", "CreateVehicle(vehicletype, Float:x, Float:y, Float:z, Float:rotation, color1, color2, respawn_delay, addsiren=0)", "int", "Creates a vehicle in the game world"),
        SampFunctionDoc("DestroyVehicle", "DestroyVehicle(vehicleid)", "bool", "Removes a vehicle from the server"),
        SampFunctionDoc("SetVehiclePos", "SetVehiclePos(vehicleid, Float:x, Float:y, Float:z)", "bool", "Sets vehicle coordinates"),
        SampFunctionDoc("GetVehiclePos", "GetVehiclePos(vehicleid, &Float:x, &Float:y, &Float:z)", "bool", "Gets vehicle coordinates"),
        SampFunctionDoc("SetVehicleHealth", "SetVehicleHealth(vehicleid, Float:health)", "bool", "Sets vehicle health (1000.0 is max)"),
        SampFunctionDoc("GetVehicleHealth", "GetVehicleHealth(vehicleid, &Float:health)", "bool", "Gets vehicle health"),
        SampFunctionDoc("SetVehicleVelocity", "SetVehicleVelocity(vehicleid, Float:x, Float:y, Float:z)", "bool", "Sets vehicle physics momentum"),
        SampFunctionDoc("RepairVehicle", "RepairVehicle(vehicleid)", "bool", "Repairs vehicle damage and health"),

        // Objects
        SampFunctionDoc("CreateObject", "CreateObject(modelid, Float:x, Float:y, Float:z, Float:rx, Float:ry, Float:rz, Float:drawdistance = 0.0)", "int", "Spawns a global object"),
        SampFunctionDoc("DestroyObject", "DestroyObject(objectid)", "bool", "Destroys an object"),
        SampFunctionDoc("SetObjectPos", "SetObjectPos(objectid, Float:x, Float:y, Float:z)", "bool", "Moves object instantly"),
        SampFunctionDoc("MoveObject", "MoveObject(objectid, Float:x, Float:y, Float:z, Float:speed, Float:rx = -1000.0, Float:ry = -1000.0, Float:rz = -1000.0)", "int", "Smoothly moves object over time"),

        // Server & Timers
        SampFunctionDoc("SetTimer", "SetTimer(const funcname[], interval, repeating)", "int", "Creates a periodic or delayed timer"),
        SampFunctionDoc("SetTimerEx", "SetTimerEx(const funcname[], interval, repeating, const format[], {Float,_}:...)", "int", "Creates timer with arguments"),
        SampFunctionDoc("KillTimer", "KillTimer(timerid)", "bool", "Cancels a running timer"),
        SampFunctionDoc("SetGameModeText", "SetGameModeText(const string[])", "bool", "Sets gamemode name shown in SA-MP server list"),
        SampFunctionDoc("SendRconCommand", "SendRconCommand(const command[])", "bool", "Executes an internal RCON console command"),
        SampFunctionDoc("print", "print(const string[])", "void", "Prints text to server console log"),
        SampFunctionDoc("printf", "printf(const format[], {Float,_}:...)", "void", "Prints formatted text to server console log"),
        SampFunctionDoc("format", "format(output[], len, const format[], {Float,_}:...)", "void", "Formats a string buffer with placeholders")
    )

    private val NATIVE_MAP: Map<String, SampFunctionDoc> = NATIVES.associateBy { it.name }

    fun isNative(token: String): Boolean = NATIVE_MAP.containsKey(token)

    fun findMatching(query: String): List<SampFunctionDoc> {
        if (query.isBlank()) return emptyList()
        val q = query.lowercase()
        return NATIVES.filter { it.name.lowercase().startsWith(q) || it.name.lowercase().contains(q) }
    }
}
