package com.magicento.models.xml.config.system;

import com.magicento.models.xml.config.MagentoConfigXmlTag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class SectionsIdTabXmlTag extends MagentoConfigXmlTag
{

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String xpath = "config/tabs/*";
        List<Element> tabs = getAllNodesFromMergedXml(xpath);
        for(Element tab : tabs)
        {
            possibleValues.add(tab.getName());
        }
        return super.getPossibleValues();
    }
}
