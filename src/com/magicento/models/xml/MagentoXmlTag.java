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


    protected boolean unique = true;

    public MagentoXmlTag() {
        super();
        attributes = new ArrayList<MagentoXmlAttribute>();
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
        child.setParent(this);
        return this;
    }

    public List<MagentoXmlAttribute> getAttributes(){
        return attributes;
    }

    public MagentoXmlTag addAttribute(MagentoXmlAttribute attr){
        if(attributes == null){
            attributes = new ArrayList<MagentoXmlAttribute>();
        }
        //attr.setXmlTag(this);
        attr.setParent(this);
        attributes.add(attr);
        return this;
    }


    @NotNull public List<String> getPossibleNames(){
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
    public Map<String, String> getPossibleDefinitions()
    {
        List<String> names = getPossibleNames();
        if(unique) {
            names = filterDuplicatedNames(names);
        }
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


    public String getAttributeHelp(@NotNull String attrName)
    {
        MagentoXmlAttribute attribute = getAttribute(attrName);
        if(attribute != null){
            return attribute.getHelp();
        }
        return null;
    }

    public MagentoXmlAttribute getAttribute(@NotNull String attrName)
    {
        if(attributes != null){
            for(MagentoXmlAttribute attribute : getAttributes()){
                if( attrName.equals(attribute.getName())){
                    return attribute;
                }
            }
        }
        return null;
    }
}