package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalResourcesIdSetupClassXmlTag extends MagentoConfigXmlTag {

    public GlobalResourcesIdSetupClassXmlTag(){

        super();
        help = "Class name of the class to be used for installing the scripts of the module referenced in <module> sibling tag. This will be the class reference by $this in your installer scripts";

    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add("Mage_Core_Model_Resource_Setup");
        possibleValues.add("Mage_Eav_Model_Entity_Setup");
        return super.getPossibleValues();
    }
}
