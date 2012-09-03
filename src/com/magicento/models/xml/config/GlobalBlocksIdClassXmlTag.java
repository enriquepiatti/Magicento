package com.magicento.models.xml.config;

import com.intellij.psi.xml.XmlTag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalBlocksIdClassXmlTag extends MagentoConfigXmlTag {

    public GlobalBlocksIdClassXmlTag(){

        //name = "class";
        help = "Base name for all blocks requested with the parent node name as the first part of the uri";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String name = getModuleName()+"_Block";
        possibleValues.add(name);
        return super.getPossibleValues();
    }

}
