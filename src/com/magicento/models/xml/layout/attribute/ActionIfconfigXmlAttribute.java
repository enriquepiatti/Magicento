package com.magicento.models.xml.layout.attribute;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.Magicento;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.xml.layout.BlockChildXmlTag;

import java.util.ArrayList;
import java.util.List;
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
