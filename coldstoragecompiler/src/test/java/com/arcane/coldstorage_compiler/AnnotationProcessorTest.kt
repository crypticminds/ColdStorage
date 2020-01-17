package com.arcane.coldstorage_compiler

import com.google.common.base.Joiner
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Text.NEW_LINE
import org.junit.Test

class AnnotationProcessorTest {

    @Test
    fun testSuccessfulCompilation() {
        val input = JavaFileObjects.forSourceString(
            "com.example.A",
            Joiner.on(NEW_LINE).join(
                "package com.example;",
                "",
                "import com.arcane.coldstorageannotation.Refrigerate;",
                "",
                "public class A {",
                "",
                "   @Refrigerate(operation = \"testoperatiom\")",
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

    @Test
    fun testCompilationFailureDueToPrivateMethod() {
        val input = JavaFileObjects.forSourceString(
            "com.example.A",
            Joiner.on(NEW_LINE).join(
                "package com.example;",
                "",
                "import com.arcane.coldstorageannotation.Refrigerate;",
                "",
                "public class A {",
                "",
                "   @Refrigerate(operation = \"testoperatiom\")",
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

    @Test
    fun testCompilationFailureDueToNoReturn() {
        val input = JavaFileObjects.forSourceString(
            "com.example.A",
            Joiner.on(NEW_LINE).join(
                "package com.example;",
                "",
                "import com.arcane.coldstorageannotation.Refrigerate;",
                "",
                "public class A {",
                "",
                "   @Refrigerate(operation = \"testoperatiom\")",
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

    @Test
    fun testCompilationFailureDueToImproperAnnotation() {
        val input = JavaFileObjects.forSourceString(
            "com.example.A",
            Joiner.on(NEW_LINE).join(
                "package com.example;",
                "",
                "import com.arcane.coldstorageannotation.Refrigerate;",
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
