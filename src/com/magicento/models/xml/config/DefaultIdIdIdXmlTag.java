package com.magicento.models.xml.config;

import com.intellij.psi.xml.XmlTag;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.magicento.models.xml.MagentoXmlType;
import org.jdom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class DefaultIdIdIdXmlTag extends IdXmlTag {

    public DefaultIdIdIdXmlTag(){

        super();
        help = "You can read the value of this element using Mage::getStoreConfig('NAME_OF_GRANDPARENT_ELEMENT/NAME_OF_PARENT_ELEMENT/NAME_OF_THIS_ELEMENT')";
    }

    @Override
    public List<String> getPossibleNames() {

        XmlTag grandParent = getNodeFromContextHierarchy("config/default/*");
        if(grandParent != null){
            String grandParentName = grandParent.getName();
            XmlTag parent = getParentXmlTagFromContext(false);
            if(parent != null){
                String parentName = parent.getName();
                MagentoXml magentoXml = MagentoXmlFactory.getInstance(MagentoXmlType.SYSTEM, getProject());
                if(magentoXml != null){
                    File systemFile = magentoXml.getMergedXmlFile();
                    List<Element> nodes = XmlHelper.findXpath(systemFile, "config/sections/"+grandParentName+"/groups/"+parentName+"/fields/*");
                    if(nodes != null){
                        for(Element node : nodes){
                            names.add(node.getName());
                        }
                    }
                }
            }
        }

        return new ArrayList<String>(names);
    }

}
