package thrashbarg

import org.dozer.DozerBeanMapper
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class TestResult(val duration: Long, val frameworkName: String)

fun printResults(results: TestResult) {
    println("${results.frameworkName}: ${results.duration}")
}

class PerformanceSpec : Spek({

    val numberOfRuns = 100000

    describe("Performace test") {
        it("results") {
            printResults(testDozer(numberOfRuns))
            printResults(testThrashbarg(numberOfRuns))
        }
    }
})

fun testThrashbarg(numberOfRuns: Int): TestResult {
    val mapper = Thrashbarg()
    return TestResult(test({ ->
        val p = PojoSource()
        mapper.map(p, PojoDest::class)
    }, numberOfRuns), "Thrashbarg")
}

fun testDozer(numberOfRuns: Int): TestResult {
    val mapper = DozerBeanMapper()
    return TestResult(test({ ->
        val p = PojoSource()
        mapper.map(p, PojoDest::class.java)
    }, numberOfRuns), "Dozer")
}

fun test(fn: () -> Unit, numberOfRuns: Int): Long {
    val start = System.currentTimeMillis()
    for (i in 0 until numberOfRuns) {
        fn()
    }
    return System.currentTimeMillis() - start
}
