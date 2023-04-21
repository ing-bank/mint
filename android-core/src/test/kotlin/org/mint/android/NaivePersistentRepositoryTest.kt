package org.mint.android

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mint.android.rule.BasicRules
import java.io.File
import java.nio.file.Files

class NaivePersistentRepositoryTest : StateBuilder {
    private val xml: String = """
            <View class="com.android.internal.policy.DecorView">
              <View class="android.view.ViewStub" id="16908719" resourceName="action_mode_bar_stub" package="android"/>
              <View class="androidx.appcompat.widget.ContentFrameLayout" id="16908290" resourceName="content" package="android">
                <View class="android.widget.LinearLayout">
                  <View class="com.google.android.material.button.MaterialButton" id="2131230819" resourceName="button2" package="org.mint.exampleapp" isClickable="true" isDisplayed="true"/>
                  <View class="com.google.android.material.textview.MaterialTextView" id="2131231165" resourceName="textView2" package="org.mint.exampleapp"/>
                  <View class="androidx.appcompat.widget.AppCompatEditText" id="2131230898" resourceName="editTextNumber2" package="org.mint.exampleapp"/>
                </View>
              </View>
            </View>
    """.trimIndent()

    private var target: File? = null

    @Before
    fun createTemporaryDirectory() {
        target = Files.createTempDirectory("mint-repotest").toFile()
    }

    @After
    fun deleteTemporaryFiles() {
        target?.deleteRecursively()
    }

    // A long test that tests whether states are stored to disk,
    // and if a newly initialised repo is correctly initialised based on the stored data
    @Test
    fun createExpectedXMLEntriesOnDisk() {
        val state = buildState(xml)
        val repo = NaivePersistentRepository("repo", target!!)

        val stateAbstraction = ExampleStateAbstraction.mapper()
        val actionAbstraction = ExampleActionAbstraction.mapper()

        val abstract = state.copy()

        // abstract over
        abstract.extend(stateAbstraction)
        abstract.extend(actionAbstraction)
        abstract.createParentHashes()

        val ruleState = abstract.copy()
        // apply rule (1 match/extension) - (click button2)
        ruleState.extendWithRuleGroups()
        ruleState.apply(BasicRules.simpleClickableRule())

        // action selection (click button2)
        val selected = ruleState.selectAction()

        // persist twice, creating a copy because we otherwise share the same reference
        selected.map { repo.persist(it.copy()) }
        selected.map { repo.persist(it.copy()) }

        // there should be one folder with one abstract id
        val states = repo.states.list()
        assertNotNull(states)
        assertEquals(1, states!!.size)

        // and 2 XML documents are in there
        val stateXMLDir = File(repo.states, states[0])
        val stateXMLs = stateXMLDir.list()
        assertNotNull(stateXMLs)
        assertEquals(2, stateXMLs!!.size)

        // we should have one action dir, one hash dir
        val actions = repo.actions.list()
        assertNotNull(actions)
        assertEquals(1, actions!!.size)

        val hashes = File(repo.actions, actions[0]).list()
        assertNotNull(hashes)
        assertEquals(1, hashes!!.size)

        // but two actions
        val actionsInDir = File(File(repo.actions, actions[0]), hashes[0]).list()
        assertNotNull(actionsInDir)
        assertEquals(2, actionsInDir!!.size)

        // now if we reinitialize the repo, we should end up with an expected state:
        val repo2 = NaivePersistentRepository("repo2", target!!)
        for (key in repo.inMemoryRepository.abstractState.keys) {
            assertEquals(
                repo.inMemoryRepository.abstractState.get(key)!!.size,
                repo2.inMemoryRepository.abstractState.get(key)!!.size,
            )
        }

        // Same for the abstract actions
        for (actionKey in repo.inMemoryRepository.abstractActions.keys) {
            for (hashKey in repo.inMemoryRepository.abstractActions.get(actionKey)!!.keys) {
                assertEquals(
                    repo.inMemoryRepository.abstractActions.get(actionKey)!!.get(hashKey)!!.size,
                    repo2.inMemoryRepository.abstractActions.get(actionKey)!!.get(hashKey)!!.size,
                )
            }
        }
    }
}
