package com.fintrack.shared.feature.auth.data.local

import com.fintrack.shared.feature.auth.domain.repository.TokenDataSource
import kotlinx.coroutines.flow.update
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
class IOSTokenDataSource : TokenDataSource {

    private val _tokenFlow = MutableStateFlow<String?>(null)

    init {
        loadTokenFromKeychain()
    }

    override val token: Flow<String?> = _tokenFlow

    override suspend fun saveToken(token: String) {
        saveTokenToKeychain(token)
        _tokenFlow.update { token }
    }

    override suspend fun clearToken() {
        deleteTokenFromKeychain()
        _tokenFlow.update { null }
    }

    private fun loadTokenFromKeychain() {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrService] = "com.yourapp.auth"
        query[kSecAttrAccount] = "auth_token"
        query[kSecReturnData] = kCFBooleanTrue
        query[kSecMatchLimit] = kSecMatchLimitOne

        val resultRef = mutableListOf<CFTypeRef?>()
        val status = SecItemCopyMatching(query, resultRef)

        if (status == errSecSuccess) {
            val data = resultRef[0] as? NSData
            data?.let {
                val token = NSString.create(it, NSUTF8StringEncoding) as String
                _tokenFlow.update { token }
            }
        } else if (status != errSecItemNotFound) {
            println("Keychain read error: $status")
        }
    }

    private fun saveTokenToKeychain(token: String) {
        deleteTokenFromKeychain()

        val tokenData = token.encodeToByteArray().toNSData()

        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrService] = "com.yourapp.auth"
        query[kSecAttrAccount] = "auth_token"
        query[kSecValueData] = tokenData
        query[kSecAttrAccessible] = kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly

        val status = SecItemAdd(query, null)
        if (status != errSecSuccess) {
            println("Keychain save error: $status")
        }
    }

    private fun deleteTokenFromKeychain() {
        val query = mutableMapOf<Any?, Any?>()
        query[kSecClass] = kSecClassGenericPassword
        query[kSecAttrService] = "com.yourapp.auth"
        query[kSecAttrAccount] = "auth_token"

        val status = SecItemDelete(query)
        if (status != errSecSuccess && status != errSecItemNotFound) {
            println("Keychain delete error: $status")
        }
    }

    private fun ByteArray.toNSData(): NSData {
        return NSData.create(this, 0UL, this.size.toULong())
    }
}

actual fun createTokenDataSource(): TokenDataSource {
    return IOSTokenDataSource()
}