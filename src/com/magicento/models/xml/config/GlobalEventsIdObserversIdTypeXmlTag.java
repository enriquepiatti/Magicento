package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalEventsIdObserversIdTypeXmlTag extends MagentoConfigXmlTag {

    public GlobalEventsIdObserversIdTypeXmlTag(){

        super();
        help = "Chooses how to instantiate this observer as a model (model|object) or as a singleton (by default), also you can disable the observer.";

    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        possibleValues.add("model");
        possibleValues.add("singleton");
        possibleValues.add("disabled");
        possibleValues.add("object");
        return super.getPossibleValues();
    }
}
