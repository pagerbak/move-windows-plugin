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

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import java.awt.GraphicsConfiguration;
import javax.swing.JComponent;

/** Modal dialog wrapping the picker; resolves to the chosen config. */
class ScreenPickerDialog extends DialogWrapper {

    private GraphicsConfiguration chosen = null;

    ScreenPickerDialog() {
        super(true);
        setTitle("Move All Windows To…");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return new ScreenPickerPanel(cfg -> {
            chosen = cfg;
            close(OK_EXIT_CODE);
        });
    }

    @Nullable
    GraphicsConfiguration getChosen() {
        return chosen;
    }
}
