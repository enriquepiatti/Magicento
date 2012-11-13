package com.magicento.models.xml.layout.attribute;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.xml.MagentoXmlAttribute;
import com.magicento.models.xml.layout.BlockChildXmlTag;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Enrique Piatti
 */
public class ActionMethodXmlAttribute extends MagentoLayoutXmlAttribute {

    public ActionMethodXmlAttribute()
    {
        super();
        name = "method";
        help = "Public method name of the Block class (type of the parent block/reference element) to be executed.\n" +
                "The method attribute defines the method name in the block instance and all child elements of the action element are treated as parameters to the method";
    }

    @Override
    public Map<String, String> getPossibleValues() {
        possibleValues = new ArrayList<String>();
        Project project = getProject();
        if(project != null){
            MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
            if(magicento != null){
                String blockType = ((BlockChildXmlTag)getXmlTag()).getParentBlockType();
                if(blockType != null){
                    List<MagentoClassInfo> classes = magicento.findBlocksOfFactoryUri(blockType);
                    if(classes != null){
                        for(MagentoClassInfo classInfo : classes){
                            String className = classInfo.name;
                            List<PsiElement> psiClasses = PsiPhpHelper.getPsiElementsFromClassName(className, project);
                            for(PsiElement psiClass : psiClasses){
                                List<PsiNamedElement> methods = PsiPhpHelper.getAllMethodsFromClass(psiClass, true);
                                for(PsiNamedElement method : methods){
                                    if(PsiPhpHelper.isMethodPublic(method)){
                                        possibleValues.add(method.getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return super.getPossibleValues();
    }
}
