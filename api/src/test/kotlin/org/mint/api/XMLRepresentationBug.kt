package org.mint.api

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mint.MINT
import org.mint.android.AndroidState
import org.mint.android.AndroidStateUtils
import org.mint.android.InMemoryRepository
import org.mint.android.StateBuilder
import org.mint.android.rule.BasicRules
import org.mint.android.rule.XQueryPredicate
import org.mint.integrationTestRunner.ITTestLoop
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class XMLRepresentationBug : StateBuilder {
    val rule = XQueryPredicate("count(//action:click[@selected = 'true']) = 1")
    var mint: MINT? = null
    var state: AndroidState? = null

    @Before
    fun setup() {
        val repo = InMemoryRepository()
        mint = MINT.Builder()
            .withStateRepository(repo)
            .withRule(BasicRules.simpleClickableRule())
            .withNumberOfSteps(1)
            .withLoopBuilder { ctx, _, _ ->
                ITTestLoop(
                    ITTestLoop
                        .builder(ctx)
                        .withStep(
                            buildState(
                                """
                    <View>
                      <View id="a" isClickable="true" isDisplayed="true"/>
                    </View>
                                """.trimIndent(),
                            ),
                        ),
                )
            }
            .build { e -> Assert.fail(e) }!!

        mint?.explore()

        // Obtain the full state from the repository.
        state = repo.abstractState.values.first().first()
    }

    private fun transformXMLNodesToDocumentAndBack(state: AndroidState): AndroidState {
        val xml = AndroidStateUtils.renderXML(state.node)
        val doc = AndroidStateUtils.factory.newDocumentBuilder().parse(xml.byteInputStream())
        val tree = doc.documentElement
        return state.derive(tree)
    }

    @Test
    fun ruleShouldSuccessfullyBeInvokedOnOriginalState() {
        assertEquals(true, rule.invoke(state!!))
    }

    @Test
    fun ruleShouldSuccessfullyBeInvokedOnTransformedState() {
        val st2 = transformXMLNodesToDocumentAndBack(state!!)
        assertTrue(rule.invoke(st2))
    }

    @Test
    fun xmlTransformationShouldBeIsomorphic() {
        val st2 = transformXMLNodesToDocumentAndBack(state!!)

        assertEquals(
            AndroidStateUtils.renderXML(state!!.node),
            AndroidStateUtils.renderXML(st2.node),
        )
    }

    // Note: to run this test, you have to enable additional test dependencies:
    // - in gradle (in this `api` project):
    // testImplementation 'org.xmlunit:xmlunit-core:2.9.0'
    // testImplementation 'org.glassfish.jaxb:jaxb-runtime:2.3.2'
    // You can trust me; this test passes :)
    /*
    @Test
    fun xmlTreesShouldBeIdentical() {
        val st2 = transformXMLNodesToDocumentAndBack(state!!)
        val diff = DiffBuilder.compare(state).withTest(st2).build()
        assertFalse(diff.hasDifferences())
    }
    */
}
