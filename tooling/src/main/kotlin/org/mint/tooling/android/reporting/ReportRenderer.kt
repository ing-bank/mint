package org.mint.tooling.android.reporting

import net.sf.saxon.TransformerFactoryImpl
import org.gradle.api.logging.Logger
import org.mint.android.xml.attribute
import org.mint.tooling.android.RenderReportTask
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.URIResolver
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

interface ReportRenderer {
    /**
     * Render a single report, by mapping the XML to a HTML report based
     *
     * Note that the source report is the single report file produced by the Report task and that
     * the resulting output file can be a file (a single-page report) or a directory
     * (containing various individual files). This is up for the implementation to decide
     */
    fun render(report: File): File

    /** Given a Document, transform it into a Source that can be used with a transformer */
    fun docToSource(document: Document): Source {
        val src = DOMSource(document)
        val factory: TransformerFactory = TransformerFactory.newInstance()
        val transformer: Transformer = factory.newTransformer()
        val result = StreamResult()
        val out = ByteArrayOutputStream()
        result.outputStream = out
        transformer.transform(src, result)
        val bis = ByteArrayInputStream(out.toByteArray())
        return StreamSource(bis)
    }

    /** Find and initialise a transformer of a given name in the context of this report renderer */
    fun transformerOfResource(logger: Logger, resourcesPrefix: String, resource: String): Transformer {
        val stylesheet: InputStream? = RenderReportTask::class.java.getResourceAsStream("$resourcesPrefix/$resource")
        val xslt: StreamSource? = stylesheet?.let { StreamSource(BufferedInputStream(it)) }

        val transAct = TransformerFactoryImpl()
        transAct.uriResolver = ResourcesUriResolver(logger, resourcesPrefix)

        return transAct.newTransformer(xslt)
    }

    /** Resolve other stylesheets from the classpath by providing a custom uri resolver */
    private class ResourcesUriResolver(private val logger: Logger, private val resourcesPrefix: String) : URIResolver {
        override fun resolve(href: String?, base: String?): Source? {
            return try {
                var r = "$resourcesPrefix/$href"
                if (base != null && base.isNotEmpty()) {
                    r = "$base/$r"
                }
                val s = RenderReportTask::class.java.getResourceAsStream(r)
                StreamSource(BufferedInputStream(s))
            } catch (e: IOException) {
                logger.error("Failed to resolve URI `{}` ({})", href, base)
                logger.error("IOException: ", e)
                null
            }
        }
    }

    // Convenient data classes containing Rule and Oracle info. Might be extended with other config data in the future
    data class Rule(val name: String, val description: String)
    data class Oracle(val name: String, val description: String, val _categories: String) {
        fun categories(): List<String> = _categories.split(",").map { it.trim() }
    }

    /** Find all rules that are used at least once in the document (i.e. have been used at least once) */
    fun activatedRules(tree: Element): Set<Rule> {
        val ruleElements = tree.getElementsByTagName("rule")
        val rules = mutableSetOf<Rule>()
        for (n in 0 until ruleElements.length) {
            val rule = ruleElements.item(n)
            rules.add(
                Rule(
                    rule.attribute("name")!!,
                    rule.attribute("description")!!
                )
            )
        }
        return rules
    }

    /** Find all oracles that are used at least once in the document (i.e. have been used at least once) */
    fun activatedOracles(tree: Element): Set<Oracle> {
        val oracleElements = tree.getElementsByTagName("oracle")
        val oracles = mutableSetOf<Oracle>()
        for (n in 0 until oracleElements.length) {
            val oracle = oracleElements.item(n)
            oracles.add(
                Oracle(
                    oracle.attribute("name")!!,
                    oracle.attribute("description")!!,
                    oracle.attribute("categories")!!
                )
            )
        }
        return oracles
    }
}
