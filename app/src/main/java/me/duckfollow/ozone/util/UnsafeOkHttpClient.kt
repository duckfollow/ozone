package me.duckfollow.ozone.util

import android.util.Log
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

object UnsafeOkHttpClient {
    // Create a trust manager that does not validate certificate chains
    // Install the all-trusting trust manager
    // Create an ssl socket factory with our all-trusting manager
    val time_out = 30.0
    val unsafeOkHttpClient: OkHttpClient
        get() {
            try {
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun getAcceptedIssuers(): Array<X509Certificate> {
                        return arrayOf()
                    }
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(
                            chain: Array<java.security.cert.X509Certificate>,
                            authType: String
                    ) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(
                            chain: Array<java.security.cert.X509Certificate>,
                            authType: String
                    ) {
                    }
                })
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())
                val sslSocketFactory = sslContext.getSocketFactory()

                val c = trustAllCerts[0] as X509TrustManager

//                val certificatePinner = CertificatePinner.Builder()
//                        .add("api.waqi.info","sha256/Hs/tF4PnZubcnKTNr4jrGuuS7PC2p6g+LkPspU3yCrE=")
//                        .add("api.waqi.info","sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=")
//                        .add("api.waqi.info","sha256/Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=")
//                        .build()

                val builder = OkHttpClient.Builder()
                builder.readTimeout(time_out.toLong(),TimeUnit.SECONDS)
                builder.connectTimeout(time_out.toLong(),TimeUnit.SECONDS)
                //builder.certificatePinner(certificatePinner)
                builder.sslSocketFactory(sslSocketFactory,c)
                builder.hostnameVerifier(object : HostnameVerifier {
                    override fun verify(hostname: String, session: SSLSession): Boolean {
                        return true
                    }
                })

                return builder.build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
}