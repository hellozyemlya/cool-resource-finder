package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo

val NBT_COMPOUND_TYPE_NAME = ClassName("net.minecraft.nbt", "NbtCompound")
val NBT_ELEMENT_TYPE_NAME = ClassName("net.minecraft.nbt", "NbtElement")
val MC_ITEM_TYPE_NAME = ClassName("net.minecraft.item", "Item")
val MC_ITEMS_TYPE_NAME = ClassName("net.minecraft.item", "Items")
val MC_REGISTRIES_TYPE_NAME = ClassName("net.minecraft.registry", "Registries")
val MC_BLOCK_POS_TYPE_NAME = ClassName("net.minecraft.util.math", "BlockPos")
val MC_IDENTIFIER_TYPE_NAME = ClassName("net.minecraft.util", "Identifier")
val MC_PACKET_BUF_TYPE_NAME = ClassName("net.minecraft.network", "PacketByteBuf")
val MC_PERSISTENT_STATE_MANAGER = ClassName("net.minecraft.world", "PersistentStateManager")
val STRING_TYPE_NAME = ClassName("kotlin", "String")

inline fun <T> Collection<T>.contains(predicate: (element: T) -> Boolean) : Boolean {
    this.forEach { element->
        if(predicate(element)) {
            return true
        }
    }
    return false
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
//            file += "// impl: ${it.implClassName}\n"
//            file += "//     prop write:\n"
//            it.propertiesToWrite.forEach { prop ->
//                file += "//         ${prop.simpleName.asString()}\n"
//            }
//            file += "//     ctor args:\n"
//            it.ctorArgs.forEach { ctorArg ->
//                file += "//         ${ctorArg.name}\n"
//            }
//            if (it.callSuperCtor) {
//                file += "//     super ctor args:\n"
//                it.superCtorArgs.forEach { ctorArg ->
//                    file += "//         ${ctorArg.name}\n"
//                }
//            }

        }

        context.apply {
            val generatedSuperFile = FileSpec
                .builder("hellozyemlya.serialization.generated", "GeneratedStuff")
                .apply {
                    context.generationInfos.forEach { genInfo ->
                        // generate implementations
                        addType(
                            TypeSpec.classBuilder(genInfo.implClassNameStr)
                                .addSuperinterface(genInfo.baseTypeName)
                                // implement properties
                                .apply {
                                    genInfo.propsToImplement.forEach { propGenInfo ->
                                        if (genInfo.isPersistentState && propGenInfo.isMutable) {
                                            addProperty(
                                                PropertySpec
                                                    .builder(
                                                        propGenInfo.name,
                                                        propGenInfo.typeName
                                                    )
                                                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                                                    .mutable(true)
                                                    .setter(FunSpec
                                                        .setterBuilder()
                                                        .addParameter("newValue", propGenInfo.typeName)
                                                        .addCode(CodeBlock.of("markDirty()\nfield = newValue"))
                                                        .build())
                                                    .build()
                                            )
                                        } else {
                                            addProperty(
                                                PropertySpec
                                                    .builder(
                                                        propGenInfo.name,
                                                        propGenInfo.typeName
                                                    )
                                                    .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
                                                    .mutable(propGenInfo.isMutable)
                                                    .build()
                                            )
                                        }
                                    }
                                }
                                // implement ctor
                                .addFunction(
                                    FunSpec.constructorBuilder()
                                        .addParameters(genInfo.implCtorArgs)
                                        .apply {
                                            if (genInfo.callSuperCtor) {
                                                callSuperConstructor(genInfo.superCtorArgs.map { CodeBlock.of(it.name) })
                                            }
                                        }
                                        .addCode(
                                            CodeBlock.builder().apply {
                                                genInfo.propsToImplement.forEach {
                                                    addStatement("this.${it.name} = ${it.name}")
                                                }
                                            }.build()
                                        )
                                        .build()
                                )
                                // generate writeNbt function
                                .apply {
                                    if (genInfo.isPersistentState) {
                                        addFunction(
                                            FunSpec.builder("writeNbt")
                                                .returns(NBT_COMPOUND_TYPE_NAME)
                                                .addModifiers(KModifier.OVERRIDE)
                                                .addParameter("nbt", NBT_COMPOUND_TYPE_NAME)
                                                .addCode(CodeBlock.of("this.writeTo(nbt)\nreturn nbt"))
                                                .build()
                                        )
                                    }
                                }
                                .build()
                        )
                        // generate persistent state manager getter function
                        if(genInfo.isPersistentState) {
                            // impl creation function
                            addFunction(
                                FunSpec.builder("get${genInfo.baseTypeNameStr}OrCreate")
                                    .receiver(MC_PERSISTENT_STATE_MANAGER)
                                    .addParameter("stateId", STRING_TYPE_NAME)
                                    .addParameters(genInfo.persistentStateArgs)
                                    .returns(genInfo.baseTypeName)
                                    .addCode(
                                        CodeBlock.builder().apply {
                                            addStatement("val existent: %T? = this.get(%T::readFrom, stateId)", genInfo.baseTypeName, genInfo.companionTypeName)
                                            beginControlFlow("if (existent != null)")
                                            addStatement("return existent")
                                            nextControlFlow("else")
                                            add("val newInstance = ${genInfo.implClassNameStr}(\n")
                                            withIndent {
                                                val propIt = genInfo.allProperties.iterator()
                                                while (propIt.hasNext()) {
                                                    val prop = propIt.next()
                                                    if(prop.isPersistentSateArg) {
                                                        // expected arg name and prop name/positions not changed
                                                        add(prop.name)
                                                    } else {
                                                        defaultValue(prop.type)
                                                    }
                                                    if (propIt.hasNext()) {
                                                        add(",\n")
                                                    }
                                                }
                                            }
                                            add("\n)\n")
                                            addStatement("this.set(stateId, newInstance)")
                                            addStatement("return newInstance")
                                            endControlFlow()
                                        }.build()
                                    )
                                    .build()
                            )
                        }
                        // impl creation function
                        addFunction(
                            FunSpec.builder("create")
                                .receiver(genInfo.companionTypeName)
                                .addParameters(genInfo.implCtorArgs)
                                .returns(genInfo.baseTypeName)
                                .addCode(
                                    CodeBlock.builder().apply {
                                        addStatement("return ${genInfo.implClassNameStr}(${genInfo.implCtorArgs.joinToString(", ") { it.name }})")
                                    }.build()
                                )
                                .build()
                        )
                        // impl default creation function
                        addFunction(
                            FunSpec.builder("createDefault")
                                .receiver(genInfo.companionTypeName)
                                .returns(genInfo.baseTypeName)
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${genInfo.implClassNameStr}(\n")
                                            withIndent {
                                                val propIt = genInfo.allProperties.iterator()
                                                while (propIt.hasNext()) {
                                                    defaultValue(propIt.next().type)
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
                        // read from nbt
                        addFunction(
                            FunSpec.builder("readFrom")
                                .receiver(genInfo.companionTypeName)
                                .addParameter("compound", NBT_COMPOUND_TYPE_NAME)
                                .returns(genInfo.baseTypeName)
                                .addCode(
                                    CodeBlock.builder()
                                        .apply {
                                            add("return ${genInfo.implClassNameStr}(\n")
                                            withIndent {
                                                val propIt = genInfo.allProperties.iterator()
                                                while (propIt.hasNext()) {
                                                    val propInfo = propIt.next()
                                                    if(propInfo.isNbtIgnore) {
                                                        defaultValue(propInfo.type)
                                                    } else {
                                                        nbtReadProperty(propInfo.decl, "compound")
                                                    }
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
                                            add("return ${genInfo.implClassNameStr}(\n")
                                            withIndent {
                                                val propIt = genInfo.allProperties.iterator()
                                                while (propIt.hasNext()) {
                                                    val propInfo = propIt.next()
                                                    if(propInfo.isPacketIgnore) {
                                                        defaultValue(propInfo.type)
                                                    } else {
                                                        packetReadStmt(propInfo.decl, "buf")
                                                    }
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
                                        genInfo.allProperties.filter { !it.isNbtIgnore }.forEach {
                                            nbtWriteProperty(it.decl, "this", "compound")
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
                                        genInfo.allProperties.filter { !it.isPacketIgnore }.forEach {
                                            packetPutStmt(it.decl, "this", "buf")
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