package com.arcane.coldstoragecompiler

import com.google.common.base.Joiner
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text.NEW_LINE
import org.junit.Test

/**
 * Test for the annotation processor.
 */
class AnnotationProcessorTest {


    private val packageName = "com.example.A"

    private val packageName2 = "package com.example;"

    private val importStatement = "import com.arcane.coldstorageannotation.Refrigerate"

    /**
     * Test compilation successful.
     */
    @Test
    fun testSuccessfulCompilation() {
        val input = JavaFileObjects.forSourceString(
            packageName,
            Joiner.on(NEW_LINE).join(
                packageName2,
                "",
                "$importStatement;",
                "",
                "public class A {",
                "",
                "   @Refrigerate(operation = \"testoperation\")",
                "   public String b(int num) {",
                "      return \"something\";  ",
                "   }",
                "}"
            )
        )


        val compilation =
            javac()
                .withProcessors(AnnotationProcessor())
                .compile(input)


        assertThat(compilation).succeeded()

    }

    /**
     * Test compilation failure.
     */
    @Test
    fun testCompilationFailureDueToPrivateMethod() {
        val input = JavaFileObjects.forSourceString(
            packageName,
            Joiner.on(NEW_LINE).join(
                packageName2,
                "",
                "$importStatement;",
                "",
                "public class A {",
                "",
                "   @Refrigerate(operation = \"testoperation1\")",
                "   private String b(int num) {",
                "      return \"something\";  ",
                "   }",
                "}"
            )
        )


        val compilation =
            javac()
                .withProcessors(AnnotationProcessor())
                .compile(input)

        assertThat(compilation).failed()

    }

    /**
     * Test compilation failure due to not return from the annotated method.
     */
    @Test
    fun testCompilationFailureDueToNoReturn() {
        val input = JavaFileObjects.forSourceString(
            packageName,
            Joiner.on(NEW_LINE).join(
                packageName2,
                "",
                "$importStatement;",
                "",
                "public class A {",
                "",
                "   @Refrigerate(operation = \"testoperation2\")",
                "   public void b(int num) {",
                "   }",
                "}"
            )
        )


        val compilation =
            javac()
                .withProcessors(AnnotationProcessor())
                .compile(input)

        assertThat(compilation).failed()

    }

    /**
     * Test annotation failure due to improper placement of annotation.
     */
    @Test
    fun testCompilationFailureDueToImproperAnnotation() {
        val input = JavaFileObjects.forSourceString(
            packageName,
            Joiner.on(NEW_LINE).join(
                packageName2,
                "",
                "$importStatement;",
                "",
                "public class A {",
                "",
                "   @Refrigerate",
                "   public String b(int num) {",
                "   return \"something\";   ",
                "   }",
                "}"
            )
        )


        val compilation =
            javac()
                .withProcessors(AnnotationProcessor())
                .compile(input)

        assertThat(compilation).failed()

    }
}
