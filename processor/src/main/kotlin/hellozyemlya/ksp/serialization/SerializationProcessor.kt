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
val NBT_ELEMENT_TYPE_NAME = ClassName("net.minecraft.nbt", "NbtElement")
val MC_ITEM_TYPE_NAME = ClassName("net.minecraft.item", "Item")
val MC_REGISTRIES_TYPE_NAME = ClassName("net.minecraft.registry", "Registries")
val MC_BLOCK_POS_TYPE_NAME = ClassName("net.minecraft.util.math", "BlockPos")
val MC_IDENTIFIER_TYPE_NAME = ClassName("net.minecraft.util", "Identifier")
val MC_PACKET_BUF_TYPE_NAME = ClassName("net.minecraft.network", "PacketByteBuf")

fun CodeBlock.Builder.addBufWrite(bufVar: String, propDecl: KSPropertyDeclaration, ctx: SerializationContext): CodeBlock.Builder  {
    val propType = propDecl.type.resolve()

    when (propType.toTypeName()) {
        INT ->
            add("$bufVar.writeInt(this.${propDecl.declShortName})")

        STRING ->
            add("$bufVar.writeString(this.${propDecl.declShortName})")

        MC_ITEM_TYPE_NAME ->
            add(
                "$bufVar.writeString(%T.ITEM.getId(this.${propDecl.declShortName}).toString())",
                MC_REGISTRIES_TYPE_NAME
            )

        MC_BLOCK_POS_TYPE_NAME -> {
            add("$bufVar.writeBlockPos(this.${propDecl.declShortName})")
        }

        else -> {
            if (ctx.targetTypes.contains(propType)) {
                add("this.${propDecl.declShortName}.writeTo($bufVar)")
            } else {
                add("// don't support '${propType.toTypeName()}'")
            }
        }
    }
    return this
}


fun CodeBlock.Builder.addBufRead(bufVar: String, propDecl: KSPropertyDeclaration, ctx: SerializationContext): CodeBlock.Builder  {
    val propType = propDecl.type.resolve()

    when (propType.toTypeName()) {
        INT ->
            add("$bufVar.readInt()")

        STRING ->
            add("$bufVar.readString()")

        MC_ITEM_TYPE_NAME ->
            add(
                "%T.ITEM.get(%T.tryParse($bufVar.readString()))",
                MC_REGISTRIES_TYPE_NAME,
                MC_IDENTIFIER_TYPE_NAME
            )

        MC_BLOCK_POS_TYPE_NAME -> {
            add("$bufVar.readBlockPos()")
        }

        else -> {
            if (ctx.targetTypes.contains(propType)) {
                add("read${propType.declShortName}From($bufVar)")
            } else {
                add("// don't support '${propType.toTypeName()}'")
            }
        }
    }
    return this
}

fun CodeBlock.Builder.addBufWriteStmt(bufVar: String, propDecl: KSPropertyDeclaration, ctx: SerializationContext): CodeBlock.Builder  {
    add("«")
    this.addBufWrite(bufVar, propDecl, ctx)
    add("\n»")
    return this
}

fun CodeBlock.Builder.addNbtWriteStatement(
    nbtVar: String,
    propDecl: KSPropertyDeclaration,
    ctx: SerializationContext
): CodeBlock.Builder {
    val propType = propDecl.type.resolve()

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
                addStatement("this.${propDecl.declShortName}.writeTo(${propDecl.declShortName}Compound)")
            } else {
                addStatement("// don't support '${propType.toTypeName()}'")
            }
        }
    }
    return this
}

fun CodeBlock.Builder.addNbtRead(
    nbtVar: String,
    propDecl: KSPropertyDeclaration,
    ctx: SerializationContext
): CodeBlock.Builder {
    val propType = propDecl.type.resolve()

    when (propType.toTypeName()) {
        INT ->
            add("$nbtVar.getInt(\"${propDecl.declShortName}\")")
        STRING ->
            add("$nbtVar.getString(\"${propDecl.declShortName}\")")

        MC_ITEM_TYPE_NAME ->
            add(
                "%T.ITEM.get(%T.tryParse($nbtVar.getString(\"${propDecl.declShortName}\")))",
                MC_REGISTRIES_TYPE_NAME,
                MC_IDENTIFIER_TYPE_NAME
            )

        MC_BLOCK_POS_TYPE_NAME -> {
            add("$nbtVar.getIntArray(\"${propDecl.declShortName}\").run { BlockPos(this[0], this[1], this[2]) }")
        }

        else -> {
            if (ctx.targetTypes.contains(propType)) {
                add("read${propType.declShortName}From($nbtVar.getCompound(\"${propDecl.declShortName}\"))")
            } else {
                add("// don't support '${propType.toTypeName()}'")
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

        val context = SerializationContext(resolver, logger)

        if(!context.addClassesForGeneration(symbols)) {
            // some classes not passed validation
            return emptyList()
        }

        val deps = Dependencies(true, *resolver.getAllFiles().toList().toTypedArray())
        val file = codeGenerator.createNewFile(
            // Make sure to associate the generated file with sources to keep/maintain it across incremental builds.
            // Learn more about incremental processing in KSP from the official docs:
            // https://kotlinlang.org/docs/ksp-incremental.html
            dependencies = deps,
            packageName = "hellozyemlya.serialization.generated",
            fileName = "GeneratedSerialization"
        )

        symbols.forEach {
            context.addTargetType(it.asType(emptyList()))
        }

        // Generating package statement.
        file += "package hellozyemlya.serialization.generated\n"
        context.printInfo(file)

        context.apply {
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
                        // read from nbt
                        addFunction(
                            FunSpec.builder("read${targetType.declShortName}From")
                                .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                                .returns(targetType.toTypeName())
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${targetType.declShortName}Impl(\n")
                                            withIndent {
                                                val propIt = targetType.allProps.iterator()
                                                while (propIt.hasNext()) {
                                                    nbtReadStmt(propIt.next(), "compound")
                                                    if(propIt.hasNext()) {
                                                        add(",\n")
                                                    }
                                                }
                                            }
                                            add("\n)")
                                        }.build()
                                )
                                .build()
                        )
                        // read from buf
                        addFunction(
                            FunSpec.builder("read${targetType.declShortName}From")
                                .addParameter("buf", MC_PACKET_BUF_TYPE_NAME)
                                .returns(targetType.toTypeName())
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${targetType.declShortName}Impl(\n")
                                            withIndent {
                                                val propIt = targetType.allProps.iterator()
                                                while (propIt.hasNext()) {
                                                    addBufRead("buf", propIt.next(), context)
                                                    if(propIt.hasNext()) {
                                                        add(",\n")
                                                    }
                                                }
                                            }
                                            add("\n)")
                                        }.build()
                                )
                                .build()
                        )
                        // write to nbt
                        addFunction(
                            FunSpec.builder("writeTo")
                                .receiver(targetType.toTypeName())
                                .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                                .addCode(
                                    CodeBlock.builder().apply {
                                        targetType.allProps.forEach {
                                            nbtPutStmt(it,"this", "compound")
                                        }
                                    }.build()
                                )
                                .build()
                        )
                        // write to buf
                        addFunction(
                            FunSpec.builder("writeTo")
                                .receiver(targetType.toTypeName())
                                .addParameter("buf", MC_PACKET_BUF_TYPE_NAME)
                                .addCode(
                                    CodeBlock.builder().apply {
                                        targetType.allProps.forEach {
                                            addBufWriteStmt("buf", it, context)
                                        }
                                    }.build()
                                )
                                .build()
                        )
                    }
                }
                .build()

            generatedSuperFile.writeTo(codeGenerator, deps)
        }


        return symbols.filterNot { it.validate() }.toList()
    }
}