package com.brownian.plugins.intellij.complexity.markers;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.intellij.ui.TextIcon;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ComplexityLineMarkerInfo<T extends PsiElement> extends LineMarkerInfo<T> {
    public ComplexityLineMarkerInfo(
            int complexity,
            T element,
            String complexityType
    ) {
        super(element,
                element.getTextRange(),
                getIcon(complexity),
                ignored -> String.format("%s: %d", complexityType, complexity),
                null,
                GutterIconRenderer.Alignment.CENTER,
                () -> String.format("%s = %d", complexityType, complexity)
        );
    }

    @NotNull
    private static TextIcon getIcon(int complexity) {
        TextIcon textIcon = new TextIcon(getComplexityText(complexity), getForeground(complexity), getBackground(complexity), 2);
        textIcon.setFont(new Font(null, Font.PLAIN, /* TODO: get editor font size */ 12));
        textIcon.setRound(3);
        return textIcon;
    }

    @NotNull
    private static String getComplexityText(int complexity) {
        if (complexity < 1e3) {
            return String.valueOf(complexity);
        } else if (complexity < 1e6) {
            return (complexity / 1000) + "k";
        } else if (complexity < 1e9) {
            return (complexity / 1_000_000) + "m";
        } else {
            return String.format("10^%.0f", Math.log10(complexity));
        }
    }

    private static Color getForeground(int complexity) {
        if (complexity < 30)
            return JBColor.WHITE;
        else if (complexity < 40)
            return JBColor.BLACK;
        return JBColor.WHITE;
    }

    private static Color getBackground(int complexity) {
        if (complexity < 20)
            return JBColor.GREEN.brighter();
        else if (complexity < 30)
            return JBColor.GREEN;
        else if (complexity < 40)
            return JBColor.YELLOW;
        else if (complexity < 100)
            return JBColor.ORANGE;
        else if (complexity < 1000)
            return JBColor.RED;
        else if (complexity < 10000)
            return JBColor.RED.darker();
        return new JBColor(new Color(128, 0, 128), new Color(220, 50, 220));
    }
}
