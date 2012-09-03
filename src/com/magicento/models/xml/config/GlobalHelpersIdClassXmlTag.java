package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalHelpersIdClassXmlTag extends MagentoConfigXmlTag {

    public GlobalHelpersIdClassXmlTag()
    {
        super();
        //name = "class";
        help = "Base name for all models requested with Mage::helper() and using the parent node name as the first part of the uri";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String name = getModuleName()+"_Helper";
        possibleValues.add(name);
        return super.getPossibleValues();
    }

}
