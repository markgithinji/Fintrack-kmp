package com.fintrack.shared.feature.core.util

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

class IosFileSaver : FileSaver {
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override suspend fun saveFile(fileName: String, content: String): String? {
        return try {
            val fileManager = NSFileManager.defaultManager
            val urls = fileManager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
            val documentDirectory = urls.first() as NSURL
            val fileURL = documentDirectory.URLByAppendingPathComponent(fileName)
            
            val nsString = NSString.create(string = content)
            nsString.writeToURL(fileURL!!, true, NSUTF8StringEncoding, null)
            fileURL.path
        } catch (e: Exception) {
            null
        }
    }
}

actual fun createFileSaver(): FileSaver = IosFileSaver()
