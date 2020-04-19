package com.brownian.plugins.intellij.complexity.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import java.util.*

class NPathComplexityLineMarker : AbstractComplexityLineMarker("NPath Complexity") {
    private val logger = Logger.getInstance(NPathComplexityLineMarker::class.java)

    override fun <E : PsiElement, P : PsiElement> getComplexityMarkerInfo(
        elementToMark: E,
        vararg blocksToMeasure: P
    ): LineMarkerInfo<E>? {
        return try {
            val visitor = NPathComplexityVisitor()
            var complexity = 0
            for (it in blocksToMeasure) {
                visitor.reset()
                it.accept(visitor)
                complexity += visitor.complexity ?: continue
            }
            if (complexity <= 1) {
                null
            } else {
                ComplexityLineMarkerInfo(complexity, elementToMark, complexityType)
            }
        } catch (e: EmptyStackException) {
            logIncorrectCalculation(elementToMark, e)
            null
        }
    }

    private fun <E : PsiElement> logIncorrectCalculation(elementToMark: E, e: EmptyStackException) {
        val fileText = elementToMark.containingFile.text
        val textOffset = elementToMark.textOffset
        val lineColumn = StringUtil.offsetToLineColumn(fileText, textOffset)
        val textLength = elementToMark.textLength
        val shortName = if (textLength < 20) {
            elementToMark.text
        } else {
            elementToMark.text.substring(0..15) + "..." + elementToMark.text.substring(textLength - 5, textLength)
        }
        logger.error(
            "Empty stack while visiting ${elementToMark.elementType} $shortName at line ${lineColumn.line + 1}, col ${lineColumn.column + 1} in ${elementToMark.containingFile.name}",
            e
        )
    }
}