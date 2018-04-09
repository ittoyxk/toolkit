package com.xk.io.xpath;

import com.sun.org.apache.xpath.internal.XPathAPI;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
/**
 * Created by xiaokang on 2017-05-13.
 */
public class XalanUtils {
    /**
     * xslt 转换
     * @param xml    xml 文档
     * @param xslt   样式表文档
     * @return       解析后的 xml 文档
     * @throws FileNotFoundException
     * @throws TransformerException
     */
    public static String transform(String xml, String xslt)
    {
        try
        {
            ByteArrayInputStream xmlIn = new ByteArrayInputStream(xml.getBytes("GBK"));
            ByteArrayInputStream xsltIn = new ByteArrayInputStream(xslt.getBytes("GBK"));
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer(new StreamSource(xsltIn));
            transformer.setOutputProperty(OutputKeys.ENCODING, "GBK");
            StringWriter wt=new StringWriter();
            StreamResult streamResult = new StreamResult();
            streamResult.setWriter(wt);
            transformer.transform(new StreamSource(xmlIn), streamResult);
            return wt.toString();
        } catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }

        return null;
    }

    /**
     * 读取文件为一个字符串
     * @param path  文件路径
     * @return      文件内容
     */
    public static String readFile(String path)
    {
        try
        {
            InputStream in = new FileInputStream(path);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int len = -1;
            while((len = in.read(b)) != -1)
            {
                out.write(b, 0, len);
            }
            in.close();
            out.close();

            return new String(out.toByteArray());
        } catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }

        return null;
    }

    /**
     * 获得一个文档中指定路径的节点或属性的值
     * @param doc     文档对象
     * @param xpath   路径
     * @return
     */
    public static Object getXPathValue(Document doc, String xpath)
    {
        try
        {
            Node node = (Node) XPathAPI.selectSingleNode(doc, xpath);
            if(node != null)
            {
                if(node.getNodeType() == Node.ATTRIBUTE_NODE)
                {
                    return node.getNodeValue();
                }

                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    NodeList nl = node.getChildNodes();
                    for(int i = 0, len = nl.getLength(); i < len; i++)
                    {
                        Node cn = nl.item(i);
                        if(cn.getNodeType() == Node.TEXT_NODE || cn.getNodeType() == Node.CDATA_SECTION_NODE)
                        {
                            return cn.getNodeValue();
                        }
                    }
                }
            }
        } catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println("getXPathValue---------"+ex.getMessage());
        }

        return null;
    }

    public static List<Node> getXPathList(Document doc, String xpath)
    {
        List<Node> list = new ArrayList<Node>();
        try
        {
            NodeIterator it = XPathAPI.selectNodeIterator(doc, xpath);
            if(it != null)
            {
                Node node = null;

                while((node = it.nextNode()) != null)
                {
                    list.add(node);
                }
            }
        } catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println("getXPathList------------"+ex.getMessage());
        }

        return list;
    }

    /**
     * 根据 xpath 路径创建节点
     * @param doc
     * @param xpath
     * @return
     */
    private static Node createXPathNode(Document doc, String xpath)
    {
		/*不处理任意路径下的情况*/
        if(xpath.indexOf("//") != -1)
        {
            return null;
        }
        try
        {
            Node node = null;
            int pos = xpath.lastIndexOf("/");
            String parentPath = xpath.substring(0, pos);
            String tag = xpath.substring(pos + 1);

            Node parentNode = XPathAPI.selectSingleNode(doc, parentPath);
            if(parentNode == null)
            {
                parentNode = createXPathNode(doc, parentPath);
            }

            if(tag.startsWith("@"))
            {
                node = doc.createAttribute(tag.substring(1));
                parentNode.getAttributes().setNamedItem(node);
            }
            else
            {
                if(tag.indexOf("[") > 0 && tag.endsWith("]"))
                {
                    int number = Integer.parseInt(tag.substring(tag.indexOf("[")).replace("[", "").replace("]", ""));
                    String tagName = tag.substring(0, tag.indexOf("["));
                    String newPath = parentPath + "/" + tagName;
                    NodeList nl = XPathAPI.selectNodeList(doc, newPath);
                    if(nl.getLength() < number)
                    {
                        for(int i = nl.getLength(); i < number; i++)
                        {
                            node = doc.createElement(tagName);
                            parentNode.appendChild(node);
                        }
                    }
                }
                else
                {
                    node = doc.createElement(tag);
                }

                parentNode.appendChild(node);
            }

            return node;
        } catch(Exception ex)
        {
            ex.printStackTrace();
            System.out.println("createXPathNode-----------"+ex.getMessage());
        }
        return null;
    }

    /**
     * 根据 xpath 指定的路径设置节点或属性的值
     * @param doc
     * @param xpath
     * @param value
     */
    public static void setXPathValue(Document doc, String xpath, String value)
    {
        try
        {
            Node node = (Node) XPathAPI.selectSingleNode(doc, xpath);

            if(node == null)
            {
                node = createXPathNode(doc, xpath);
            }

            if(node != null)
            {
                if(node.getNodeType() == Node.ATTRIBUTE_NODE)
                {
                    node.setNodeValue(value);
                }

                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    NodeList nl = node.getChildNodes();
                    if(nl != null && nl.getLength() > 0)
                    {
                        for(int i = 0, len = nl.getLength(); i < len; i++)
                        {
                            node.removeChild(nl.item(i));
                        }
                    }
                    node.appendChild(doc.createTextNode(value));
                }
            }

        } catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

}
