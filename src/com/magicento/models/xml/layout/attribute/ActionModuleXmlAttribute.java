package com.magicento.models.xml.layout.attribute;

import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ActionModuleXmlAttribute extends MagentoLayoutXmlAttribute {

    public ActionModuleXmlAttribute()
    {
        super();
        name = "module";
        help = "Module scope to be used for translating the action parameters.\n" +
                "This is an optional attribute and it's only valid when you have defined a translate attribute too.";

    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();

        return super.getPossibleValues();
    }
}
