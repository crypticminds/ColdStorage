package com.arcane.coldstoragecompiler

import com.arcane.coldstorageannotation.LoadImage
import com.arcane.coldstoragecompiler.helper.CodeGenerationHelper
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class) // For registering the service
@SupportedSourceVersion(SourceVersion.RELEASE_8) // to support Java 8
@SupportedOptions(CodeGenerationHelper.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class LoadImageProcessor : AbstractProcessor() {

    companion object {

        private const val GENERATED_BIND_CLASSNAME = "BindingClass"
    }


    private var classToFieldsMap: MutableMap<Element, MutableList<Element>> = mutableMapOf()


    private lateinit var processingEnvironment: ProcessingEnvironment


    override fun init(pe: ProcessingEnvironment?) {
        super.init(pe)
        processingEnvironment = pe!!
    }

    /**
     * Set the supported annotation types.
     */
    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(LoadImage::class.java.name)
    }

    /**
     * Sets the source version.
     */
    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(
        set: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment?
    ): Boolean {

        val annotatedElements = roundEnvironment?.getElementsAnnotatedWith(
            LoadImage::class.java
        )

        annotatedElements!!.forEach { element ->
            if (element.modifiers.contains(Modifier.PRIVATE)) {
                processingEnvironment.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Annotation can not be used on private fields"
                )
            } else {
                if (classToFieldsMap.containsKey(element.enclosingElement)) {
                    classToFieldsMap.getValue(element.enclosingElement).add(element)
                } else {
                    classToFieldsMap[element.enclosingElement] = mutableListOf(element)
                }
            }
        }

        generateClass()

        //get the class of each element and create generated classes for each


        //get all elements annotated with
        return false
    }


    private fun generateClass() {

        val kaptKotlinGeneratedDir = processingEnv.options[
                FreezeAnnotationProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME]

        val methods: MutableList<FunSpec> = arrayListOf()

        classToFieldsMap.forEach { entry ->
            val className = "target"

            methods.add(
                FunSpec.builder("bind${entry.key.simpleName}")
                    .addParameter(
                        ParameterSpec
                            .builder(
                                className,
                                entry.key.asType().asTypeName()
                            ).build()
                    )
                    .addCode(generateCodeBlockForActivity(className, entry.value))
                    .build()
            )
        }

        val file = File(kaptKotlinGeneratedDir, "$GENERATED_BIND_CLASSNAME.kt")


        val kotlinClass = TypeSpec.classBuilder(GENERATED_BIND_CLASSNAME)
            .addFunctions(methods)
            .build()

        val kotlinFile = FileSpec.builder(
            FreezeAnnotationProcessor.GENERATED_PACKAGE_NAME,
            GENERATED_BIND_CLASSNAME
        )
            .addType(kotlinClass)
            .build()


        kotlinFile.writeTo(file)


    }


    //TODO the url can also point to a gif
    @Suppress("SameParameterValue")
    private fun generateCodeBlockForActivity(
        target: String, parameterList: List<Element>
    ): CodeBlock {

        val cacheClass = ClassName("com.arcane.coldstoragecache.cache", "Cache")

        val loadImageConfig = ClassName(
            "com.arcane.coldstoragecache.model",
            "LoadImageConfig"
        )

        val imageView = ClassName(
            "android.widget",
            "ImageView"
        )

        val bindHelper = ClassName(
            "com.arcane.coldstoragecache.helper",
            "BindHelper"
        )

        val imageHelper = ClassName(
            "com.arcane.coldstoragecache.helper",
            "ImageHelper"
        )

        val coldStorage = ClassName("com.arcane.coldstoragecache.cache", "ColdStorage")

        val bitmap = ClassName("android.graphics", "Bitmap")


        val builder = CodeBlock.builder()
        builder.addStatement("val map = hashMapOf<%T , %T>()", imageView, loadImageConfig)
            .addStatement(
                "%T.d(\"COLD_STORAGE\",\"Binding view to cache\")", ClassName(
                    "android.util",
                    "Log"
                )
            )

        //  .beginControlFlow("$activityName.runOnUiThread  ")
        parameterList.forEach { parameter ->
            val loadImage = parameter.getAnnotation(LoadImage::class.java)
            builder
                .addStatement(
                    "$target.${parameter.simpleName} = " +
                            "%T.bindViewToResource($target ," +
                            " ${loadImage.imageViewResourceId})", bindHelper
                )
                .addStatement(
                    "map.put($target.${parameter.simpleName}," +
                            " LoadImageConfig(\"${loadImage.url}\"," +
                            "${loadImage.placeHolder}, " +
                            "${loadImage.enableLoadingAnimation}," +
                            "${loadImage.imageViewResourceId}))"
                )
        }

        //builder.endControlFlow()

        return builder.addStatement("map.forEach { entry ->")
            .beginControlFlow("%T.executorService.execute", cacheClass)
            .addStatement("")
            .beginControlFlow("if(%T.get(entry.value.url) != null)", coldStorage)
            .addStatement(
                "%T.setImageInView(entry.key,%T.get(entry.value.url) as %T)"
                , imageHelper, coldStorage, bitmap
            )
            .nextControlFlow("else")
            .addStatement("val animator = %T.animateImageView(entry.key)", bindHelper)
            .beginControlFlow("if (entry.value.placeHolder != -1)")
            .beginControlFlow("if(entry.value.enableLoadingAnimation)")
            .addStatement("%T.startAnimation(entry.key,animator)", imageHelper)
            .addStatement("entry.key.setImageResource(entry.value.placeHolder)")
            .endControlFlow()
            .endControlFlow()
            .addStatement(
                "val bitmap =  %T.downloadImage(entry.value.url)  ",
                imageHelper
            )
            .beginControlFlow("if(bitmap != null)")
            .addStatement("%T.put(entry.value.url,bitmap,null)", coldStorage)
            .addStatement("%T.setImageInView(entry.key,bitmap,animator)", imageHelper)
            .endControlFlow()
            .endControlFlow()
            .endControlFlow()
            .addStatement("}")
            .build()

    }


}