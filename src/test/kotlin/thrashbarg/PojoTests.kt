package thrashbarg

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.SubjectSpek
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.describe
import kotlin.reflect.KFunction

class PojoSpec : SubjectSpek<Thrashbarg>({

    subject { Thrashbarg() }

    describe("Thrashbarg mapping pojos") {

        it("should map associated properties") {
            val m = Mapping(PojoSource::class, PojoDest::class)
            m.associate(PojoSource::getUniqueNameOnSource).withGetter(PojoDest::getUniqueNameOnDest)
            subject.addMapping(m)
            val source = PojoSource()
            val dest = subject.map(source, PojoDest::class)
            assertThat(dest.uniqueNameOnDest, equalTo(source.uniqueNameOnSource))
        }

        it("should map matching getters") {
            val source = PojoSource()
            val dest = subject.map(source, PojoDest::class)
            assertThat(dest.getterOnSourceAndDest, equalTo(source.getterOnSourceAndDest))
        }

        // TODO should a mapping be necessary here?
        it("should map matchin setters") {
            val source = PojoSource()
            val m = Mapping(PojoSource::class, PojoDest::class)
                    .associateSetter(PojoSource::setSetterOnSourceAndDest)
                    .withSetter(PojoDest::setSetterOnSourceAndDest)
            subject.addMapping(m)
            val dest = subject.map(source, PojoDest::class)
            val sV = getBackingFieldVal<Int>(PojoSource::setSetterOnSourceAndDest, source)
            val dV = getBackingFieldVal<Int>(PojoDest::setSetterOnSourceAndDest, dest)
            assertThat(dV, equalTo(sV))
        }

        it("should map getter matching setter") {
            val source = PojoSource()
            val dest = subject.map(source, PojoDest::class)
            val dV = getBackingFieldVal<Int>(PojoDest::setGetterOnSourceSetterOnDest, dest)
            assertThat(dV, equalTo(source.getterOnSourceSetterOnDest))
        }

        // TODO should a mapping be necessary here?
        it("should map setter matching getter") {
            val source = PojoSource()
            val m = Mapping(PojoSource::class, PojoDest::class)
                    .associateSetter(PojoSource::setSetterOnSourceGetterOnDest)
                    .withGetter(PojoDest::getSetterOnSourceGetterOnDest)
            subject.addMapping(m)
            val dest = subject.map(source, PojoDest::class)
            val sV = getBackingFieldVal<Int>(PojoSource::setSetterOnSourceGetterOnDest, source)
            assertThat(dest.setterOnSourceGetterOnDest, equalTo(sV))
        }
    }
})

fun <T> getBackingFieldVal(kFunction: KFunction<*>, dest: Any): T {
    val f = dest.javaClass.getDeclaredField(kFunction.backingFieldName)
    f.isAccessible = true
    return f.get(dest) as T
}

