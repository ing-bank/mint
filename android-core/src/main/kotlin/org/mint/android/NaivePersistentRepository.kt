package com.ing.mint.android

import com.ing.mint.StateRepository
import com.ing.mint.lib.Query
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A small wrapper that is used in conjunction with the InMemoryRepository that:
 * 1) Stores all new states that are requested to be persisted --> effectively an append-only log
 * 2) When initialised, populates the in memory state with the data that is persisted
 *
 * The recommended way of using this is by obtaining an instance via `testScopeInstance`
 *
 * The directory layout that is used:
 * <basedir>
 *     - abstract_states
 *          - abstract_id_a
 *              - AndroidState a
 *          - abstract_id_b
 *              - AndroidState b
 *     - abstract_actions
 *          - abstract_id_a
 *              - parent_hash_x
 *                  - concrete action a
 *                  - concrete action b
 *              - parent_hash_y
 *                  - concrete action c
 *          - abstract_id_b
 *              - parent_hash_z
 *                  - concrete action d
 */
class NaivePersistentRepository(val name: String, val basedir: File) : StateRepository<AndroidState>, StateBuilder {
    companion object {
        private val abstractStates = "abstract_states"
        private val abstractActions = "abstract_actions"
        private val ext = ".xml"

        /** Create a dated, named instance of this repository */
        fun datedNamedInstance(name: String): StateRepository<AndroidState> =
            namedInstance(datedName(name))

        /** Create a named instance of this repository */
        fun namedInstance(name: String): StateRepository<AndroidState> =
            NaivePersistentRepository(name, File(StoreInExternalStorage.mintFolder(), name))

        /** Create a 'dated' name with yyyy-MM-dd-HH-mm-name */
        private fun datedName(name: String): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.getDefault())
            return dateFormat.format(Date()) + "-" + name
        }
    }
    val inMemoryRepository = InMemoryRepository(name)

    val states = File(basedir, abstractStates)
    val actions = File(basedir, abstractActions)
    init {
        states.mkdirs()
        actions.mkdirs()
        // Populating the repository is okay, we just need to find all XMLs in the abstractStates directory
        populateRepository(states)
    }

    override fun correlate(state: AndroidState): AndroidState = inMemoryRepository.correlate(state)

    override fun query(query: Query<AndroidState>): Set<AndroidState> = inMemoryRepository.query(query)

    override fun persist(state: AndroidState): AndroidState {
        val abstractID = AndroidStateUtils.toBase36(inMemoryRepository.abstractID(state))

        // Store the state, creating the folder if it doesn't already exist
        val stateFolder = File(states, abstractID)
        stateFolder.mkdirs()
        // Check for the latest sequence number (i.e. # entries + 1)
        // Including the sequence number as a prefix to the filename ensures
        // that the persisted states & actions can be recovered in the order in which they occurred.
        val stateSeq = (stateFolder.listFiles() ?: arrayOf()).size.toString()
        persistAndroidState(File(stateFolder, filename(state, stateSeq)), state)

        // Now also store the action -- here order is important so we follow up with as sequence number
        inMemoryRepository.abstractAction(state)?.let {
            val (parentHash, action) = it
            val actionFolder =
                File(File(actions, abstractID), AndroidStateUtils.toBase36(parentHash))
            actionFolder.mkdirs()
            // Check for the latest sequence number (i.e. # entries + 1)
            val actionSeq = (actionFolder.listFiles() ?: arrayOf()).size.toString()
            persistAndroidState(File(actionFolder, filename(action, actionSeq)), action)
        }

        return inMemoryRepository.persist(state)
    }

    private fun filename(state: AndroidState, sequenceNumber: String): String {
        return sequenceNumber.padStart(5, '0') + state.filenameHash() + ext
    }

    /** Populates the repository with whatever data is currently stored on disk.
     *
     * Note that we need to sort based on name to handle the order correctly */
    private fun populateRepository(dir: File) {
        dir.listFiles()?.sortedBy { it.name }?.forEach { child ->
            if (child.isDirectory) {
                populateRepository(child)
            } else {
                val sb = StringBuilder()
                BufferedReader(FileReader(child)).use {
                    var sCurrentLine: String? = it.readLine()
                    while (sCurrentLine != null) {
                        sb.append(sCurrentLine).append(System.lineSeparator())
                        sCurrentLine = it.readLine()
                    }
                }
                // Note that because we recover based on whatever is stored on disk, we just need to
                // populate the in memory repository that actually reflects our state as we use it
                // at runtime.
                inMemoryRepository.persist(buildState(sb.toString()))
            }
        }
    }

    /** Persist a given android state to the specified file, overwriting whatever used to be there.
     * This can be done because the target filename is a function of its contents and order,
     * meaning different contents and/or order will be persisted to different files. */
    private fun persistAndroidState(target: File, contents: AndroidState) {
        val bw = BufferedWriter(FileWriter(target))
        bw.write(AndroidStateUtils.renderXML(contents.node))
        bw.close()
    }

    fun serializeToReport() {
        val report = StoreInExternalStorage.apply(this, name, basedir)
    }
}
