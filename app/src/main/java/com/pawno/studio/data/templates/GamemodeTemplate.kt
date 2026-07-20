package com.pawno.studio.data.templates

enum class GamemodeTemplate(
    val title: String,
    val description: String,
    val filenameSuffix: String
) {
    BLANK(
        title = "Blank Gamemode",
        description = "Minimal clean SA-MP skeleton with main() and OnGameModeInit()",
        filenameSuffix = "_blank.pwn"
    ),
    ROLEPLAY(
        title = "Roleplay Starter",
        description = "Roleplay framework with player data enums, spawn coordinates, and dialogs",
        filenameSuffix = "_rp.pwn"
    ),
    FREEROAM_DM(
        title = "Freeroam / Deathmatch",
        description = "Instant action template with weapon spawning, vehicle creation, and kill counter",
        filenameSuffix = "_dm.pwn"
    ),
    FILTERSCRIPT(
        title = "Filterscript Module",
        description = "Modular script with FILTERSCRIPT macro definition and clean lifecycle hooks",
        filenameSuffix = "_fs.pwn"
    );

    fun generateSourceCode(name: String): String {
        return when (this) {
            BLANK -> """
                /*
                 * Gamemode: $name
                 * Created with Pawno Studio Mobile
                 * Author: By M.B.A & AXEL - Blackpanther Company
                 */
                
                #include <a_samp>
                #include <core>
                #include <float>
                
                main() {
                    print("\n----------------------------------");
                    print(" $name Gamemode Initialized!");
                    print(" Powered by Pawno Studio Mobile");
                    print("----------------------------------\n");
                }
                
                public OnGameModeInit() {
                    SetGameModeText("$name v1.0");
                    AddPlayerClass(0, 1958.3783, 1343.1572, 15.3746, 269.1425, 0, 0, 0, 0, 0, 0);
                    ShowNameTags(1);
                    return 1;
                }
                
                public OnGameModeExit() {
                    return 1;
                }
                
                public OnPlayerRequestClass(playerid, classid) {
                    SetPlayerPos(playerid, 1958.3783, 1343.1572, 15.3746);
                    SetPlayerCameraPos(playerid, 1958.3783, 1338.1572, 15.3746);
                    SetPlayerCameraLookAt(playerid, 1958.3783, 1343.1572, 15.3746);
                    return 1;
                }
                
                public OnPlayerConnect(playerid) {
                    SendClientMessage(playerid, 0x7BDB80FF, "Welcome to $name!");
                    return 1;
                }
                
                public OnPlayerDisconnect(playerid, reason) {
                    return 1;
                }
                
                public OnPlayerSpawn(playerid) {
                    SetPlayerInterior(playerid, 0);
                    SetPlayerVirtualWorld(playerid, 0);
                    GivePlayerMoney(playerid, 500);
                    return 1;
                }
            """.trimIndent()

            ROLEPLAY -> """
                /*
                 * Roleplay Starter: $name
                 * Created with Pawno Studio Mobile
                 * Author: By M.B.A & AXEL - Blackpanther Company
                 */
                
                #include <a_samp>
                #include <streamer>
                #include <sscanf2>
                #include <zcmd>
                
                #define COLOR_SERVER_PRIMARY 0x238636FF
                #define COLOR_SERVER_ERROR   0xDA3633FF
                #define COLOR_SERVER_WHITE   0xF0F6FCFF
                
                enum E_PLAYER_DATA {
                    pDatabaseId,
                    pMoney,
                    pLevel,
                    pAdminLevel,
                    pSkin,
                    bool:pIsLoggedIn
                }
                new PlayerData[MAX_PLAYERS][E_PLAYER_DATA];
                
                main() {
                    print("\n----------------------------------");
                    print(" $name (Roleplay) Running...");
                    print("----------------------------------\n");
                }
                
                public OnGameModeInit() {
                    SetGameModeText("$name RP");
                    DisableInteriorEnterExits();
                    EnableStuntBonusForAll(0);
                    return 1;
                }
                
                public OnPlayerConnect(playerid) {
                    PlayerData[playerid][pIsLoggedIn] = false;
                    PlayerData[playerid][pMoney] = 1000;
                    PlayerData[playerid][pLevel] = 1;
                    
                    SendClientMessage(playerid, COLOR_SERVER_PRIMARY, "[SERVER] Welcome to $name Roleplay!");
                    return 1;
                }
                
                public OnPlayerSpawn(playerid) {
                    SetPlayerPos(playerid, 1481.0, -1771.5, 18.8); // City Hall spawn
                    SetPlayerFacingAngle(playerid, 0.0);
                    SetCameraBehindPlayer(playerid);
                    GivePlayerMoney(playerid, PlayerData[playerid][pMoney]);
                    return 1;
                }
                
                CMD:stats(playerid, params[]) {
                    new buffer[128];
                    format(buffer, sizeof(buffer), "Level: %d | Money: $%d", PlayerData[playerid][pLevel], PlayerData[playerid][pMoney]);
                    SendClientMessage(playerid, COLOR_SERVER_WHITE, buffer);
                    return 1;
                }
            """.trimIndent()

            FREEROAM_DM -> """
                /*
                 * Freeroam / Deathmatch: $name
                 * Created with Pawno Studio Mobile
                 * Author: By M.B.A & AXEL - Blackpanther Company
                 */
                
                #include <a_samp>
                #include <core>
                
                new gPlayerKills[MAX_PLAYERS];
                
                main() {
                    print("Deathmatch $name Loaded!");
                }
                
                public OnGameModeInit() {
                    SetGameModeText("$name DM/Freeroam");
                    AddPlayerClass(285, 2038.5, 1343.2, 10.8, 0.0, 24, 500, 31, 1000, 0, 0); // SWAT
                    AddPlayerClass(286, 2038.5, 1343.2, 10.8, 0.0, 24, 500, 30, 1000, 0, 0); // FBI
                    return 1;
                }
                
                public OnPlayerDeath(playerid, killerid, reason) {
                    if (killerid != INVALID_PLAYER_ID) {
                        gPlayerKills[killerid]++;
                        SetPlayerScore(killerid, gPlayerKills[killerid]);
                        GivePlayerMoney(killerid, 250);
                    }
                    return 1;
                }
            """.trimIndent()

            FILTERSCRIPT -> """
                /*
                 * Filterscript: $name
                 * Created with Pawno Studio Mobile
                 * Author: By M.B.A & AXEL - Blackpanther Company
                 */
                
                #define FILTERSCRIPT
                #include <a_samp>
                
                public OnFilterScriptInit() {
                    print("\n==================================");
                    print(" Filterscript: $name Loaded!");
                    print("==================================\n");
                    return 1;
                }
                
                public OnFilterScriptExit() {
                    print(" Filterscript: $name Unloaded.");
                    return 1;
                }
            """.trimIndent()
        }
    }

    val starterCode: String
        get() = generateSourceCode("MyGamemode")

    companion object {
        val Blank = BLANK
        val RoleplayStarter = ROLEPLAY
        val FreeroamDm = FREEROAM_DM
        val Filterscript = FILTERSCRIPT
    }
}

