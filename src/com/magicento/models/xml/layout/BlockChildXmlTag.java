package com.magicento.models.xml.layout;

import com.intellij.psi.xml.XmlTag;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
abstract public class BlockChildXmlTag extends MagentoLayoutXmlTag {

    public BlockChildXmlTag(boolean initChildren) {
        super(initChildren);
    }

    public BlockChildXmlTag() {
        super();
    }

    public String getParentBlockName()
    {
        XmlTag currentParent = getCurrentParentXmlTag();
        if(currentParent != null){
            return currentParent.getAttributeValue("name");
        }
        return null;
    }

    @NotNull
    public List<Element> getSiblings()
    {
        List<Element> siblings = new ArrayList<Element>();
        String parentName = getParentBlockName();
        if(parentName != null){
            String xpath = "//*[@name='"+parentName+"']/block";
            siblings = getAllNodesFromMergedXml(xpath);
        }
        return siblings;
    }

    public String getParentBlockType()
    {
        XmlTag currentParent = getCurrentParentXmlTag();
        if(currentParent != null){
            String type = currentParent.getAttributeValue("type");
            if(type == null){
                String blockName = currentParent.getAttributeValue("name");
                if(blockName != null){
                    String xpath = "//block[@name='"+blockName+"']";
                    List<Element> blocks = getAllNodesFromMergedXml(xpath);
                    if(blocks != null){
                        for(Element block : blocks){
                            type = block.getAttributeValue("type");
                            if(type != null){
                                break;
                            }
                        }
                    }
                }
            }
            return type;
        }
        return null;
    }


}
