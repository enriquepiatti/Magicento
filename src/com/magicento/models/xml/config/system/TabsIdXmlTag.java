package com.magicento.models.xml.config.system;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class TabsIdXmlTag extends IdXmlTag {

    @Override
    public List<String> getPossibleNames() {
        names.add("your_custom_tab_identifier_here");
        //addAllNamesFrom("config/tabs/*");
        addAllEquivalentNames();
        return new ArrayList<String>(names);
    }

}
