package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalTemplateEmailIdTypeXmlTag extends MagentoConfigXmlTag {

    public GlobalTemplateEmailIdTypeXmlTag(){

        super();
        help = "Defines if the body of this email should be sent as text or html";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add("html");
        possibleValues.add("text");
        return super.getPossibleValues();
    }
}
