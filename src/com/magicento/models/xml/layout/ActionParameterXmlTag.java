package com.magicento.models.xml.layout;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.attribute.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class ActionParameterXmlTag extends MagentoLayoutXmlTag {
    @Override
    protected void initChildren() {

    }

    @Override
    protected void initAttributes()
    {
        MagentoXmlAttribute helper = new ActionParameterHelperXmlAttribute();
        addAttribute(helper);
    }

    @Override
    protected void initName() {
        name = null;
    }

    @Override
    protected void initHelp() {
        help = "Action method parameter. The node name can be anything but the order of every node corresponds to the order on the parameter list of the method";
    }

    @NotNull
    @Override
    public List<String> getPossibleNames()
    {
        List<String> names = new ArrayList<String>();
        String method = getActionMethod();
        String className = getActionBlockClassName();
        if(className != null){
            if(method == null){
                names.add("Couldn't find the method of the parent action");
            }
            else
            {
                List<PsiElement> psiMethods = PsiPhpHelper.findMethodInClass(method, className, getProject(), true);
                if(psiMethods.size() > 0){
                    PsiElement psiMethod = psiMethods.get(0);
                    List<PsiElement> parameters = PsiPhpHelper.getMethodParameters(psiMethod);
                    for(PsiElement parameter : parameters){
                        String parameterName = PsiPhpHelper.getParameterName(parameter);
                        if(parameterName != null){
                            // TODO: show only next allowed parameter? (corresponding to the current order)
                            names.add(parameterName.substring(1));  // remove "$"
                        }
                    }
                }
            }
        }
        return names;
    }


    protected XmlTag getActionXmlTag()
    {
        return getCurrentPsiXmlTag();
    }

    protected String getActionBlockType()
    {
        ActionXmlTag actionModel = (ActionXmlTag)getParent();
        if(actionModel != null){
            return actionModel.getParentBlockType();
        }
        return null;
    }

    protected String getActionBlockClassName()
    {
        String blockType = getActionBlockType();
        if(blockType != null){
            MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(getProject());
            List<MagentoClassInfo> classes = magicento.findBlocksOfFactoryUri(blockType);
            if(classes != null && classes.size() > 0){
                MagentoClassInfo classInfo = classes.get(0);
                return classInfo.name;
            }
        }
        return null;
    }

    protected String getActionMethod()
    {
        XmlTag action = getActionXmlTag();
        if(action != null){
            return action.getAttributeValue("method");
        }
        return null;
    }


}
