package service

import io.nayuki.qrcodegen.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import models.QrModel
import models.ErrorCorrection
import QrCode.*
import service.QrCodeGen.*

final class QrCodeGen private (qr: QrCode, scale: Option[Int], border: Int, lightColor: Int, darkColor: Int):
    def renderImage: BufferedImage =
        QrCodeGen.renderImage(qr, scale.get, border, lightColor, darkColor)

    def renderSvg: String =
        QrCodeGen.renderSvgString(qr, border, lightColor, darkColor)

object QrCodeGen:
    def apply(model: QrModel): QrCodeGen =
        new QrCodeGen(
            qr = QrCode.encodeText(model.url, model.ecc.toJavaEcc),
            scale = model.scale,
            border = model.border,
            lightColor = parseHexColor(model.lightColor),
            darkColor = parseHexColor(model.darkColor)
        )

    private def renderImage(qr: QrCode, scale: Int, border: Int, lightColor: Int = 0xffffff, darkColor: Int = 0x000000): BufferedImage =
        val size   = (qr.size + border * 2) * scale
        val result = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)

        val pixels =
            for
                y <- 0 until size
                x <- 0 until size
            yield (x, y, if qr.getModule(x / scale - border, y / scale - border) then darkColor else lightColor)

        pixels.foreach((x, y, rgb) => result.setRGB(x, y, rgb))
        result

    private def renderSvgString(qr: QrCode, border: Int, lightColor: Int = 0xffffff, darkColor: Int = 0x000000): String =
        val brd         = border.toLong
        val size        = qr.size + brd * 2
        val darkModules =
            for
                y <- 0 until qr.size
                x <- 0 until qr.size
                if qr.getModule(x, y)
            yield s"M${x + brd},${y + brd}h1v1h-1z"

        s"""<?xml version="1.0" encoding="UTF-8"?>
         |<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
         |<svg xmlns="http://www.w3.org/2000/svg" version="1.1" viewBox="0 0 $size $size" stroke="none">
         |\t<rect width="100%" height="100%" fill="$lightColor"/>
         |\t<path d="${darkModules.mkString(" ")}" fill="$darkColor"/>
         |</svg>
         |""".stripMargin

    private def parseHexColor(hex: String): Int =
        Integer.parseInt(hex.replace("#", ""), 16)
