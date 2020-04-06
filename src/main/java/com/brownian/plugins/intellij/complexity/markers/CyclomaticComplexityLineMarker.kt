package com.brownian.plugins.intellij.complexity.markers

import com.siyeh.ig.classmetrics.CyclomaticComplexityVisitor

class CyclomaticComplexityLineMarker : AbstractComplexityLineMarker<CyclomaticComplexityVisitor>(
    { CyclomaticComplexityVisitor() },
    CyclomaticComplexityVisitor::getComplexity
)
