package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalResourcesIdConnectionUseXmlTag extends MagentoConfigXmlTag {

    public GlobalResourcesIdConnectionUseXmlTag(){

        super();
        help = "Reference to another connection identifier to be used when this connection is requested, by default if there isn't any connection for this identifier it will use core_setup";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        List<String> resources = getAllNodeNamesFromMergedXml("//global/resources/*");
        if(resources != null){
            possibleValues = resources;
        }
        return super.getPossibleValues();
    }

}
