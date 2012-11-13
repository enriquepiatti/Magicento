package com.magicento.models.xml.layout;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.attribute.BlockNameXmlAttribute;
import com.magicento.models.xml.layout.attribute.UpdateHandleXmlAttribute;

import java.util.*;

/**
 * @author Enrique Piatti
 */
public class HandleXmlTag extends MagentoLayoutXmlTag {

    protected Set<String> names;

    @Override
    protected void initChildren() {
        MagentoLayoutXmlTag block = new BlockXmlTag();

        MagentoLayoutXmlTag reference = new ReferenceXmlTag();

        MagentoLayoutXmlTag remove = new RemoveXmlTag();

        MagentoLayoutXmlTag update = new UpdateXmlTag();

        MagentoLayoutXmlTag label = new LabelXmlTag();

        addChild(block);
        addChild(reference);
        addChild(remove);
        addChild(update);
        addChild(label);
    }

    @Override
    protected void initAttributes() {

    }

    @Override
    protected void initName() {
        name = null;
        names = new LinkedHashSet<String>();
    }

    @Override
    protected void initHelp() {
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
        unique = true;
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