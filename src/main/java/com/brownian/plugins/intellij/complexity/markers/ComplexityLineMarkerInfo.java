package com.brownian.plugins.intellij.complexity.markers;

import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.intellij.ui.RowIcon;
import com.intellij.ui.TextIcon;
import com.intellij.util.ui.EmptyIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ComplexityLineMarkerInfo<T extends PsiElement> extends MergeableLineMarkerInfo<T> {
    private final int complexity;
    private final String complexityType;

    public ComplexityLineMarkerInfo(
            int complexity,
            T element,
            String complexityType
    ) {
        super(element,
                element.getTextRange(),
                getIcon(complexity, complexityType),
                ignored -> String.format("%s: %d", complexityType, complexity),
                null,
                GutterIconRenderer.Alignment.CENTER
        );
        this.complexity = complexity;
        if (complexityType.isEmpty()) {
            throw new IllegalArgumentException("Complexity type must have at least one character");
        }
        this.complexityType = complexityType;
    }

    @NotNull
    private static TextIcon getIcon(int complexity, @Nullable String complexityType) {
        String complexityText = getComplexityText(complexity);
        if (complexityType != null) {
            complexityText = Character.toLowerCase(complexityType.charAt(0)) + complexityText;
        }
        TextIcon textIcon = new TextIcon(complexityText, getForeground(complexity), getBackground(complexity), 2);
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

    @Override
    public boolean canMergeWith(@NotNull MergeableLineMarkerInfo<?> info) {
        return info instanceof ComplexityLineMarkerInfo && info.getElement() == this.getElement();
    }

    /**
     * Groups together markers for the same element,
     * sorts them by type,
     * and ensures that all complexities are shown on the same tooltip.
     *
     * @param infos the markers that might be able to be merged with this element
     * @return the icon to show to represent all of the given markers at once
     */
    @Override
    public Icon getCommonIcon(@NotNull List<MergeableLineMarkerInfo> infos) {
        Icon[] icons = infos.stream()

                .filter(this::canMergeWith) // sanity check
                .map(i -> (ComplexityLineMarkerInfo<?>) i)

                .sorted(Comparator.comparing(v -> v.complexityType))

                .map(i -> getIcon(i.complexity, i.complexityType))

                // Creates a list of icons, spaced out by empty icons of the same size between each
                .collect(
                        ArrayList::new,
                        (List<Icon> l, Icon r) -> {
                            l.add(EmptyIcon.create(3));
                            l.add(r);
                        },
                        (List<Icon> l, List<Icon> r) -> {
                            l.add(EmptyIcon.create(3));
                            l.addAll(r);
                        }
                )

                .toArray(new Icon[0]);
        return new RowIcon(icons);
    }
}
