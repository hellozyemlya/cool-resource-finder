package hellozyemlya.serialization.annotations

@Target(AnnotationTarget.CLASS)
public annotation class McSerialize {
}

@Target(AnnotationTarget.PROPERTY)
public annotation class PersistentStateArg {
}