package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
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

fun KSClassDeclaration.singleCtor(): KSFunctionDeclaration {
    return this.declarations.filterIsInstance<KSFunctionDeclaration>().filter { it.isConstructor() }.single()
}

val KSClassDeclaration.companionObject: KSClassDeclaration
    get() = this.declarations.filterIsInstance<KSClassDeclaration>().first { it.isCompanionObject }

val KSPropertyDeclaration.declShortName: String
    get() = this.simpleName.asString()


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


class SerializableStructAccessGenerator(private val structs: List<ClassGenerationInfo>) : AccessGenerator {
    override val priority: Int
        get() = Int.MAX_VALUE

    override fun match(sourceType: KSType): Boolean {
        return structs.singleOrNull { it.baseType == sourceType } != null
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

    protected abstract fun CodeBlock.Builder.nbtPutEntry(
        argumentsList: List<KSType>,
        targetNbt: String,
        entrySource: String
    )

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

    protected abstract fun CodeBlock.Builder.bufPutEntry(
        argumentsList: List<KSType>,
        bufVar: String,
        entrySource: String
    )

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
    private val generatedStructs: List<ClassGenerationInfo>,
    nbtListType: KSType,
    private val resolver: Resolver
) : BaseCollectionAccessGenerator(nbtListType) {
    override val priority: Int
        get() = 0

    override fun match(sourceType: KSType): Boolean {
        if (sourceType.declaration == listDecl) {
            val argType = sourceType.arguments[0].type!!.resolve()
            return generatedStructs.singleOrNull { it.baseType == argType } != null
        }

        return false
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

fun KSClassDeclaration.getPropDeclFromCtorArg(arg: KSValueParameter): KSPropertyDeclaration {
    val parent = arg.parent
    if ((arg.isVal || arg.isVar) && parent is KSFunctionDeclaration && parent.isConstructor()) {
        val funcParent = parent.parent
        if(funcParent is KSClassDeclaration) {
            return funcParent.getAllProperties().single { it.simpleName == arg.name }
        }
    }

    throw IllegalArgumentException("not a KSProperty parameter")
}

class GenPropertyInfo(public val decl: KSPropertyDeclaration, val ctorParam: Pair<Int, KSValueParameter>? = null) {
    val isNbtIgnore: Boolean by lazy {
        decl.annotations.firstOrNull { it.shortName.asString() == "NbtIgnore" } != null
    }
    val isPacketIgnore: Boolean by lazy {
        decl.annotations.firstOrNull { it.shortName.asString() == "PacketIgnore" } != null
    }

    val isPersistentSateArg: Boolean by lazy {
        decl.annotations.firstOrNull { it.shortName.asString() == "PersistentStateArg" } != null
    }

    val isMutable: Boolean = decl.isMutable

    val type: KSType by lazy {
        decl.type.resolve()
    }

    val typeName: TypeName by lazy {
        type.toTypeName()
    }

    val name: String by lazy {
        decl.simpleName.asString()
    }

    fun getParameterSpec(rename: String? = null): ParameterSpec {
        return ParameterSpec(rename ?: name, typeName)
    }
}

class ClassGenerationInfo(public val classDecl: KSClassDeclaration, val persistentStateType: KSType) {
    val superClsCtorProps: List<GenPropertyInfo> by lazy {
        if (classDecl.classKind == ClassKind.CLASS && classDecl.isAbstract()) {
            classDecl.singleCtor().parameters.mapIndexed { idx, param ->
                GenPropertyInfo(classDecl.getPropDeclFromCtorArg(param), idx to param)
            }
        } else {
            emptyList()
        }
    }

    val propsToImplement: List<GenPropertyInfo> by lazy {
        classDecl.getAllProperties().filter { it.isAbstract() && it.isPublic() }.map { GenPropertyInfo(it) }.toList()
    }

    val allProperties: List<GenPropertyInfo> by lazy {
        propsToImplement + superClsCtorProps
    }

    private val implCtorParamSpecToPropInfo = mutableMapOf<ParameterSpec, GenPropertyInfo>()
    private val superCtorParamSpecs = mutableListOf<ParameterSpec>()

    /**
     * List of arguments for implementation ctor.
     */
    val implCtorArgs: List<ParameterSpec> by lazy {
        propsToImplement.map { propInfo ->
            val paramSpec = ParameterSpec(propInfo.name, propInfo.typeName)
            implCtorParamSpecToPropInfo[paramSpec] = propInfo
            paramSpec
        } + superCtorArgs
    }

    val superCtorArgs: List<ParameterSpec> by lazy {
        superClsCtorProps.map { propInfo ->
            val paramSpec = ParameterSpec(propInfo.name, propInfo.typeName)
            implCtorParamSpecToPropInfo[paramSpec] = propInfo
            superCtorParamSpecs.add(paramSpec)
            paramSpec
        }
    }

    public val isPersistentState: Boolean by lazy {
        classDecl.superTypes.firstOrNull { it.resolve() == persistentStateType } != null
    }

    public val baseType: KSType by lazy {
        classDecl.asType(emptyList())
    }

    public val companionTypeName: TypeName by lazy {
        classDecl.companionObject.toTypeName()
    }

    public val baseTypeName: TypeName by lazy {
        classDecl.toTypeName()
    }

    public val baseTypeNameStr: String by lazy {
        classDecl.simpleName.asString()
    }

    val implClassNameStr: String by lazy {
        "${baseTypeNameStr}Impl"
    }

//    val propsToImplement: List<KSPropertyDeclaration> by lazy {
//        classDecl.getAllProperties().filter { it.isAbstract() && it.isPublic() }.toList()
//    }

//    val implCtorCallArgs: String by lazy {
//        this.ctorArgs.joinToString(", ") { it.name }
//    }

//    val baseCtorProps: List<KSPropertyDeclaration> by lazy {
//        if (classDecl.classKind == ClassKind.CLASS && classDecl.isAbstract()) {
//            classDecl.singleCtor().parameters.map { param ->
//                classDecl.getAllProperties().single { it.simpleName == param.name }
//            }
//        } else {
//            emptyList()
//        }
//    }

//    val propertiesToWrite: List<KSPropertyDeclaration> by lazy {
//        baseCtorProps + propsToImplement
//    }

    val callSuperCtor: Boolean = classDecl.classKind == ClassKind.CLASS && classDecl.isAbstract()

    public val persistentStateArgs: List<ParameterSpec> by lazy {
        implCtorArgs.filter {
            val propInfo = implCtorParamSpecToPropInfo[it]!!
            propInfo.isPersistentSateArg
        }
    }

//    val superCtorArgs: List<ParameterSpec> by lazy {
//        if (classDecl.classKind == ClassKind.CLASS && classDecl.isAbstract()) {
//            classDecl.singleCtor().parameters.map {
//                ParameterSpec
//                    .builder(it.name!!.asString(), it.type.toTypeName())
//                    .build()
//            }
//        } else {
//            emptyList()
//        }
//    }

//    val ctorArgs: List<ParameterSpec> by lazy {
//        val abstractCtorClassArgs = if (classDecl.classKind == ClassKind.CLASS && classDecl.isAbstract()) {
//            classDecl.singleCtor().parameters.map {
//                ParameterSpec
//                    .builder(it.name!!.asString(), it.type.toTypeName())
//                    .build()
//            }
//        } else {
//            emptyList()
//        }
//
//        abstractCtorClassArgs + propsToImplement.map {
//            ParameterSpec
//                .builder(it.declShortName, it.type.toTypeName())
//                .build()
//        }
//    }
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
    private val NbtCompondType = resolver.getKSTypeByName("net.minecraft.nbt.NbtCompound")
    private val PersistentStateType = resolver.getKSTypeByName("net.minecraft.world.PersistentState")

    public val generationInfos: MutableList<ClassGenerationInfo> = ArrayList()


    fun addClassesForGeneration(candidates: Sequence<KSClassDeclaration>): Boolean {
        val nonNullableCandidates = candidates.map { it.asType(emptyList()).makeNotNullable() }.toList()
        val isValid = candidates.all { validateGenCandidateDecl(it, nonNullableCandidates) }

        if (isValid) {
            generationInfos.addAll(candidates.map { ClassGenerationInfo(it, PersistentStateType) })
        }

        return isValid
    }

    private fun validateGenCandidateDecl(
        candidate: KSClassDeclaration,
        nonNullableCandidates: List<KSType>
    ): Boolean {
        if (candidate.classKind == ClassKind.INTERFACE) {
            return validateGenCandidateInterfaceDecl(candidate, nonNullableCandidates)
        } else if (candidate.classKind == ClassKind.CLASS) {
            if (candidate.isAbstract()) {
                return validateGenCandidateAbstractDecl(candidate, nonNullableCandidates)
            }
        }

        logger.error("only interface or abstract classes supported", candidate)

        return false
    }

    private fun validateGenCandidateAbstractDecl(
        candidate: KSClassDeclaration,
        nonNullableCandidateTypes: List<KSType>
    ): Boolean {
        var isValid = true

        // validate all abstract properties
        candidate.getAllProperties().filter { it.isAbstract() }.forEach { propDecl ->
            if (!propDecl.isPublic()) {
                isValid = false
                logger.error("only public abstract properties supported", propDecl)
            }

            val propType = propDecl.type.resolve().makeNotNullable()
            if (!isTypeSupported(propType, nonNullableCandidateTypes)) {
                isValid = false
                logger.error("property has unsupported type", propDecl)
            }
        }

        // validate companion object
        if (candidate.declarations.filterIsInstance<KSClassDeclaration>()
                .firstOrNull { it.isCompanionObject } == null
        ) {
            logger.error("serializable requires companion object to assign creation functions", candidate)
            isValid = false
        }

        // validate ctor
        val constructors =
            candidate.declarations.filterIsInstance<KSFunctionDeclaration>().filter { it.isConstructor() }

        if (constructors.count() != 1) {
            logger.error("only one constructor must exist", candidate)
            isValid = false
        } else {
            val ctor = constructors.first()
            ctor.parameters.forEach { ctorParam ->
                if (ctorParam.isVal || ctorParam.isVal) {
                    if (!candidate.getAllProperties().first { it.simpleName == ctorParam.name }.isPublic()) {
                        logger.error("ctor parameters must be public properties", ctorParam)
                        isValid = false
                    }
                } else {
                    logger.error("ctor parameters must be properties", candidate)
                    isValid = false
                }
            }
        }
        // validate PersistentState inheritance
        candidate.superTypes.forEach { baseTypeRef ->
            if (baseTypeRef.resolve() == PersistentStateType) {
                val writeNbtMethod = candidate.getAllFunctions().single {
                    it.simpleName.asString() == "writeNbt"
                            && it.returnType!!.resolve().declaration.qualifiedName == NbtCompondType.declaration.qualifiedName
                            && it.parameters.singleOrNull()?.type?.resolve()?.declaration?.qualifiedName == NbtCompondType.declaration.qualifiedName
                }
                if (!writeNbtMethod.isAbstract) {
                    logger.error("writeNbt must not be implemented", writeNbtMethod)
                    isValid = false
                }
            }
        }
        return isValid
    }

    private fun validateGenCandidateInterfaceDecl(
        candidate: KSClassDeclaration,
        nonNullableCandidateTypes: List<KSType>
    ): Boolean {

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
            if (!isTypeSupported(propType, nonNullableCandidateTypes)) {
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
    fun CodeBlock.Builder.defaultValue(type: KSType) {
        when {
            type.isMarkedNullable ->
                add("null")

            type == IntType ->
                add("0")

            type == StringType ->
                add("\"\"")

            type == BlockPosType ->
                add("%T.ORIGIN", MC_BLOCK_POS_TYPE_NAME)

            type == ItemType ->
                add("%T.AIR", MC_ITEMS_TYPE_NAME)

            type.declaration == MutableMapDecl ->
                add("mutableMapOf()")

            type.declaration == MutableListDecl ->
                add("mutableListOf()")

            generationInfos.firstOrNull { it.baseType == type } != null ->
                add("%T.createDefault()", generationInfos.first { it.baseType == type }.companionTypeName)
        }
    }

    private val accessGenerators = listOf(
        NullWrapAccessGenerator(this),
        IntAccessGenerator(IntType),
        StringAccessGenerator(StringType),
        BlockPosAccessGenerator(BlockPosType),
        IntListAccessGenerator(IntListType),
        SerializableStructAccessGenerator(generationInfos),
        GenericListAccessGenerator(MutableListDecl, NbtListType, resolver, this),
        MapAccessGenerator(MutableMapDecl, NbtListType, resolver, this),
        SerializableStructListAccessGenerator(MutableListDecl, generationInfos, NbtListType, resolver),
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