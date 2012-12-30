package com.magicento.models.xml.config.modules;

import com.magicento.models.xml.config.MagentoConfigXmlTag;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ModulesIdCodePoolXmlTag extends MagentoConfigXmlTag
{

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add("local");
        possibleValues.add("community");
        possibleValues.add("core");
        return super.getPossibleValues();
    }
}

