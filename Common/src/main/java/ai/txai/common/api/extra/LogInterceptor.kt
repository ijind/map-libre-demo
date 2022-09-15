package ai.txai.common.api.extra

import ai.txai.common.api.ApiConfig
import ai.txai.common.log.LOG
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.GzipSource
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Time: 14/07/2022
 * Author Hay
 */
class LogInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {

        val sBuilder = StringBuilder("\n")
        val request = chain.request()

        val logBody = true
        val logHeaders = true

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
            ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        if (!logHeaders && requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        sBuilder.append(requestStartMessage).append("\n")

        val requestHeaders = request.headers

        if (requestBody != null) {
            // Request body headers are only present when installed as a network interceptor. When not
            // already present, force them to be included (if available) so their values are known.
            requestBody.contentType()?.let {
                if (requestHeaders["Content-Type"] == null) {
                    sBuilder.append("Content-Type: $it").append("\n")
                }
            }
            if (requestBody.contentLength() != -1L) {
                if (requestHeaders["Content-Length"] == null) {
                    sBuilder.append("Content-Length: ${requestBody.contentLength()}").append("\n")
                }
            }
        }

        for (i in 0 until requestHeaders.size) {
            sBuilder.append(requestHeaders.name(i) + ": " + requestHeaders.value(i)).append("\n")
        }
        sBuilder.append("\n")
        if (!logBody || requestBody == null) {
            sBuilder.append("--> END ${request.method}").append("\n")
        } else if (bodyHasUnknownEncoding(request.headers)) {
            sBuilder.append("--> END ${request.method} (encoded body omitted)").append("\n")
        } else if (requestBody.isDuplex()) {
            sBuilder.append("--> END ${request.method} (duplex request body omitted)").append("\n")
        } else if (requestBody.isOneShot()) {
            sBuilder.append("--> END ${request.method} (one-shot body omitted)").append("\n")
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val contentType = requestBody.contentType()
            val charset: Charset =
                contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8


            if (buffer.isProbablyUtf8()) {
                if (!ApiConfig.filterUpload(request)) {
                    sBuilder.append(buffer.readString(charset))
                }
                sBuilder.append("--> END ${request.method} (${requestBody.contentLength()}-byte body)").append("\n")
            } else {
                sBuilder.append(
                    "--> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)"
                ).append("\n")
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            sBuilder.append("\n")
            sBuilder.append("<-- HTTP FAILED: $e").append("\n")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        sBuilder.append(
            "<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (${tookMs}ms${if (!logHeaders) ", $bodySize body" else ""})"
        ).append("\n")

        val responseHeaders = response.headers
        for (i in 0 until responseHeaders.size) {
            sBuilder.append(responseHeaders.name(i) + ": " + responseHeaders.value(i)).append("\n")
        }
        sBuilder.append("\n")
        if (!logBody || !response.promisesBody()) {
            sBuilder.append("<-- END HTTP").append("\n")
        } else if (bodyHasUnknownEncoding(response.headers)) {
            sBuilder.append("<-- END HTTP (encoded body omitted)").append("\n")
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer

            var gzippedLength: Long? = null
            if ("gzip".equals(responseHeaders["Content-Encoding"], ignoreCase = true)) {
                gzippedLength = buffer.size
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            val contentType = responseBody.contentType()
            val charset: Charset =
                contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            if (!buffer.isProbablyUtf8()) {
                sBuilder.append("<-- END HTTP (binary ${buffer.size}-byte body omitted)").append("\n")
                return response
            }

            if (contentLength != 0L) {
                sBuilder.append(buffer.clone().readString(charset)).append("\n")
            }

            if (gzippedLength != null) {
                sBuilder.append("<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body)").append("\n")
            } else {
                sBuilder.append("<-- END HTTP (${buffer.size}-byte body)").append("\n")
            }
        }

        try {
            LOG.i("Http", sBuilder.toString())
        } catch (ignore: Exception) {
            //DO Nothing
        }

        return response
    }


    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }
}