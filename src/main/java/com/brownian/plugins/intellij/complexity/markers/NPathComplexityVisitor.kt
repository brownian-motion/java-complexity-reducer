/*
 * Copyright 2003-2014 Dave Griffith, Bas Leijdekkers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.brownian.plugins.intellij.complexity.markers

import com.intellij.psi.*
import com.intellij.util.containers.IntStack

/**
 * Calculates the NPath complexity of any arbitrary Java expression.
 * It uses a stack to manage its state during traversal, so it can break for very, very deep trees.
 *
 * It follows a post-order traversal.
 *
 * @see [CheckStyle NPath Complexity description](https://checkstyle.sourceforge.io/apidocs/com/puppycrawl/tools/checkstyle/checks/metrics/NPathComplexityCheck.html)
 */
class NPathComplexityVisitor : JavaRecursiveElementWalkingVisitor() {
    private var childNodeComplexities = IntStack()

    override fun visitAnonymousClass(aClass: PsiAnonymousClass) {
        // no call to super, to keep this from drilling down
    }

    override fun visitLambdaExpression(expression: PsiLambdaExpression) {
        // no call to super, to keep this from drilling down
    }

    override fun visitClass(aClass: PsiClass) {
        // no call to super, to keep this from drilling down
    }

    override fun visitForStatement(statement: PsiForStatement) {
        visit(statement.initialization)
        visit(statement.condition)
        visit(statement.update)
        visit(statement.body)

        val total = (childNodeComplexities.pop() - 1 // initializer (Treated as expr to match CheckStyle)
                + childNodeComplexities.pop() // condition expr
                + childNodeComplexities.pop() - 1 // update (Treated as expr to match CheckStyle)
                + childNodeComplexities.pop() // body
                + 1) // do/don't enter for loop
        childNodeComplexities.push(total)
    }

    override fun visitForeachStatement(statement: PsiForeachStatement) {
        visit(statement.iteratedValue)
        visit(statement.body)

        val total = (childNodeComplexities.pop() // iterator stmt
                + childNodeComplexities.pop() // body
                + 1) // do/don't enter for loop
        childNodeComplexities.push(total)
    }

    override fun visitIfStatement(statement: PsiIfStatement) {
        visit(statement.condition)
        visit(statement.thenBranch)
        visit(statement.elseBranch)

        val total = (childNodeComplexities.pop() // if branch
                + childNodeComplexities.pop() // else branch, or 1 if absent to represent code path skipping if
                + childNodeComplexities.pop()) // condition
        childNodeComplexities.push(total)
    }

    override fun visitDoWhileStatement(statement: PsiDoWhileStatement) {
        visit(statement.body)
        visit(statement.condition)

        val total = (childNodeComplexities.pop() // body
                + childNodeComplexities.pop() // condition
                + 1)
        childNodeComplexities.push(total)
    }


    override fun visitConditionalExpression(expression: PsiConditionalExpression) {
        visit(expression.condition)
        visit(expression.thenExpression)
        visit(expression.elseExpression)

        val total = (childNodeComplexities.pop() // condition
                + childNodeComplexities.pop() // true branch
                + childNodeComplexities.pop() // false branch
                + 2) // two paths through
        childNodeComplexities.push(total)
    }

    override fun visitSwitchStatement(statement: PsiSwitchStatement) {
        val body = statement.body
        visit(statement.expression)
        var total =
            1 + childNodeComplexities.pop() // 1 for the switch statement itself, plus the complexity of the expression
        if (body == null) {
            childNodeComplexities.push(total)
            return
        }

        val statements = body.statements
        var pendingLabel = false
        var nLabels = 0

        for (child in statements) {
            if (child is PsiSwitchLabelStatement) {
                if (!pendingLabel) {
                    nLabels++
                    childNodeComplexities.push(1)
                    // by default, an empty statement (empty branch) is worth 1.
                    // Pushing 1 also enables us to compute the product for the full branch by finding each statement seperately, popping the running total, and pushing the product as a new running total, without special checks at the end of the loop.
                }
                pendingLabel = true
            } else {
                pendingLabel = false
                visit(child)
                val branchTotal = (childNodeComplexities.pop() // last running total
                        * childNodeComplexities.pop() // last statement we just visited
                        )
                childNodeComplexities.push(branchTotal)
            }
        }
        for (i in 0 until nLabels) {
            total += childNodeComplexities.pop() // count each unique branch
        }
        childNodeComplexities.push(total)
    }

    override fun visitSwitchExpression(statement: PsiSwitchExpression) {
        val body = statement.body
        visit(statement.expression)
        var total =
            1 + childNodeComplexities.pop() // 1 for the switch statement itself, plus the complexity of the expression
        if (body == null) {
            childNodeComplexities.push(total)
            return
        }

        val statements = body.statements
        var pendingLabel = false
        var nLabels = 0

        for (child in statements) {
            if (child is PsiSwitchLabelStatement) {
                if (!pendingLabel) {
                    nLabels++
                    childNodeComplexities.push(1)
                    // by default, an empty statement (empty branch) is worth 1.
                    // Pushing 1 also enables us to compute the product for the full branch
                    // by finding each statement separately, popping the running total,
                    // and pushing the product as a new running total, without special checks at the end of the loop.
                }
                pendingLabel = true
            } else {
                pendingLabel = false
                visit(child)
                val branchTotal = (childNodeComplexities.pop() // last running total
                        * childNodeComplexities.pop() // last statement we just visited
                        )
                childNodeComplexities.push(branchTotal)
            }
        }
        for (i in 0 until nLabels) {
            total += childNodeComplexities.pop() // count each unique branch
        }
        childNodeComplexities.push(total)
    }

    override fun visitWhileStatement(statement: PsiWhileStatement) {
        visit(statement.condition)
        visit(statement.body)
        val total = (childNodeComplexities.pop() // condition
                + childNodeComplexities.pop() // body
                + 1) // do/don't enter while stmt
        childNodeComplexities.push(total)
    }

    override fun visitPolyadicExpression(expression: PsiPolyadicExpression) {
        val token = expression.operationTokenType
        if (token == JavaTokenType.ANDAND || token == JavaTokenType.OROR) {
            var total = expression.operands.size - 1
            expression.operands.forEach { visit(it); total += childNodeComplexities.pop() }
            childNodeComplexities.push(total)
        } else {
            childNodeComplexities.push(0)
        }
    }

    override fun visitBinaryExpression(expression: PsiBinaryExpression?) {
        visitExpressions(expression?.lOperand, expression?.rOperand)
    }

    override fun visitArrayAccessExpression(expression: PsiArrayAccessExpression?) {
        visitExpressions(expression?.indexExpression, expression?.arrayExpression)
    }

    override fun visitAssignmentExpression(expression: PsiAssignmentExpression?) {
        visitExpressions(expression?.lExpression, expression?.rExpression)
    }

    override fun visitPostfixExpression(expression: PsiPostfixExpression?) {
        visit(expression?.operand)
    }

    override fun visitPrefixExpression(expression: PsiPrefixExpression?) {
        visit(expression?.operand)
    }

    override fun visitExpressionList(list: PsiExpressionList?) {
        visitExpressions(*list?.expressions ?: emptyArray())
    }

    override fun visitExpressionListStatement(statement: PsiExpressionListStatement?) {
        visitExpressionList(statement?.expressionList)
    }

    override fun visitCallExpression(callExpression: PsiCallExpression?) {
        visitExpressionList(callExpression?.argumentList)
    }

    override fun visitAssertStatement(statement: PsiAssertStatement?) {
        // an assert is essentially if(condition) { description }, so treat it just like an if condition with no else:
        visitExpressions(statement?.assertCondition, statement?.assertDescription)
        childNodeComplexities.push(
            1 + // do/don't fail assert
                    childNodeComplexities.pop() + // condition
                    childNodeComplexities.pop() // description
        )
    }

    override fun visitReturnStatement(statement: PsiReturnStatement?) {
        visit(statement?.returnValue)
        childNodeComplexities.push(1 + childNodeComplexities.pop())
    }

    override fun visitExpression(expression: PsiExpression?) {
        childNodeComplexities.push(0)
    }

    private fun visitExpressions(vararg expressions: PsiExpression?) {
        var total = 0
        expressions.forEach { visit(it); total += childNodeComplexities.pop() }
        childNodeComplexities.push(total)
    }

    override fun visitBlockStatement(statement: PsiBlockStatement) {
        // NPath complexity of a code block is the product of its statements' complexities
        visitCodeBlock(statement.codeBlock)
    }

    override fun visitMethod(method: PsiMethod) {
        // NPath complexity of a method is the product of its statements' complexities
        visitCodeBlock(method.body)
    }

    override fun visitCodeBlock(codeBlock: PsiCodeBlock?) {
        var total = 1
        codeBlock?.statements?.forEach { visit(it); total *= childNodeComplexities.pop() }
        childNodeComplexities.push(total)
    }

    override fun visitStatement(statement: PsiStatement) {
        childNodeComplexities.push(1)
    }

    override fun visitTryStatement(statement: PsiTryStatement) {
        visitCodeBlock(statement.tryBlock)
        var total = childNodeComplexities.pop() // body

        // When leaving our try block, if we have N catch sections, then we have N+1 code paths out of the try.
        var catchTotal = 1
        statement.catchBlocks.forEach { visitCodeBlock(it); catchTotal += childNodeComplexities.pop() } // add the complexities, because each catch is totally independent
        total *= catchTotal

        if (statement.finallyBlock != null) {
            visitCodeBlock(statement.finallyBlock)
            total *= childNodeComplexities.pop() // EVERY path goes through this sequentially, no matter what, so it multiplies
        }
        childNodeComplexities.push(total)
    }

    // Assumes that it is called after walking the tree, so the stack is size one!!
    val complexity: Int
        get() = childNodeComplexities.peek()

    fun reset() {
        childNodeComplexities = IntStack()
    }


    private fun visit(maybeStatement: PsiStatement?) {
        maybeStatement?.accept(this) ?: childNodeComplexities.push(1)
    }

    private fun visit(maybeExpression: PsiExpression?) {
        maybeExpression?.accept(this) ?: childNodeComplexities.push(0)
    }
}