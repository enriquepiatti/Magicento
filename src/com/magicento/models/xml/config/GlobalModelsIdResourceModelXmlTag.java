package com.magicento.models.xml.config;

import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalModelsIdResourceModelXmlTag extends MagentoConfigXmlTag {

    public GlobalModelsIdResourceModelXmlTag(){

        //name = "resourceModel";
        help = "First part of the URI to be used when requesting a ResourceModel using Mage::getResourceModel() using the URI of this model";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        XmlTag modelId = getNodeFromContextHierarchy("config/global/models/*");
        if(modelId != null){
            possibleValues.add(modelId.getName()+"_resource");
        }
        return super.getPossibleValues();
    }
}
