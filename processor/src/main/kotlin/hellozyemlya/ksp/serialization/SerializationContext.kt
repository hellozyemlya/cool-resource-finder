package hellozyemlya.ksp.serialization

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
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

class SerializationContext(private val resolver: Resolver) {
    private val supportedTypes: MutableSet<KSType> = HashSet()
    public val targetTypes: MutableList<KSType> = ArrayList()

    init {
        supportedTypes.add(resolver.getClassDeclarationByName("kotlin.Int")!!.asType(emptyList()))
    }

    fun addTargetType(type: KSType) {
        supportedTypes.add(type)
        targetTypes.add(type)
    }


    fun printInfo(outputStream: OutputStream) {
        supportedTypes.forEach {
            outputStream += "// supports ${it.declaration.qualifiedName!!.asString()} \n"
        }
    }
}