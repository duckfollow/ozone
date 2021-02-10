package me.duckfollow.ozone.util

import android.net.SSLCertificateSocketFactory
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ApiConnection{
    fun getData(url_api:String): String? {
        val url: URL
        var connection: HttpsURLConnection? = null
        try {
            url = URL(url_api)
            connection = url.openConnection() as HttpsURLConnection
            if (connection is HttpsURLConnection) {
                val httpsConn = connection as HttpsURLConnection?
                httpsConn!!.sslSocketFactory = SSLCertificateSocketFactory.getInsecure(0, null)
                httpsConn.hostnameVerifier = AllowAllHostnameVerifier()
            }
            connection.requestMethod = "GET"
            //Get Response
            val input_stream = connection.inputStream
            val rd = BufferedReader(InputStreamReader(input_stream))
            val response= rd.readLine().toString()
            rd.close()
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return e.toString()
        } finally {
            connection?.disconnect()
        }
    }

    fun getDataWeather(url_api:String): String? {
        val url: URL
        var connection: HttpsURLConnection? = null
        try {
            url = URL(url_api)
            connection = url.openConnection() as HttpsURLConnection
            if (connection is HttpsURLConnection) {
                val httpsConn = connection as HttpsURLConnection?
                httpsConn!!.sslSocketFactory = SSLCertificateSocketFactory.getInsecure(0, null)
                httpsConn.hostnameVerifier = AllowAllHostnameVerifier()
            }
            connection.requestMethod = "GET"
            connection.setRequestProperty("x-rapidapi-key","7667190828msheb69bc7cf74c7a0p19f668jsn3c79d493665c")
            connection.setRequestProperty("x-rapidapi-host","weatherbit-v1-mashape.p.rapidapi.com")
            connection.setRequestProperty("useQueryString","true")
            //Get Response
            val input_stream = connection.inputStream
            val rd = BufferedReader(InputStreamReader(input_stream))
            val response= rd.readLine().toString()
            rd.close()
            return response
        } catch (e: Exception) {
            e.printStackTrace()
            return e.toString()
        } finally {
            connection?.disconnect()
        }
    }
}