package com.brownian.plugins.intellij.complexity.markers;

import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.intellij.ui.TextIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ComplexityLineMarkerInfo<T extends PsiElement> extends MergeableLineMarkerInfo<T> {
    private final int complexity;

    public ComplexityLineMarkerInfo(
            int complexity,
            T element
    ) {
        super(element,
                element.getTextRange(),
                getIcon(complexity),
                ignored -> String.valueOf(complexity),
                null,
                GutterIconRenderer.Alignment.CENTER
        );
        this.complexity = complexity;
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
        if (complexity < 40)
            return JBColor.BLACK;
        return JBColor.WHITE;
    }

    private static Color getBackground(int complexity) {
        if (complexity < 20)
            return JBColor.GREEN.brighter();
        if (complexity < 30)
            return JBColor.GREEN;
        if (complexity < 40)
            return JBColor.YELLOW;
        if (complexity < 100)
            return JBColor.ORANGE;
        if (complexity < 1000)
            return JBColor.RED;
        if (complexity < 10000)
            return JBColor.RED.darker();
        return new JBColor(new Color(128, 0, 128), new Color(220, 50, 220));
    }

    @Override
    public boolean canMergeWith(@NotNull MergeableLineMarkerInfo<?> info) {
        return info instanceof ComplexityLineMarkerInfo;
    }

    @Override
    public Icon getCommonIcon(@NotNull List<MergeableLineMarkerInfo> infos) {
        int totalComplexity = infos.stream()
                .filter(i -> i instanceof ComplexityLineMarkerInfo)
                .mapToInt(i -> ((ComplexityLineMarkerInfo<?>) i).complexity)
                .sum();
        return getIcon(totalComplexity);
    }
}
