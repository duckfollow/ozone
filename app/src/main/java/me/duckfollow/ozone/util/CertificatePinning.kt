package me.duckfollow.ozone.util

import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class CertificatePinning {
    private val client = OkHttpClient.Builder()
            .certificatePinner(
                    CertificatePinner.Builder()
                            .add("api.waqi.info", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
                            .build())
            .build()

    @Throws(Exception::class)
    fun run() {
        val request = Request.Builder()
                .url("https://api.waqi.info")
                .build()
        client.newCall(request).execute()/*.use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            for (certificate in response.handshake()!!.peerCertificates()) {
                println(CertificatePinner.pin(certificate))
            }
        }*/
    }

    companion object {
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {

            val host = "api.waqi.info"
                val certificatePinner = CertificatePinner.Builder()
                        .add("api.waqi.info","sha256/Hs/tF4PnZubcnKTNr4jrGuuS7PC2p6g+LkPspU3yCrE=")
                        .add("api.waqi.info","sha256/YLh1dUR9y6Kja30RrAn7JKnbQG/uEtLMkBgFF2Fuihg=")
                        .add("api.waqi.info","sha256/Vjs8r4z+80wjNcr1YKepWQboSIRi63WsWXhIMN+eWys=")
                        .build()
                val client = OkHttpClient.Builder()
                        .certificatePinner(certificatePinner)
                        .build()
                val request = Request.Builder()
                        .url("https://"+host)
                        .build()
                client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            for (certificate in response.handshake()!!.peerCertificates()) {
                println(CertificatePinner.pin(certificate))
            }
        }

        }
    }
}