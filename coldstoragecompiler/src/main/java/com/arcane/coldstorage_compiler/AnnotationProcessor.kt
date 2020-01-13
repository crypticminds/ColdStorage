package com.arcane.coldstorage_compiler

import com.arcane.coldstorageannotation.Refrigerate
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

import java.io.File
import java.util.stream.Collectors
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.ExecutableType
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

/**
 * The annotation processor that generates the cache layer.
 *
 * @author Anurag
 */
@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(FileGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class FileGenerator : AbstractProcessor() {

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

        val methods: MutableList<FunSpec> = arrayListOf()
        annotatedElements?.forEach { element ->
            methods.add(
                generateWrapperMethod(
                    element
                )
            )
        }
        generateClass(methods)
        return true
    }

    /**
     * Method that generated the wrapper method.
     */
    private fun generateWrapperMethod(element: Element): FunSpec {
        val executableType = element.asType() as ExecutableType
        val name = element.simpleName.toString()
        val parameterTypes = executableType.parameterTypes
        val executableElement = element as ExecutableElement
        val enclosingElement = executableElement.enclosingElement

        val refrigerate = element.getAnnotation(Refrigerate::class.java)


        var counter = 0
        val parameterList = arrayListOf<ParameterSpec>()
        val declaringClass = (enclosingElement as TypeElement).qualifiedName.toString()
        val variables = arrayListOf<String>()

        parameterTypes.forEach { parameter ->
            val variableName = executableElement.parameters[counter]
                .simpleName.toString()

            val parameterSpec = ParameterSpec
                .builder(
                    variableName,
                    parameter.asTypeName().javaToKotlinType()
                )
                .build()
            parameterList.add(parameterSpec)
            variables.add(variableName)
            counter += 1
        }

        //adding the class to the parameter.
        if (!element.modifiers.contains(Modifier.STATIC)) {
            val variableType = ClassName(
                getPackageName(declaringClass),
                getClassName(declaringClass)
            )
            val parameterSpec = ParameterSpec.builder(
                GENERATED_OBJECT_PARAMETER_NAME, variableType
            )
                .build()
            parameterList.add(parameterSpec)
        }

        //adding the callback variable
        val callBack = ClassName(
            CALL_BACK_PACKAGE_NAME,
            CALL_BACK_INTERFACE_NAME
        )
        val parametrizedCallback = callBack.parameterizedBy(
            executableElement
                .returnType.asTypeName().javaToKotlinType().copy(nullable = true)
        )
        val callbackSpec = ParameterSpec.builder(
            CALL_BACK_PARAMETER_NAME, parametrizedCallback
        )
            .build()
        parameterList.add(callbackSpec)


        return FunSpec.builder(name)
            .addParameters(parameterList)
            .addCode(
                generateCodeBlock(
                    name,
                    variables,
                    executableElement
                        .returnType.asTypeName().javaToKotlinType(),
                    refrigerate
                    , executableElement.parameters
                )
            )
            .build()
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
     * Method to get the class name from FQFN.
     */
    private fun getClassName(fullyQualifiedName: String): String {
        return fullyQualifiedName.substring(
            fullyQualifiedName.lastIndexOf('.') + 1, fullyQualifiedName.length
        )

    }

    /**
     * Method to get the package name from FQFN.
     */
    private fun getPackageName(fullyQualifiedName: String): String {
        return fullyQualifiedName.substring(
            0,
            fullyQualifiedName.lastIndexOf('.')
        )

    }

    /**
     * Method that generates the code block inside each method.
     * The logic here is that the actual method will be called
     * if the value against the key is not available inside the
     * cache.
     */
    private fun generateCodeBlock(
        name: String,
        variables: ArrayList<String>,
        returnType: Any,
        refrigerate: Refrigerate,
        parameters: MutableList<out VariableElement>
    ): CodeBlock {


        val timeToLive: Long? =
            if (refrigerate.timeToLive < 0) {
                null
            } else {
                refrigerate.timeToLive
            }

        //TODO check if variable name is incorrect
        val keys: MutableList<String> =
            if (refrigerate.keys.isEmpty()) {
                parameters.stream().map { parameter ->
                    parameter.simpleName.toString()
                }.collect(Collectors.toList())
            } else {
                refrigerate.keys.toMutableList()
            }

        var calculateKeyCodeBlock = "val $KEY_VARIABLE = "

        if (keys.isNullOrEmpty()) {
            calculateKeyCodeBlock += " \"${refrigerate.operation}\""
        } else {
            keys.forEach { key ->
                calculateKeyCodeBlock += "Integer.toString($key.hashCode())"
                if (keys.indexOf(key) != keys.size - 1) {
                    calculateKeyCodeBlock += " + "
                }
            }
        }

        val coldStorageCache = ClassName(
            "com.arcane.coldstoragecache.cache",
            "ColdStorage"
        )


        val run = FunSpec.builder("run")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
            .returns(Void.TYPE)
            .addStatement(calculateKeyCodeBlock)
            .beginControlFlow("if (%T.get($KEY_VARIABLE) == null)", coldStorageCache)
            .addStatement(
                "val generatedVariable = $GENERATED_OBJECT_PARAMETER_NAME" +
                        ".$name(${variables.joinToString(separator = ", ")})", returnType
            )
            .addStatement("%T.put($KEY_VARIABLE,generatedVariable,$timeToLive)", coldStorageCache)
            .addStatement(
                "$CALL_BACK_PARAMETER_NAME.onSuccess(generatedVariable," +
                        "\"${refrigerate.operation}\")"
            )
            .nextControlFlow("else")
            .addStatement(
                "$CALL_BACK_PARAMETER_NAME.onSuccess(" +
                        "%T.get($KEY_VARIABLE) as (%T),\"${refrigerate.operation}\")",
                coldStorageCache,
                returnType
            )
            .endControlFlow()
            .addAnnotation(Override::class.java).build()

        val runnable = TypeSpec.anonymousClassBuilder()
            .addSuperinterface(Runnable::class.java)
            .addFunction(run).build()


        return CodeBlock.builder()
            .addStatement(
                "%T( %L )", ClassName(
                    "android.os.AsyncTask",
                    "execute"
                ), runnable
            )
            .build()


    }


    /**
     * Converting java types to kotlin types.
     */
    private fun TypeName.javaToKotlinType(): TypeName = if (this is ParameterizedTypeName) {
        (rawType.javaToKotlinType() as ClassName).parameterizedBy(
            *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
        )
    } else {
        val className = JavaToKotlinClassMap.INSTANCE
            .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
        if (className == null) this
        else ClassName.bestGuess(className)
    }


    /**
     * The static members of this class.
     */
    companion object {

        const val GENERATED_CLASS_NAME = "GeneratedCacheLayer"

        const val GENERATED_PACKAGE_NAME = "com.arcane.generated"

        const val GENERATED_OBJECT_PARAMETER_NAME = "obj"

        const val CALL_BACK_PARAMETER_NAME = "callback"

        const val CALL_BACK_PACKAGE_NAME = "com.arcane.coldstoragecache.callback"

        const val CALL_BACK_INTERFACE_NAME = "OnOperationSuccessfulCallback"

        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        const val KEY_VARIABLE = "thisvariablenameisspecial"
    }

}