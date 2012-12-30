package com.magicento.models.xml.config.modules;

import com.magicento.models.xml.config.MagentoConfigXmlTag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ModulesIdActiveXmlTag extends MagentoConfigXmlTag
{

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add("true");
        possibleValues.add("false");
        return super.getPossibleValues();
    }
}
