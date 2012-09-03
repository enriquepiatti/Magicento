package com.magicento.models.xml.config;

/**
 * @author Enrique Piatti
 */
public class ModulesXmlTag extends MagentoConfigXmlTag {

    public ModulesXmlTag(){
        super();
        name = "modules";
        isRequired = true;

        help = "";
    }

}
