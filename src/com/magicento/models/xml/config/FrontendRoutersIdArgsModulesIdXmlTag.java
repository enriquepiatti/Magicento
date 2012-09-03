package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class FrontendRoutersIdArgsModulesIdXmlTag extends IdXmlTag {

    public FrontendRoutersIdArgsModulesIdXmlTag(){

        super();
        // TODO: create code completion for attribute "before"
        help = "Unique Identifier. Is important to define the attribute 'before' in this element." +
                "<br/>If the attribute before has the value: '-' (dash) then magento will try to execute the controller of this module before all the other modules that could be defined for this frontName." +
                "<br/>This is not an override, is a definition for a fallback system used by magento when searching the controller and action";
    }

    @Override
    public List<String> getPossibleNames() {

        addModuleName();

        return new ArrayList<String>(names);
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String moduleName = getModuleName();
        if(moduleName != null){
            possibleValues.add(moduleName);
        }
        return super.getPossibleValues();
    }
}
