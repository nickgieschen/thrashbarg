package thrashbarg

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.jetbrains.spek.api.SubjectSpek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class KotlinSource(val getterSetter: Int = 1,
                   val uniqueNameOnSource: Int = 2,
                   val differentTypeOnDest:Int = 5)

class KotlinDest(val getterSetter: Int,
                 val uniqueNameOnDest: Int,
                 val differentTypeOnDest:String)

// TODO defaults
// TODO type check subtypes on asssociations?
// TODO what if pojo only has setter
// TODO Kotlin getters and setters?
// TODO what to do about non getter/setter methods
// TODO how do we handle setters vs methods
// TODO add jackson's BeanUtil getter/setter mangle stuff?
// TODO make ignore configurable
// TODO implement ignores
class ThrashbargSpec : SubjectSpek<Thrashbarg>({

    subject { Thrashbarg() }

    describe("Thrashbarg") {

        it("should map without an explicit mapping") {
            val source = KotlinSource()
            val dest = subject.map(source, KotlinDest::class)
            assertThat(dest.getterSetter, equalTo(source.getterSetter))
        }

        it("should map associated properties") {
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, KotlinDest::class)
                    .associate(KotlinSource::uniqueNameOnSource).withProperty(KotlinDest::uniqueNameOnDest)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinDest::class)
            assertThat(dest.uniqueNameOnDest, equalTo(source.uniqueNameOnSource))
        }

        it("should use transformer 1 to transform value") {
            val appendA = { it:Int -> "{$it}A" }
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, KotlinDest::class)
                    .associate(KotlinSource::differentTypeOnDest).withProperty(KotlinDest::differentTypeOnDest, appendA)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinDest::class)
            assertThat(dest.differentTypeOnDest, equalTo(appendA(source.differentTypeOnDest)))
        }

        it("should use transformer 2 to transform value") {
            val trans = { propValue: Int, parentObj:KotlinSource -> (parentObj.getterSetter + propValue).toString() }
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, KotlinDest::class)
                    .associate(KotlinSource::differentTypeOnDest).withProperty(KotlinDest::differentTypeOnDest, trans)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinDest::class)
            assertThat(dest.differentTypeOnDest, equalTo(trans(source.differentTypeOnDest, source)))
        }

        it("should ignore unassociated fields") {
            val source = KotlinSource()
            val mapping = Mapping(KotlinSource::class, KotlinDest::class)
            subject.addMapping(mapping)
            val dest = subject.map(source, KotlinDest::class)
            assertThat(dest.uniqueNameOnDest, equalTo(0))
        }
    }
})
