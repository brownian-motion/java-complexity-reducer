import com.brownian.plugins.intellij.complexity.markers.NPathComplexityVisitor
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase

internal class NPathComplexityVisitorTest : JavaCodeInsightFixtureTestCase() {

    fun test_simpleForLoopIsCorrect() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple for loop.
                 * Expected complexity is 3.
                 **/
                public void forLoopTest() {
                    // complexity is (init + cond + update + body + 1), = (0 + 0 + 0 + 2 + 1) = 3
                    for(int i = 0 ; i < 10 ; i++){
                        // provides a complexity of 2
                        if(i % 2 == 0){
                            System.out.println("even");
                        } else {
                            System.out.println("odd");
                        }
                    }
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("forLoopTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(3, visitor.complexity)
    }

    fun test_forLoopAndThenIfStatement_multiplyComplexity() {
        //language=JAVA
        myFixture.addClass(
            """
            
            public class NPathComplexityTest {
                /**
                 * Just a simple for loop.
                 * Expected complexity is 3 * 2 = 6.
                 **/
                public void forLoopTest() {
                    // complexity is (init + cond + update + body + 1), = (0 + 0 + 0 + 2 + 1) = 3
                    for(int i = 0 ; i < 10 ; i++){
                        // provides a complexity of 2
                        if(i % 2 == 0){
                            System.out.println("even");
                        } else {
                            System.out.println("odd");
                        }
                    }
                    // expected complexity is 2
                    if("true" == true){
                        System.out.println("Unexpected");
                    } else {
                        System.out.println("Expected");
                    }
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("forLoopTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(6, visitor.complexity)
    }


    fun test_simpleThreeBranchSwitchStatement_isSumOfBranches() {
        //language=JAVA
        myFixture.addClass(
            """
            
            public class NPathComplexityTest {
                /**
                 * Just a simple switch statement.
                 * Expected complexity is (0 + 3 + 2 + 1 + 1) = 7
                 **/
                public void simpleSwitchTest() {
                    // complexity is (cond + case 0 + case 1 + case 2 + default)
                    switch((int)(Math.random() * 3)) {
                        case 0:
                            if(Math.random() > 0.5){
                                // dummy
                            } else if(Math.random() > 0.5){
                                // dummy
                            } else {
                                // dummy
                            }
                        case 1:
                            if(Math.random() > 0.5) {
                                // doesn't matter
                            } else {
                                // also irrelevant
                            }
                            break;
                        case 2:
                        default:
                            // also doesn't matter
                    }
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("simpleSwitchTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(7, visitor.complexity)
    }
    // TODO: make (and enforce) test that handles fallthroughs correctly


    fun test_simpleTryCatchIsTryTimes1PlusSumOfCatch() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple try/catch/catch.
                 * Expected complexity is (3) * (1 + 2 + 1) = 12
                 **/
                public void tryCatchCatchTest() {
                    try {
                        // 3
                        if(true){
                            
                        } else if(true){
                            
                        }
                    } catch(RuntimeException e){
                        // 2
                        if(false) {
                            
                        }
                    } catch(Exception e){
                        // 1
                    }
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("tryCatchCatchTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(12, visitor.complexity)
    }

    fun test_simplestReturnExpressionStatement_isComplexity1() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple return.
                 * Expected complexity is 1.
                 **/
                public int returnTest() {
                   return 1;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("returnTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(1, visitor.complexity)
    }

    fun test_returnAndAndExpression_isComplexity2() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple return of a boolean expression.
                 * Expected complexity is 2.
                 **/
                public int returnTest() {
                   return left && right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("returnTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(2, visitor.complexity)
    }

    fun test_returnOrOrExpression_isComplexity2() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple return of a boolean expression.
                 * Expected complexity is 2.
                 **/
                public int returnTest() {
                   return left || right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("returnTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(2, visitor.complexity)
    }

    fun test_returnDoubleAndAndExpression_isComplexity3() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple return of a boolean expression.
                 * Expected complexity is 3.
                 **/
                public int returnTest() {
                   return left && middle && right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("returnTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(3, visitor.complexity)
    }

    fun test_returnDoubleOrOrExpression_isComplexity3() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple return of a boolean expression.
                 * Expected complexity is 3.
                 **/
                public int returnTest() {
                   return left || middle || right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("returnTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(3, visitor.complexity)
    }

    fun test_returnMixedAndAndOrOrExpression_isComplexity3() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple return of a boolean expression.
                 * Expected complexity is 3.
                 **/
                public int returnTest() {
                   return left || middle && right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("returnTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(3, visitor.complexity)
    }

    fun test_simplestReturnVoidStatement_isComplexity1() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple return.
                 * Expected complexity is 1.
                 **/
                public void returnTest() {
                   return;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("returnTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(1, visitor.complexity)
    }

    fun test_ternary_simplestTernary() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple ternary.
                 * Expected complexity is 2
                 **/
                public boolean simpleTernaryTest() {
                    // 1 + (complexity of expression)
                    return cond ? left : right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("simpleTernaryTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(2, visitor.complexity)
    }

    fun test_ternary_twoConditions_doublesComplexity_simplePaths() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple ternary.
                 * Expected complexity is 3
                 **/
                public boolean simpleTernaryTest() {
                    // 1 + (complexity of expression)
                    return (cond1 || cond2) ? left : right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("simpleTernaryTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(4, visitor.complexity)
    }

    fun test_ternary_twoPathsInLeftBranch() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple ternary.
                 * Expected complexity is 3
                 **/
                public boolean simpleTernaryTest() {
                    // 1 + (complexity of expression)
                    return cond ? (left1 && left2) : right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("simpleTernaryTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(3, visitor.complexity)
    }

    fun test_ternary_threePathsInRightBranch() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple ternary.
                 * Expected complexity is 4
                 **/
                public boolean simpleTernaryTest() {
                    // 1 + (complexity of expression)
                    return cond ? left : (right1 || right2 || right3);
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("simpleTernaryTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(4, visitor.complexity)
    }

    fun test_ternary_twoPathsInBothBranches() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple ternary.
                 * Expected complexity is (1) * (2 + 2) = 4
                 **/
                public boolean simpleTernaryTest() {
                    return cond ? (left1 || left2) : (right1 && right2);
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("simpleTernaryTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(4, visitor.complexity)
    }

    fun test_ternary_twoPathsInLeft_andTwoInCondition() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /**
                 * Just a simple ternary.
                 * Expected complexity is (2) * (2 + 1) = 6
                 **/
                public boolean simpleTernaryTest() {
                    return (cond1 && cond2) ? (left1 || left2) : right;
                }
            }
        """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("simpleTernaryTest", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(6, visitor.complexity)
    }

    fun test_treatsLambdasAsOpaqueValues_whichDoNotContributeComplexity() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /*
                 * Loads the data
                 */
                public void getData(Bar[] bs)
                {
                    List<String> as = new LinkedList<>();
                    for (Bar record : bs)
                    {
                        as.add(record.field("k"));
                    }
                    String[] ks = as.toArray(new String[0]);
                    final Record bam = new Record();
            
                    //Get some data
                    bam.setField("original ks", ks);
                    bam.setField("floop", this.quux.floop().flip());
                    bam.setField("flop", this.quux.flop().flip());
                    //Use some data
                    this.quux.getSource().foo("bar", bam, (baz, data, qux) ->
                    {
                        //Do stuff with data
                        Record[] cs = baz.getData();
                        List<Bar> ds = new LinkedList<>();
                        for (Record r : cs)
                        {
                            Bar quum = new Bar();
                            for (String k : r.fields())
                            {
                                quum.setField(k, r.field(k));
                            }
                            ds.add(quum);
                        }
                        clamBambler = ds.toArray(new Bar[0]);
                        if (clamBambler != null && clamBambler.length > 1)
                        {
                            MyObject myObj = getMyObject();
                            myObj.setData(clamBambler);
                            this.add(myObj);
            
                            setClamBambler(clamBambler);
            
                            ObjectLabel e = new ObjectLabel("Label", this.quux.getSource().getFieldNames());
                            this.quux.setLabel(e);
                            this.add(this.quux);
                            this.add(this.fu);
            
                            this.myComponent.setData();
                            this.show();
                        }
                        else
                        {
                            Warner.warn("warning");
                        }
                    });
                }
            }
            """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")
        val testMethods = testClass.findMethodsByName("getData", false)
        assertEquals(1, testMethods.size)
        val testMethod = testMethods.first()

        val visitor = NPathComplexityVisitor()
        testMethod.accept(visitor)
        assertEquals(2, visitor.complexity)
    }

    fun test_complexityOfClassIsNotDefined() {
        //language=JAVA
        myFixture.addClass(
            """
            public class NPathComplexityTest {
                /*
                 * Loads the data
                 */
                public void getData(Bar[] bs)
                {
                    List<String> as = new LinkedList<>();
                    for (Bar record : bs)
                    {
                        as.add(record.field("k"));
                    }
                    String[] ks = as.toArray(new String[0]);
                    final Record bam = new Record();
            
                    //Get some data
                    bam.setField("original ks", ks);
                    bam.setField("floop", this.quux.floop().flip());
                    bam.setField("flop", this.quux.flop().flip());
                    //Use some data
                    this.quux.getSource().foo("bar", bam, (baz, data, qux) ->
                    {
                        //Do stuff with data
                        Record[] cs = baz.getData();
                        List<Bar> ds = new LinkedList<>();
                        for (Record r : cs)
                        {
                            Bar quum = new Bar();
                            for (String k : r.fields())
                            {
                                quum.setField(k, r.field(k));
                            }
                            ds.add(quum);
                        }
                        clamBambler = ds.toArray(new Bar[0]);
                        if (clamBambler != null && clamBambler.length > 1)
                        {
                            MyObject myObj = getMyObject();
                            myObj.setData(clamBambler);
                            this.add(myObj);
            
                            setClamBambler(clamBambler);
            
                            ObjectLabel e = new ObjectLabel("Label", this.quux.getSource().getFieldNames());
                            this.quux.setLabel(e);
                            this.add(this.quux);
                            this.add(this.fu);
            
                            this.myComponent.setData();
                            this.show();
                        }
                        else
                        {
                            Warner.warn("warning");
                        }
                    });
                }
            }
            """.trimIndent()
        )

        val testClass = myFixture.findClass("NPathComplexityTest")

        val visitor = NPathComplexityVisitor()
        testClass.accept(visitor)
        assertNull(visitor.complexity)
    }
}