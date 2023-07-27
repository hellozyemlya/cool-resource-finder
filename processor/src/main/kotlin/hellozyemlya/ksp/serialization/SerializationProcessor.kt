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
                    context.generatedClassDecls.forEach { targetType ->
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
                            FunSpec.builder("create")
                                .receiver(targetType.companionObject.toTypeName())
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
                            FunSpec.builder("readFrom")
                                .receiver(targetType.companionObject.toTypeName())
                                .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                                .returns(targetType.toTypeName())
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${targetType.declShortName}Impl(\n")
                                            withIndent {
                                                val propIt = targetType.allProps.iterator()
                                                while (propIt.hasNext()) {
                                                    nbtReadProperty(propIt.next(), "compound")
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
                            FunSpec.builder("readFrom")
                                .receiver(targetType.companionObject.toTypeName())
                                .addParameter("buf", MC_PACKET_BUF_TYPE_NAME)
                                .returns(targetType.toTypeName())
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${targetType.declShortName}Impl(\n")
                                            withIndent {
                                                val propIt = targetType.allProps.iterator()
                                                while (propIt.hasNext()) {
                                                    packetReadStmt(propIt.next(), "buf")
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
                                            nbtWriteProperty(it,"this", "compound")
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
                                            packetPutStmt(it, "this", "buf")
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