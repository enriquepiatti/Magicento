package com.magicento.extensions;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElementType;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.psi.XmlTagParentsFakeElement;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.MagentoXmlTag;
import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;

import java.util.List;

/**
 * documentation for XML nodes
 * @author Enrique Piatti
 */
public class MagicentoXmlDocumentationProvider extends AbstractDocumentationProvider {

    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return null;
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return null;
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement)
    {
        if( ! MagicentoSettings.getInstance(element.getProject()).enabled){
            return null;
        }

        MagentoXmlTag matchedTag = null;
        String attrName = null;

        MagentoXml magentoXml = MagentoXmlFactory.getInstance(originalElement);
        if(magentoXml == null){
            return null;
        }

        if(element instanceof XmlTagParentsFakeElement){
            XmlTagParentsFakeElement fake = (XmlTagParentsFakeElement) element;
            matchedTag = magentoXml.getMatchedTag(fake.getFullPath());   // matchedElement?
            attrName = fake.getAttribute();
        }
        else if(originalElement instanceof XmlTag){
            matchedTag = magentoXml.getMatchedTag(originalElement.getLastChild());
        }
        else if(originalElement instanceof XmlElement){
            matchedTag = magentoXml.getMatchedTag(originalElement);
            if( XmlHelper.isAttribute(originalElement) ){
                XmlAttribute attr = XmlHelper.getParentOfType(originalElement, XmlAttribute.class, false);
                if(attr != null){
                    attrName = XmlHelper.getAttributeName(attr);
                }
            }
        }
        if(matchedTag != null){
            String help = "";
            if( attrName != null ){
                help = matchedTag.getAttributeHelp(attrName);
            }
            else {
                help = matchedTag.getHelp();
            }
            return help;
            // TODO: use HTML with colors according to current theme
            //TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(PropertiesHighlighter.PROPERTY_COMMENT).clone();
            //Color background = attributes.getBackgroundColor();
            //GuiUtils.colorToHex(attributes.getForegroundColor())
            // String doc = StringUtil.join(StringUtil.split(text, "\n"), "<br>");
        }
        return null;
    }

    /**
     * When we are requesting documentation from the lookupitem, the PsiElement for that item does'nt exist yet, because it wasn't inserted.
     * That's why we need an extra param (object in this case) to identify the correct node
     * @param psiManager
     * @param object
     * @param element
     * @return
     */
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element)
    {
        if(element == null || ! MagicentoProjectComponent.isEnabled(element.getProject())) {
            return null;
        }

        // TODO do this inside MagicentoXmlCompletionContributor when the lookupItem is created?
        if(object instanceof XmlTagParentsFakeElement){
            return (XmlTagParentsFakeElement)object;
        }

        return null;

//        TODO: use XmlElementDescriptor ?
//        final XmlTag tagFromText = XmlElementFactory.getInstance(psiManager.getProject()).createTagFromText("<modules/>");
//        final XmlElementDescriptor tagDescriptor = tagFromText.getDescriptor();
//        return tagDescriptor != null ? tagDescriptor.getDeclaration() : null;


    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return null;
    }

}
