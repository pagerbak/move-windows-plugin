/*
Copyright 2026 Per Agerbæk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package dk.utilis.movewindows

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * A clickable map of the current display arrangement, mirroring the
 * macOS System Settings > Displays layout. Clicking a screen selects it.
 */
class ScreenPickerPanel(
    private val onPick: (GraphicsConfiguration) -> Unit
) : JPanel() {

    private data class Screen(
        val cfg: GraphicsConfiguration,
        val real: Rectangle,                 // bounds in the OS virtual coord space
        val isPrimary: Boolean,
        var drawn: Rectangle = Rectangle()   // scaled rect on this panel
    )

    private val screens: List<Screen>
    private var hovered: Screen? = null
    private val pad = JBUI.scale(16)

    init {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val primary = ge.defaultScreenDevice
        screens = ge.screenDevices.map { d ->
            val cfg = d.defaultConfiguration
            Screen(cfg, cfg.bounds, d == primary)
        }
        preferredSize = JBUI.size(460, 300)
        isFocusable = true

        object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                val h = screens.firstOrNull { it.drawn.contains(e.point) }
                if (h != hovered) { hovered = h; repaint() }
            }
            override fun mouseExited(e: MouseEvent) { hovered = null; repaint() }
            override fun mouseClicked(e: MouseEvent) {
                screens.firstOrNull { it.drawn.contains(e.point) }
                    ?.let { onPick(it.cfg) }
            }
        }.also { addMouseListener(it); addMouseMotionListener(it) }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // 1. Union of all screen rectangles = the whole virtual desktop.
        var union = screens.first().real
        for (s in screens) union = union.union(s.real)

        // 2. One scale factor that fits the union into the padded panel.
        val availW = width - pad * 2
        val availH = height - pad * 2
        val scale = minOf(availW.toDouble() / union.width, availH.toDouble() / union.height)

        // 3. Center the scaled layout in the panel.
        val offX = pad + (availW - union.width * scale) / 2
        val offY = pad + (availH - union.height * scale) / 2

        for (s in screens) {
            val dx = (offX + (s.real.x - union.x) * scale).toInt()
            val dy = (offY + (s.real.y - union.y) * scale).toInt()
            val dw = (s.real.width * scale).toInt()
            val dh = (s.real.height * scale).toInt()
            s.drawn = Rectangle(dx, dy, dw, dh)

            val fill = when {
                s == hovered -> JBColor(0xCFE3FF, 0x2D4F73)
                s.isPrimary  -> JBColor(0xE8E8E8, 0x3C3F41)
                else         -> JBColor(0xF5F5F5, 0x313335)
            }
            g2.color = fill
            g2.fillRoundRect(dx, dy, dw, dh, 10, 10)
            g2.color = JBColor.border()
            g2.stroke = BasicStroke(if (s == hovered) 2.5f else 1.5f)
            g2.drawRoundRect(dx, dy, dw, dh, 10, 10)

            // Labels: resolution + primary marker.
            g2.color = JBColor.foreground()
            val label = "${s.real.width}\u00D7${s.real.height}"
            drawCentered(g2, label, dx + dw / 2, dy + dh / 2 - 7)
            if (s.isPrimary) drawCentered(g2, "Primary", dx + dw / 2, dy + dh / 2 + 9)
        }
    }

    private fun drawCentered(g2: Graphics2D, text: String, cx: Int, cy: Int) {
        val fm = g2.fontMetrics
        g2.drawString(text, cx - fm.stringWidth(text) / 2, cy + fm.ascent / 2)
    }
}

/** Modal dialog wrapping the picker; resolves to the chosen config. */
class ScreenPickerDialog : DialogWrapper(true) {
    var chosen: GraphicsConfiguration? = null
        private set

    init {
        title = "Move All Windows To\u2026"
        init()
    }

    override fun createCenterPanel(): JComponent =
        ScreenPickerPanel { cfg ->
            chosen = cfg
            close(OK_EXIT_CODE)   // clicking a screen confirms and closes
        }
}
