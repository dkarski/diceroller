package dev.dkarski.diceroller.ui

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.net.URI

/** Only web links are offered; no link is opened or fetched by the app. */
fun isSupportUrl(url: String): Boolean = runCatching {
    val uri = URI(url)
    uri.scheme in listOf("https", "http") && !uri.host.isNullOrBlank() && url.length <= 500
}.getOrDefault(false)

/** The displayed URL is the sole input to the QR encoder; no separately maintained bitmap. */
fun supportQrRows(url: String): List<String> {
    require(isSupportUrl(url)) { "Support URL must be an http(s) URL of at most 500 characters" }
    val matrix = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 1, 1, mapOf(
        EncodeHintType.MARGIN to 0,
        EncodeHintType.CHARACTER_SET to "UTF-8",
    ))
    return List(matrix.height) { y -> buildString {
        repeat(matrix.width) { x -> append(if (matrix[x, y]) '1' else '0') }
    } }
}
