package com.ing.mint.tooling.android.reporting

import com.ing.mint.android.AndroidStateUtils
import com.ing.mint.android.xml.attribute
import com.ing.mint.android.xml.children
import com.ing.mint.tooling.android.reporting.VerdictUtil.countVerdicts
import com.ing.mint.util.MapUtil.getOrDefaultExt
import org.gradle.api.logging.Logger
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import javax.xml.transform.Transformer
import javax.xml.transform.stream.StreamResult

/** A reference implementation of a report renderer.
 *
 * Shows how the MINT captured data can be used to generate an (interactive) report.
 */
class ReferenceReportRenderer(private val logger: Logger) : ReportRenderer {
    private val resourcesPrefix: String = "/reference-report"

    // Load the stylesheet transformers of the various pages, pulling them from the classpath and
    // making sure the uri resolver works that way as well.
    private val reportTransformer: Transformer = transformerOfResource(logger, resourcesPrefix, "sequence.xslt")
    private val allVerdictsTransformer: Transformer = transformerOfResource(logger, resourcesPrefix, "all-verdicts.xslt")
    private val indexTransformer: Transformer = transformerOfResource(logger, resourcesPrefix, "index.xslt")

    override fun render(report: File): File {
        val dir = File(report.parentFile, "${report.name}.report")
        val imgdir = File(dir, "img")
        imgdir.mkdirs()

        // First parse the whole report
        val doc = AndroidStateUtils.factory.newDocumentBuilder().parse(report.inputStream())

        // Create a verdicts overview page
        generateAllVerdicts(File(dir.absolutePath, "verdicts.html"), doc)

        // Create individual documents [/SystemUnderTest/AndroidLoop]
        val groupedSuts: MutableMap<String, Set<Node>> = mutableMapOf()
        val sutSessionToSequence: MutableMap<String, String> = mutableMapOf()
        keyBySession(doc.documentElement, groupedSuts, sutSessionToSequence)

        // create an index.html linking to the individual sequence pages
        logger.debug("Generating index.html in {}", dir.absolutePath)
        generateIndex(File(dir.absolutePath, "index.html"), groupedSuts, sutSessionToSequence, doc)

        // Create all of the individual sequence pages
        logger.debug("Generating {} individual sequence pages", groupedSuts.size)
        // This is not thread safe: create a new map containing copies of the original keys and
        // new documents with imported nodes before executing the transformations in parallel
        val documents = groupedSuts.toMap().mapValues { (session, nodes) ->
            val sequenceDocument = AndroidStateUtils.factory.newDocumentBuilder().newDocument()
            val el = sequenceDocument.createElement("document")
            sequenceDocument.appendChild(el)
            for (n in nodes) {
                el.appendChild(sequenceDocument.importNode(n, true))
            }
            sequenceDocument
        }

        // executing sequentially because parallelization caused screenshot mix-ups
        groupedSuts.keys.forEach { session ->
            generateSequencePage(
                sequenceDocument = documents[session]!!,
                dir = dir,
                imgdir = imgdir,
                session = session,
                sequences = sutSessionToSequence,
            ).invoke()
        }
        return dir
    }

    /** For each of the AndroidLoop elements in the SUT node, acquire the id of the session and sequence */
    private fun keyBySession(tree: Element, suts: MutableMap<String, Set<Node>>, sequences: MutableMap<String, String>) {
        // Create individual documents [/SystemUnderTest/AndroidLoop]
        val sutNodes = tree.getElementsByTagName("SystemUnderTest")
        for (n in 0 until sutNodes.length) {
            val sut = sutNodes.item(n)
            val loop = sut.children().find { it.nodeName == "AndroidLoop" }
            loop?.let {
                val session = it.attribute("session").orEmpty()
                val sequence = it.attribute("sequence").orEmpty()
                val set = suts.getOrDefaultExt(session, setOf())
                suts += Pair(session, set + sut)
                sequences += Pair(session, sequence)
            }
        }
    }

    /** Retrieve the name of the Application that the test sequence has been applied on */
    private fun nameOf(n: Node): String {
        val appinfo = (n as Element).getElementsByTagName("AppInfo").item(0)
        val name = appinfo.attribute("applicationName") ?: "<unknown>"
        val pkg = appinfo.attribute("applicationPackageName") ?: "<unknown>"
        return "$name - $pkg"
    }

    /** Find the latest (i.e. most recent) date / time when a sequence has been executed */
    private fun findLatestDateTime(suts: Map<String, Set<Node>>): Pair<String, String> {
        return findDateTime(suts) { date, time, d, t ->
            date == "<unknown>" || (d == date && t > time) || d > date
        }
    }

    /** Find the earliest (i.e. oldest) date / time when a sequence has been executed */
    private fun findEarliestDateTime(suts: Map<String, Set<Node>>): Pair<String, String> {
        return findDateTime(suts) { date, time, d, t ->
            date == "<unknown>" || (d == date && t < time) || d < date
        }
    }

    /** Don't use this, prefer the other find*DateTime functions -- Find the earliest or latest date/time of a sequences, given a large number of them */
    private fun findDateTime(suts: Map<String, Set<Node>>, pred: (String, String, String, String) -> Boolean): Pair<String, String> {
        var date = "<unknown>"
        var time = "<unknown>"
        for ((_, nodes) in suts) {
            for (n in nodes) {
                val al = (n as Element).getElementsByTagName("AndroidLoop").item(0)
                val d = al.attribute("date") ?: "<unknown>"
                val t = (al.attribute("time") ?: "<unknown>") + (al.attribute("zone") ?: "")
                if (pred(date, time, d, t)) {
                    date = d
                    time = t
                }
            }
        }
        return Pair(date, time)
    }

    private fun createMetadataIndexBlock(metadata: Element, suts: MutableMap<String, Set<Node>>): Element {
        suts.firstNotNullOfOrNull { it.value }?.let { metadata.setAttribute("appname", nameOf(it.first())) }
        val (date, time) = findLatestDateTime(suts)
        metadata.setAttribute("time", date)
        metadata.setAttribute("date", time)
        return metadata
    }

    private fun createSessionIndexBlock(ul: Element, rootDoc: Document, suts: MutableMap<String, Set<Node>>, sequences: MutableMap<String, String>): Element {
        for ((session, seq) in suts) {
            val li = rootDoc.createElement("session")
            li.textContent = "$session.html"
            li.setAttribute("sequence", sequences[session])
            li.setAttribute("steps", seq.size.toString())
            val (verdicts, notOkVerdicts) = seq.countVerdicts()
            li.setAttribute("verdicts", verdicts.toString())
            li.setAttribute("notOkVerdicts", notOkVerdicts.toString())
            val (date, time) = findEarliestDateTime(mapOf(Pair("", seq)))
            li.setAttribute("date", date)
            li.setAttribute("time", time)
            ul.appendChild(rootDoc.importNode(li, true))
        }
        return ul
    }

    private fun createRuleIndexBlock(rules: Element, original: Document, rootDoc: Document): Element {
        for (rule in activatedRules(original.documentElement)) {
            val r = rootDoc.createElement("rule")
            r.setAttribute("name", rule.name)
            r.setAttribute("description", rule.description)
            rules.appendChild(r)
        }
        return rules
    }

    private fun createOracleIndexBlock(oracles: Element, original: Document, rootDoc: Document): Element {
        for (oracle in activatedOracles(original.documentElement)) {
            val r = rootDoc.createElement("oracle")
            r.setAttribute("name", oracle.name)
            r.setAttribute("description", oracle.description)
            val cats = rootDoc.createElement("categories")
            cats.setAttribute("name", oracle._categories)
            for (c in oracle.categories()) {
                cats.appendChild(rootDoc.createElement(c))
            }
            r.appendChild(cats)
            oracles.appendChild(r)
        }
        return oracles
    }

    /** Generate an index page based on the index XSLT and some data we collect */
    private fun generateIndex(index: File, suts: MutableMap<String, Set<Node>>, sequences: MutableMap<String, String>, original: Document) {
        val rootDoc = AndroidStateUtils.factory.newDocumentBuilder().newDocument()
        val root = rootDoc.createElement("index")
        rootDoc.appendChild(root)

        root.appendChild(createMetadataIndexBlock(rootDoc.createElement("metadata"), suts))
        root.appendChild(createOracleIndexBlock(rootDoc.createElement("oracles"), original, rootDoc))
        root.appendChild(createRuleIndexBlock(rootDoc.createElement("rules"), original, rootDoc))
        root.appendChild(createSessionIndexBlock(rootDoc.createElement("sessions"), rootDoc, suts, sequences))

        indexTransformer.transform(docToSource(rootDoc), StreamResult(index))
    }

    /** Generate a page with all verdicts linking to the corresponding step */
    private fun generateAllVerdicts(index: File, original: Document) {
        allVerdictsTransformer.transform(docToSource(original), StreamResult(index))
    }

    /** Generate an individual page for all of the sequences, containing everything deemed important */
    private fun generateSequencePage(sequenceDocument: Document, dir: File, imgdir: File, session: String, sequences: Map<String, String>): () -> Unit {
        // extract all the (base64) images
        val screenshotNodes = sequenceDocument.documentElement.getElementsByTagName("Screenshot")
        for (n in 0 until screenshotNodes.length) {
            // Write all of the images to disk, replacing the b64 string with the relative path to the image
            val screenshotNode = screenshotNodes.item(n)
            val b64img = screenshotNode.textContent
            val hash = AndroidStateUtils.toBase36(AndroidStateUtils.hash(b64img.toByteArray()))
            val image = File(imgdir, "$hash.png")
            screenshotNode.textContent = image.relativeTo(imgdir.parentFile).path
            image.writeBytes(AndroidStateUtils.b64Decoder.decode(b64img))
        }
        val seq = File(dir.absolutePath, "${sequences[session]}-$session.html")
        return {
            reportTransformer.transform(docToSource(sequenceDocument), StreamResult(seq))
        }
    }
}
