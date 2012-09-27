package com.magicento.models.xml;

import com.magicento.helpers.XmlHelper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.SortedList;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/**
 * @author Enrique Piatti
 */
public class MagentoXmlTag extends MagentoXmlElement {

//    protected boolean isUnique = false;
//    protected boolean isLeafNode = false;
    protected List<MagentoXmlTag> children;
    protected List<MagentoXmlAttribute> attributes;

    protected PsiElement context;

    public MagentoXmlTag() {
        super();
        attributes = new ArrayList<MagentoXmlAttribute>();
    }

    public void setContext(PsiElement context) {
        this.context = context;
    }

    public PsiElement getContext() {
        if(context != null){
            return context;
        }
        // if thre isn't any current context, use parent context
        MagentoXmlTag parent = getParent();
        if(parent != null){
            return parent.getContext();    // this is recursive
        }
        else{
            return null;
        }
    }

    public Project getProject() {
        PsiElement context = getContext();
        if(context != null){
            return context.getProject();
        }
        return null;
    }



//    protected Callable<List<String>> possibleNamesCallback;


//    public boolean isUnique() {
//        return isUnique;
//    }
//
//    public void isUnique(boolean unique) {
//        isUnique = unique;
//    }

//    public boolean isLeafNode() {
//        return isLeafNode;
//    }

//    public void setPossibleNamesCallback(Callable<List<String>> possibleNamesCallback) {
//        this.possibleNamesCallback = possibleNamesCallback;
//    }

    public List<MagentoXmlTag> getChildren(){
        return children;
    }


    public MagentoXmlTag addChild(MagentoXmlTag child){
        if(children == null){
            children = new ArrayList<MagentoXmlTag>();
        }
        children.add(child);
        return this;
    }

    public List<MagentoXmlAttribute> getAttributes(){
        return attributes;
    }

    public MagentoXmlTag addAttribute(MagentoXmlAttribute attr){
        if(attributes == null){
            attributes = new ArrayList<MagentoXmlAttribute>();
        }
        attributes.add(attr);
        return this;
    }


    public List<String> getPossibleNames(){
        //Set<String> names = new LinkedHashSet<String>();
        List<String> names = new ArrayList<String>();
        //if(possibleNamesCallback == null && ! getName().isEmpty())
        {
            names.add(getName());
        }
//        else{
//            try {
//                names = possibleNamesCallback.call();
//            } catch (Exception e) {
//                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//            }
//        }
        return names;
    }

    /**
     * when the node can have Multiple Names, name is null and this returns the possible names
     * @return
     */
    public Map<String, String> getPossibleDefinitions(){

        List<String> names = filterDuplicatedNames(getPossibleNames());
        if(names.size() > 0){
            // LinkedHashMap to preserve order of insertion
            Map<String, String> values = new LinkedHashMap<String, String>();
            // TODO: add required attributes
            for(String name : names){
                values.put(name, "<"+name+">"+"</"+name+">" );
            }
            return values;
        }
        return null;
    }

    /**
     * remove tag names existent in the current file
     * @param names
     * @return
     */
    protected List<String> filterDuplicatedNames(List<String> names)
    {
        if(names != null && names.size() > 0)
        {
            XmlTag parent = getParentXmlTagFromContext(true);
            if(parent != null){
                // List<String> existentNames = new ArrayList<String>();
                // parent.getSubTags()
                for(int i=names.size()-1; i>=0; i--){
                    String name = names.get(i);
                    XmlTag subTag = parent.findFirstSubTag(name);
                    if(subTag != null){
                        names.remove(i);
                    }
                }
            }
        }
        return names;
    }

    protected XmlTag getParentXmlTagFromContext(boolean mustBeComplete)
    {
        PsiElement context = getContext();
        if(context != null){
            XmlTag parent = null;
            if( XmlHelper.isXmlTag(context)){
                parent = (XmlTag) context;
            }
            else {
                parent = PsiTreeUtil.getParentOfType(context, XmlTag.class);
            }
            if(mustBeComplete){
                if( XmlHelper.isXmlTagIncomplete(parent))
                {
                    parent = PsiTreeUtil.getParentOfType(parent, XmlTag.class);
                }
            }
            return parent;
        }
        return null;
    }

    /**
     * get node from the context hierarchy, not the merged xml or the full current xml, only the hierarchy from the current node (context)
     * @param path
     * @return
     */
    protected XmlTag getNodeFromContextHierarchy(String path)
    {
        PsiElement context = getContext();
        if(context != null){
            //XmlTag sectionsTag = PsiTreeUtil.getParentOfType(context, XmlTag.class, false);
            List<XmlTag> parents = XmlHelper.getParents(context);
            String previous = "";
            XmlTag matchedTag = null;
            String[] pathElements = path.split("/");
            for(int i=0; i<pathElements.length; i++){
                matchedTag = null;
                if(parents.size() > i){
                    if(pathElements[i].equals("*") || pathElements[i].equals(parents.get(i).getName())){
                        matchedTag = parents.get(i);
                    }
                }
                if(matchedTag == null){
                    return null;
                }
            }
            return matchedTag;
        }
        return null;
    }


    /**
     * returns the XmlTag fron the current config.xml (not merged) with hierarchy defined by path
     * @param path path is not an xpath, is a list of subtags (without "config") separated by "/" ex: "global/blocks"
     */
    protected XmlTag getTagFromCurrentFile(String path)
    {
        PsiElement context = getContext();
        if(context != null){
            XmlFile xmlFile = (XmlFile) context.getContainingFile();
            XmlTag root = xmlFile.getRootTag();
            return XmlHelper.findSubTag(root, path);
        }
        return null;
    }

    /**
     * get all nodes from the merged xml (config.xml, system.xml, etc) matching the xpath
     * @param xpath
     * @return
     */
    @NotNull protected List<Element> getAllNodesFromMergedXml(String xpath)
    {
        List<Element> nodes = new ArrayList<Element>();
        PsiElement context = getContext();
        if(context != null){
            // suggest the rest of the model ids (for <rewrite>)
            //File configFile = Magicento.getCachedConfigXml(context.getProject());
            File configFile = getManager().getMergedXmlFile(context.getProject());
            //String xpath = "/config/global/blocks/* | /config/global/helpers/*";
            //String xpath = "/config/global/models/*";
            nodes = XmlHelper.findXpath(configFile, xpath);
            return nodes;
        }
        return nodes;
    }

    /**
     * get all nodes from the current xml matching the xpath
     * @param xpath
     * @return
     */
    protected List<Element> getAllNodesFromCurrentXml(String xpath)
    {
        PsiElement context = getContext();
        if(context != null){
            // suggest the rest of the model ids (for <rewrite>)
            //File configFile = Magicento.getCachedConfigXml(context.getProject());
            File configFile = new File(context.getContainingFile().getOriginalFile().getVirtualFile().getPath());
            //String xpath = "/config/global/blocks/* | /config/global/helpers/*";
            //String xpath = "/config/global/models/*";
            List<Element> nodes = XmlHelper.findXpath(configFile, xpath);
            return nodes;
        }
        return null;
    }

    /**
     * get all nodes names (sorted alphabetically) from the merged config.xml matching the xpath
     * @param xpath
     * @return
     */
    protected List<String> getAllNodeNamesFromMergedXml(String xpath)
    {
        List<Element> nodes = getAllNodesFromMergedXml(xpath);
        if(nodes != null){
            //List<String> allNames = new SortedList<String>(new ComparableComparator()); // insert these nodes in alphabetical order
            List<String> allNames = new SortedList<String>(String.CASE_INSENSITIVE_ORDER); // insert these nodes in alphabetical order
            for (int i = 0; i < nodes.size(); i++) {
                Element node = nodes.get(i);
                //names.add(node.getName());
                allNames.add(node.getName());
            }
            return allNames;
        }
        return null;
    }


}