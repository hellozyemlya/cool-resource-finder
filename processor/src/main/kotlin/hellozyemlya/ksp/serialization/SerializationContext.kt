package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import java.io.OutputStream

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
class SerializationContext(private val resolver: Resolver, private val logger: KSPLogger) {
    private val MutableListDecl = resolver.getClassDeclarationByName("kotlin.collections.MutableList")!!
    private val MutableMapDecl = resolver.getClassDeclarationByName("kotlin.collections.MutableMap")!!
    private val AnyType = resolver.getKSTypeByName("kotlin.Any")
    private val IntType = resolver.getKSTypeByName("kotlin.Int")
    private val StringType = resolver.getKSTypeByName("kotlin.String")
    private val ItemType = resolver.getKSTypeByName("net.minecraft.item.Item")
    private val BlockPosType = resolver.getKSTypeByName("net.minecraft.util.math.BlockPos")

    private val generatedClassDecls: MutableList<KSClassDeclaration> = ArrayList()

    fun addClassesForGeneration(candidates: Sequence<KSClassDeclaration>): Boolean {
        val isValid = candidates.all { validateGenCandidateDecl(it, candidates) }
        if (isValid)
            generatedClassDecls.addAll(candidates)
        return isValid
    }

    private fun validateGenCandidateDecl(candidate: KSClassDeclaration, candidates: Sequence<KSClassDeclaration>): Boolean {
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
                    if((parentDecl as KSClassDeclaration).asType(emptyList()) == AnyType) {
                        return@forEach
                    }
                }
            }
            isValid = false
            logger.error("functions not supported in @McSerialize classes, ${funcDecl.simpleName.asString()}", funcDecl)
        }

        candidate.superTypes.forEach { superTypeRef ->
            // ignore Any(Object)
            if(superTypeRef.resolve() == AnyType) {
                return@forEach
            }

            isValid = false
            logger.error("super types not supported in @McSerialize classes, ${superTypeRef.resolve().declShortName}", superTypeRef)
        }

        candidate.getAllProperties().forEach { propDecl ->
            val propType = propDecl.type.resolve()
            if(!isTypeSupported(propType, candidatesTypes)) {
                isValid = false
                logger.error("property has unsupported type", propDecl)
            }
        }

        return isValid
    }

    private fun isTypeSupported(type: KSType, candidates: List<KSType>): Boolean {
        if(!isBuiltInType(type)) {
            if(type.declaration == MutableListDecl) {
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
        return when(type) {
            IntType, StringType, ItemType, BlockPosType -> true
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
}