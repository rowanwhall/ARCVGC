package com.arcvgc.app.data

import com.arcvgc.app.domain.model.AppConfig
import com.arcvgc.app.domain.model.Format
import com.arcvgc.app.domain.model.NetworkResult
import com.arcvgc.app.network.ApiService
import com.arcvgc.app.util.createSafeScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppConfigRepository(
    private val apiService: ApiService,
    private val storage: AppConfigStorageApi,
    private val catalogCacheStorage: CatalogCacheStorageApi
) {
    private val scope = createSafeScope()

    private val _config = MutableStateFlow<AppConfig?>(null)
    val config: StateFlow<AppConfig?> = _config.asStateFlow()

    private val _catalogVersionChanged = MutableStateFlow(false)
    val catalogVersionChanged: StateFlow<Boolean> = _catalogVersionChanged.asStateFlow()

    init {
        scope.launch {
            try {
                loadCachedConfig()
                fetchAndUpdateConfig()
            } catch (_: Exception) {
                // Fail-open: keep cached config or null
            }
        }
    }

    private fun loadCachedConfig() {
        val formatId = storage.getInt(KEY_FORMAT_ID, 0)
        if (formatId == 0) return

        val formatName = storage.getString(KEY_FORMAT_NAME, "")
        val formatFormattedName = storage.getString(KEY_FORMAT_FORMATTED_NAME, "")

        _config.value = AppConfig(
            defaultFormat = Format(
                id = formatId,
                name = formatName,
                formattedName = formatFormattedName.ifEmpty { null }
            ),
            minAndroidVersion = storage.getInt(KEY_MIN_ANDROID_VERSION, 1),
            minIosVersion = storage.getInt(KEY_MIN_IOS_VERSION, 1),
            minWebVersion = storage.getInt(KEY_MIN_WEB_VERSION, 1),
            minCatalogVersion = storage.getInt(KEY_MIN_CATALOG_VERSION, 1)
        )
    }

    private suspend fun fetchAndUpdateConfig() {
        when (val result = apiService.getConfig()) {
            is NetworkResult.Success -> {
                val appConfig = result.data
                _config.value = appConfig
                persistConfig(appConfig)
                checkCatalogVersion(appConfig.minCatalogVersion)
            }
            is NetworkResult.Error -> {
                // Fail-open: keep cached config
            }
        }
    }

    private fun persistConfig(config: AppConfig) {
        storage.putInt(KEY_FORMAT_ID, config.defaultFormat.id)
        storage.putString(KEY_FORMAT_NAME, config.defaultFormat.name)
        storage.putString(KEY_FORMAT_FORMATTED_NAME, config.defaultFormat.formattedName ?: "")
        storage.putInt(KEY_MIN_ANDROID_VERSION, config.minAndroidVersion)
        storage.putInt(KEY_MIN_IOS_VERSION, config.minIosVersion)
        storage.putInt(KEY_MIN_WEB_VERSION, config.minWebVersion)
        storage.putInt(KEY_MIN_CATALOG_VERSION, config.minCatalogVersion)
    }

    private fun checkCatalogVersion(minVersion: Int) {
        val localVersion = storage.getInt(KEY_LOCAL_CATALOG_VERSION, 0)
        if (minVersion > localVersion) {
            CatalogCache.clearAll(catalogCacheStorage)
            storage.putInt(KEY_LOCAL_CATALOG_VERSION, minVersion)
            _catalogVersionChanged.value = true
        }
    }

    /** Snapshot getter for iOS interop. */
    fun getConfig(): AppConfig? = _config.value

    /** Returns the default format ID from config, or 1 as fallback. */
    fun getDefaultFormatId(): Int = _config.value?.defaultFormat?.id ?: 1

    companion object {
        private const val KEY_FORMAT_ID = "format_id"
        private const val KEY_FORMAT_NAME = "format_name"
        private const val KEY_FORMAT_FORMATTED_NAME = "format_formatted_name"
        private const val KEY_MIN_ANDROID_VERSION = "min_android_version"
        private const val KEY_MIN_IOS_VERSION = "min_ios_version"
        private const val KEY_MIN_WEB_VERSION = "min_web_version"
        private const val KEY_MIN_CATALOG_VERSION = "min_catalog_version"
        private const val KEY_LOCAL_CATALOG_VERSION = "local_catalog_version"
    }
}
