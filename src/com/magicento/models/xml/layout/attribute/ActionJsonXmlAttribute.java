package com.magicento.models.xml.layout.attribute;

import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ActionJsonXmlAttribute extends MagentoLayoutXmlAttribute {

    public ActionJsonXmlAttribute()
    {
        super();
        name = "json";
        help = "The attribute json is used to define which parameters will be passed as a json string (magento will apply json_decode automatically).\n" +
                "This could be useful for passing arrays as parameters for example (although this is possible without json too...)";

    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();

        XmlTag actionTag = getCurrentPsiXmlTag();
        XmlTag[] subTags = actionTag.getSubTags();
        if(subTags != null){
            for(XmlTag subTag : subTags){
                possibleValues.add(subTag.getName());
            }
        }

        return super.getPossibleValues();
    }
}
