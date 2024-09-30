package ru.logosph

import java.rmi.UnexpectedException
import kotlin.jvm.Throws
import kotlin.math.abs

data class HSV @Throws(IllegalArgumentException::class) constructor (
    val h: Int,
    val s: Int,
    val v: Int,
) {

    @Throws(IllegalArgumentException::class)
    constructor(hsvList: List<Int>) : this(
        hsvList[0],
        hsvList[1],
        hsvList[2]
    )

    init {
        if (!(h in 0..360 && s in 0..100 && v in 0..100)) throw IllegalArgumentException("HSV values must be in range from 0 to 360 for H and from 0 to 100 for S and V")
    }
}

data class RGB @Throws(IllegalArgumentException::class) constructor (
    val r: Int,
    val g: Int,
    val b: Int,
) {

    @Throws(IllegalArgumentException::class)
    constructor(rgbList: List<Int>) : this(
        rgbList[0],
        rgbList[1],
        rgbList[2]
    )


    init {
        if (!(r in 0..255 && g in 0..255 && b in 0..255)) throw IllegalArgumentException("RGB values must be in range from 0 to 255")
    }
}

fun Boolean.asInt() = if (this) 1 else 0

@Throws(IllegalArgumentException::class, UnexpectedException::class)
fun hexToRgb(hex: String): RGB {

    // Check for valid input
    if (hex.length != 7) throw IllegalArgumentException("hexToRgb function must get a 7-symbols string")
    if (!hex.startsWith("#")) throw IllegalArgumentException("hexToRgb function must get a string starts with '#'")

    // Check for valid characters in hex
    var chars = 0
    val upperHex = hex.uppercase().slice(1..6)
    val range = ('0'..'9') + ('A'..'F') + '#'
    for (ch in upperHex) {
        chars += (ch in range).asInt()
    }
    if (chars != 6) throw IllegalArgumentException("hexToRgb function must get a string contains only digits, letters from A to Z and '#' symbol")

    // Creating HEX values table
    val hexValues = mutableMapOf<Char, Int>()
    for ((value, char) in (('0'..'9') + ('A'..'Z')).withIndex()) hexValues[char] = value

    // Converting
    val rgb = mutableListOf(0, 0, 0)
    for ((ind, ch) in upperHex.withIndex()) rgb[ind / 2] += hexValues[ch]?.times(if (ind % 2 == 0) 16 else 1)
        ?: throw UnexpectedException("Unexpected exception")

    return RGB(rgb)
}

@Throws(IllegalArgumentException::class)
fun rgbToHex(r: Int, g: Int, b: Int) = rgbToHex(RGB(r, g, b))

@Throws(IllegalArgumentException::class)
fun rgbToHex(rgb: RGB): String {
    // Creating HEX values table
    val hexValues = mutableMapOf<Int, Char>()
    for ((value, char) in (('0'..'9') + ('A'..'Z')).withIndex()) hexValues[value] = char

    // Converting
    return StringBuilder("#").apply {
        append(hexValues[rgb.r % 16])
        append(hexValues[rgb.r / 16 % 16])
        append(hexValues[rgb.g % 16])
        append(hexValues[rgb.g / 16 % 16])
        append(hexValues[rgb.b % 16])
        append(hexValues[rgb.b / 16 % 16])
    }.toString()
}

fun hsvToRgb(hsv: HSV) = hsvToRgb(hsv.h, hsv.s, hsv.v)

fun hsvToRgb(h: Int, s: Int, v: Int): RGB {
    // Validate input ranges
    require(h in 0..360) { "Hue must be in range from 0 to 360" }
    require(s in 0..100) { "Saturation must be in range from 0 to 100" }
    require(v in 0..100) { "Value must be in range from 0 to 100" }

    // Calculate Chroma
    val c = v * s / 10000.0f
    // Calculate X
    val x = c * (1 - abs((h / 60) % 2 - 1))
    // Calculate M
    val m = v / 100.0f - c

    // Determine RGB values based on hue range
    val (r, g, b) = when (h) {
        in 0..59 -> Triple(c, x, 0.0f)
        in 60..119 -> Triple(x, c, 0.0f)
        in 120..179 -> Triple(0.0f, c, x)
        in 180..239 -> Triple(0.0f, x, c)
        in 240..299 -> Triple(x, 0.0f, c)
        in 300..360 -> Triple(c, 0.0f, x)
        else -> throw IllegalArgumentException("Hue must be in range from 0 to 360")
    }

    // Convert to 0-255 range and return RGB object
    return RGB(
        ((r + m) * 255).toInt(),
        ((g + m) * 255).toInt(),
        ((b + m) * 255).toInt()
    )
}

fun hsvToHex(hsv: HSV) = rgbToHex(hsvToRgb(hsv))

fun hsvToHex(h: Int, s: Int, v: Int) = rgbToHex(hsvToRgb(h, s, v))

fun rgbToHsv(rgb: RGB) = rgbToHsv(rgb.r, rgb.g, rgb.b)

fun rgbToHsv(r: Int, g: Int, b: Int): HSV {
    // Validate input ranges
    require(r in 0..255) { "Red must be in range from 0 to 255" }
    require(g in 0..255) { "Green must be in range from 0 to 255" }
    require(b in 0..255) { "Blue must be in range from 0 to 255" }

    // Normalize RGB values
    val rNorm = r / 255.0f
    val gNorm = g / 255.0f
    val bNorm = b / 255.0f

    // Calculate Chroma
    val cMax = maxOf(rNorm, gNorm, bNorm)
    val cMin = minOf(rNorm, gNorm, bNorm)
    val c = cMax - cMin

    // Calculate Hue
    val h = when {
        c == 0.0f -> 0
        cMax == rNorm -> 60 * ((gNorm - bNorm) / c % 6)
        cMax == gNorm -> 60 * ((bNorm - rNorm) / c + 2)
        cMax == bNorm -> 60 * ((rNorm - gNorm) / c + 4)
        else -> throw UnexpectedException("Unexpected exception")
    }

    // Calculate Value
    val v = cMax * 100

    // Calculate Saturation
    val s = if (v == 0.0f) 0 else c / cMax * 100

    return HSV(h.toInt(), s.toInt(), v.toInt())
}

fun hexToHsv(hex: String) = rgbToHsv(hexToRgb(hex))
