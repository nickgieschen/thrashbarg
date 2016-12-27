package thrashbarg

import com.fasterxml.jackson.annotation.JsonProperty
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.SubjectSpek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class KotlinSource() {
    val x = 1
    val y = 2
    val z = 3
}

class KotlinDest() {
    val x1 = 0
    val y = 0
    val z:String = ""
}

class KotlinRightNoArgConstructor(val x1: Int, val y: Int)

//thrashbarg.objectMapper.addMixIn(Source::class.java, SourceMixin::class.java)
class SourceMixin() {
    @JsonProperty("x1")
    val x = 0
}

// TODO transformations
// TODO defaults
// TODO type check subtypes on asssociations?
// TODO what if pojo only has setter
// TODO Kotlin getters and setters?
// TODO what to do about non getter/setter methods
// TODO can we map without having a mapping?
//class RawSpec : Spek({
//    describe("raw") {
//        it("how does raw work") {
//            val source = PojoSource()
//            val om = ObjectMapper()
//            om.registerKotlinModule()
//            //om.addMixIn(PojoSource::class.java, SourceMixin::class.java)
//            val dest = om.convertValue(source, PojoDest::class.java)
//
//            assertThat(dest.x1, equalTo(source.x))
//        }
//    }
//})

class ThrashbargSpec : SubjectSpek<Thrashbarg>({

    subject { Thrashbarg() }

    describe("Thrashbarg") {

        it("should map without an explicit mapping") {
            val source = KotlinSource()
            val dest = subject.map(source, KotlinDest::class)

            assertThat(dest.y, equalTo(source.y))
        }

        it("should map a poko to a poko") {
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, KotlinDest::class)
                    .associate(KotlinSource::x).with(KotlinDest::x1)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinDest::class)

            assertThat(dest.x1, equalTo(source.x))
        }

        it("should map a poko to a poko without a noarg constructor"){
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, KotlinRightNoArgConstructor::class)
                    .associate(KotlinSource::x).with(KotlinRightNoArgConstructor::x1)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinRightNoArgConstructor::class)

            assertThat(dest.x1, equalTo(source.x))
        }

        it("should map a pojo to a poko"){
            val source = PojoSource()
            val mapping = Mapping(PojoSource::class, KotlinDest::class)
                    .associate(PojoSource::getX).with(KotlinDest::x1)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinDest::class)

            assertThat(dest.x1, equalTo(source.x))
        }

        it("should map a poko to a pojo"){
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, PojoDest::class)
                    .associate(KotlinSource::x).with(PojoDest::getX1)
            subject.addMapping(mapping)
            val dest = subject.map(source, PojoDest::class)

            assertThat(dest.x1, equalTo(source.x))
        }


        it("should map a pojo to a pojo"){
            val thrashbard = Thrashbarg()
            val source = PojoSource()
            val mapping = Mapping(PojoSource::class, PojoDest::class)
                    .associate(PojoSource::getX).with(PojoDest::getX1)
            thrashbard.addMapping(mapping)
            val dest = thrashbard.map(source, PojoDest::class)

            assertThat(dest.x1, equalTo(source.x))
        }

        it("should ignore unassociated fields") {
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, KotlinDest::class)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinDest::class)

            val preMappedDest = KotlinDest()

            assertThat(dest.x1, equalTo(preMappedDest.x1))
        }

//        it("should use supplied function to transform value"){
//            val source = KotlinSource()
//            val mapping = Mapping(KotlinSource::class, KotlinDest::class)
//                    .associate(KotlinSource::z).with(KotlinDest::x1)
//                    .associate(KotlinSource::z).with(KotlinDest::z, Int::toString)
//            subject.addMapping(mapping)
//            val dest = subject.map(source, KotlinDest::class)
//
//            assertThat(dest.z, equalTo(source.z.toString()))
//        }
    }
})
