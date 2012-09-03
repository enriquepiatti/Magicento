package com.magicento.models.xml.config;

import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ModulesIdVersionXmlTag extends MagentoConfigXmlTag {

    public ModulesIdVersionXmlTag(){
        super();
        name = "version";
        isRequired = true;

        help = "";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        return null;
    }
}
