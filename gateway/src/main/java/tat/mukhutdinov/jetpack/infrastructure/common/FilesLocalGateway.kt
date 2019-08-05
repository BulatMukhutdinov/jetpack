package tat.mukhutdinov.jetpack.infrastructure.common

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import tat.mukhutdinov.jetpack.infrastructure.common.api.FilesGateway
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset

class FilesLocalGateway(private val baseFolder: File, private val context: Context) : FilesGateway {

    private val keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC

    private val masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)

    override fun saveLumasFile(content: String) {
        val file = File(baseFolder, FILE_NAME)

        if (file.exists() && !file.isDirectory) {
            file.delete()
        }

        try {
            val encryptedFile = buildFile(file)
            encryptedFile.openFileOutput().use {
                it.write(content.toByteArray(Charset.forName("UTF-8")))
                it.flush()
            }
        } catch (e: Exception) {
            Timber.w(e)
        }
    }

    override fun getLumasFileContent(): ByteArray {
        val file = File(baseFolder, FILE_NAME)

        if (!file.exists() || file.isDirectory) {
            file.createNewFile()
        }

        val encryptedFile = buildFile(file)

        try {
            encryptedFile.openFileInput().use { fileInputStream ->
                val byteStream = ByteArrayOutputStream()
                var nextByte = fileInputStream.read()
                while (nextByte != -1) {
                    byteStream.write(nextByte)
                    nextByte = fileInputStream.read()
                }

                return byteStream.toByteArray()
            }
        } catch (e: Exception) {
            Timber.w(e)
            return ByteArray(0)
        }
    }

    private fun buildFile(file: File): EncryptedFile =
        EncryptedFile
            .Builder(
                file,
                context,
                masterKeyAlias,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            )
            .build()

    companion object {
        private const val FILE_NAME = "lumas.txt"
    }
}