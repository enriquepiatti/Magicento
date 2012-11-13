package com.magicento.models.xml.layout.attribute;

import com.intellij.openapi.project.Project;
import com.magicento.helpers.Magicento;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ActionParameterHelperXmlAttribute extends MagentoLayoutXmlAttribute {

    public ActionParameterHelperXmlAttribute()
    {
        super();
        name = "helper";
        help = "Use the result of this helper method call to fill this parameter.\n" +
                "For example if the value of the helper attribute is:\n" +
                "mymodule/myhelper/myMethod\n"+
                "it will execute:\n"+
                "Mage::helper('mymodule/myhelper')->myMethod();";

    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        possibleValues = new ArrayList<String>();



        return super.getPossibleValues();
    }
}
