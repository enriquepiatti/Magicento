package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalFieldsetsIdXmlTag extends IdXmlTag {

    public GlobalFieldsetsIdXmlTag(){
        super();
        // help = "Unique identifier";
    }

    @Override
    public List<String> getPossibleNames() {

        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
