package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class GlobalTemplateEmailIdXmlTag extends IdXmlTag {

    public GlobalTemplateEmailIdXmlTag(){

        super();
        help = "A unique identifier for the template email, if you want to use this template inside the System > Configuration " +
                "using adminhtml/system_config_source_email_template as the source model then you must use the correct identifier" +
                " according to the field path in system.xml: for example if you get the value with getStoreConfig('my_section/my_group/my_field')" +
                " then you should use here 'my_section_my_group_my_field'";
    }

    @Override
    public List<String> getPossibleNames() {

        addAllEquivalentNames();

        return new ArrayList<String>(names);
    }

}
