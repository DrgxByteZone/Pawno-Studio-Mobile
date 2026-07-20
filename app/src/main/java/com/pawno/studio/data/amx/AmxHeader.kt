package com.pawno.studio.data.amx

data class AmxHeader(
    val isValid: Boolean,
    val magic: Int,
    val fileVersion: Int,
    val amxVersion: Int,
    val flags: Int,
    val codeSize: Long,
    val dataSize: Long,
    val stackSize: Long,
    val totalSize: Long,
    val cip: Long,
    val numPublics: Int,
    val numNatives: Int,
    val publics: List<AmxSymbol>,
    val natives: List<AmxSymbol>,
    val errorMessage: String? = null,
    val filePath: String = ""
) {
    val heapStackReserve: Long get() = stackSize

    val codeSizeKb: String
        get() = String.format("%.2f KB", codeSize / 1024.0)

    val dataSizeKb: String
        get() = String.format("%.2f KB", dataSize / 1024.0)

    val stackSizeKb: String
        get() = String.format("%.2f KB", stackSize / 1024.0)

    val totalSizeKb: String
        get() = String.format("%.2f KB", totalSize / 1024.0)

    // Calculate dynamic memory budget percentage (default standard SA-MP dynamic size is 16384 bytes unless specified)
    val memoryBudgetPercent: Float
        get() {
            if (stackSize <= 0) return 0f
            val percent = (dataSize.toFloat() / stackSize.toFloat()) * 100f
            return percent.coerceIn(0f, 100f)
        }
}
