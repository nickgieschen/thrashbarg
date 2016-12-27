package thrashbarg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.util.BeanUtil
import kotlin.reflect.*

class Thrashbarg(val objectMapper: ObjectMapper, useKotlinModule: Boolean = true) {

    constructor() : this(ObjectMapper())

    init {
        if (useKotlinModule) {
            objectMapper.registerKotlinModule()
        }

        val aiPair = AnnotationIntrospectorPair(ThrashbargAnnotationIntrospector(this), objectMapper.serializationConfig.annotationIntrospector)
        objectMapper.setConfig(objectMapper.serializationConfig.with(aiPair))
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    val mappings = mutableMapOf<KClass<*>, Mapping<*, *>>()

    fun getAssociation(clazz: KClass<*>, sourceFieldName: String): String? {
        val mapping = mappings.get(clazz)
        val association = mapping?.associations?.get(sourceFieldName)
        return association?.desinationFieldName
    }

    fun addMapping(mapping: Mapping<*, *>) {
        mappings.put(mapping.source, mapping)
    }

    fun <T : Any> map(source: Any, dest: KClass<T>): T {
        return objectMapper.convertValue(source, dest.java)
    }
}


object MyBeanUtil : BeanUtil() {
    fun getStandartdPropertyName(basename: String, offset: Int): String {
        return stdManglePropertyName(basename, offset)
    }
}

class Mapping<S : Any, D : Any>(val source: KClass<S>, val dest: KClass<D>) {

    val associations = mutableMapOf<String, Association<S, D, *>>()

    class Association<L : Any, R : Any, T>(val sourceFieldName: String, val mapping: Mapping<L, R>) {

        var desinationFieldName: String? = null

        fun with(dest: KProperty1<R, T>): Mapping<L, R> {
            desinationFieldName = dest.name
            return mapping
        }

        fun with(dest: KFunction1<R, T>): Mapping<L, R> {
            desinationFieldName = dest.backingFieldName
            return mapping
        }

//        fun <U> with(dest: KProperty1<R, U>, transformer: (T) -> U): Mapping<L, R> {
//            backingFieldName = dest.name
//            return mapping
//        }
//
//        fun <U> with(dest: KFunction1<R, U>, transformer: (T) -> U): Mapping<L, R> {
//            backingFieldName = dest.name
//            return mapping
//        }
    }

    fun <T> associate(source: KProperty1<S, T>): Association<S, D, T> {
        val association = Association<S, D, T>(source.name, this)
        associations.put(source.name, association)
        return association
    }

    fun <T> associate(source: KFunction1<S, T>): Association<S, D, T> {
        val association = Association<S, D, T>(source.backingFieldName, this)
        associations.put(source.backingFieldName, association)
        return association
    }
}

class ThrashbargAnnotationIntrospector(private val thrashbarg: Thrashbarg) : JacksonAnnotationIntrospector() {

    override fun findNameForSerialization(a: Annotated?): PropertyName? {

        // Not sure under what circumstances these casts will fail. let's wait and see and fix as necessary.
        val clazz = ((a as AnnotatedMember).typeContext as AnnotatedClass).annotated
        val destinationFieldName = thrashbarg.getAssociation(clazz.kotlin, a.name)

        if (destinationFieldName != null) {
            return PropertyName.construct(destinationFieldName)
        }
        return null
    }
}

val KFunction1<*, *>.isGetter: Boolean
    get() {
        return this.parameters.count() == 1 && this.returnType != Unit.javaClass && name.startsWith("get")
    }

val KFunction1<*, *>.isSetter: Boolean
    get() {
        return this.parameters.count() == 2 && this.returnType == Unit.javaClass && name.startsWith("set")
    }

val KFunction1<*, *>.backingFieldName: String
    get() {
        if (isGetter || isSetter) {
            return MyBeanUtil.getStandartdPropertyName(this.name, 3)
        }
        return this.name
    }
