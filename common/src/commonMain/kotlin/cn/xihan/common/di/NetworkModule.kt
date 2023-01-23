package cn.xihan.common.di


import cn.xihan.common.di.NetworkModule.provideKtorClient
import cn.xihan.common.di.NetworkModule.provideRemoteService
import cn.xihan.common.di.NetworkModule.provideRetrofit
import cn.xihan.common.di.RepositoryModule.provideRepository
import cn.xihan.common.network.RemoteService
import cn.xihan.common.repository.RemoteRepository
import cn.xihan.common.utils.BASE_API
import de.jensklingenberg.ktorfit.Ktorfit
import de.jensklingenberg.ktorfit.converter.builtin.FlowResponseConverter
import de.jensklingenberg.ktorfit.create
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule = module {
    single { provideKtorClient() }
    single { provideRetrofit(get()) }
    single { provideRemoteService(get()) }
    single { provideRepository(get()) }
}

object NetworkModule {

    fun provideKtorClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }

//        install(HttpTimeout) {
//            requestTimeoutMillis = 5000
//            connectTimeoutMillis = 3000
//            socketTimeoutMillis = 5000
//        }

        engine {
            config {
                followRedirects(true)
                followSslRedirects(true)
                // 信任自签名证书
                //sslSocketFactory(SslSettings.getSslContext()!!.socketFactory, SslSettings.getTrustManager())
            }
        }

    }

    fun provideRetrofit(httpClient: HttpClient): Ktorfit = Ktorfit
        .Builder()
        .baseUrl(BASE_API)
        .httpClient(httpClient)
        .responseConverter(FlowResponseConverter())
        .build()

    fun provideRemoteService(retrofit: Ktorfit): RemoteService {
        return retrofit.create()
    }

}

object RepositoryModule {


    fun provideRepository(remoteService: RemoteService): RemoteRepository {
        return RemoteRepository(remoteService)
    }

}

/*
object SslSettings {
    fun getKeyStore(): KeyStore {
        //val keyStoreFile = FileInputStream("build/keystore.jks")
        //Buffer().writeUtf8(CER).inputStream()
        val keyStorePassword = "6ozaqkmiK2UJ".toCharArray()
        val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(keyStoreFile, keyStorePassword)
        return keyStore
    }

    fun getTrustManagerFactory(): TrustManagerFactory? {
        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(getKeyStore())
        return trustManagerFactory
    }

    fun getSslContext(): SSLContext? {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, getTrustManagerFactory()?.trustManagers, null)
        return sslContext
    }

    fun getTrustManager(): X509TrustManager {
        return getTrustManagerFactory()?.trustManagers?.first { it is X509TrustManager } as X509TrustManager
    }
}

 */

