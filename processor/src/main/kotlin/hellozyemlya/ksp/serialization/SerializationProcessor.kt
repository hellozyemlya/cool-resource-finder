package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

val NBT_COMPOUND_TYPE_NAME = ClassName("net.minecraft.nbt", "NbtCompound")
val MC_ITEM_TYPE_NAME = ClassName("net.minecraft.item", "Item")
val MC_REGISTRIES_TYPE_NAME = ClassName("net.minecraft.registry", "Registries")
val MC_BLOCK_POS_TYPE_NAME = ClassName("net.minecraft.util.math", "BlockPos")
val MC_IDENTIFIER_TYPE_NAME = ClassName("net.minecraft.util", "Identifier")
fun CodeBlock.Builder.addNbtWriteStatement(
    nbtVar: String,
    propDecl: KSPropertyDeclaration,
    ctx: SerializationContext
): CodeBlock.Builder {
    val propType = propDecl.type.resolve()
    propType.nullability
    when (propType.toTypeName()) {
        INT ->
            addStatement("$nbtVar.putInt(\"${propDecl.declShortName}\", this.${propDecl.declShortName})")

        STRING ->
            addStatement("$nbtVar.putString(\"${propDecl.declShortName}\", this.${propDecl.declShortName})")

        MC_ITEM_TYPE_NAME ->
            addStatement(
                "$nbtVar.putString(\"${propDecl.declShortName}\", %T.ITEM.getId(this.${propDecl.declShortName}).toString())",
                MC_REGISTRIES_TYPE_NAME
            )

        MC_BLOCK_POS_TYPE_NAME -> {
            addStatement("$nbtVar.putIntArray(\"${propDecl.declShortName}\", intArrayOf(this.${propDecl.declShortName}.x, this.${propDecl.declShortName}.y, this.${propDecl.declShortName}.z))")
        }

        else -> {
            if (ctx.targetTypes.contains(propType)) {
                addStatement("val ${propDecl.declShortName}Compound = NbtCompound()")
                addStatement("$nbtVar.put(\"${propDecl.declShortName}\", ${propDecl.declShortName}Compound)")
                addStatement("this.${propDecl.declShortName}.writeToNbt(${propDecl.declShortName}Compound)")
            } else {
                addStatement("// don't support '${propType.toTypeName()}'")
            }
        }
    }
    return this
}

fun CodeBlock.Builder.addNbtReadStatement(
    nbtVar: String,
    propDecl: KSPropertyDeclaration,
    trailing: String,
    ctx: SerializationContext
): CodeBlock.Builder {
    val propType = propDecl.type.resolve()
    propType.nullability
    when (propType.toTypeName()) {
        INT ->
            addStatement("$nbtVar.getInt(\"${propDecl.declShortName}\")$trailing")
        STRING ->
            addStatement("$nbtVar.getString(\"${propDecl.declShortName}\")$trailing")

        MC_ITEM_TYPE_NAME ->
            addStatement(
                "%T.ITEM.get(%T.tryParse($nbtVar.getString(\"${propDecl.declShortName}\")))$trailing",
                MC_REGISTRIES_TYPE_NAME,
                MC_IDENTIFIER_TYPE_NAME
            )

        MC_BLOCK_POS_TYPE_NAME -> {
            addStatement("$nbtVar.getIntArray(\"${propDecl.declShortName}\").run { BlockPos(this[0], this[1], this[2]) }$trailing")
        }

        else -> {
            if (ctx.targetTypes.contains(propType)) {
                addStatement("create${propType.declShortName}FromNbt($nbtVar.getCompound(\"${propDecl.declShortName}\"))$trailing")
            } else {
                addStatement("// don't support '${propType.toTypeName()}'")
            }
        }
    }
    return this
}

class SerializationProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {


    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            // Getting all symbols that are annotated with @Function.
            .getSymbolsWithAnnotation("hellozyemlya.serialization.annotations.McSerialize")
            // Making sure we take only class declarations.
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val deps = Dependencies(true, *resolver.getAllFiles().toList().toTypedArray())
        val file = codeGenerator.createNewFile(
            // Make sure to associate the generated file with sources to keep/maintain it across incremental builds.
            // Learn more about incremental processing in KSP from the official docs:
            // https://kotlinlang.org/docs/ksp-incremental.html
            dependencies = deps,
            packageName = "hellozyemlya.serialization.generated",
            fileName = "GeneratedSerialization"
        )

        val context = SerializationContext(resolver)

        symbols.forEach {
            context.addTargetType(it.asType(emptyList()))
        }

        // Generating package statement.
        file += "package hellozyemlya.serialization.generated\n"
        context.printInfo(file)

        val generatedSuperFile = FileSpec
            .builder("hellozyemlya.serialization.generated", "GeneratedStuff")
            .apply {
                context.targetTypes.forEach { targetType ->
                    // impl class
                    addType(
                        TypeSpec.classBuilder("${targetType.declShortName}Impl")
                            .addSuperinterface(targetType.toTypeName())
                            .apply {
                                targetType.allProps.forEach { propertyDecl ->
                                    addProperty(
                                        PropertySpec
                                            .builder(
                                                propertyDecl.declShortName,
                                                propertyDecl.type.resolve().toTypeName()
                                            )
                                            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                                            .mutable(propertyDecl.isMutable)
                                            .build()
                                    )
                                }
                            }
                            .addFunction(
                                FunSpec.constructorBuilder()
                                    .addParameters(targetType.ctorParameters)
                                    .addCode(
                                        CodeBlock.builder().apply {
                                            targetType.allProps.forEach {
                                                addStatement("this.${it.declShortName} = ${it.declShortName}")
                                            }
                                        }.build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    // impl creation function
                    addFunction(
                        FunSpec.builder("create${targetType.declShortName}")
                            .addParameters(targetType.ctorParameters)
                            .returns(targetType.toTypeName())
                            .addCode(
                                CodeBlock.builder().apply {
                                    addStatement("return ${targetType.declShortName}Impl(${targetType.ctorCallArgs})")
                                }.build()
                            )
                            .build()
                    )
                    // impl creation from nbt
                    addFunction(
                        FunSpec.builder("create${targetType.declShortName}FromNbt")
                            .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                            .returns(targetType.toTypeName())
                            .addCode(
                                CodeBlock.builder()
                                    .apply {
                                        add("return ${targetType.declShortName}Impl(\n")
                                        withIndent {
                                            targetType.allProps.forEach {
                                                addNbtReadStatement("compound", it, ",", context)
                                            }
                                        }
                                        add(")")
                                    }.build()
                            )
                            .build()
                    )
                    // nbt put
                    addFunction(
                        FunSpec.builder("writeToNbt")
                            .receiver(targetType.toTypeName())
                            .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                            .addCode(
                                CodeBlock.builder().apply {
                                    targetType.allProps.forEach {
                                        addNbtWriteStatement("compound", it, context)
                                    }
                                }.build()
                            )
                            .build()
                    )
                }
            }
            .build()

        generatedSuperFile.writeTo(codeGenerator, deps)

        return symbols.filterNot { it.validate() }.toList()
    }
}