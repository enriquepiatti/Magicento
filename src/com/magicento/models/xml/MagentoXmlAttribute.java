package com.magicento.models.xml;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTag;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.helpers.XmlHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Enrique Piatti
 */
public class MagentoXmlAttribute extends MagentoXmlElement {


    // protected MagentoXmlTag xmlTag;

    @Override
    public Map<String, String> getPossibleDefinitions() {
        List<String> names = getPossibleNames();
        if(names.size() > 0){
            // LinkedHashMap to preserve order of insertion
            Map<String, String> values = new LinkedHashMap<String, String>();
            Set<String> currentAttr = getCurrentAttributesInContext();
            for(String name : names){
                // omit attributes already defined
                if( ! currentAttr.contains(name)){
                    values.put(name, name+"=\"\" ");
                }
            }
            return values;
        }
        return null;
    }

    public MagentoXmlTag getXmlTag() {
        return getParent();
    }

//    public void setXmlTag(MagentoXmlTag xmlTag) {
//        this.xmlTag = xmlTag;
//    }

    @NotNull protected Set<String> getCurrentAttributesInContext()
    {
        Set<String> currentAttr = new HashSet<String>();
        PsiElement context = getXmlTag().getContext();
        XmlTag parentTag = PsiTreeUtil.getParentOfType(context, XmlTag.class);
        if(parentTag != null){
            for(XmlAttribute attribute : parentTag.getAttributes()){
                String name = XmlHelper.getAttributeName(attribute);
                if(name != null){
                    currentAttr.add(name);
                }
            }
        }
        return currentAttr;
    }

    public String getPrefix()
    {
        PsiElement context = getXmlTag().getContext();
        String prefix = XmlHelper.getValueOnAutocomplete(context);
        return prefix;
    }

}
