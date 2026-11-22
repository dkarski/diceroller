package dev.dkarski.diceroller.ui

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.*
import org.junit.Test

class SupportQrTest {
    @Test fun `QR decodes to the exact configured URL`() {
        listOf("https://example.org/tip", "https://example.org/tip?q=zażółć&app=notes", "https://example.org/" + "x".repeat(450)).forEach { url ->
            val rows = supportQrRows(url)
            val scale = 4
            val side = (rows.size + 8) * scale
            val pixels = IntArray(side * side) { -1 }
            rows.forEachIndexed { y, row -> row.forEachIndexed { x, c ->
                if (c == '1') repeat(scale) { dy -> repeat(scale) { dx ->
                    pixels[((y + 4) * scale + dy) * side + (x + 4) * scale + dx] = -16777216
                } }
            } }
            val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(side, side, pixels)))
            assertEquals(url, QRCodeReader().decode(bitmap, mapOf(DecodeHintType.CHARACTER_SET to "UTF-8", DecodeHintType.PURE_BARCODE to true)).text)
        }
    }

    @Test fun `empty or non web support links are disabled`() {
        listOf("", "not a link", "mailto:me@example.org", "javascript:alert(1)", "https://", "https://example.org/" + "x".repeat(501)).forEach {
            assertFalse(it, isSupportUrl(it))
        }
        assertTrue(isSupportUrl("https://example.org/support"))
    }
}
