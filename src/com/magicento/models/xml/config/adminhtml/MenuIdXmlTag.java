package com.magicento.models.xml.config.adminhtml;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class MenuIdXmlTag extends IdXmlTag {

    @Override
    public String getHelp() {
        return "Unique identifier for the menu";
    }

    @Override
    public List<String> getPossibleNames() {
        names.add("your_custom_menu_identifier_here");
        addAllEquivalentNames();
        return new ArrayList<String>(names);
    }

}
