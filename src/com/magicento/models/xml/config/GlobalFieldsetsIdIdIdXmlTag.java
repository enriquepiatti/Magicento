package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalFieldsetsIdIdIdXmlTag extends IdXmlTag {

    public GlobalFieldsetsIdIdIdXmlTag(){
        super();
        help = "Aspect of the fieldset, the value of this element is the property name of the target element where the property of the source element will be copied. If the value is an '*' then the source property will be copied to the target property with the same name";
    }

    @Override
    public List<String> getPossibleNames() {

        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

    @Override
    public Map<String, String> getPossibleValues() {

        possibleValues = new ArrayList<String>();
        possibleValues.add("*");
        return super.getPossibleValues();
    }
}
