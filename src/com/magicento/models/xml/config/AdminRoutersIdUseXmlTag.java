package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class AdminRoutersIdUseXmlTag extends MagentoConfigXmlTag {

    public AdminRoutersIdUseXmlTag(){
        super();
        help = "Router to use for this new frontName";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add("admin");

        // TODO: read other routers defined in the config.xml with <area>admin</area>

        return super.getPossibleValues();
    }
}
