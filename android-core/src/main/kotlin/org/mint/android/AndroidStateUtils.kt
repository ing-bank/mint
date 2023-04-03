package org.mint.android

import net.sf.saxon.s9api.DOMDestination
import net.sf.saxon.s9api.DocumentBuilder
import net.sf.saxon.s9api.Processor
import net.sf.saxon.s9api.XQueryCompiler
import net.sf.saxon.s9api.XdmNode
import org.apache.xerces.parsers.DOMParser
import org.mint.android.base64.Decoder
import org.mint.android.base64.Encoder
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import java.io.StringWriter
import java.math.BigInteger
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

object AndroidStateUtils {
    private val _processor = Processor(false)
    val builder: DocumentBuilder = run {
        _processor.newDocumentBuilder()
    }

    val factory: DocumentBuilderFactory = run {
        val f = DocumentBuilderFactory.newInstance()
        f.isNamespaceAware = true
        f
    }

    val digest = MessageDigest.getInstance("SHA-224")

    val b64Encoder = Encoder.apply()
    val b64Decoder = Decoder.apply()

    val tRenderer: Transformer = run {
        // hardcode saxon, otherwise xalan or other transformers might be used
        val tt = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null).newTransformer()
        tt.setOutputProperty(OutputKeys.INDENT, "yes")
        tt.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        tt
    }

    fun hash(byteArray: ByteArray): ByteArray {
        digest.reset()
        return digest.digest(byteArray)
    }

    fun xqueryCompiler(): XQueryCompiler {
        val compiler = _processor.newXQueryCompiler()

        // Namespace prefixes that can be used in the xquery
        compiler.declareNamespace("action", AndroidConstants.ACTION_NS)
        compiler.declareNamespace("abstract", AndroidConstants.ABSTRACT_NS)
        compiler.declareNamespace("abstract-a", AndroidConstants.ABSTRACT_ACTION_NS)
        compiler.declareNamespace("correlate", AndroidConstants.CORRELATE_NS)
        compiler.declareNamespace("rule", AndroidConstants.RULE_NS)

        return compiler
    }

    fun renderXML(d: Node?, renderer: Transformer = tRenderer): String {
        try {
            val sw = StringWriter()
            renderer.transform(DOMSource(d), StreamResult(sw))
            return sw.toString()
        } catch (e: Exception) {
            throw RuntimeException("Could not print XML", e)
        }
    }

    fun printXML(d: Node?) = print(renderXML(d))

    // Hardcode xerces implementation (apache harmony is garbage)
    fun newDocument(): Document = org.apache.xerces.dom.DocumentImpl()

    fun parser(): DOMParser {
        val parser = DOMParser()

        // enable namespace support
        parser.setFeature("http://xml.org/sax/features/namespaces", true)

        return parser
    }

    fun toString(n: Node): String = toXdm(n).toString()

    fun toXdm(n: Node): XdmNode {
        return builder.wrap(n)
    }

    // Doesn't copy everything (no comments, processing nodes or userData)
    // Probably faster than fullCopy
    fun copy(n: Node, deep: Boolean = true): Node {
        val doc = newDocument()
        val copy = doc.importNode(n, deep)
        // FIXES: "XML representation bug"
        doc.appendChild(copy)
        return copy
    }

    // Copies everything
    // Probably slower than copy
    fun fullCopy(n: Node): Node {
        val r = DOMResult(newDocument())

        tRenderer.transform(DOMSource(n), r)
        val el = (r.getNode() as Document).documentElement

        return el
    }

    fun toNode(n: XdmNode): Node {
        val doc = newDocument()
        val dest = DOMDestination(doc)
        _processor.writeXdmValue(n, dest)
        return doc
    }

    fun toHex(b: ByteArray): String = b.joinToString("") {
        java.lang.Byte.toUnsignedInt(it).toString(radix = 16).padStart(2, '0')
    }

    fun toDoc(s: Set<AndroidState>): Document {
        val doc = newDocument()

        val el = doc.createElement("document")
        doc.appendChild(el)

        // append all AndroidStates into one big document
        s.forEach {
            val n = doc.importNode(it.node, true)
            el.appendChild(n)
        }
        return doc
    }

    fun writeDoc(doc: Document, file: File) {
        val o = FileOutputStream(file)
        tRenderer.transform(DOMSource(doc), StreamResult(o))
        o.close()
    }

    fun toBase64(b: ByteArray): String = b64Encoder.encode(b)

    fun toBase36(b: ByteArray): String = BigInteger(1, b).toString(36)

    fun toBase36(s: String): String = toBase36(s.toByteArray(Charsets.UTF_8))
}
