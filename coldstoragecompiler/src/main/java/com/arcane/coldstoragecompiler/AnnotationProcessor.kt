package com.arcane.coldstoragecompiler

import com.arcane.coldstoragecompiler.helper.CodeGenerationHelper
import com.arcane.coldstorageannotation.Refrigerate
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.tools.Diagnostic

/**
 * The annotation processor that generates the cache layer.
 *
 * @author Anurag
 */
@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(AnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AnnotationProcessor : AbstractProcessor() {

    private lateinit var processingEnvironment: ProcessingEnvironment

    private lateinit var codeGenerationHelper: CodeGenerationHelper

    private val methods: MutableList<FunSpec> = arrayListOf()

    override fun init(pe: ProcessingEnvironment?) {
        super.init(pe)
        processingEnvironment = pe!!
        codeGenerationHelper = CodeGenerationHelper()
    }

    /**
     * Set the supported annotation types.
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Refrigerate::class.java.name)
    }

    /**
     * Sets the source version.
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }


    /**
     * The main logic for processing is inside this method.
     * The method generates a kotlin class that wraps the methods
     * annotated with @Refrigerate and adds caching logic over
     * the actual method logic.
     *
     * The generated file name is GeneratedCacheLayer.kt
     */
    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment?
    ): Boolean {

        val annotatedElements = roundEnvironment?.getElementsAnnotatedWith(
            Refrigerate::class.java
        )

        //TODO check if all the annotated elements are methods and each have a return type
        annotatedElements?.forEach { element ->
            if (element is ExecutableElement) {
                when {
                    element.modifiers.contains(Modifier.PRIVATE) -> {
                        processingEnvironment.messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Refrigerate can not be used on private method : ${element.simpleName}"
                        )

                    }
                    element.returnType.kind == TypeKind.VOID -> {
                        processingEnvironment.messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Refrigerate can not be used on methods that does not return anything. " +
                                    "There is nothing to put in cache : ${element.simpleName}"
                        )
                    }
                    else -> {
                        methods.add(
                            codeGenerationHelper.generateWrapperMethod(
                                element, element.getAnnotation(Refrigerate::class.java)
                            )
                        )
                    }
                }
            } else {
                processingEnvironment.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Refrigerate can only be used on functions"
                )

            }


        }
        generateClass(methods)
        return true
    }


    /**
     * Method that generates the class.
     */
    private fun generateClass(
        methods: MutableList<FunSpec>
    ) {
        val kaptKotlinGeneratedDir = processingEnv.options[
                KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "$GENERATED_CLASS_NAME.kt")

        //companion object containing all the methods.
        val companion = TypeSpec.companionObjectBuilder()
            .addFunctions(methods)
            .build()


        val kotlinClass = TypeSpec.classBuilder(GENERATED_CLASS_NAME)
            .addType(companion)
            .build()

        val kotlinFile = FileSpec.builder(GENERATED_PACKAGE_NAME, GENERATED_CLASS_NAME)
            .addType(kotlinClass)
            .build()

        kotlinFile.writeTo(file)
    }


    /**
     * The static members of this class.
     */
    companion object {

        const val GENERATED_CLASS_NAME = "GeneratedCacheLayer"

        const val GENERATED_PACKAGE_NAME = "com.arcane.generated"

        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

    }

}