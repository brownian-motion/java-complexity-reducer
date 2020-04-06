package com.brownian.plugins.intellij.complexity.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.psi.*

abstract class AbstractComplexityLineMarker<T : JavaElementVisitor>(
    val visitorSupplier: () -> T,
    val complexityGetter: T.() -> Int
) : LineMarkerProviderDescriptor() {
    override fun getName(): String? = "Cyclomatic complexity line marker"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val parent = element.parent
        if (element is PsiIdentifier) {
            if ((parent is PsiMethod && parent.nameIdentifier == element) ||
                (parent is PsiClass && parent.nameIdentifier == element)
            ) {
                return getComplexityMarkerInfo(element, parent)
            }
        } else if (element is PsiKeyword) {
            if (
                ((element.textMatches(PsiKeyword.SWITCH) || element.textMatches(PsiKeyword.CASE)) && (parent is PsiSwitchStatement || parent is PsiSwitchExpression)) ||
                (element.textMatches(PsiKeyword.WHILE) && parent is PsiWhileStatement) ||
                (element.textMatches(PsiKeyword.FOR) && (parent is PsiForStatement || parent is PsiForeachStatement)) ||
                (element.textMatches(PsiKeyword.DO) && parent is PsiDoWhileStatement) ||
                (element.textMatches(PsiKeyword.TRY) && parent is PsiTryStatement) ||
                (element.textMatches(PsiKeyword.CATCH) && parent is PsiCatchSection)
            ) {
                return getComplexityMarkerInfo(element, parent)
            } else if (element.textMatches(PsiKeyword.IF) && parent is PsiIfStatement) {
                return getComplexityMarkerInfo(
                    element,
                    parent.thenBranch ?: return null,
                    parent.condition ?: return null
                )
            } else if (element.textMatches(PsiKeyword.ELSE) && parent is PsiIfStatement && parent.elseBranch !is PsiIfStatement) {
                return getComplexityMarkerInfo(element, parent.elseBranch ?: return null)
            }
        }

        return null
    }

    private fun <E : PsiElement, P : PsiElement> getComplexityMarkerInfo(
        elementToMark: E,
        vararg blocksToMeasure: P
    ): LineMarkerInfo<*>? {
        val visitor = visitorSupplier()
        blocksToMeasure.forEach { visitor.visitElement(it) }
        val complexity = visitor.complexityGetter()
        return if (complexity == 1) {
            null
        } else {
            ComplexityLineMarkerInfo(complexity, elementToMark)
        }
    }
}
