package com.brownian.plugins.intellij.complexity.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.psi.PsiElement

class NPathComplexityLineMarker : AbstractComplexityLineMarker("NPath Complexity") {
    override fun <E : PsiElement, P : PsiElement> getComplexityMarkerInfo(
        elementToMark: E,
        vararg blocksToMeasure: P
    ): LineMarkerInfo<E>? {
        val visitor = NPathComplexityVisitor()
        var complexity = 0
        for (it in blocksToMeasure) {
            visitor.reset(); it.accept(visitor); complexity += visitor.complexity
        }
        return if (complexity <= 1) {
            null
        } else {
            ComplexityLineMarkerInfo(complexity, elementToMark, complexityType)
        }
    }
}