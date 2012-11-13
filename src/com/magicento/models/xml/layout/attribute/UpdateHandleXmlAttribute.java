package com.magicento.models.xml.layout.attribute;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class UpdateHandleXmlAttribute extends MagentoLayoutXmlAttribute {

    public UpdateHandleXmlAttribute()
    {
        super();
        name = "handle";
        help = "Handle to include";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        possibleValues = new ArrayList<String>();

        String xpath = "layout/*";
        List<Element> handles = getAllNodesFromMergedXml(xpath);

        if(handles != null){
            for(Element handle : handles){
                possibleValues.add(handle.getName());
            }
        }

        return super.getPossibleValues();
    }
}
