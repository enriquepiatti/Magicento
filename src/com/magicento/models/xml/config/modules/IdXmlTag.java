package com.magicento.models.xml.config.modules;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class IdXmlTag extends com.magicento.models.xml.config.IdXmlTag {


    @Override
    public List<String> getPossibleNames() {
        //names = new ArrayList<String>();
        names.add("YOURNAMESPACE_YOURMODULE");
        // addAllEquivalentNames();
        return new ArrayList<String>(names);
    }

}
