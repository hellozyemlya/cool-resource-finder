package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

fun KSClassDeclaration.toTypeName(): TypeName {
    return this.asType(emptyList()).toTypeName()
}

val KSClassDeclaration.declShortName: String
    get() = this.simpleName.asString()

val KSClassDeclaration.companionObject: KSClassDeclaration
    get() = this.declarations.filterIsInstance<KSClassDeclaration>().first { it.isCompanionObject }

val KSClassDeclaration.allProps: List<KSPropertyDeclaration>
    get() = this.getAllProperties().toList()

val KSPropertyDeclaration.declShortName: String
    get() = this.simpleName.asString()

val KSClassDeclaration.ctorParameters: List<ParameterSpec>
    get() = this.allProps.map { propertyDecl ->
        ParameterSpec
            .builder(propertyDecl.declShortName, propertyDecl.type.resolve().toTypeName())
            .build()
    }.toList()

val KSClassDeclaration.ctorCallArgs: String
    get() = this.allProps.joinToString(", ") { it.declShortName }

fun Resolver.getKSTypeByName(name: String): KSType {
    return this.getClassDeclarationByName(name)!!.asType(emptyList())
}


inline fun <T> KSType.unwrapNull(
    nonNullArg: T,
    nullArg: T,
    block: (nonNullable: KSType, wasNullable: Boolean, arg: T) -> Unit
) {
    val isNullable = this.nullability == Nullability.NULLABLE


    val nonNullableType = if (isNullable) {
        this.makeNotNullable()
    } else {
        this
    }

    val arg = if (isNullable) {
        nullArg
    } else {
        nonNullArg
    }

    block(nonNullableType, isNullable, arg)
}

fun Boolean.doIf(block: () -> Unit) {
    if (this) {
        block()
    }
}

fun Boolean.doUnless(block: () -> Unit) {
    if (!this) {
        block()
    }
}

fun Resolver.getKSTypeByName(name: String, vararg argTypes: KSType): KSType {
    val mainTypeDecl = this.getClassDeclarationByName(name)!!
    return mainTypeDecl.asType(argTypes.map {
        this.getTypeArgument(this.createKSTypeReferenceFromKSType(it), Variance.INVARIANT)
    })
}

public fun CodeBlock.Builder.endControlFlowNoNl(): CodeBlock.Builder = apply {
    unindent()
    add("}")
}

class SerializationContext(private val resolver: Resolver, private val logger: KSPLogger) {
    private val MutableListDecl = resolver.getClassDeclarationByName("kotlin.collections.MutableList")!!
    private val MutableMapDecl = resolver.getClassDeclarationByName("kotlin.collections.MutableMap")!!
    private val AnyType = resolver.getKSTypeByName("kotlin.Any")
    private val IntType = resolver.getKSTypeByName("kotlin.Int")
    private val StringType = resolver.getKSTypeByName("kotlin.String")
    private val ItemType = resolver.getKSTypeByName("net.minecraft.item.Item")
    private val BlockPosType = resolver.getKSTypeByName("net.minecraft.util.math.BlockPos")
    private val IntList = resolver.getKSTypeByName("kotlin.collections.MutableList", IntType)
    private val StringList = resolver.getKSTypeByName("kotlin.collections.MutableList", StringType)
    private val NbtListType = resolver.getKSTypeByName("net.minecraft.nbt.NbtList")

    public val generatedClassDecls: MutableList<KSClassDeclaration> = ArrayList()
    private val generatedClassTypes: MutableList<KSType> = ArrayList()

    fun addClassesForGeneration(candidates: Sequence<KSClassDeclaration>): Boolean {
        val isValid = candidates.all { validateGenCandidateDecl(it, candidates) }

        if (isValid) {
            generatedClassDecls.addAll(candidates)
            generatedClassTypes.addAll(candidates.map { it.asType(emptyList()) })
        }

        return isValid
    }

    private fun validateGenCandidateDecl(
        candidate: KSClassDeclaration,
        candidates: Sequence<KSClassDeclaration>
    ): Boolean {
        val candidatesTypes = candidates.map { it.asType(emptyList()).makeNotNullable() }.toList()

        var isValid = true
        if (candidate.classKind != ClassKind.INTERFACE) {
            isValid = false
            logger.error("only interfaces can be marked by @McSerialize", candidate)
        }
        candidate.getAllFunctions().forEach { funcDecl ->
            // ignore Any(Object) methods
            funcDecl.findOverridee()?.let { overrideDecl ->
                overrideDecl.parentDeclaration?.let { parentDecl ->
                    if ((parentDecl as KSClassDeclaration).asType(emptyList()) == AnyType) {
                        return@forEach
                    }
                }
            }
            isValid = false
            logger.error("functions not supported in @McSerialize classes, ${funcDecl.simpleName.asString()}", funcDecl)
        }

        candidate.superTypes.forEach { superTypeRef ->
            // ignore Any(Object)
            if (superTypeRef.resolve() == AnyType) {
                return@forEach
            }

            isValid = false
            logger.error(
                "super types not supported in @McSerialize classes, ${superTypeRef.resolve().declaration.simpleName.asString()}",
                superTypeRef
            )
        }

        candidate.getAllProperties().forEach { propDecl ->
            val propType = propDecl.type.resolve().makeNotNullable()
            if (!isTypeSupported(propType, candidatesTypes)) {
                isValid = false
                logger.error("property has unsupported type", propDecl)
            }
        }

        if (candidate.declarations.filterIsInstance<KSClassDeclaration>()
                .firstOrNull { it.isCompanionObject } == null
        ) {
            logger.error("serializable requires companion object to assign creation functions", candidate)
            isValid = false
        }

        return isValid
    }

    private fun isTypeSupported(type: KSType, candidates: List<KSType>): Boolean {
        if (!isBuiltInType(type)) {
            if (type.declaration == MutableListDecl) {
                return isTypeSupported(type.arguments[0].type!!.resolve(), candidates)
            } else if (type.declaration == MutableMapDecl) {
                val keyType = type.arguments[0].type!!.resolve()
                val valueType = type.arguments[1].type!!.resolve()
                return isTypeSupported(keyType, candidates) && isTypeSupported(valueType, candidates)
            }
            return candidates.contains(type)
        }

        return true
    }

    private fun isBuiltInType(type: KSType): Boolean {
        return when (type) {
            IntType, StringType, ItemType, BlockPosType, IntList, StringList -> true
            else -> false
        }
    }

    private val supportedTypes: MutableSet<KSType> = HashSet()
    public val targetTypes: MutableList<KSType> = ArrayList()

    init {
        supportedTypes.add(resolver.getClassDeclarationByName("kotlin.Int")!!.asType(emptyList()))
        supportedTypes.add(resolver.getClassDeclarationByName("kotlin.collections.MutableList")!!.asType(emptyList()))
        supportedTypes.add(resolver.getClassDeclarationByName("net.minecraft.nbt.NbtCompound")!!.asType(emptyList()))
    }

    fun addTargetType(type: KSType) {
        supportedTypes.add(type)
        targetTypes.add(type)
    }


    fun printInfo(outputStream: OutputStream) {
    }


    // region Statement Helper

    private val codeGenNbtPutMap: List<Pair<KSTypePredicate, NbtPutFunc>> = listOf(
        // wrapping nullable must match first
        { type: KSType -> type.isMarkedNullable } to { sourceType, source, nbtKey, compound, _ ->
            val nonNullableType = sourceType.makeNotNullable()
            beginControlFlow("$source?.let { nonNull ->")
            nbtPutStmt(nonNullableType, "nonNull", nbtKey, compound, true)
            endControlFlow()
        },

        ofType(IntType) to { _, source, nbtKey, compound, _ ->
            addStatement("$compound.putInt(%S, %L)", nbtKey, source)
        },

        ofType(StringType) to { _, source, nbtKey, compound, _ ->
            addStatement("$compound.putString(\"$nbtKey\", $source)")
        },
        ofType(ItemType) to { _, source, nbtKey, compound, _ ->
            addStatement(
                "$compound.putString(\"$nbtKey\", %T.ITEM.getId($source).toString())",
                MC_REGISTRIES_TYPE_NAME
            )
        },

        ofType(BlockPosType) to { _, source, nbtKey, compound, _ ->
            addStatement("$compound.putIntArray(\"$nbtKey\", intArrayOf($source.x, $source.y, $source.z))")
        },

        ofType(IntList) to { _, source, nbtKey, compound, _ ->
            addStatement("$compound.putIntArray(\"$nbtKey\", $source)")
        },

        // our custom structures
        { type: KSType -> generatedClassTypes.contains(type) } to { _, source, nbtKey, compound, hasWrapper ->
            hasWrapper.doUnless { beginControlFlow("run") }
            addStatement("val childCompound = NbtCompound()")
            addStatement("$compound.put(\"$nbtKey\", childCompound)")
            addStatement("$source.writeTo(childCompound)")
            hasWrapper.doUnless { endControlFlow() }
        },
        // map
        { type: KSType -> type.declaration == MutableMapDecl } to { sourceType, source, nbtKey, compound, hasWrapper ->
            hasWrapper.doUnless { beginControlFlow("run") }
            addStatement("val entries = %T()", NbtListType.toTypeName())
            addStatement("$compound.put(\"$nbtKey\", entries)")
            beginControlFlow("for(entry in $source)")
            addStatement("val entryNbt = NbtCompound()")
            nbtPutStmt(sourceType.arguments[0].type!!.resolve(), "entry.key", "key", "entryNbt", true)
            nbtPutStmt(sourceType.arguments[1].type!!.resolve(), "entry.value", "value", "entryNbt", true)
            endControlFlow()
            hasWrapper.doUnless { endControlFlow() }
        }
    )

    private fun CodeBlock.Builder.nbtPutStmt(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean = false
    ) {
        val generator = codeGenNbtPutMap
            .first { it.first(sourceType) }
            .second
        this.generator(sourceType, source, nbtKey, compound, hasWrapper)
    }

    fun CodeBlock.Builder.nbtWriteProperty(propDecl: KSPropertyDeclaration, thisVar: String, nbtVar: String) {
        val propType = propDecl.type.resolve()
        nbtPutStmt(propType, "${thisVar}.${propDecl.declShortName}", propDecl.declShortName, nbtVar)
    }

    private val codeGenNbtGetMap: List<Pair<KSTypePredicate, NbtGetFunc>> = listOf(
        // wrapping nullable must match first
        { type: KSType -> type.isMarkedNullable } to { sourceType, nbtKey, compound, _ ->
            val nonNullableType = sourceType.makeNotNullable()
            beginControlFlow("run")
            beginControlFlow("if($compound.contains(\"$nbtKey\"))")
            nbtReadStmt(nonNullableType, nbtKey, compound, true)
            nextControlFlow("else")
            addStatement("null")
            endControlFlow()
            endControlFlowNoNl()
        },

        ofType(IntType) to { _, nbtKey, compound, _ ->
            add("$compound.getInt(\"$nbtKey\")")
        },

        ofType(StringType) to { _, nbtKey, compound, _ ->
            add("$compound.getString(\"$nbtKey\")")
        },
        ofType(ItemType) to { _, nbtKey, compound, _ ->
            add(
                "%T.ITEM.get(%T.tryParse($compound.getString(\"$nbtKey\")))",
                MC_REGISTRIES_TYPE_NAME,
                MC_IDENTIFIER_TYPE_NAME
            )
        },

        ofType(BlockPosType) to { _, nbtKey, compound, _ ->
            add("$compound.getIntArray(\"$nbtKey\").run { BlockPos(this[0], this[1], this[2]) }")
        },

        ofType(IntList) to { _, nbtKey, compound, _ ->
            add("$compound.getIntArray(\"$nbtKey\").toMutableList()")
        },

        // our custom structures
        { type: KSType -> generatedClassTypes.contains(type) } to { sourceType, nbtKey, compound, _ ->
            add("${sourceType.declaration.simpleName.asString()}.readFrom($compound.getCompound(\"$nbtKey\"))")
        },
        // map
        { type: KSType -> type.declaration == MutableMapDecl } to { sourceType, nbtKey, compound, hasWrapper ->
            hasWrapper.doUnless { beginControlFlow("run") }

            val keyType = sourceType.arguments[0].type!!.resolve()
            val valueType = sourceType.arguments[1].type!!.resolve()
            addStatement(
                "val result = %T()", resolver.getKSTypeByName(
                    "java.util.HashMap", keyType, valueType
                ).toTypeName()
            )
            addStatement("val list = $compound.getList(\"$nbtKey\", %T.COMPOUND_TYPE.toInt())", NBT_ELEMENT_TYPE_NAME)
            beginControlFlow("for(entry in list.map { it as %T })", NBT_COMPOUND_TYPE_NAME)
            add("val key = ")
            nbtReadStmt(keyType, "key", "entry", true)
            add("\n")
            add("val value = ")
            nbtReadStmt(valueType, "value", "entry", true)
            add("\n")
            addStatement("result[key] = value")
            endControlFlow()
            addStatement("result")
            hasWrapper.doUnless { endControlFlowNoNl() }
        }
    )

    private fun CodeBlock.Builder.nbtReadStmt(
        targetType: KSType,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean = false
    ) {
        val generator = codeGenNbtGetMap
            .first { it.first(targetType) }
            .second
        this.generator(targetType, nbtKey, compound, hasWrapper)
    }

    fun CodeBlock.Builder.nbtReadProperty(propDecl: KSPropertyDeclaration, compound: String) {
        val propType = propDecl.type.resolve()
        nbtReadStmt(propType, propDecl.declShortName, compound)
    }


    private val typeToPacketRead = listOf<Pair<KSTypePredicate, BufGetFunc>>(
        { type: KSType -> type.isMarkedNullable } to { sourceType, bufVar, _ ->
            val nonNullableType = sourceType.makeNotNullable()
            beginControlFlow("run")
            beginControlFlow("if($bufVar.readBoolean())")
            packetReadStmt(nonNullableType, bufVar, true)
            nextControlFlow("else")
            addStatement("null")
            endControlFlow()
            endControlFlowNoNl()
        },

        ofType(IntType) to { _, bufVar, _ ->
            add("$bufVar.readInt()")
        },
        ofType(StringType) to { _, bufVar, _ ->
            add("$bufVar.readString()")
        },
        ofType(ItemType) to { _, bufVar, _ ->
            add(
                "%T.ITEM.get(%T.tryParse($bufVar.readString()))",
                MC_REGISTRIES_TYPE_NAME,
                MC_IDENTIFIER_TYPE_NAME
            )
        },
        ofType(BlockPosType) to { _, bufVar, _ ->
            add("$bufVar.readBlockPos()")
        },
        ofType(IntList) to { _, bufVar, _ ->
            add("$bufVar.readIntList()")
        },

        { type: KSType -> generatedClassTypes.contains(type) } to { sourceType, bufVar, _ ->
            add("${sourceType.declaration.simpleName.asString()}.readFrom($bufVar)")
        },
        { type: KSType -> type.declaration == MutableMapDecl } to { sourceType, bufVar, hasWrapper ->
            hasWrapper.doUnless { beginControlFlow("run") }

            val keyType = sourceType.arguments[0].type!!.resolve()
            val valueType = sourceType.arguments[1].type!!.resolve()
            addStatement(
                "val result = %T()", resolver.getKSTypeByName(
                    "java.util.HashMap", keyType, valueType
                ).toTypeName()
            )
            beginControlFlow("repeat($bufVar.readInt())")
            add("val key = ")
            packetReadStmt(keyType, bufVar, true)
            add("\n")
            add("val value = ")
            packetReadStmt(valueType, bufVar, true)
            add("\n")
            addStatement("result[key] = value")
            endControlFlow()
            addStatement("result")
            hasWrapper.doUnless { endControlFlowNoNl() }
        }
    )


    fun CodeBlock.Builder.packetReadStmt(targetType: KSType, bufVar: String, hasWrapper: Boolean = false) {
        val generator = typeToPacketRead
            .first { it.first(targetType) }
            .second
        this.generator(targetType, bufVar, hasWrapper)
    }

    fun CodeBlock.Builder.packetReadStmt(propDecl: KSPropertyDeclaration, nbtVar: String) {
        val propType = propDecl.type.resolve()
        packetReadStmt(propType, nbtVar)
    }


    private val typeToPacketPut = listOf<Pair<KSTypePredicate, BufPutFunc>>(
        { type: KSType -> type.isMarkedNullable } to { sourceType, source, bufVar, _ ->
            val nonNullableType = sourceType.makeNotNullable()
            addStatement("$bufVar.writeBoolean($source != null)")
            beginControlFlow("$source?.let")
            packetPutStmt(nonNullableType, "it", bufVar, true)
            endControlFlow()
        },
        ofType(IntType) to { _, source, bufVar, _ ->
            addStatement("$bufVar.writeInt($source)")
        },
        ofType(StringType) to { _, source, bufVar, _ ->
            addStatement("$bufVar.writeString($source)")
        },
        ofType(ItemType) to { _, source, bufVar, _ ->
            addStatement(
                "$bufVar.writeString(%T.ITEM.getId($source).toString())",
                MC_REGISTRIES_TYPE_NAME
            )
        },
        ofType(BlockPosType) to { _, source, bufVar, _ ->
            addStatement("$bufVar.writeBlockPos($source)")
        },
        ofType(IntList) to { _, source, bufVar, _ ->
            addStatement("$bufVar.writeVarInt($source.size)")
            beginControlFlow("for(e in $source)")
            addStatement("$bufVar.writeVarInt(e)")
            endControlFlow()
        },
        { type: KSType -> generatedClassTypes.contains(type) } to { sourceType, source, bufVar, _ ->
            addStatement("$source.writeTo($bufVar)")
        },
        { type: KSType -> type.declaration == MutableMapDecl } to { sourceType, source, bufVar, _ ->
            addStatement("$bufVar.writeInt($source.size)")
            beginControlFlow("for(entry in $source)")
            packetPutStmt(sourceType.arguments[0].type!!.resolve(), "entry.key", bufVar)
            packetPutStmt(sourceType.arguments[1].type!!.resolve(), "entry.value", bufVar)
            endControlFlow()
        },
    )

    fun CodeBlock.Builder.packetPutStmt(
        sourceType: KSType,
        source: String,
        bufVar: String,
        hasWrapper: Boolean = false
    ) {
        val generator = typeToPacketPut
            .first { it.first(sourceType) }
            .second
        this.generator(sourceType, source, bufVar, hasWrapper)
    }

    fun CodeBlock.Builder.packetPutStmt(propDecl: KSPropertyDeclaration, propSource: String, nbtVar: String) {
        val propType = propDecl.type.resolve()
        packetPutStmt(propType, "${propSource}.${propDecl.declShortName}", nbtVar)
    }
    // endregion
}

typealias KSTypePredicate = (type: KSType) -> Boolean
typealias NbtPutFunc = CodeBlock.Builder.(sourceType: KSType, source: String, nbtKey: String, compound: String, hasWrapper: Boolean) -> Unit
typealias NbtGetFunc = CodeBlock.Builder.(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) -> Unit
typealias BufGetFunc = CodeBlock.Builder.(sourceType: KSType, bufVar: String, hasWrapper: Boolean) -> Unit
typealias BufPutFunc = CodeBlock.Builder.(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) -> Unit

fun ofType(type: KSType): KSTypePredicate {
    return { it == type }
}