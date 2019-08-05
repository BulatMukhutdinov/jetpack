package tat.mukhutdinov.jetpack.infrastructure.common.api

interface FilesGateway {

    fun saveLumasFile(content: String)

    fun getLumasFileContent(): ByteArray
}