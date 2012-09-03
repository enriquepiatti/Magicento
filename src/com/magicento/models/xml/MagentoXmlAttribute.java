package com.magicento.models.xml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class MagentoXmlAttribute extends MagentoXmlElement {

    @Override
    public Map<String, String> getPossibleDefinitions() {
        List<String> names = getPossibleNames();
        if(names.size() > 0){
            // LinkedHashMap to preserve order of insertion
            Map<String, String> values = new LinkedHashMap<String, String>();
            for(String name : names){
                values.put(name, name+"=\"\" ");
            }
            return values;
        }
        return null;
    }
}
