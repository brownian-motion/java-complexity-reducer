package com.brownian.plugins.intellij.complexity.markers

import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.ui.JBColor
import com.intellij.ui.RowIcon
import com.intellij.ui.TextIcon
import com.intellij.util.containers.toArray
import com.intellij.util.ui.EmptyIcon
import java.awt.Color
import java.awt.Font
import java.util.function.Supplier
import javax.swing.Icon

class ComplexityLineMarkerInfo<T : PsiElement?>(
    private val complexity: Int,
    element: T,
    complexityType: String
) : MergeableLineMarkerInfo<T>(element,
    element!!.textRange,
    getIcon(complexity, complexityType),
    com.intellij.util.Function<T, String> { ignored: T ->
        String.format(
            "%s: %d", complexityType,
            complexity
        )
    },
    null,
    GutterIconRenderer.Alignment.CENTER,
    Supplier { "$complexityType complexity is $complexity" }
) {
    private val complexityType: String
    override fun canMergeWith(info: MergeableLineMarkerInfo<*>): Boolean {
        return info is ComplexityLineMarkerInfo<*> && info.getElement() === this.element
    }

    /**
     * Groups together markers for the same element,
     * sorts them by type,
     * and ensures that all complexities are shown on the same tooltip.
     *
     * @param infos the markers that might be able to be merged with this element
     * @return the icon to show to represent all of the given markers at once
     */
    override fun getCommonIcon(infos: List<MergeableLineMarkerInfo<*>?>): Icon {
        val icons = infos
            .filter { info: MergeableLineMarkerInfo<*>? -> info != null && canMergeWith(info) } // sanity check
            .map { i: MergeableLineMarkerInfo<*>? -> i as ComplexityLineMarkerInfo<*> }
            .sortedBy { v -> v.complexityType }
            .map { i ->
                getIcon(
                    i.complexity,
                    i.complexityType
                )
            } // Creates a list of icons, spaced out by empty icons of the same size between each
            .foldRight(ArrayList(), { r: Icon?, l: MutableList<Icon?> ->
                l.add(EmptyIcon.create(3))
                l.add(r)
                l
            }
            )
            .toArray(emptyArray())
        return RowIcon(*icons)
    }

    companion object {
        private fun getIcon(complexity: Int, complexityType: String?): TextIcon {
            var complexityText = getComplexityText(complexity)
            if (complexityType != null) {
                complexityText = Character.toLowerCase(complexityType[0]).toString() + complexityText
            }
            val textIcon = TextIcon(complexityText, getForeground(complexity), getBackground(complexity), 2)
            textIcon.setFont(Font(null, Font.PLAIN,  /* TODO: get editor font size */12))
            textIcon.setRound(3)
            return textIcon
        }

        private fun getComplexityText(complexity: Int): String {
            return if (complexity < 1e3) {
                complexity.toString()
            } else if (complexity < 1e6) {
                (complexity / 1000).toString() + "k"
            } else if (complexity < 1e9) {
                (complexity / 1000000).toString() + "m"
            } else {
                String.format("10^%.0f", Math.log10(complexity.toDouble()))
            }
        }

        private fun getForeground(complexity: Int): Color {
            if (complexity < 30) return JBColor.WHITE else if (complexity < 40) return JBColor.BLACK
            return JBColor.WHITE
        }

        private fun getBackground(complexity: Int): Color {
            if (complexity < 20) return JBColor.GREEN.brighter() else if (complexity < 30) return JBColor.GREEN else if (complexity < 40) return JBColor.YELLOW else if (complexity < 100) return JBColor.ORANGE else if (complexity < 1000) return JBColor.RED else if (complexity < 10000) return JBColor.RED.darker()
            return JBColor(Color(128, 0, 128), Color(220, 50, 220))
        }
    }

    init {
        require(!complexityType.isEmpty()) { "Complexity type must have at least one character" }
        this.complexityType = complexityType
    }
}