package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalEventsIdXmlTag extends IdXmlTag {

    public GlobalEventsIdXmlTag(){

        super();
        help = "Event identifier. This is the string used when the event is dispatched using: Mage::dispatchEvent('[IDENTIFIER]')";
    }

    @Override
    public List<String> getPossibleNames() {

        addAllEquivalentNames();

        // TODO: search all the events from the code searching "::dispatchEvent(" how to make this fast?

        return new ArrayList<String>(names);
    }

}
