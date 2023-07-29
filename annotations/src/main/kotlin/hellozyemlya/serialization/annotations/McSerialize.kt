package hellozyemlya.serialization.annotations

@Target(AnnotationTarget.CLASS)
public annotation class McSerialize {
}

@Target(AnnotationTarget.PROPERTY)
public annotation class PersistentStateArg {
}


@Target(AnnotationTarget.PROPERTY)
public annotation class NbtIgnore {
}

@Target(AnnotationTarget.PROPERTY)
public annotation class PacketIgnore {
}