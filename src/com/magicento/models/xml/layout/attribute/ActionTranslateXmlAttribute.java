package com.magicento.models.xml.layout.attribute;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlTag;
import com.magicento.helpers.Magicento;
import com.magicento.helpers.XmlHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ActionTranslateXmlAttribute extends MagentoLayoutXmlAttribute {

    public ActionTranslateXmlAttribute()
    {
        super();
        name = "translate";
        help = "The attribute translate is used to define which parameters need to be translated before passing them to the method.\n" +
                "You need to put the name of the child node names of this <action> node separated by whitespaces";

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
