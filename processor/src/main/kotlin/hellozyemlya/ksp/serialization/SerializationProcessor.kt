package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
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

        if (!context.addClassesForGeneration(symbols)) {
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

        // Generating package statement.
        file += "package hellozyemlya.serialization.generated\n"

        context.generationInfos.forEach {
            file += "// impl: ${it.implClassName}\n"
            file += "//     prop write:\n"
            it.propertiesToWrite.forEach { prop ->
                file += "//         ${prop.simpleName.asString()}\n"
            }
            file += "//     ctor args:\n"
            it.ctorArgs.forEach { ctorArg ->
                file += "//         ${ctorArg.name}\n"
            }
            if (it.callSuperCtor) {
                file += "//     super ctor args:\n"
                it.superCtorArgs.forEach { ctorArg ->
                    file += "//         ${ctorArg.name}\n"
                }
            }

        }

        context.apply {
            val generatedSuperFile = FileSpec
                .builder("hellozyemlya.serialization.generated", "GeneratedStuff")
                .apply {
                    context.generationInfos.forEach { genInfo ->
                        // generate implementations
                        addType(
                            TypeSpec.classBuilder(genInfo.implClassName)
                                .addSuperinterface(genInfo.baseTypeName)
                                // implement properties
                                .apply {
                                    genInfo.propsToImplement.forEach { propertyDecl ->
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
                                // implement ctor
                                .addFunction(
                                    FunSpec.constructorBuilder()
                                        .addParameters(genInfo.ctorArgs)
                                        .apply {
                                            if (genInfo.callSuperCtor) {
                                                callSuperConstructor(genInfo.superCtorArgs.map { CodeBlock.of(it.name) })
                                            }
                                        }
                                        .addCode(
                                            CodeBlock.builder().apply {
                                                genInfo.propsToImplement.forEach {
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
                                .receiver(genInfo.companionTypeName)
                                .addParameters(genInfo.ctorArgs)
                                .returns(genInfo.baseTypeName)
                                .addCode(
                                    CodeBlock.builder().apply {
                                        addStatement("return ${genInfo.implClassName}(${genInfo.implCtorCallArgs})")
                                    }.build()
                                )
                                .build()
                        )
                        // read from nbt
                        addFunction(
                            FunSpec.builder("readFrom")
                                .receiver(genInfo.companionTypeName)
                                .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                                .returns(genInfo.baseTypeName)
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${genInfo.implClassName}(\n")
                                            withIndent {
                                                val propIt = genInfo.propertiesToWrite.iterator()
                                                while (propIt.hasNext()) {
                                                    nbtReadProperty(propIt.next(), "compound")
                                                    if (propIt.hasNext()) {
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
                                .receiver(genInfo.companionTypeName)
                                .addParameter("buf", MC_PACKET_BUF_TYPE_NAME)
                                .returns(genInfo.baseTypeName)
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${genInfo.implClassName}(\n")
                                            withIndent {
                                                val propIt = genInfo.propertiesToWrite.iterator()
                                                while (propIt.hasNext()) {
                                                    packetReadStmt(propIt.next(), "buf")
                                                    if (propIt.hasNext()) {
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
                                .receiver(genInfo.baseTypeName)
                                .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                                .addCode(
                                    CodeBlock.builder().apply {
                                        genInfo.propertiesToWrite.forEach {
                                            nbtWriteProperty(it, "this", "compound")
                                        }
                                    }.build()
                                )
                                .build()
                        )
                        // write to buf
                        addFunction(
                            FunSpec.builder("writeTo")
                                .receiver(genInfo.baseTypeName)
                                .addParameter("buf", MC_PACKET_BUF_TYPE_NAME)
                                .addCode(
                                    CodeBlock.builder().apply {
                                        genInfo.propertiesToWrite.forEach {
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