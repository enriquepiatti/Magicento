package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalFieldsetsIdIdXmlTag extends IdXmlTag {

    public GlobalFieldsetsIdIdXmlTag(){
        super();
        help = "Property name of the source element to be copied";
    }

    @Override
    public List<String> getPossibleNames() {

        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
