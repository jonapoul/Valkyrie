package io.github.composegears.valkyrie.generator.jvm.imagevector.spec

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.buildCodeBlock
import io.github.composegears.valkyrie.generator.jvm.ext.fileSpecBuilder
import io.github.composegears.valkyrie.generator.jvm.ext.propertySpecBuilder
import io.github.composegears.valkyrie.generator.jvm.ext.removeExplicitModeCode
import io.github.composegears.valkyrie.generator.jvm.ext.setIndent
import io.github.composegears.valkyrie.generator.jvm.imagevector.ImageVectorSpecConfig
import io.github.composegears.valkyrie.generator.jvm.imagevector.ImageVectorSpecOutput
import io.github.composegears.valkyrie.generator.jvm.imagevector.util.ClassNames
import io.github.composegears.valkyrie.ir.IrImageVector

internal class LazyPropertySpec(private val config: ImageVectorSpecConfig) {

    fun createAsLazyProperty(irVector: IrImageVector): ImageVectorSpecOutput {
        val iconPackClassName = config.resolveIconPackClassName()
        val packageName = config.resolvePackageName()

        val fileSpec = fileSpecBuilder(
            packageName = packageName,
            fileName = config.iconName,
        ) {
            addProperty(
                propertySpec = iconProperty(
                    irVector = irVector,
                    iconPackClassName = iconPackClassName,
                ),
            )
            addPreview(
                config = config,
                iconPackClassName = iconPackClassName,
                packageName = packageName,
            )
            setIndent(config.indentSize)
        }

        return ImageVectorSpecOutput(
            content = when {
                config.useExplicitMode -> fileSpec.toString()
                else -> fileSpec.removeExplicitModeCode()
            },
            name = fileSpec.name,
        )
    }

    private fun iconProperty(
        irVector: IrImageVector,
        iconPackClassName: ClassName?,
    ): PropertySpec = propertySpecBuilder(name = config.iconName, type = ClassNames.ImageVector) {
        receiver(iconPackClassName)
        val codeBlock = buildCodeBlock {
            addImageVectorBlock(config = config, irVector = irVector)
        }

        delegate(
            CodeBlock.builder()
                .beginControlFlow("lazy(%T.NONE)", ClassNames.LazyThreadSafetyMode)
                .add(codeBlock)
                .endControlFlow()
                .build(),
        )
    }
}
