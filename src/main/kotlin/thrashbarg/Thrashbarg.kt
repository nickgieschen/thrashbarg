package thrashbarg

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.introspect.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier
import com.fasterxml.jackson.databind.util.BeanUtil
import kotlin.reflect.*

class Thrashbarg(val objectMapper: ObjectMapper, useKotlinModule: Boolean = true) {

    constructor() : this(ObjectMapper())

    init {
        if (useKotlinModule) {
            objectMapper.registerKotlinModule()
        }

        val module = SimpleModule("ThrashbardModule")
        module.setSerializerModifier(ThrashbargBeanSerializerModifier(this))

        val aiPair = AnnotationIntrospectorPair(ThrashbargAnnotationIntrospector(this), objectMapper.serializationConfig.annotationIntrospector)
        objectMapper.setConfig(objectMapper.serializationConfig.with(aiPair))
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.registerModule(module)
    }

    val mappings = mutableMapOf<Class<*>, Mapping<out Any, out Any>>()

    fun getAssociation(clazz: Class<*>, sourceFieldName: String): String? {
        val mapping = mappings.get(clazz)
        val association = mapping?.associations?.get(sourceFieldName)
        return association?.desinationFieldName
    }

    fun getAssociationsWithTransformer(clazz: Class<*>): Map<String, Association<out Any, out Any, out Any?>> {
        val mapping = mappings.get(clazz)
        return mapping?.associations?.filter { a -> a.value.transformer != null } ?: mapOf()
    }

    fun addMapping(mapping: Mapping<out Any, out Any>) {
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

class Mapping<S : Any, D : Any>(source: KClass<S>, val dest: KClass<D>) {

    val source = source.java

    val associations = mutableMapOf<String, Association<S, D, out Any?>>()

    fun <T:Any?> associate(source: KProperty1<S, T>): Association<S, D, T> {
        val association = Association<S, D, T>(source.name, this)
        associations.put(source.name, association)
        return association
    }

    fun <T:Any?> associate(source: KFunction1<S, T>): Association<S, D, T> {
        val association = Association<S, D, T>(source.backingFieldName, this)
        associations.put(source.backingFieldName, association)
        return association
    }

    fun <T:Any?> associateSetter(source: KFunction2<S, T, Unit>): Association<S, D, T> {
        val association = Association<S, D, T>(source.backingFieldName, this)
        associations.put(source.backingFieldName, association)
        return association
    }
}

class Association<S : Any, D: Any, ST: Any?>(val sourceFieldName: String, val mapping: Mapping<S, D>) {

    var desinationFieldName: String? = null
    var transformer: ((Any?, Any?) -> Any?)? = null

    fun withProperty(dest: KProperty1<D, ST>): Mapping<S, D> {
        desinationFieldName = dest.name
        return mapping
    }

    @Suppress("UNCHECKED_CAST")
    fun <DT> withProperty(dest: KProperty1<D, DT>, transformer: (ST) -> DT): Mapping<S, D> {
        desinationFieldName = dest.name
        this.transformer = wrapLittleTransformer(transformer)
        return mapping
    }

    @Suppress("UNCHECKED_CAST")
    fun <DT> withProperty(dest: KProperty1<D, DT>, transformer: (ST, S) -> DT): Mapping<S, D> {
        desinationFieldName = dest.name
        this.transformer = transformer as (Any?, Any?) -> Any?
        return mapping
    }

    fun withGetter(dest: KFunction1<D, ST>): Mapping<S, D> {
        desinationFieldName = dest.backingFieldName
        return mapping
    }

    @Suppress("UNCHECKED_CAST")
    fun <DT> withGetter(dest: KFunction1<D, DT>, transformer: (ST) -> DT): Mapping<S, D> {
        desinationFieldName = dest.name
        this.transformer = wrapLittleTransformer(transformer)
        return mapping
    }

    fun <DT> withSetter(dest: KFunction2<D, DT, Unit>): Mapping<S, D> {
        desinationFieldName = dest.backingFieldName
        return mapping
    }

    @Suppress("UNCHECKED_CAST")
    fun <O:Any?, P:Any?> wrapLittleTransformer(transformer: (O) -> P) = { a: Any?, b: Any? -> transformer.invoke(a as O) }
}

class ThrashbargAnnotationIntrospector(private val thrashbarg: Thrashbarg) : JacksonAnnotationIntrospector() {

    override fun findNameForSerialization(a: Annotated?): PropertyName? {

        // Not sure under what circumstances these casts will fail. let's wait and see and fix as necessary.
        val clazz = ((a as AnnotatedMember).typeContext as AnnotatedClass).annotated
        val destinationFieldName = thrashbarg.getAssociation(clazz.kotlin.java, a.name)

        if (destinationFieldName != null) {
            return PropertyName.construct(destinationFieldName)
        }
        return null
    }
}

val KFunction<*>.isGetter: Boolean
    get() {
        return this.parameters.count() == 1 && name.startsWith("get")
    }

val KFunction<*>.isSetter: Boolean
    get() {
        return this.parameters.count() == 2 && name.startsWith("set")
    }

val KFunction<*>.backingFieldName: String
    get() {
        return if (isGetter || isSetter) {
            MyBeanUtil.getStandartdPropertyName(this.name, 3)
        }
        else this.name
    }

class ThrashbargSerializer(val transformer: (Any?, Any?) -> Any?) : JsonSerializer<Any?>() {
    override fun serialize(value: Any?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        val parent = serializers?.generator?.currentValue
        val newValue = transformer(value, parent)
        gen?.writeObject(newValue)
    }
}

class ThrashbargBeanSerializerModifier(val thrashbarg: Thrashbarg) : BeanSerializerModifier() {
    override fun changeProperties(config: SerializationConfig?, beanDesc: BeanDescription?, beanProperties: MutableList<BeanPropertyWriter>?): MutableList<BeanPropertyWriter> {
        if (beanProperties != null) {
            for (beanPropertyWriter in beanProperties.iterator()) {
                // TODO does this get cached? If not, what's performance like?
                val associations = thrashbarg.getAssociationsWithTransformer(beanDesc?.beanClass!!)
                for ((key, value) in associations.iterator()){
                    if (value.desinationFieldName == beanPropertyWriter.name){
                        if (value.transformer != null) {
                            beanPropertyWriter.assignSerializer(ThrashbargSerializer(value.transformer!!))
                        }
                    }
                }
            }
        }
        return super.changeProperties(config, beanDesc, beanProperties)
    }
}