/*
 * Pawn.CMD Include
 * Author: urandom
 */

#if defined _pawn_cmd_included
    #endinput
#endif
#define _pawn_cmd_included

#define PC_CMD:%1(%2) \
    forward cmd_%1(%2); \
    public cmd_%1(%2)

#define flags:cmd_%1(%2) \
    forward flags_cmd_%1(); \
    public flags_cmd_%1()

native PC_RegAlias(const cmd[], const alias[], ...);
native PC_SetFlags(const cmd[], flags);
native PC_GetFlags(const cmd[]);
native PC_EmulateCommand(playerid, const cmdtext[]);
native PC_RenameCommand(const cmd[], const newname[]);
native PC_CommandExists(const cmd[]);
native PC_DeleteCommand(const cmd[]);
