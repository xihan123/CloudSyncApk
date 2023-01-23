@file:OptIn(ExperimentalSerializationApi::class)

package cn.xihan.cloudsync.utils.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.*
import okio.Path.Companion.toPath
import kotlinx.serialization.json.okio.decodeFromBufferedSource as decode
import kotlinx.serialization.json.okio.encodeToBufferedSink as encode

/**
 * @项目名 : AGE动漫
 * @作者 : MissYang
 * @创建时间 : 2022/10/28 11:04
 * @介绍 : https://github.com/xxfast/KStore 0.1.1
 */
val FILE_SYSTEM: FileSystem = FileSystem.SYSTEM

inline fun <reified T : @Serializable Any> storeOf(
    filePath: String,
    default: T? = null,
    enableCache: Boolean = true,
    serializer: Json = Json,
): KStore<T> {
    val encoder: (T?) -> Unit = { value: T? ->
        FILE_SYSTEM.sink(filePath.toPath()).buffer().use { serializer.encode(value, it) }
    }
    val decoder: () -> T? = { serializer.decode(FILE_SYSTEM.source(filePath.toPath()).buffer()) }
    return KStore(filePath.toPath(), default, enableCache, encoder, decoder)
}

class KStore<T : @Serializable Any>(
    private val path: Path,
    private val default: T? = null,
    private val enableCache: Boolean = true,
    private val encoder: suspend (T?) -> Unit,
    private val decoder: suspend () -> T?,
) {
    private val lock: Mutex = Mutex()
    private val stateFlow: MutableStateFlow<T?> = MutableStateFlow(default)

    val updates: Flow<T?>
        get() = this.stateFlow
            .onStart { read(fromCache = false) } // updates will always start with a fresh read

    private suspend fun write(value: T?) {
        encoder.invoke(value)
        stateFlow.emit(value)
    }

    private suspend fun read(fromCache: Boolean): T? {
        if (fromCache && stateFlow.value != default) return stateFlow.value
        val decoded: T? = try {
            decoder.invoke()
        } catch (e: Exception) {
            null
        }
        val emitted: T? = decoded ?: default
        stateFlow.emit(emitted)
        return emitted
    }

    suspend fun set(value: T?) = lock.withLock { write(value) }
    suspend fun get(): T? = lock.withLock { read(enableCache) }

    suspend fun update(operation: (T?) -> T?) = lock.withLock {
        val previous: T? = read(enableCache)
        val updated: T? = operation(previous)
        write(updated)
    }

    suspend fun delete() {
        FILE_SYSTEM.delete(path)
        stateFlow.emit(null)
    }

    suspend fun reset() {
        set(default)
        stateFlow.emit(default)
    }
}

inline fun <reified T : @Serializable Any> listStoreOf(
    filePath: String,
    default: List<T> = emptyList(),
    enableCache: Boolean = true,
    serializer: Json = Json,
): KStore<List<T>> =
    storeOf(filePath, default, enableCache, serializer)

suspend fun <T : @Serializable Any> KStore<List<T>>.getOrEmpty(): List<T> =
    get() ?: emptyList()

suspend fun <T : @Serializable Any> KStore<List<T>>.get(index: Int): T? =
    get()?.get(index)

suspend fun <T : @Serializable Any> KStore<List<T>>.plus(vararg value: T) {
    update { list -> list?.plus(value) ?: listOf(*value) }
}

suspend fun <T : @Serializable Any> KStore<List<T>>.minus(vararg value: T) =
    update { list -> list?.minus(value.toSet()) ?: emptyList() }

suspend fun <T : @Serializable Any> KStore<List<T>>.map(operation: (T) -> T) {
    update { list -> list?.map { t -> operation(t) } }
}

suspend fun <T : @Serializable Any> KStore<List<T>>.mapIndexed(operation: (Int, T) -> T) {
    update { list -> list?.mapIndexed { index, t -> operation(index, t) } }
}

val <T : @Serializable Any> KStore<List<T>>.updatesOrEmpty: Flow<List<T>>
    get() =
        updates.filterNotNull()

inline fun <reified T : @Serializable Any> folderOf(
    folderPath: String,
    serializer: Json = Json,
    noinline indexWith: (T) -> String,
): KFolder<T> {
    val encoder: (BufferedSink, T?) -> Unit =
        { sink: BufferedSink, value: T? -> sink.use { serializer.encode(value, it) } }
    val decoder: (BufferedSource) -> T? = { source: BufferedSource -> serializer.decode(source) }
    return KFolder(folderPath, indexWith, encoder, decoder)
}

class KFolder<T : @Serializable Any>(
    private val folderPath: String,
    private val indexWith: (T) -> String,
    private val encoder: (BufferedSink, T?) -> Unit,
    private val decoder: (BufferedSource) -> T?,
) {
    private val lock: Mutex = Mutex()
    private val stateFlow: MutableStateFlow<Set<String>> = MutableStateFlow(indexes)

    val indexes: Set<String>
        get() {
            if (!FILE_SYSTEM.exists(folderPath.toPath())) return emptySet()
            return FILE_SYSTEM.list(folderPath.toPath()).map { it.name }.toSet()
        }

    val indexUpdates: Flow<Set<String>> = stateFlow

    suspend fun get(index: String): T? = lock.withLock {
        val path: Path = "$folderPath/$index".toPath()
        if (!FILE_SYSTEM.exists(path)) return@withLock null
        decoder(FILE_SYSTEM.source(path).buffer())
    }

    suspend fun add(value: T) {
        if (!FILE_SYSTEM.exists(folderPath.toPath()))
            FILE_SYSTEM.createDirectory(folderPath.toPath(), false)

        val index: String = indexWith(value)
        lock.withLock { encoder(FILE_SYSTEM.sink("$folderPath/$index".toPath()).buffer(), value) }
        stateFlow.emit(indexes)
    }

    suspend fun remove(index: String) {
        if (!FILE_SYSTEM.exists(folderPath.toPath())) return
        lock.withLock { FILE_SYSTEM.delete("$folderPath/$index".toPath()) }
        stateFlow.emit(indexes)
    }

    suspend fun delete() {
        if (!FILE_SYSTEM.exists(folderPath.toPath())) return
        lock.withLock { FILE_SYSTEM.deleteRecursively(folderPath.toPath()) }
        stateFlow.emit(indexes)
    }
}