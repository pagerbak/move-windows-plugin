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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import java.awt.Rectangle;
import java.awt.Window;
import javax.swing.JFrame;

public class MoveAllWindowsAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ScreenPickerDialog dialog = new ScreenPickerDialog();
        if (!dialog.showAndGet()) return;
        if (dialog.getChosen() == null) return;
        Rectangle target = dialog.getChosen().getBounds();

        WindowManager wm = WindowManager.getInstance();

        for (var project : ProjectManager.getInstance().getOpenProjects()) {
            JFrame frame = wm.getFrame(project);
            if (frame == null) continue;
            moveFrameToScreen(frame, target);
        }

        for (Window w : Window.getWindows()) {
            if (w instanceof JFrame || !w.isVisible()) continue;
            Rectangle src = w.getGraphicsConfiguration() != null
                    ? w.getGraphicsConfiguration().getBounds() : null;
            moveWindowToScreen(w, target, src);
        }
    }

    private void moveFrameToScreen(JFrame frame, Rectangle target) {
        // A maximized frame can't be relocated; restore it first.
        if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
            frame.setExtendedState(JFrame.NORMAL);
        }
        Rectangle src = frame.getGraphicsConfiguration() != null
                ? frame.getGraphicsConfiguration().getBounds() : null;
        moveWindowToScreen(frame, target, src);
    }

    private void moveWindowToScreen(Window w, Rectangle target, Rectangle src) {
        if (src == null) return;
        // Preserve the window's relative position within its current screen.
        int relX = w.getX() - src.x;
        int relY = w.getY() - src.y;
        int newX = target.x + relX;
        int newY = target.y + relY;
        // Clamp so the window stays fully on the target screen.
        newX = Math.max(target.x, Math.min(newX, target.x + target.width - w.getWidth()));
        newY = Math.max(target.y, Math.min(newY, target.y + target.height - w.getHeight()));
        w.setLocation(newX, newY);
    }
}
