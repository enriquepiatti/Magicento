package com.magicento.extensions;

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
        MagentoXml magentoXml = MagentoXmlFactory.getInstance(originalElement);
        if(magentoXml == null){
            return null;
        }

        if(element instanceof XmlTagParentsFakeElement){
            XmlTagParentsFakeElement fake = (XmlTagParentsFakeElement) element;
            matchedTag = magentoXml.getMatchedTag(fake.getFullPath());   // matchedElement?
        }
        else if(originalElement instanceof XmlTag){
            matchedTag = magentoXml.getMatchedTag(originalElement.getLastChild());
        }
        else if(originalElement instanceof XmlElement){
            matchedTag = magentoXml.getMatchedTag(originalElement);
        }
        if(matchedTag != null){
            return matchedTag.getHelp();
            // TODO: use HTML with colors according to current theme
            //TextAttributes attributes = EditorColorsManager.getInstance().getGlobalScheme().getAttributes(PropertiesHighlighter.PROPERTY_COMMENT).clone();
            //Color background = attributes.getBackgroundColor();
            //GuiUtils.colorToHex(attributes.getForegroundColor())
            // String doc = StringUtil.join(StringUtil.split(text, "\n"), "<br>");
        }
        return null;
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element)
    {
        if(element == null || ! MagicentoProjectComponent.isEnabled(element.getProject())) {
            return null;
        }

        // TODO do this inside MagicentoXmlCompletionContributor when the lookupItem is created?
        if(object instanceof String){
            String name = (String) object;
            XmlTag xmlTagParent = PsiTreeUtil.getParentOfType(element, XmlTag.class);
            if(xmlTagParent != null){
                if(XmlHelper.isXmlTagIncomplete(xmlTagParent)){
                    xmlTagParent = PsiTreeUtil.getParentOfType(xmlTagParent, XmlTag.class);
                }
                if(xmlTagParent != null){
                    // TODO: this is really ugly, but we need the parents hierarchy for finding the right documentation and jIDEA requires a PsiElement ! (this will be changed in the next version of jIDEA I think)
                    XmlTagParentsFakeElement fake = new XmlTagParentsFakeElement();
                    fake.setParent(xmlTagParent);
                    fake.setName(name);
                    return fake;

                    // TODO: this works too, analyze which is better
//                    //XmlTag fake = xmlTagParent.copy();
//                    final XmlTag fake = xmlTagParent;
//                    final XmlTag newTag = XmlElementFactory.getInstance(xmlTagParent.getProject()).createTagFromText('<' + name + "></" + name + '>');
//                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
//                        @Override
//                        public void run() {
//                            fake.addSubTag(newTag, true);
//                        }
//                    });
//                    return XmlUtil.findSubTag( fake, newTag.getName());

                }
            }
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
