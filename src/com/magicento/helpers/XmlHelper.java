package com.magicento.helpers;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.apache.commons.lang.ArrayUtils;
import org.apache.tools.ant.util.FileUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * Helper for XML related
 * @author Enrique Piatti
 */
public class XmlHelper {

    public static List<XmlTag> getParents(PsiElement element)
    {
        return getParents(element,true);
    }

    public static List<XmlTag> getParents(PsiElement element, boolean rootFirst)
    {
        List<XmlTag> parents = new ArrayList<XmlTag>();
        PsiElement parent = element.getParent();
        while(parent != null)
        {
            if(isXmlTag(parent)){
                //parents.add(((XmlTag) parent).getName());
                if( ! isXmlTagIncomplete((XmlTag) parent)){
                    parents.add((XmlTag)parent);
                }
            }
            parent = parent.getParent();
        }
        if(rootFirst){
            Collections.reverse(parents);
        }
        return parents;
    }

    public static boolean isXmlTagIncomplete(XmlTag xmlTag)
    {
        if(xmlTag != null){
            return xmlTag.getLastChild() instanceof PsiErrorElement;	// getErrorDescription() == "Tag start is not closed"
        }
        return false;

        // xmlTag.getNextSibling() instanceof PsiErrorElement

        // xmlTag.getFirstChild().getText() == "<" || xmlTag.getFirstChild().getNode().getElementType().toString() == "XML_START_TAG_START"
        // &&
        // xmlTag.getLastChild() instanceof PsiErrorElement || xmlTag.getText().endsWith("IntellijIdeaRulezzz")

        // return xmlTag.getText().endsWith("IntellijIdeaRulezzz");
    }

    public static boolean isXmlTag(PsiElement element)
    {
        return element instanceof XmlTag;
    }

    public static boolean isXmlFile(PsiFile currentFile)
    {
        return currentFile instanceof XmlFile;
    }

    public static List<Element> findXpath(File xmlFile, String xpath)
    {
        Document xmlDocuemnt = getDocumentFromFile(xmlFile);
        if(xmlDocuemnt != null){
            return findXpath(xmlDocuemnt, xpath);
        }
        return null;
    }


    public static List<Element> findXpath(Document xmlDocument, String xpath)
    {
        if(xmlDocument != null){
            XPath filterXpression = null;
            try {
                filterXpression = XPath.newInstance(xpath);
                // String xpathCompiled = filterXpression.getXPath();
                List<Element> nodes = filterXpression.selectNodes(xmlDocument);
                return nodes;
            } catch (JDOMException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }


    public static List<Element> findXpath(String xml, String xpath)
    {
        Document xmlDocument =  getDocumentFromXmlString(xml);
        return findXpath(xmlDocument, xpath);
    }


    public static Document getDocumentFromFile(File xmlFile)
    {

//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//        Document document = documentBuilder.parse(xmlFile.getAbsolutePath());

        if(xmlFile != null && xmlFile.exists() && xmlFile.isFile()){
            SAXBuilder builder = new SAXBuilder();
            try {
                Document build = builder.build(xmlFile);
                return build;
            } catch (JDOMException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }


    public static Document getDocumentFromXmlString(String xml)
    {
        SAXBuilder builder = new SAXBuilder();
        Reader in = new StringReader(xml);
        try {
            return builder.build(in);
        } catch (JDOMException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }


    public static String getXmlStringFromDocument(Document xmlDocument)
    {
       if(xmlDocument == null){
           return null;
       }
       return new XMLOutputter().outputString(xmlDocument);
    }

    public static PsiElement convertDomElementToPsiElement(Element domElement)
    {
        // TODO: search the equilavent PsiElement for the DOMElement, inside the PsiFile of the original file (we need the file too here)
        return null;
    }


    public static File createFileFromXmlDocument(Document xmlDocument, String filePath)
    {
        FileWriter writer = null;
        try {
            writer = new FileWriter(filePath);
            XMLOutputter outputter = new XMLOutputter();
            // outputter.setFormat(Format.getPrettyFormat());
            outputter.output(xmlDocument, writer);
            writer.close();

//            StringWriter out = new StringWriter();
//            outputter.output(xmlDocument, out);
            // FileUtils.writeStringToFile(new File(filePath), out.toString(), true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(filePath);
    }

    /**
     * merges the content of xml files, the last file will overwrite the first file if there are common nodes
     * @param xmlFiles
     * @return
     */
    public static Document mergeXmlFiles(List<File> xmlFiles)
    {
        if(xmlFiles != null || xmlFiles.size() > 0){
            File xmlFile1 = xmlFiles.get(0);
            Document mergedDocument = getDocumentFromFile(xmlFile1);
            int size = xmlFiles.size();
            for(int i=1; i<size; i++){
                mergedDocument = mergeXmlDocuments(mergedDocument, getDocumentFromFile(xmlFiles.get(i)));
            }
            return mergedDocument;
        }
        return null;
    }


    /**
     * merges content from xmlFile2 into xmlFile1 (xmlFile2 will overwrite nodes from xmlFile1)
     * @param xmlFile1
     * @param xmlFile2
     * @return
     */
    public static Document mergeXmlFiles(File xmlFile1, File xmlFile2)
    {
        Document xmlDocument1 = getDocumentFromFile(xmlFile1);
        Document xmlDocument2 = getDocumentFromFile(xmlFile2);
        return mergeXmlDocuments(xmlDocument1, xmlDocument2);
    }



    /**
     * merges content from xmlDocument2 into xmlDocument1 (xmlDocument2 will overwrite nodes from xmlDocument1)
     * @param xmlDocument1
     * @param xmlDocument2
     * @return
     */
    public static Document mergeXmlDocuments(Document xmlDocument1, Document xmlDocument2)
    {
        if(xmlDocument1 == null){
            return xmlDocument2;
        }
        else if(xmlDocument2 == null){
            return xmlDocument1;
        }
        Element root1 = xmlDocument1.getRootElement();
        Element root2 = xmlDocument2.getRootElement();
        if( ! root1.getName().equals(root2.getName())){
            // we can't merge files with different root nodes
            return xmlDocument1;
        }

        //List<Element> children = root2.getChildren();
        List<Element> children = new ArrayList(root2.getChildren());

        if(children.size() > 0){
            for(Element child : children)
            {
                // Element childCopy = (Element) child.clone();
                mergeXmlElement(child, root1, true);
            }
        }

        return root1.getDocument();
        // return new Document(root1);  // we need to detach root1 first if we want to return a new Document
    }


    /**
     * merge element into parent
     * Simulates the Varien_Simplexml_Element::extendChild method
     * @param element
     * @param parent
     * @param overwrite if true it will delete the original "element" from parent and use the new one (only if the original has no children)
     * @return
     */
    public static Element mergeXmlElement(Element element, Element parent, boolean overwrite)
    {

        if(element == null || parent == null){
            return parent;
        }

        // detach parent
        //element = (Element)element.clone();
        element.detach();

        String sourceName = element.getName();
        //List<Element> sourceChildren = element.getChildren();
        // we need to create a new static list for avoiding a ConcurrentModificationException in the for loop below
        List<Element> sourceChildren = new ArrayList(element.getChildren());    // static list

//        Iterator itr = (currentElement.getChildren()).iterator();
//        while(itr.hasNext()) {
//            Element oneLevelDeep = (Element)itr.next();
//            List twoLevelsDeep = oneLevelDeep.getChildren();
//            // Do something with these children
//        }

        Element original = parent.getChild(sourceName);
        // if element has no children
        if(sourceChildren.size() == 0)
        {

            if(original != null){
                // if target already has children return without regard
                if(original.getChildren().size() > 0){
                    return parent;
                }
                // new element has no children, original exists without children too, and we are asking for an overwrite
                if(overwrite){
                    // remove it only here (the new one is added later)
                    parent.removeChild(sourceName);
                }
                else {
                    return parent;
                }
            }

            parent.addContent(element);

        }
        else
        {
            if(original == null){
                parent.addContent(element);
            }
            else {
                // we need to merge children
                for(Element child : sourceChildren){
                    Element newParent = original;
                    mergeXmlElement(child, newParent, overwrite);
                }
            }
        }

        return parent;

    }


    /**
     * Converts meaningful xml characters to xml entities
     * (simulates Varien_Simplexml_Element::xmlentities
     *
     * @param  value
     */
    public static String xmlentities(String value)
    {
        if(value == null){
            return null;
        }

        Map<String, String> replacements = new HashMap<String, String>();
        replacements.put("&", "&amp;");
        replacements.put("\"", "&quot;");
        replacements.put("'", "&apos;");
        replacements.put("<", "&lt;");
        replacements.put(">", "&gt;");

        for(Map.Entry<String, String> entry : replacements.entrySet()){
            value = value.replaceAll(entry.getKey(), entry.getValue());
        }
        return value;
    }


    /**
     *
     * @param rootTag
     * @param path relative to root
     * @return
     */
    public static XmlTag findSubTag(@NotNull XmlTag rootTag, String path)
    {
        String[] pathElements = path.split("/");

        XmlTag curTag = rootTag;
        for (String curTagName : pathElements) {
            curTag = curTag.findFirstSubTag(curTagName);
            if (curTag == null) break;
        }
        return curTag;
    }

    /**
     *
     * @param xmlFile
     * @param path absolute Path
     * @return
     */
    public static XmlTag findSubTag(XmlFile xmlFile, String path)
    {
        XmlTag root = xmlFile.getRootTag();
        if(root != null){
            String rootName = root.getName();
            String relativePath = path.substring(rootName.length()+1);
            return findSubTag(root, relativePath);
        }
        return null;
    }


    /**
     *
     * @param xmlFile
     * @param tagName
     * @param tagValue
     * @param path  hierarchy path, this will be the parent of the new tag, ex: root/node1/node2
     * @return
     */
    public static XmlTag createTagInFile(final XmlFile xmlFile, String tagName, String tagValue, String path)
    {
        XmlTag root = xmlFile.getRootTag();

        String[] pathElements = path.split("/");

        if(pathElements.length > 0 && pathElements[0].equals(root.getName()))
        {
            XmlTag lastExistentParent = root;
            String curPath = pathElements[0];
            pathElements = (String[]) ArrayUtils.remove(pathElements, 0); // ArrayUtils.removeElement(pathElements, pathElements[0]);
            for (String curTagName : pathElements)
            {
                lastExistentParent = lastExistentParent.findFirstSubTag(curTagName);
                if (lastExistentParent == null){
                    lastExistentParent = createTagInFile(xmlFile, curTagName, "", curPath);
                    if(lastExistentParent == null){
                        return null;
                    }
                }
                curPath += "/"+curTagName;
            }
            final XmlTag newTag = lastExistentParent.createChildTag(tagName, "", tagValue, false);
            final XmlTag parent = lastExistentParent;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    new WriteCommandAction.Simple(xmlFile.getProject(), "Create Xml Tag in File", xmlFile) {
                        @Override
                        protected void run() {

 //                           newTag = (XmlTag)parent.add(newTag);
                            parent.addAfter(newTag, null);

                        }
                    }.execute();
                }
            };
            runnable.run();
            // PsiDocumentManager.getInstance(xmlFile.getProject()).commitDocument(document);
            return findSubTag(xmlFile, path+"/"+newTag.getName());

        }
        else {
            return null;
        }

    }


}
