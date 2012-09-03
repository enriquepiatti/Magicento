package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class FrontendTranslateModulesIdXmlTag extends IdXmlTag {

    public FrontendTranslateModulesIdXmlTag(){

        super();
        help = "";
    }

    @Override
    public List<String> getPossibleNames() {

        addModuleName();

        return new ArrayList<String>(names);
    }

}
