package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalEventsIdObserversIdXmlTag extends IdXmlTag {

    public GlobalEventsIdObserversIdXmlTag(){

        super();
        help = "Observer identifier. This must be a unique value for the event";
    }

    @Override
    public List<String> getPossibleNames() {

        String guessedName = guessIdNameFromCurrentFile();
        if(guessedName != null){
            names.add(guessedName);
        }

        addModuleName();

        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
