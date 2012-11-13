package com.magicento.models.xml;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.SortedList;
import com.magicento.helpers.XmlHelper;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author Enrique Piatti
 */
abstract public class MagentoXmlElement {

    protected boolean isRequired = false;
    // TODO: this is weird, the type is a child class !
    protected MagentoXmlTag parent;

    protected Callable<Map<String, String>> possibleValuesCallback;

    protected List<String> possibleValues;

    protected String name;
    protected String help;

    protected MagentoXml manager;

    protected PsiElement context;


//    public MagentoXmlElement() {
//        possibleValues = new ArrayList<String>();
//    }

    public boolean isRequired() {
        return isRequired;
    }
    public void isRequired(boolean required) {
        isRequired = required;
    }

    public String getName() {
        return name;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPossibleValuesCallback(Callable<Map<String, String>> possibleValuesCallback) {
        this.possibleValuesCallback = possibleValuesCallback;
    }

    public MagentoXmlTag getParent(){
        return parent;
    }

    public void setParent(MagentoXmlTag parent){
        this.parent = parent;
    }


    public List<String> getPossibleNames(){
        //Set<String> names = new LinkedHashSet<String>();
        List<String> names = new ArrayList<String>();
        names.add(getName());
        return names;
    }

    /**
     * when the node can have Multiple Names, name is null and this returns the possible names
     * @return
     */
    abstract public Map<String, String> getPossibleDefinitions();

    /**
     * mapping nameToBeShownOnPopup => codeInsetrtedInCompletion
     * by default it uses possibleValuesCallback or possibleValues if they are not null
     * this is for leaf nodes
     * @return
     */
    public Map<String, String> getPossibleValues(){
        if(possibleValuesCallback != null){
            try {
                return possibleValuesCallback.call();
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        else if(possibleValues != null){
            Map<String, String> map = new LinkedHashMap<String, String>();
            for(String value : possibleValues){
                if(value != null){
                    map.put(value, value);
                }
            }
            return map;
        }
        return null;
    }


    /**
     * documentation for this node
     * @return
     */
    public String getHelp(){
        return help;
    }


    public MagentoXml getManager() {

        if(manager != null){
            return manager;
        }
        // if thre isn't any current context, use parent context
        MagentoXmlElement parent = getParent();
        if(parent != null){
            return parent.getManager();    // this is recursive
        }
        else{
            return null;
        }

    }

    public void setManager(MagentoXml manager) {
        this.manager = manager;
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


    protected XmlTag getCurrentPsiXmlTag()
    {
        PsiElement context = getContext();
        if(context != null){
            return XmlHelper.getParentOfType(context, XmlTag.class, false);
        }
        return null;
    }

    protected XmlTag getCurrentParentXmlTag()
    {
        XmlTag currentTag = getCurrentPsiXmlTag();
        if(currentTag != null){
            return currentTag.getParentTag();
        }
        return null;
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
    @NotNull
    protected List<Element> getAllNodesFromMergedXml(String xpath)
    {
        List<Element> nodes = new ArrayList<Element>();
        PsiElement context = getContext();
        if(context != null){
            // suggest the rest of the model ids (for <rewrite>)
            //File configFile = Magicento.getCachedConfigXml(context.getProject());
            File configFile = getManager().getMergedXmlFile(/*context.getProject()*/);
            //String xpath = "/config/global/blocks/* | /config/global/helpers/*";
            //String xpath = "/config/global/models/*";
            nodes = XmlHelper.findXpath(configFile, xpath);
            if(nodes == null){
                nodes = new ArrayList<Element>();
            }
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

    public Project getProject()
    {
        PsiElement context = getContext();
        if(context != null){
            return context.getProject();
        }
        return null;
    }


}
