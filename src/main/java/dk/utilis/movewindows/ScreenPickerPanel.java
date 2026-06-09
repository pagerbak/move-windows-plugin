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
package dk.utilis.movewindows;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JPanel;

/**
 * A clickable map of the current display arrangement, mirroring the
 * macOS System Settings > Displays layout. Clicking a screen selects it.
 */
class ScreenPickerPanel extends JPanel {

    private static class Screen {
        final GraphicsConfiguration cfg;
        final Rectangle real;
        final boolean isPrimary;
        Rectangle drawn = new Rectangle();

        Screen(GraphicsConfiguration cfg, Rectangle real, boolean isPrimary) {
            this.cfg = cfg;
            this.real = real;
            this.isPrimary = isPrimary;
        }
    }

    private final List<Screen> screens = new ArrayList<>();
    private Screen hovered = null;
    private final int pad = JBUI.scale(16);

    ScreenPickerPanel(Consumer<GraphicsConfiguration> onPick) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice primary = ge.getDefaultScreenDevice();
        for (GraphicsDevice d : ge.getScreenDevices()) {
            GraphicsConfiguration cfg = d.getDefaultConfiguration();
            screens.add(new Screen(cfg, cfg.getBounds(), d == primary));
        }
        setPreferredSize(JBUI.size(460, 300));
        setFocusable(true);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Screen h = screens.stream()
                        .filter(s -> s.drawn.contains(e.getPoint()))
                        .findFirst()
                        .orElse(null);
                if (h != hovered) { hovered = h; repaint(); }
            }
            @Override
            public void mouseExited(MouseEvent e) { hovered = null; repaint(); }
            @Override
            public void mouseClicked(MouseEvent e) {
                screens.stream()
                        .filter(s -> s.drawn.contains(e.getPoint()))
                        .findFirst()
                        .ifPresent(s -> onPick.accept(s.cfg));
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Union of all screen rectangles = the whole virtual desktop.
        Rectangle union = screens.get(0).real;
        for (Screen s : screens) union = union.union(s.real);

        // 2. One scale factor that fits the union into the padded panel.
        int availW = getWidth() - pad * 2;
        int availH = getHeight() - pad * 2;
        double scale = Math.min((double) availW / union.width, (double) availH / union.height);

        // 3. Center the scaled layout in the panel.
        double offX = pad + (availW - union.width * scale) / 2.0;
        double offY = pad + (availH - union.height * scale) / 2.0;

        for (Screen s : screens) {
            int dx = (int)(offX + (s.real.x - union.x) * scale);
            int dy = (int)(offY + (s.real.y - union.y) * scale);
            int dw = (int)(s.real.width * scale);
            int dh = (int)(s.real.height * scale);
            s.drawn = new Rectangle(dx, dy, dw, dh);

            Color fill;
            if (s == hovered) {
                fill = new JBColor(0xCFE3FF, 0x2D4F73);
            } else if (s.isPrimary) {
                fill = new JBColor(0xE8E8E8, 0x3C3F41);
            } else {
                fill = new JBColor(0xF5F5F5, 0x313335);
            }
            g2.setColor(fill);
            g2.fillRoundRect(dx, dy, dw, dh, 10, 10);
            g2.setColor(JBColor.border());
            g2.setStroke(new BasicStroke(s == hovered ? 2.5f : 1.5f));
            g2.drawRoundRect(dx, dy, dw, dh, 10, 10);

            // Labels: resolution + primary marker.
            g2.setColor(JBColor.foreground());
            String label = s.real.width + "×" + s.real.height;
            drawCentered(g2, label, dx + dw / 2, dy + dh / 2 - 7);
            if (s.isPrimary) drawCentered(g2, "Primary", dx + dw / 2, dy + dh / 2 + 9);
        }
    }

    private void drawCentered(Graphics2D g2, String text, int cx, int cy) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, cx - fm.stringWidth(text) / 2, cy + fm.getAscent() / 2);
    }
}
