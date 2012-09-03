package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalResourcesIdSetupModuleXmlTag extends MagentoConfigXmlTag {

    public GlobalResourcesIdSetupModuleXmlTag(){

        super();
        help = "Module name. Magento will use this to know where is the /sql/ folder to be read when installing this resource";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add(getModuleName());
        return super.getPossibleValues();
    }
}
