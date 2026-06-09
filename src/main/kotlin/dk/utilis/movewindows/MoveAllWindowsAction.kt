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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import java.awt.Rectangle
import java.awt.Window
import javax.swing.JFrame

class MoveAllWindowsAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val dialog = ScreenPickerDialog()
        if (!dialog.showAndGet()) return            // user cancelled
        val target = dialog.chosen?.bounds ?: return

        val wm = WindowManager.getInstance()

        // Move every open project window.
        for (project in ProjectManager.getInstance().openProjects) {
            val frame = wm.getFrame(project) ?: continue
            moveFrameToScreen(frame, target)
        }

        // Also move detached / floating tool windows (separate AWT windows).
        for (w in Window.getWindows()) {
            if (w is JFrame || !w.isVisible) continue
            moveWindowToScreen(w, target, w.graphicsConfiguration?.bounds)
        }
    }

    private fun moveFrameToScreen(frame: JFrame, target: Rectangle) {
        // A maximized frame can't be relocated; restore it first.
        if (frame.extendedState and JFrame.MAXIMIZED_BOTH != 0) {
            frame.extendedState = JFrame.NORMAL
        }
        moveWindowToScreen(frame, target, frame.graphicsConfiguration?.bounds)
    }

    private fun moveWindowToScreen(w: Window, target: Rectangle, src: Rectangle?) {
        if (src == null) return
        // Preserve the window's relative position within its current screen.
        val relX = w.x - src.x
        val relY = w.y - src.y
        var newX = target.x + relX
        var newY = target.y + relY
        // Clamp so the window stays fully on the target screen.
        newX = newX.coerceIn(target.x, target.x + target.width - w.width)
        newY = newY.coerceIn(target.y, target.y + target.height - w.height)
        w.setLocation(newX, newY)
    }
}
