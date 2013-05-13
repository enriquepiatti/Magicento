package com.magicento.models.xml.layout.attribute;

import com.intellij.openapi.project.Project;
import com.magicento.helpers.Magicento;

import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ActionIfconfigXmlAttribute extends MagentoLayoutXmlAttribute
{

    public ActionIfconfigXmlAttribute()
    {
        super();
        name = "ifconfig";
        help = "Executes this action only if this config value is true.";

    }

    @Override
    public Map<String, String> getPossibleValues()
    {
        // possibleValues = new ArrayList<String>();

        Project project = getProject();
        possibleValues = Magicento.getStoreConfigPaths(project, getPrefix());

        return super.getPossibleValues();
    }
}
