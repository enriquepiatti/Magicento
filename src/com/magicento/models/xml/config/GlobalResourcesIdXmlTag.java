package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalResourcesIdXmlTag extends IdXmlTag {

    public GlobalResourcesIdXmlTag(){
        super();
        help = "A unique identifier. This will be the name saved in the core_resource table, and you must create a folder with this name under YourModule/sql/ if you need installer scripts";
    }

    @Override
    public List<String> getPossibleNames() {

        // search similar node ids used in current file first
        addNamesFromCurrentFile("global/models");
        addNamesFromCurrentFile("global/helpers");
        String guessedName = guessIdNameFromCurrentFile();
        if(guessedName != null){
            names.add(guessedName+"_setup");
            names.add(guessedName+"_read");
            names.add(guessedName+"_write");
        }

        // addModuleName();

        // suggest the rest of the model ids (for <rewrite>)
        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
