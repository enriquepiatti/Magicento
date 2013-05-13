package com.magicento.models.xml.config;

import com.magicento.helpers.Magicento;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
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
            String fullClassName = moduleName+"_Model_Observer";
            possibleValues.add(fullClassName);
            List<Element> modelNodes = getAllNodesFromCurrentXml("/config/global/models/*[class='"+moduleName+"_Model']");
            if(modelNodes != null){
                Element modelNode = modelNodes.get(0);
                String firstPart = modelNode.getName();
                String secondPart = "observer";
                String uri = firstPart+"/"+secondPart;
                possibleValues.add(uri);
            }

        }
        return super.getPossibleValues();
    }
}
