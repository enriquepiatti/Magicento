package com.magicento.models.xml.config.adminhtml;

import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class IdXmlTag extends com.magicento.models.xml.config.IdXmlTag {


    @Override
    public List<String> getPossibleNames() {
        //names = new ArrayList<String>();
        names.add("your_custom_identifier_here");
        addAllEquivalentNames();
        return new ArrayList<String>(names);
    }

}
