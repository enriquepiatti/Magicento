package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class FrontendRoutersIdXmlTag extends IdXmlTag {

    public FrontendRoutersIdXmlTag(){

        super();
        help = "Unique identifier. This normally is equals to the value of the child <frontName> element";
    }

    @Override
    public List<String> getPossibleNames() {

        names.add("YOUR_NEW_ROUTER_NAME_HERE");
        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
