package com.arcane.annotationprocessor

import com.arcane.coldstorageannotations.annotation.Refrigerate
import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import java.io.File
import java.util.*
import java.util.stream.Collectors
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic


@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedAnnotationTypes("com.arcane.coldstorageannotations.annotation.Refrigerate")
class RefrigeratorProcessor : AbstractProcessor() {

    companion object {

        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        const val GENERATED_PACKAGE_NAME = "com.arcane.generated"

        const val GENERATED_FILE_NAME = "GeneratedCacheLayer"

        const val GENERATED_MAP_NAME = "generatedMap"

        const val GENERATED_OBJECT_PARAMETER_NAME = "obj"

        const val CALL_BACK_PARAMETER_NAME = "callback"

        const val CALL_BACK_PACKAGE_NAME = "com.arcane.coldstoragecache.callback"

        const val CALL_BACK_INTERFACE_NAME = "OnOperationSuccessfulCallback"
    }

    private lateinit var processingEnvironment: ProcessingEnvironment

    private val methods: MutableList<MethodSpec> = arrayListOf()

    @Synchronized
    override fun init(processingEnv: ProcessingEnvironment?) {
        processingEnvironment = processingEnv!!

        //processingEnv!!.messager.printMessage(Diagnostic.Kind.ERROR, "THROW ERROR")

    }

    //TODO check preconditions.
    override fun process(annotations: MutableSet<out TypeElement>?,
                         roundEnvironment: RoundEnvironment?): Boolean {
        if (roundEnvironment == null) {
            return false
        }

        val cacheClass = ClassName.get(
                "com.arcane.coldstoragecache.cache", "ColdStorage")
        val asyncTaskClass = ClassName.get("android.os", "AsyncTask")
        val generatedSourcesRoot: String = processingEnvironment
                .options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (generatedSourcesRoot.isEmpty()) {
            processingEnvironment.messager.printMessage(Diagnostic.Kind.ERROR,
                    "Can't find the target directory for generated Kotlin files.$generatedSourcesRoot"
            )
            return false
        }
        //get all elements annotated with refrigerate.
        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(Refrigerate::class.java)

        //get all the methods
        annotatedElements.forEach { element ->
            methods.add(generateWrapperMethod(
                    element))
        }


        val file = File(generatedSourcesRoot).apply { mkdir() }

        val classSpec = TypeSpec.classBuilder(GENERATED_FILE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethods(methods)
                .build()

        val javaFile = JavaFile.builder(GENERATED_PACKAGE_NAME, classSpec)
                .addStaticImport(cacheClass, "*")
                .addStaticImport(asyncTaskClass, "execute")
                .build()

        javaFile.writeTo(file)

        return true
    }


    private fun generateWrapperMethod(element: Element): MethodSpec {
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
                    .builder(getType(parameter),
                            variableName)
                    .addModifiers(Modifier.FINAL)
                    .build()
            parameterList.add(parameterSpec)
            variables.add(variableName)
            counter += 1
        }

        //adding the class to the parameter.
        if (!element.modifiers.contains(Modifier.STATIC)) {
            val variableType = ClassName.get(getPackageName(declaringClass),
                    getClassName(declaringClass))
            val parameterSpec = ParameterSpec.builder(variableType,
                    GENERATED_OBJECT_PARAMETER_NAME)
                    .addModifiers(Modifier.FINAL)
                    .build()
            parameterList.add(parameterSpec)
        }

        //adding the callback variable
        val callBack = ClassName.get(CALL_BACK_PACKAGE_NAME,
                CALL_BACK_INTERFACE_NAME)
        val parametrizedCallback = ParameterizedTypeName
                .get(callBack, TypeName.get(executableType.returnType))
        val callbackSpec = ParameterSpec.builder(parametrizedCallback,
                CALL_BACK_PARAMETER_NAME)
                .addModifiers(Modifier.FINAL)
                .build()
        parameterList.add(callbackSpec)





        return MethodSpec.methodBuilder(name)
                .addParameters(parameterList)
                .addCode(generateCodeBlock(name, variables,
                        TypeName.get(executableType.returnType), refrigerate
                        , executableElement.parameters))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build()
    }

    private fun generateCodeBlock(
            name: String,
            variables: ArrayList<String>,
            returnType: Any,
            refrigerate: Refrigerate,
            parameters: MutableList<out VariableElement>): CodeBlock? {


        val timeToLive: Long? =
                if (refrigerate.timeToLive < 0) {
                    null
                } else {
                    refrigerate.timeToLive
                }

        //TODO check if variable name is incorrect
        val keys: MutableList<String>? =
                if (refrigerate.keys.isEmpty()) {
                    parameters.stream().map { parameter ->
                        parameter.simpleName.toString()
                    }.collect(Collectors.toList())
                } else {
                    refrigerate.keys.toMutableList()
                }

        var calculateKeyCodeBlock: String = "String key = "

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

        val run = MethodSpec.methodBuilder("run")
                .addModifiers(Modifier.PUBLIC)
                .returns(Void.TYPE)
                .addStatement(calculateKeyCodeBlock)
                .beginControlFlow("if (Companion.get(key) == null)")
                .addStatement("\$T generatedVariable = $GENERATED_OBJECT_PARAMETER_NAME" +
                        ".$name(${variables.joinToString(separator = ", ")})", returnType)
                .addStatement("Companion.put(key,generatedVariable,$timeToLive)")
                .addStatement("$CALL_BACK_PARAMETER_NAME.onSuccess(generatedVariable," +
                        "\"${refrigerate.operation}\")")
                .nextControlFlow("else")
                .addStatement("$CALL_BACK_PARAMETER_NAME.onSuccess(" +
                        "(\$T) Companion.get(key),\"${refrigerate.operation}\")", returnType)
                .endControlFlow()
                .addAnnotation(Override::class.java).build()

        val runnable = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(Runnable::class.java)
                .addMethod(run).build()


        return CodeBlock.builder()
                .addStatement("execute( \$L )", runnable)
                .build()


    }


    /**
     * Method to get the class name from FQFN.
     */
    private fun getClassName(fullyQualifiedName: String): String {
        return fullyQualifiedName.substring(
                fullyQualifiedName.lastIndexOf('.') + 1, fullyQualifiedName.length)

    }

    /**
     * Method to get the package name from FQFN.
     */
    private fun getPackageName(fullyQualifiedName: String): String {
        return fullyQualifiedName.substring(0,
                fullyQualifiedName.lastIndexOf('.'))

    }

    /**
     * Method to get the JavaPoet typename from the TypeMirror object.
     */
    private fun getType(typeMirror: TypeMirror?): TypeName? {
        when {
            typeMirror == null -> {
                return TypeName.VOID
            }
            typeMirror.kind == TypeKind.INT -> {
                return TypeName.INT
            }
            typeMirror.kind == TypeKind.BOOLEAN -> {
                return TypeName.BOOLEAN
            }
            typeMirror.kind == TypeKind.DOUBLE -> {
                return TypeName.DOUBLE
            }
            typeMirror.kind == TypeKind.FLOAT -> {
                return TypeName.FLOAT
            }
            typeMirror.kind == TypeKind.SHORT -> {
                return TypeName.SHORT
            }
            typeMirror.kind == TypeKind.LONG -> {
                return TypeName.LONG
            }
            typeMirror.kind == TypeKind.BYTE -> {
                return TypeName.BYTE
            }
            else -> {
                val variableClass = Class.forName(typeMirror.toString())
                return ClassName.get(variableClass)
            }
        }

    }

}
