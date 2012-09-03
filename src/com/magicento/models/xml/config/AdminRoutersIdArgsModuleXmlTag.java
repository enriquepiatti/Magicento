package com.magicento.models.xml.config;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class AdminRoutersIdArgsModuleXmlTag extends MagentoConfigXmlTag {

    public AdminRoutersIdArgsModuleXmlTag(){
        super();
        help = "Module name to be used for searching the controller files when the this router (frontName) is requested." +
                "<br/>You can specify a subfolder inside /controllers/ of the module specified here just adding the name of the folder separated with '_'. "+
                "For example 'Mage_Core_Subfolder' will be translated to 'Mage/Core/controllers/Subfolder/' when searching for the controller filename"+
                "<br/>This value can be overriden by the <modules> sibling.";
    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();
        String moduleName = getModuleName();
        if(moduleName != null){
            possibleValues.add(moduleName+"_Adminhtml");
            possibleValues.add(moduleName);
        }

        return super.getPossibleValues();
    }
}
