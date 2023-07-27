package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.ksp.toTypeName
import java.io.OutputStream
import kotlin.reflect.KType

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

val KSType.declShortName: String
    get() = this.declaration.simpleName.asString()

val KSType.allProps: List<KSPropertyDeclaration>
    get() = (this.declaration as KSClassDeclaration).getAllProperties().toList()

val KSPropertyDeclaration.declShortName: String
    get() = this.simpleName.asString()

val KSType.ctorParameters: List<ParameterSpec>
    get() = this.allProps.map { propertyDecl ->
        ParameterSpec
            .builder(propertyDecl.declShortName, propertyDecl.type.resolve().toTypeName())
            .build()
    }.toList()

val KSType.ctorCallArgs: String
    get() = this.allProps.joinToString(", ") { it.declShortName }

fun Resolver.getKSTypeByName(name: String): KSType {
    return this.getClassDeclarationByName(name)!!.asType(emptyList())
}

fun Resolver.getKSTypeByName(name: String, vararg argTypes: KSType): KSType {
    val mainTypeDecl = this.getClassDeclarationByName(name)!!
    return mainTypeDecl.asType(argTypes.map {
        this.getTypeArgument(this.createKSTypeReferenceFromKSType(it), Variance.INVARIANT)
    })
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

    private val generatedClassDecls: MutableList<KSClassDeclaration> = ArrayList()
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
        val candidatesTypes = candidates.map { it.asType(emptyList()) }.toList()

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
                "super types not supported in @McSerialize classes, ${superTypeRef.resolve().declShortName}",
                superTypeRef
            )
        }

        candidate.getAllProperties().forEach { propDecl ->
            val propType = propDecl.type.resolve()
            if (!isTypeSupported(propType, candidatesTypes)) {
                isValid = false
                logger.error("property has unsupported type", propDecl)
            }
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

        supportedTypes.forEach {
            outputStream += "// supports ${it.declaration.qualifiedName!!.asString()} \n"
            it.arguments.forEach { arg ->

                outputStream += "//     arg ${arg} \n"
            }
        }

        targetTypes.forEach {
            outputStream += "// target ${it.declaration.qualifiedName!!.asString()} \n"
            it.allProps.forEach { prop ->
                outputStream += "//     prop ${prop.declShortName} \n"
                outputStream += "//         type ${prop.type} \n"

                val propResolvedType = prop.type.resolve()
                outputStream += "//             !!! ${propResolvedType.declaration} \n"

                propResolvedType.declaration.typeParameters.forEach { typeDeclArg ->
                    outputStream += "//             tda ${typeDeclArg} \n"
                }
                prop.type.resolve().arguments.forEach { arg ->

                    outputStream += "//             arg ${arg.type} \n"
                }
            }
        }
    }


    // region Statement Helper

//
//        MC_BLOCK_POS_TYPE_NAME -> {
//            addStatement("$nbtVar.putIntArray(\"${propDecl.declShortName}\", intArrayOf(this.${propDecl.declShortName}.x, this.${propDecl.declShortName}.y, this.${propDecl.declShortName}.z))")
//        }
//
//        else -> {
//            if (ctx.targetTypes.contains(propType)) {
//                addStatement("val ${propDecl.declShortName}Compound = NbtCompound()")
//                addStatement("$nbtVar.put(\"${propDecl.declShortName}\", ${propDecl.declShortName}Compound)")
//                addStatement("this.${propDecl.declShortName}.writeTo(${propDecl.declShortName}Compound)")
//            } else {
//                addStatement("// don't support '${propType.toTypeName()}'")
//            }
//        }
//    }


    private val typeToNbtPut = hashMapOf<KSType, CodeBlock.Builder.(String, String, String) -> Unit>(
        IntType to { source, nbtKey, nbtVar ->
            addStatement("$nbtVar.putInt(\"$nbtKey\", $source)")
        },
        StringType to { source, nbtKey, nbtVar ->
            addStatement("$nbtVar.putString(\"$nbtKey\", $source)")
        },
        ItemType to { source, nbtKey, nbtVar ->
            addStatement(
                "$nbtVar.putString(\"$nbtKey\", %T.ITEM.getId($source).toString())",
                MC_REGISTRIES_TYPE_NAME
            )
        },
        BlockPosType to { source, nbtKey, nbtVar ->
            addStatement("$nbtVar.putIntArray(\"$nbtKey\", intArrayOf($source.x, $source.y, $source.z))")
        },
        IntList to { source, nbtKey, nbtVar ->
            addStatement("$nbtVar.putIntArray(\"$nbtKey\", $source)")
        }
    )

    fun CodeBlock.Builder.nbtPutStmt(propType: KSType, nbtKey: String, source: String, nbtVar: String) {
        if (typeToNbtPut.containsKey(propType)) {
            typeToNbtPut[propType]!!(source, nbtKey, nbtVar)
        } else if (generatedClassTypes.contains(propType)) {
            beginControlFlow("run")
            addStatement("val childCompound = NbtCompound()")
            addStatement("$nbtVar.put(\"$nbtKey\", childCompound)")
            addStatement("$source.writeTo(childCompound)")
            endControlFlow()
        } else if (propType.declaration == MutableMapDecl) {
            beginControlFlow("run")
            addStatement("val entries = %T()", NbtListType.toTypeName())
            addStatement("$nbtVar.put(\"$nbtKey\", entries)")
            beginControlFlow("for(entry in $source)")
            addStatement("val entryNbt = NbtCompound()")
            nbtPutStmt(propType.arguments[0].type!!.resolve(), "key", "entry.key", "entryNbt")
            nbtPutStmt(propType.arguments[1].type!!.resolve(), "value", "entry.value", "entryNbt")
            endControlFlow()
            endControlFlow()
        }
    }

    fun CodeBlock.Builder.nbtPutStmt(propDecl: KSPropertyDeclaration, propSource: String, nbtVar: String) {
        val propType = propDecl.type.resolve()
        nbtPutStmt(propType, propDecl.declShortName, "${propSource}.${propDecl.declShortName}", nbtVar)
    }

    private val typeToNbtRead = hashMapOf<KSType, CodeBlock.Builder.(nbtKey: String, nbtVar: String) -> Unit>(
        IntType to { nbtKey, nbtVar ->
            add("$nbtVar.getInt(\"$nbtKey\")")
        },
        StringType to { nbtKey, nbtVar ->
            add("$nbtVar.getString(\"$nbtKey\")")
        },
        ItemType to { nbtKey, nbtVar ->
            add(
                "%T.ITEM.get(%T.tryParse($nbtVar.getString(\"$nbtKey\")))",
                MC_REGISTRIES_TYPE_NAME,
                MC_IDENTIFIER_TYPE_NAME
            )
        },
        BlockPosType to { nbtKey, nbtVar ->
            add("$nbtVar.getIntArray(\"$nbtKey\").run { BlockPos(this[0], this[1], this[2]) }")
        },
        IntList to { nbtKey, nbtVar ->
            add("$nbtVar.getIntArray(\"$nbtKey\").toMutableList()")
        }
    )


    fun CodeBlock.Builder.nbtReadStmt(propType: KSType, nbtKey: String, nbtVar: String) {
        if (typeToNbtRead.containsKey(propType)) {
            typeToNbtRead[propType]!!(nbtKey, nbtVar)
        } else if (generatedClassTypes.contains(propType)) {
            add("read${propType.declShortName}From($nbtVar.getCompound(\"$nbtKey\"))")
        } else if (propType.declaration == MutableMapDecl) {
            beginControlFlow("run")
            val keyType = propType.arguments[0].type!!.resolve()
            val valueType = propType.arguments[1].type!!.resolve()
            addStatement(
                "val result = %T()", resolver.getKSTypeByName(
                    "java.util.HashMap", keyType, valueType
                ).toTypeName()
            )
            addStatement("val list = $nbtVar.getList(\"$nbtKey\", %T.COMPOUND_TYPE.toInt())", NBT_ELEMENT_TYPE_NAME)
            beginControlFlow("for(entry in list.map { it as %T })", NBT_COMPOUND_TYPE_NAME)
            add("val key = ")
            nbtReadStmt(keyType, "key", "entry")
            add("\n")
            add("val value = ")
            nbtReadStmt(valueType, "value", "entry")
            add("\n")
            addStatement("result[key] = value")
            endControlFlow()
            addStatement("result")
//            endControlFlow()
            unindent()
            add("}")
        }
    }

    fun CodeBlock.Builder.nbtReadStmt(propDecl: KSPropertyDeclaration, nbtVar: String) {
        val propType = propDecl.type.resolve()
        nbtReadStmt(propType, propDecl.declShortName, nbtVar)
    }
    // endregion
}