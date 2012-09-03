package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalBlocksIdXmlTag extends IdXmlTag {

    public GlobalBlocksIdXmlTag(){

        super();
        help = "A unique identifier, this is the \"group\" (first) part used in the type attribute of the <block> element in the layout files";
    }

    @Override
    public List<String> getPossibleNames() {

        // search similar node ids used in current file first
        addNamesFromCurrentFile("global/models");
        addNamesFromCurrentFile("global/helpers");
        String guessedName = guessIdNameFromCurrentFile();
        if(guessedName != null){
            names.add(guessedName);
        }

        addModuleName();

        // suggest the rest of the model ids (for <rewrite>)
        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
