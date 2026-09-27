# CMakeLists.txt for Pawno Studio Mobile Native Engine
# Author: By M.B.A & AXEL - Blackpanther Company

cmake_minimum_required(VERSION 3.22.1)
project("pawno_engine" C CXX)

set(PAWNC_32_DIR "${CMAKE_CURRENT_SOURCE_DIR}/compilers/pawnc-3.2/source")
set(PAWNC_3107_DIR "${CMAKE_CURRENT_SOURCE_DIR}/compilers/pawnc-3.10.7/source")
set(PAWNC_31011_DIR "${CMAKE_CURRENT_SOURCE_DIR}/compilers/pawnc-3.10.11/source")

# Generate version headers
set(VERSION_MAJOR 3)
set(VERSION_MINOR 10)
set(VERSION_BUILD 7)
set(VERSION_STR "${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_BUILD}")
math(EXPR VERSION_INT "${VERSION_MAJOR} << 8 | ${VERSION_MINOR}")
configure_file(
    "${PAWNC_3107_DIR}/compiler/version.h.in"
    "${CMAKE_CURRENT_BINARY_DIR}/pawnc3107/version.h"
)

set(VERSION_BUILD 11)
set(VERSION_STR "${VERSION_MAJOR}.${VERSION_MINOR}.${VERSION_BUILD}")
configure_file(
    "${PAWNC_31011_DIR}/compiler/version.h.in"
    "${CMAKE_CURRENT_BINARY_DIR}/pawnc31011/version.h"
)

set(COMMON_DEFINITIONS
    NO_MAIN
    NDEBUG
    LINUX
    PAWN_CELL_SIZE=32
    HAVE_UNISTD_H
    HAVE_INTTYPES_H
    HAVE_STDINT_H
    __BYTE_ORDER=1234
    __LITTLE_ENDIAN=1234
    __BIG_ENDIAN=4321
)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -O3 -flto")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -flto")

set(COMMON_C_FLAGS
    -Wno-implicit-function-declaration
    -Wno-int-conversion
    -Wno-pointer-sign
    -Wno-incompatible-pointer-types
    -Wno-format
    -Wno-unused-variable
    -Wno-unused-but-set-variable
    -Wno-parentheses
    -Wno-switch
    -Wno-dangling-else
    -Wno-empty-body
    -Wno-misleading-indentation
    -Wno-address-of-packed-member
    -fPIC
)

# Sources for 3.2 (CompuPhase Legacy)
set(PAWNC_32_SOURCES
    ${PAWNC_32_DIR}/compiler/sc1.c
    ${PAWNC_32_DIR}/compiler/sc2.c
    ${PAWNC_32_DIR}/compiler/sc3.c
    ${PAWNC_32_DIR}/compiler/sc4.c
    ${PAWNC_32_DIR}/compiler/sc5.c
    ${PAWNC_32_DIR}/compiler/sc6.c
    ${PAWNC_32_DIR}/compiler/sc7.c
    ${PAWNC_32_DIR}/compiler/scexpand.c
    ${PAWNC_32_DIR}/compiler/sci18n.c
    ${PAWNC_32_DIR}/compiler/sclist.c
    ${PAWNC_32_DIR}/compiler/scmemfil.c
    ${PAWNC_32_DIR}/compiler/scstate.c
    ${PAWNC_32_DIR}/compiler/scvars.c
    ${PAWNC_32_DIR}/compiler/lstring.c
    ${PAWNC_32_DIR}/compiler/memfile.c
    ${PAWNC_32_DIR}/linux/binreloc.c
)

# Sources for 3.10.7
set(PAWNC_3107_SOURCES
    ${PAWNC_3107_DIR}/compiler/sc1.c
    ${PAWNC_3107_DIR}/compiler/sc2.c
    ${PAWNC_3107_DIR}/compiler/sc3.c
    ${PAWNC_3107_DIR}/compiler/sc4.c
    ${PAWNC_3107_DIR}/compiler/sc5.c
    ${PAWNC_3107_DIR}/compiler/sc6.c
    ${PAWNC_3107_DIR}/compiler/sc7.c
    ${PAWNC_3107_DIR}/compiler/sci18n.c
    ${PAWNC_3107_DIR}/compiler/sclist.c
    ${PAWNC_3107_DIR}/compiler/scmemfil.c
    ${PAWNC_3107_DIR}/compiler/scstate.c
    ${PAWNC_3107_DIR}/compiler/scvars.c
    ${PAWNC_3107_DIR}/compiler/lstring.c
    ${PAWNC_3107_DIR}/compiler/memfile.c
    ${PAWNC_3107_DIR}/compiler/hashtable/wrap_hashtable.c
    ${PAWNC_3107_DIR}/linux/binreloc.c
)

# Sources for 3.10.11
set(PAWNC_31011_SOURCES
    ${PAWNC_31011_DIR}/compiler/sc1.c
    ${PAWNC_31011_DIR}/compiler/sc2.c
    ${PAWNC_31011_DIR}/compiler/sc3.c
    ${PAWNC_31011_DIR}/compiler/sc4.c
    ${PAWNC_31011_DIR}/compiler/sc5.c
    ${PAWNC_31011_DIR}/compiler/sc6.c
    ${PAWNC_31011_DIR}/compiler/sc7.c
    ${PAWNC_31011_DIR}/compiler/sci18n.c
    ${PAWNC_31011_DIR}/compiler/sclist.c
    ${PAWNC_31011_DIR}/compiler/scmemfil.c
    ${PAWNC_31011_DIR}/compiler/scstate.c
    ${PAWNC_31011_DIR}/compiler/scvars.c
    ${PAWNC_31011_DIR}/compiler/lstring.c
    ${PAWNC_31011_DIR}/compiler/memfile.c
    ${PAWNC_31011_DIR}/compiler/hashtable/wrap_hashtable.c
    ${PAWNC_31011_DIR}/linux/binreloc.c
)

# Library pawnc32 (CompuPhase Legacy)
add_library(pawnc32 SHARED
    ${PAWNC_32_SOURCES}
    ${CMAKE_CURRENT_SOURCE_DIR}/compiler_jni.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/amx_inspector.cpp
)

target_include_directories(pawnc32 PRIVATE
    ${PAWNC_32_DIR}/compiler
    ${PAWNC_32_DIR}/linux
    ${PAWNC_32_DIR}/amx
)

target_compile_definitions(pawnc32 PRIVATE ${COMMON_DEFINITIONS} PAWNC_BUILD_32)
target_compile_options(pawnc32 PRIVATE
    $<$<COMPILE_LANGUAGE:C>:${COMMON_C_FLAGS}>
    $<$<COMPILE_LANGUAGE:CXX>:-std=c++20 -fPIC>
)
target_link_libraries(pawnc32 android log)
target_link_options(pawnc32 PRIVATE
    "-Wl,-Bsymbolic"
    "-Wl,-Bsymbolic-functions"
    "-Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/hide_symbols.lds"
)

# Library pawnc3107
add_library(pawnc3107 SHARED
    ${PAWNC_3107_SOURCES}
    ${CMAKE_CURRENT_SOURCE_DIR}/compiler_jni.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/amx_inspector.cpp
)

target_include_directories(pawnc3107 PRIVATE
    ${CMAKE_CURRENT_BINARY_DIR}/pawnc3107
    ${PAWNC_3107_DIR}/compiler
    ${PAWNC_3107_DIR}/linux
    ${PAWNC_3107_DIR}/amx
)

target_compile_definitions(pawnc3107 PRIVATE ${COMMON_DEFINITIONS} PAWNC_BUILD_3107)
target_compile_options(pawnc3107 PRIVATE
    $<$<COMPILE_LANGUAGE:C>:${COMMON_C_FLAGS}>
    $<$<COMPILE_LANGUAGE:CXX>:-std=c++20 -fPIC>
)
target_link_libraries(pawnc3107 android log)
target_link_options(pawnc3107 PRIVATE
    "-Wl,-Bsymbolic"
    "-Wl,-Bsymbolic-functions"
    "-Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/hide_symbols.lds"
)

# Library pawnc31011
add_library(pawnc31011 SHARED
    ${PAWNC_31011_SOURCES}
    ${CMAKE_CURRENT_SOURCE_DIR}/compiler_jni.cpp
    ${CMAKE_CURRENT_SOURCE_DIR}/amx_inspector.cpp
)

target_include_directories(pawnc31011 PRIVATE
    ${CMAKE_CURRENT_BINARY_DIR}/pawnc31011
    ${PAWNC_31011_DIR}/compiler
    ${PAWNC_31011_DIR}/linux
    ${PAWNC_31011_DIR}/amx
)

target_compile_definitions(pawnc31011 PRIVATE
    ${COMMON_DEFINITIONS}
    sNAMEMAX=63
    PAWNC_BUILD_31011
)

target_compile_options(pawnc31011 PRIVATE
    $<$<COMPILE_LANGUAGE:C>:${COMMON_C_FLAGS}>
    $<$<COMPILE_LANGUAGE:CXX>:-std=c++20 -fPIC>
)
target_link_libraries(pawnc31011 android log)
target_link_options(pawnc31011 PRIVATE
    "-Wl,-Bsymbolic"
    "-Wl,-Bsymbolic-functions"
    "-Wl,--version-script=${CMAKE_CURRENT_SOURCE_DIR}/hide_symbols.lds"
)
