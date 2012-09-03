package com.magicento.models.xml.config;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class CrontabJobsIdRunModelXmlTag extends MagentoConfigXmlTag {

    public CrontabJobsIdRunModelXmlTag(){
        super();
        help = "The model and method to be executed. It must be in the format: 'MODELCLASSNAME::METHODNAME' and MODELCLASSNAME can be the model factory too";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String moduleName = getModuleName();
        if(moduleName == null){
            moduleName = "model_factory_here";
        }

        String methodName = "method_name_here";
        XmlTag cronId = getNodeFromContextHierarchy("config/crontab/jobs/*");
        if(cronId != null){
            methodName = cronId.getName();
        }
        possibleValues.add(moduleName+"_Model_Observer::"+methodName);
        possibleValues.add(StringUtil.toLowerCase(moduleName)+"/observer::"+methodName);

        return super.getPossibleValues();
    }
}
