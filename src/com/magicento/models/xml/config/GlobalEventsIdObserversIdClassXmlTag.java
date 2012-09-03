package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalEventsIdObserversIdClassXmlTag extends MagentoConfigXmlTag {

    public GlobalEventsIdObserversIdClassXmlTag(){

        super();
        help = "Class name (or model factory uri) to be instantiated when the event is dispatched";

    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String moduleName = getModuleName();
        if(moduleName != null){
            possibleValues.add(moduleName+"_Model_Observer");
        }
        return super.getPossibleValues();
    }
}
