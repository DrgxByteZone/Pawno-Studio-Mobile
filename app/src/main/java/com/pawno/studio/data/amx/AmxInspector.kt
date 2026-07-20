package com.pawno.studio.data.amx

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

class AmxInspector {

    suspend fun inspect(amxPath: String): AmxHeader = inspect(File(amxPath))

    suspend fun inspect(amxFile: File): AmxHeader = withContext(Dispatchers.IO) {
        if (!amxFile.exists()) {
            return@withContext AmxHeader(
                isValid = false,
                magic = 0,
                fileVersion = 0,
                amxVersion = 0,
                flags = 0,
                codeSize = 0,
                dataSize = 0,
                stackSize = 0,
                totalSize = 0,
                cip = 0,
                numPublics = 0,
                numNatives = 0,
                publics = emptyList(),
                natives = emptyList(),
                errorMessage = "File does not exist: ${amxFile.absolutePath}"
            )
        }

        try {
            val jsonStr = inspectNative(amxFile.absolutePath)
            val json = JSONObject(jsonStr)

            if (json.has("error")) {
                return@withContext AmxHeader(
                    isValid = false,
                    magic = 0,
                    fileVersion = 0,
                    amxVersion = 0,
                    flags = 0,
                    codeSize = 0,
                    dataSize = 0,
                    stackSize = 0,
                    totalSize = 0,
                    cip = 0,
                    numPublics = 0,
                    numNatives = 0,
                    publics = emptyList(),
                    natives = emptyList(),
                    errorMessage = json.getString("error")
                )
            }

            val publicsList = mutableListOf<AmxSymbol>()
            val publicsArray = json.optJSONArray("publics")
            if (publicsArray != null) {
                for (i in 0 until publicsArray.length()) {
                    val pObj = publicsArray.getJSONObject(i)
                    publicsList.add(
                        AmxSymbol(
                            name = pObj.optString("name", "unknown"),
                            address = pObj.optLong("address", 0L)
                        )
                    )
                }
            }

            val nativesList = mutableListOf<AmxSymbol>()
            val nativesArray = json.optJSONArray("natives")
            if (nativesArray != null) {
                for (i in 0 until nativesArray.length()) {
                    val nObj = nativesArray.getJSONObject(i)
                    nativesList.add(
                        AmxSymbol(
                            name = nObj.optString("name", "unknown"),
                            address = nObj.optLong("address", 0L)
                        )
                    )
                }
            }

            AmxHeader(
                isValid = json.optBoolean("isValid", true),
                magic = json.optInt("magic", 0xF1E0),
                fileVersion = json.optInt("fileVersion", 8),
                amxVersion = json.optInt("amxVersion", 8),
                flags = json.optInt("flags", 0),
                codeSize = json.optLong("codeSize", 0L),
                dataSize = json.optLong("dataSize", 0L),
                stackSize = json.optLong("stackSize", 16384L),
                totalSize = json.optLong("totalSize", amxFile.length()),
                cip = json.optLong("cip", 0L),
                numPublics = json.optInt("numPublics", publicsList.size),
                numNatives = json.optInt("numNatives", nativesList.size),
                publics = publicsList,
                natives = nativesList
            )
        } catch (e: Exception) {
            AmxHeader(
                isValid = false,
                magic = 0,
                fileVersion = 0,
                amxVersion = 0,
                flags = 0,
                codeSize = 0,
                dataSize = 0,
                stackSize = 0,
                totalSize = 0,
                cip = 0,
                numPublics = 0,
                numNatives = 0,
                publics = emptyList(),
                natives = emptyList(),
                errorMessage = e.message ?: "Unknown error while reading AMX"
            )
        }
    }

    private external fun inspectNative(amxFilePath: String): String
}
