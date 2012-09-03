package com.magicento.models.xml.config;

import com.intellij.psi.xml.XmlTag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class GlobalModelsIdClassXmlTag extends MagentoConfigXmlTag {

    public GlobalModelsIdClassXmlTag(){

        //name = "class";
        help = "Base name for all models requested with Mage::getModel() and using the parent node name as the first part of the uri";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String name = getModuleName()+"_Model";
        if(isResourceModelUri()){
            name += "_Resource";
        }
        possibleValues.add(name);
        return super.getPossibleValues();
    }

    protected boolean isResourceModelUri()
    {
        XmlTag modelId = getNodeFromContextHierarchy("config/global/models/*");
        if(modelId != null){
            String uri = modelId.getName();
            //XmlTag classNode = getTagFromCurrentFile()
            String xpath = "config/global/models/*/resourceModel[text()=\""+uri+"\"]";
            List<Element> matches = getAllNodesFromCurrentXml(xpath);
            return matches != null && matches.size()>0;
        }
        return false;
    }


}
