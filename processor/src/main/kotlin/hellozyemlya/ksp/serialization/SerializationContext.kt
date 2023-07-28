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
import kotlin.reflect.KClass

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

interface AccessStatements {
    fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    )

    fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean)
    fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean)
    fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean)
}

interface AccessGenerator : AccessStatements {
    val priority: Int
    fun match(sourceType: KSType): Boolean

}

abstract class SimpleAccessGenerator(private val compareType: KSType) : AccessGenerator {
    override val priority: Int
        get() = 0

    override fun match(sourceType: KSType): Boolean {
        return sourceType == compareType
    }
}

class IntAccessGenerator(intType: KSType) : SimpleAccessGenerator(intType) {
    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        addStatement("$compound.putInt(%S, %L)", nbtKey, source)
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        add("$compound.getInt(\"$nbtKey\")")
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        addStatement("$bufVar.writeInt($source)")
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        add("$bufVar.readInt()")
    }
}

class StringAccessGenerator(intType: KSType) : SimpleAccessGenerator(intType) {
    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        addStatement("$compound.putString(%S, %L)", nbtKey, source)
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        add("$compound.getString(\"$nbtKey\")")
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        addStatement("$bufVar.writeString($source)")
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        add("$bufVar.readString()")
    }
}


class ItemAccessGenerator(itemType: KSType) : SimpleAccessGenerator(itemType) {
    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        addStatement(
            "$compound.putString(\"$nbtKey\", %T.ITEM.getId($source).toString())",
            MC_REGISTRIES_TYPE_NAME
        )
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        add(
            "%T.ITEM.get(%T.tryParse($compound.getString(\"$nbtKey\")))",
            MC_REGISTRIES_TYPE_NAME,
            MC_IDENTIFIER_TYPE_NAME
        )
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        addStatement(
            "$bufVar.writeString(%T.ITEM.getId($source).toString())",
            MC_REGISTRIES_TYPE_NAME
        )
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        add(
            "%T.ITEM.get(%T.tryParse($bufVar.readString()))",
            MC_REGISTRIES_TYPE_NAME,
            MC_IDENTIFIER_TYPE_NAME
        )
    }
}

class BlockPosAccessGenerator(blockPosType: KSType) : SimpleAccessGenerator(blockPosType) {
    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        addStatement("$compound.putIntArray(\"$nbtKey\", intArrayOf($source.x, $source.y, $source.z))")
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        add("$compound.getIntArray(\"$nbtKey\").run { BlockPos(this[0], this[1], this[2]) }")
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        addStatement("$bufVar.writeBlockPos($source)")
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        add("$bufVar.readBlockPos()")
    }
}

class IntListAccessGenerator(intListType: KSType) : SimpleAccessGenerator(intListType) {
    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        addStatement("$compound.putIntArray(\"$nbtKey\", $source)")
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        add("$compound.getIntArray(\"$nbtKey\").toMutableList()")
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        addStatement("$bufVar.writeVarInt($source.size)")
        beginControlFlow("for(e in $source)")
        addStatement("$bufVar.writeVarInt(e)")
        endControlFlow()
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        add("$bufVar.readIntList()")
    }
}

class NullWrapAccessGenerator(private val nested: AccessStatements) : AccessGenerator {
    override val priority: Int
        get() = Int.MIN_VALUE

    override fun match(sourceType: KSType): Boolean {
        return sourceType.isMarkedNullable
    }

    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        val nonNullableType = sourceType.makeNotNullable()
        beginControlFlow("$source?.let { nonNull ->")
        nested.run {
            nbtPut(nonNullableType, "nonNull", nbtKey, compound, true)
        }
        endControlFlow()
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        val nonNullableType = sourceType.makeNotNullable()
        beginControlFlow("run")
        beginControlFlow("if($compound.contains(\"$nbtKey\"))")
        nested.run {
            nbtGet(nonNullableType, nbtKey, compound, true)
        }
        nextControlFlow("else")
        addStatement("null")
        endControlFlow()
        endControlFlowNoNl()
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        val nonNullableType = sourceType.makeNotNullable()
        addStatement("$bufVar.writeBoolean($source != null)")
        beginControlFlow("$source?.let")
        nested.run {
            bufPut(nonNullableType, "it", bufVar, true)
        }
        endControlFlow()
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        val nonNullableType = sourceType.makeNotNullable()
        beginControlFlow("run")
        beginControlFlow("if($bufVar.readBoolean())")
        nested.run {
            bufGet(nonNullableType, bufVar, true)
        }
        nextControlFlow("else")
        addStatement("null")
        endControlFlow()
        endControlFlowNoNl()
    }
}


class SerializableStructAccessGenerator(private val structs: List<KSType>) : AccessGenerator {
    override val priority: Int
        get() = Int.MAX_VALUE

    override fun match(sourceType: KSType): Boolean {
        return structs.contains(sourceType)
    }

    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        hasWrapper.doUnless { beginControlFlow("run") }
        addStatement("val childCompound = NbtCompound()")
        addStatement("$compound.put(\"$nbtKey\", childCompound)")
        addStatement("$source.writeTo(childCompound)")
        hasWrapper.doUnless { endControlFlow() }
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        add("${sourceType.declaration.simpleName.asString()}.readFrom($compound.getCompound(\"$nbtKey\"))")
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        addStatement("$source.writeTo($bufVar)")
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        add("${sourceType.declaration.simpleName.asString()}.readFrom($bufVar)")
    }
}

abstract class BaseCollectionAccessGenerator(private val nbtListType: KSType) : AccessGenerator {
    override val priority: Int
        get() = Int.MAX_VALUE

    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        hasWrapper.doUnless { beginControlFlow("run") }
        addStatement("val entries = %T()", nbtListType.toTypeName())
        addStatement("$compound.put(\"$nbtKey\", entries)")
        beginControlFlow("for(entry in $source)")
        addStatement("val entryNbt = NbtCompound()")
        addStatement("entries.add(entryNbt)")
        nbtPutEntry(sourceType.arguments.map { it.type!!.resolve() }, "entryNbt", "entry")
        endControlFlow()
        hasWrapper.doUnless { endControlFlow() }
    }

    protected abstract fun CodeBlock.Builder.nbtPutEntry(argumentsList: List<KSType>, targetNbt: String, entrySource: String)
    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        val elementTypes = getPrelude(sourceType, hasWrapper)
        addStatement("val list = $compound.getList(\"$nbtKey\", %T.COMPOUND_TYPE.toInt())", NBT_ELEMENT_TYPE_NAME)
        beginControlFlow("for(entry in list.map { it as %T })", NBT_COMPOUND_TYPE_NAME)
        nbtGetEntry(elementTypes, "entry")
        endControlFlow()
        addStatement("result")
        hasWrapper.doUnless { endControlFlowNoNl() }
    }

    protected abstract fun CodeBlock.Builder.nbtGetEntry(argumentsList: List<KSType>, sourceNbt: String)
    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        addStatement("$bufVar.writeInt($source.size)")
        beginControlFlow("for(entry in $source)")
        bufPutEntry(sourceType.arguments.map { it.type!!.resolve() }, bufVar, "entry")
        endControlFlow()
    }

    protected abstract fun CodeBlock.Builder.bufPutEntry(argumentsList: List<KSType>, bufVar: String, entrySource: String)

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        val elementTypes = getPrelude(sourceType, hasWrapper)
        beginControlFlow("repeat($bufVar.readInt())")
        bufGetEntry(elementTypes, bufVar)
        endControlFlow()
        addStatement("result")
        hasWrapper.doUnless { endControlFlowNoNl() }
    }

    protected abstract fun CodeBlock.Builder.bufGetEntry(argumentsList: List<KSType>, bufVar: String)

    private fun CodeBlock.Builder.getPrelude(sourceType: KSType, hasWrapper: Boolean): List<KSType> {
        hasWrapper.doUnless { beginControlFlow("run") }

        val arguments = sourceType.arguments.map { it.type!!.resolve() }

        addStatement("val result = %T()", getCollectionType(arguments).toTypeName())

        return arguments
    }

    protected abstract fun getCollectionType(arguments: List<KSType>): KSType
}
class MapAccessGenerator(
    private val mapDecl: KSClassDeclaration,
    private val nbtListType: KSType,
    private val resolver: Resolver,
    private val nested: AccessStatements
) : BaseCollectionAccessGenerator(nbtListType) {
    override val priority: Int
        get() = Int.MAX_VALUE

    override fun CodeBlock.Builder.nbtPutEntry(argumentsList: List<KSType>, targetNbt: String, entrySource: String) {
        nested.run {
            nbtPut(argumentsList[0], "$entrySource.key", "key", targetNbt, true)
            nbtPut(argumentsList[1], "$entrySource.value", "value", targetNbt, true)
        }
    }

    override fun CodeBlock.Builder.nbtGetEntry(argumentsList: List<KSType>, sourceNbt: String) {
        add("val key = ")
        nested.run { nbtGet(argumentsList[0], "key", sourceNbt, false) }
        add("\n")
        add("val value = ")
        nested.run { nbtGet(argumentsList[1], "value", sourceNbt, false) }
        add("\n")
        addStatement("result[key] = value")
    }

    override fun CodeBlock.Builder.bufPutEntry(argumentsList: List<KSType>, bufVar: String, entrySource: String) {
        nested.run {
            bufPut(argumentsList[0], "$entrySource.key", bufVar, true)
            bufPut(argumentsList[1], "$entrySource.value", bufVar, true)
        }
    }

    override fun CodeBlock.Builder.bufGetEntry(argumentsList: List<KSType>, bufVar: String) {
        add("val key = ")
        nested.run { bufGet(argumentsList[0], bufVar, false) }
        add("\n")
        add("val value = ")
        nested.run { bufGet(argumentsList[1], bufVar, false) }
        add("\n")
        addStatement("result[key] = value")
    }

    override fun getCollectionType(arguments: List<KSType>): KSType {
        return resolver.getKSTypeByName(
            "java.util.HashMap", arguments[0], arguments[1]
        )
    }

    override fun match(sourceType: KSType): Boolean {
        return sourceType.declaration == mapDecl
    }
}
class SerializableStructListAccessGenerator(
    private val listDecl: KSClassDeclaration,
    private val generatedStructs: List<KSType>,
    nbtListType: KSType,
    private val resolver: Resolver
) : BaseCollectionAccessGenerator(nbtListType) {
    override val priority: Int
        get() = 0

    override fun match(sourceType: KSType): Boolean {
        return sourceType.declaration == listDecl && generatedStructs.contains(sourceType.arguments[0].type!!.resolve())
    }

    override fun CodeBlock.Builder.nbtPutEntry(argumentsList: List<KSType>, targetNbt: String, entrySource: String) {
        addStatement("$entrySource.writeTo($targetNbt)")
    }

    override fun CodeBlock.Builder.nbtGetEntry(argumentsList: List<KSType>, sourceNbt: String) {
        add("val value = ")
        add("${argumentsList[0].declaration.simpleName.asString()}.readFrom($sourceNbt)")
        add("\n")
        addStatement("result.add(value)")
    }

    override fun CodeBlock.Builder.bufPutEntry(argumentsList: List<KSType>, bufVar: String, entrySource: String) {
        addStatement("$entrySource.writeTo($bufVar)")
    }

    override fun CodeBlock.Builder.bufGetEntry(argumentsList: List<KSType>, bufVar: String) {
        add("val value = ")
        add("${argumentsList[0].declaration.simpleName.asString()}.readFrom($bufVar)")
        add("\n")
        addStatement("result.add(value)")
    }

    override fun getCollectionType(arguments: List<KSType>): KSType {
        return resolver.getKSTypeByName("java.util.ArrayList", arguments[0])
    }
}
class GenericListAccessGenerator(
    private val listDecl: KSClassDeclaration,
    nbtListType: KSType,
    private val resolver: Resolver,
    private val nested: AccessStatements
) : BaseCollectionAccessGenerator(nbtListType) {
    override val priority: Int
        get() = Int.MAX_VALUE

    override fun match(sourceType: KSType): Boolean {
        return sourceType.declaration == listDecl
    }

    override fun CodeBlock.Builder.nbtPutEntry(argumentsList: List<KSType>, targetNbt: String, entrySource: String) {
        nested.run {
            nbtPut(argumentsList[0], entrySource, "data", targetNbt, true)
        }
    }

    override fun CodeBlock.Builder.nbtGetEntry(argumentsList: List<KSType>, sourceNbt: String) {
        add("val value = ")
        nested.run {
            nbtGet(argumentsList[0], "data", sourceNbt, true)
        }
        add("\n")
        addStatement("result.add(value)")
    }

    override fun CodeBlock.Builder.bufPutEntry(argumentsList: List<KSType>, bufVar: String, entrySource: String) {
        nested.run {
            bufPut(argumentsList[0], entrySource, bufVar, true)
        }
    }

    override fun CodeBlock.Builder.bufGetEntry(argumentsList: List<KSType>, bufVar: String) {
        add("val value = ")
        nested.run {
            bufGet(argumentsList[0], bufVar, false)
        }
        add("\n")
        addStatement("result.add(value)")
    }

    override fun getCollectionType(arguments: List<KSType>): KSType {
        return resolver.getKSTypeByName("java.util.ArrayList", arguments[0])
    }
}

class SerializationContext(private val resolver: Resolver, private val logger: KSPLogger) : AccessStatements {
    private val MutableListDecl = resolver.getClassDeclarationByName("kotlin.collections.MutableList")!!
    private val MutableMapDecl = resolver.getClassDeclarationByName("kotlin.collections.MutableMap")!!
    private val AnyType = resolver.getKSTypeByName("kotlin.Any")
    private val IntType = resolver.getKSTypeByName("kotlin.Int")
    private val StringType = resolver.getKSTypeByName("kotlin.String")
    private val ItemType = resolver.getKSTypeByName("net.minecraft.item.Item")
    private val BlockPosType = resolver.getKSTypeByName("net.minecraft.util.math.BlockPos")
    private val IntListType = resolver.getKSTypeByName("kotlin.collections.MutableList", IntType)
    private val StringListType = resolver.getKSTypeByName("kotlin.collections.MutableList", StringType)
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
            IntType, StringType, ItemType, BlockPosType, IntListType, StringListType -> true
            else -> false
        }
    }


    // region Statement Helper

    private val accessGenerators = listOf(
        NullWrapAccessGenerator(this),
        IntAccessGenerator(IntType),
        StringAccessGenerator(StringType),
        BlockPosAccessGenerator(BlockPosType),
        IntListAccessGenerator(IntListType),
        SerializableStructAccessGenerator(generatedClassTypes),
        GenericListAccessGenerator(MutableListDecl, NbtListType, resolver, this),
        MapAccessGenerator(MutableMapDecl, NbtListType, resolver, this),
        SerializableStructListAccessGenerator(MutableListDecl, generatedClassTypes, NbtListType, resolver),
        ItemAccessGenerator(ItemType)
    ).sortedBy { it.priority }


    fun CodeBlock.Builder.nbtWriteProperty(propDecl: KSPropertyDeclaration, thisVar: String, nbtVar: String) {
        val propType = propDecl.type.resolve()
        nbtPut(propType, "${thisVar}.${propDecl.declShortName}", propDecl.declShortName, nbtVar, false)
    }


    fun CodeBlock.Builder.nbtReadProperty(propDecl: KSPropertyDeclaration, compound: String) {
        val propType = propDecl.type.resolve()
        nbtGet(propType, propDecl.declShortName, compound, false)
    }


    fun CodeBlock.Builder.packetReadStmt(propDecl: KSPropertyDeclaration, nbtVar: String) {
        val propType = propDecl.type.resolve()
        bufGet(propType, nbtVar, false)
    }

    fun CodeBlock.Builder.packetPutStmt(propDecl: KSPropertyDeclaration, propSource: String, nbtVar: String) {
        val propType = propDecl.type.resolve()
        bufPut(propType, "${propSource}.${propDecl.declShortName}", nbtVar, false)
    }

    override fun CodeBlock.Builder.nbtPut(
        sourceType: KSType,
        source: String,
        nbtKey: String,
        compound: String,
        hasWrapper: Boolean
    ) {
        accessGenerators.first { it.match(sourceType) }.run {
            nbtPut(sourceType, source, nbtKey, compound, hasWrapper)
        }
    }

    override fun CodeBlock.Builder.nbtGet(sourceType: KSType, nbtKey: String, compound: String, hasWrapper: Boolean) {
        accessGenerators.first { it.match(sourceType) }.run {
            nbtGet(sourceType, nbtKey, compound, hasWrapper)
        }
    }

    override fun CodeBlock.Builder.bufPut(sourceType: KSType, source: String, bufVar: String, hasWrapper: Boolean) {
        accessGenerators.first { it.match(sourceType) }.run {
            bufPut(sourceType, source, bufVar, hasWrapper)
        }
    }

    override fun CodeBlock.Builder.bufGet(sourceType: KSType, bufVar: String, hasWrapper: Boolean) {
        accessGenerators.first { it.match(sourceType) }.run {
            bufGet(sourceType, bufVar, hasWrapper)
        }
    }
    // endregion
}