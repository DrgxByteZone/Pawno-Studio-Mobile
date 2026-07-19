package com.pawno.studio.editor

enum class PawnTokenType {
    KEYWORD,      // public, stock, forward, new, enum, switch, case, if, else, while, for, return, static, const
    DIRECTIVE,    // #include, #define, #pragma, #if, #else, #endif, #tryinclude, #assert
    NATIVE,       // SetPlayerPos, GivePlayerMoney, CreateVehicle, SendClientMessage, etc.
    CALLBACK,     // OnGameModeInit, OnPlayerConnect, OnPlayerDeath, etc.
    OPERATOR,     // +, -, *, /, %, =, ==, !=, <, >, <=, >=, &&, ||, !, &, |, ^, ~, <<, >>
    STRING,       // "string literal"
    CHARACTER,    // 'c'
    NUMBER,       // 1234, 0xABCD, 12.34
    COMMENT,      // // single line or /* multi line */
    SYMBOL,       // ;, ,, (, ), {, }, [, ], :
    IDENTIFIER,   // custom variable, tag, or function name
    WHITESPACE,
    UNKNOWN
}
