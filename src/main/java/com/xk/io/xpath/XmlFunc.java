package com.xk.io.xpath;

import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Created by xiaokang on 2017-05-13.
 */
public class XmlFunc {
    public static Document string2Dom(String XMLData)
    {
        Document dom = new DocumentImpl();
        try {
            InputSource source = new InputSource(new CharArrayReader(XMLData.toCharArray()));
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            dom = docBuilder.parse(source);

            return dom;
        } catch (Exception e) {
            dom = null;
            System.out.println("com.taikang.utils.xmlFunc.string2Dom error:" + e);

        } finally {
        }
        return dom;
    }

    public static Document string2Dom(String data, boolean isSpace)
            throws IOException, SAXException
    {
        if (isSpace) {
            DOMParser parser = new DOMParser();

            StringReader sr = new StringReader(data);
            InputSource is = new InputSource(sr);
            parser.parse(is);
            sr.close();
            return parser.getDocument();
        }

        return null;
    }

    public static String dom2String(Document dom)
    {
        String aa = new String();
        try {
            StringWriter ss = new StringWriter();
            OutputFormat format = new OutputFormat(dom);
            format.setEncoding("GB2312");
            XMLSerializer serial = new XMLSerializer(ss, format);
            serial.asDOMSerializer();
            serial.serialize(dom.getDocumentElement());
            aa = ss.toString();
            ss.flush();
            ss.close();
        } catch (Exception e) {
            System.out.println("com.taikang.utils.xmlFunc.dom2String:" + e);
        }

        return aa;
    }

    public static String element2String(Element em)
    {
        String aa = new String();
        try {
            StringWriter ss = new StringWriter();
            OutputFormat format = new OutputFormat();
            format.setEncoding("GB2312");
            XMLSerializer serial = new XMLSerializer(ss, format);
            serial.asDOMSerializer();
            serial.serialize(em);
            aa = ss.toString();
            ss.flush();
            ss.close();
        } catch (Exception e) {
            System.out.println("com.taikang.utils.xmlFunc.element2String:" + e);
        }

        return aa;
    }

    public static boolean dom2File(Document dom, String filename)
    {
        try {
            FileOutputStream outfile = new FileOutputStream(filename);

            OutputFormat format = new OutputFormat(dom);

            format.setEncoding("GB2312");

            XMLSerializer serial = new XMLSerializer(outfile, format);

            serial.asDOMSerializer();

            serial.serialize(dom.getDocumentElement());

            outfile.flush();

            outfile.close();
            return true;
        } catch (Exception e) {
            System.out.println("com.taikang.utils.xmlFunc.dom2File:" + e);
        }
        return false;
    }

    public static Node lookforNode(Document doc, String xpath)
    {
        try {
            NodeIterator nl = XPathAPI.selectNodeIterator(doc, xpath);
            return nl.nextNode();
        } catch (Exception e) {
            System.out.println("com.taikang.utils.xmlFunc.lookforNode ：" + e);
        }
        return null;
    }

    public static NodeIterator lookforNodes(Document doc, String xpath)
    {
        try {
            return XPathAPI.selectNodeIterator(doc, xpath);
        } catch (Exception e) {
            System.out.println("com.taikang.utils.xmlFunc.lookforNodes ：" + e);
        }
        return null;
    }

    public static void Node2OutStream(Node nd, PrintWriter out)
    {
        try {
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty("encoding", "GB2312");
            serializer.transform(new DOMSource(nd), new StreamResult(out));
        } catch (Exception e) {
            System.out.println("com.taikang.utils.xmlFunc.Node2OutStream ：" + e);
        }
    }
}
