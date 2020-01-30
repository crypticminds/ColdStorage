package com.arcane.coldstorage_compiler

import com.arcane.coldstorage_compiler.helper.CodeGenerationHelper
import com.arcane.coldstorageannotation.Freeze
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FreezeAnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FreezeAnnotationProcessor : AbstractProcessor() {

    private lateinit var processingEnvironment: ProcessingEnvironment

    private lateinit var codeGenerationHelper: CodeGenerationHelper

    override fun init(pe: ProcessingEnvironment?) {
        super.init(pe)
        processingEnvironment = pe!!
        codeGenerationHelper = CodeGenerationHelper()
    }

    /**
     * Set the supported annotation types.
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Freeze::class.java.name)
    }

    /**
     * Sets the source version.
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }


    /**
     * The main logic for processing is inside this method.
     * The method generates a kotlin class for each classes
     * annotated with "@Freeze"
     *
     */
    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment?
    ): Boolean {

        val annotatedElements = roundEnvironment?.getElementsAnnotatedWith(
            Freeze::class.java
        )

        annotatedElements?.forEach { element ->
            if (element.kind != ElementKind.CLASS) {
                processingEnvironment.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Annotation should only be used on classes"
                )
                return false
            }
            val methods = element.enclosedElements
            val wrappedMethods = arrayListOf<FunSpec>()

            methods.forEach {
                val wrappedMethod = codeGenerationHelper.generateWrapperMethod(
                    it,
                    element.getAnnotation(Freeze::class.java)
                )
                if (!it.simpleName.contains("init") && !it.modifiers.contains(
                        javax.lang.model.element.Modifier.PRIVATE
                    )
                ) {
                    wrappedMethods.add(wrappedMethod)
                }
            }

            generateClass(wrappedMethods, element)

        }
        return true
    }

    /**
     * Method that generates the class.
     */
    private fun generateClass(
        methods: MutableList<FunSpec>,
        element: Element
    ) {
        val kaptKotlinGeneratedDir = processingEnv.options[
                KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "Cache${element.simpleName}.kt")


        val property =
            PropertySpec.builder(
                "obj",
                element.asType().asTypeName()
            )
                .mutable()
                .addModifiers(KModifier.PRIVATE)
                .initializer("${element.simpleName}()")
                .build()


        val kotlinClass = TypeSpec.classBuilder("Cache${element.simpleName}")
            .addProperty(property)
            .addFunctions(methods)
            .build()

        val fileName =
            if (element.getAnnotation(Freeze::class.java).generatedClassName.isBlank())
                "Generated${element.simpleName} " else
                element.getAnnotation(Freeze::class.java).generatedClassName

        val kotlinFile = FileSpec.builder(
            GENERATED_PACKAGE_NAME,
            fileName
        )
            .addType(kotlinClass)
            .build()


        kotlinFile.writeTo(file)
    }


    companion object {

        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        const val GENERATED_PACKAGE_NAME = "com.arcane.generated"

    }
}