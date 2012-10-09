package com.magicento.models.xml.layout;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.config.MagentoConfigXmlTag;

import java.util.*;

/**
 * @author Enrique Piatti
 */
public class IdXmlTag extends MagentoLayoutXmlTag {

    protected Set<String> names;

    public IdXmlTag(){
        super();
        name = null;
        names = new LinkedHashSet<String>();
        isRequired = true;

        help = "Handle";
    }

    @Override
    public List<String> getPossibleNames()
    {
        // names.add("default");
        addAllEquivalentNames();
        return new ArrayList<String>(names);
    }

    @Override
    public Map<String, String> getPossibleDefinitions()
    {
        // we need to clear the names because we are reusing this object with different nodes
        names.clear();
        return super.getPossibleDefinitions();
    }


    /**
     * add to names all the sibling tag nodes from the merged config.xml
     */
    protected void addAllEquivalentNames()
    {
        PsiElement context = getContext();
        if(context != null){
            List<XmlTag> parents = XmlHelper.getParents(context);
            String xpath = "";
            if(parents != null){
                for(XmlTag tag : parents){
                    xpath += "/"+tag.getName();
                }
            }
            if( ! xpath.isEmpty()){
                xpath += "/*";
                addAllNamesFrom(xpath);
            }
        }
    }

    /**
     * add to names all the tag names from the merged xml that matches the xpath
     * @param xpath
     */
    protected void addAllNamesFrom(String xpath)
    {
        List<String> nodeNames = getAllNodeNamesFromMergedXml(xpath);
        if(nodeNames != null){
            names.addAll(nodeNames);
        }
    }



}
