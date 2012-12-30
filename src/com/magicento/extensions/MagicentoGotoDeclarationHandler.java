package com.magicento.extensions;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.*;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.ide.util.gotoByName.GotoClassModel2;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.ProjectScope;
import com.magicento.models.MagentoClassInfo;
import com.magicento.models.layout.LayoutFile;
import com.magicento.models.layout.Template;
import com.magicento.models.xml.MagentoXml;
import com.magicento.models.xml.MagentoXmlFactory;
import com.magicento.models.xml.layout.MagentoLayoutXml;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * extension point for gotoDeclarationHandler
 * @author Enrique Piatti
 */
public class MagicentoGotoDeclarationHandler implements GotoDeclarationHandler
{
    /**
     * this is new in jIDEA 11+ (PHPStorm 4)
     * @param sourceElement
     * @param offset
     * @param editor
     * @return
     */
    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor){
        return getGotoDeclarationTargets(sourceElement, editor);
    }


    public PsiElement[] getGotoDeclarationTargets(PsiElement sourceElement, Editor editor)
    {

        if(editor == null || ! MagicentoProjectComponent.isEnabled(editor.getProject())) {
            return null;
        }

        Project project = editor.getProject();

        if(project == null){
            project = sourceElement.getProject();
        }


        MagicentoSettings settings = MagicentoSettings.getInstance(project);

        List<PsiElement> psiElements = new ArrayList<PsiElement>();

        // if cursor is over a uri factory (it could be anywhere, php, xml, etc not only inside something like Mage::getModel
        if(MagentoParser.isUri(sourceElement))
        {
            String uri = MagentoParser.getUri(sourceElement);
            MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);


            // we are not using this because this gotodeclaration works everywhere, not only in PHP, an this method tries to guess the uri type based on the factory method
            // List<String> classes = magicento.findClassesOfFactory(sourceElement);

            List<MagentoClassInfo> classes = null;
            if(MagentoParser.isBlockUri(sourceElement)){
                classes = magicento.findBlocksOfFactoryUri(uri);
            }
            else if(MagentoParser.isModelUri(sourceElement)){
                classes = magicento.findModelsOfFactoryUri(uri);
            }
            else if(MagentoParser.isResourceModelUri(sourceElement)){
                classes = magicento.findResourceModelsOfFactoryUri(uri);
            }
            else if(MagentoParser.isHelperUri(sourceElement)){
                classes = magicento.findHelpersOfFactoryUri(uri);
            }
            else {
                classes = magicento.findClassesOfFactoryUri(uri);
            }

            // search the psiElement of the classes
            if(classes != null && classes.size()>0)
            {
                List<String> classNames = new ArrayList<String>();
                for(MagentoClassInfo classInfo : classes)
                {
                    classNames.add(classInfo.name);
                }
                psiElements = PsiPhpHelper.getPsiElementsFromClassesNames(classNames, project);
            }
        }
        // is cursor is over a file path (like template paths in layout xml)
        else if(MagentoParser.isFilePath(sourceElement))
        {
            String filePath = MagentoParser.getFilePath(sourceElement);

            List<PsiFile> psiFiles = FileHelper.findPsiFiles(project, filePath);

            // filter by allowed packages and themes
            Iterator<PsiFile> i = psiFiles.iterator();
            while (i.hasNext()) {
                PsiFile file = i.next(); // must be called before i.remove()
                LayoutFile layoutFile = new LayoutFile(file.getVirtualFile());
                if(layoutFile.isValidLayoutFile()){
                    if( ! layoutFile.isValidPackageAndTheme(sourceElement.getProject())) {
                        i.remove();
                    }
                }
            }

            psiElements.addAll(psiFiles);

        }
        // if cursor is over the name of the event in Mage::dispatchEvernt('CURSOR IS HERE')
        else if(MagentoParser.isEventDispatcherName(sourceElement))
        {
            String eventName = MagentoParser.getEventDispatcherName(sourceElement);
            MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
            File configXml = magicento.getCachedConfigXml();

            String xpath = "config/*[name()='global' or name()='frontend' or name()='adminhtml']/events/"+eventName+"/observers/*";
            List<Element> observers = XmlHelper.findXpath(configXml, xpath);
            if(observers != null && observers.size() > 0)
            {
                for(Element observer : observers){
                    Element classElement = observer.getChild("class");
                    Element methodElement = observer.getChild("method");
                    if(classElement != null && methodElement != null){
                        String className =  classElement.getValue();
                        String methodName = methodElement.getValue();
                        List<String> classNames = new ArrayList<String>();
                        if(MagentoParser.isUri(className)){
                            List<MagentoClassInfo> classes = magicento.findModelsOfFactoryUri(className);
                            if(classes != null){
                                for(MagentoClassInfo classInfo : classes){
                                    classNames.add(classInfo.name);
                                }
                            }
                        }
                        else {
                            classNames.add(className);
                        }
                        for(String clazz : classNames){
                            psiElements.addAll( PsiPhpHelper.findMethodInClass(methodName, clazz, project));
                        }
                    }
                }

            }

        }
        else if(settings.layoutEnabled && (MagentoParser.isBlockNameInLayoutXml(sourceElement) || MagentoParser.isBlockNameInTemplate(sourceElement)))
        {
            String blockName = sourceElement.getText().replace("\"", "").replace("'", "");
            if(blockName != null && ! blockName.isEmpty())
            {

                String[] nodeNames = new String[]{"block"};

                XmlTag xmlTag = XmlHelper.getParentOfType(sourceElement, XmlTag.class, true);
                if(xmlTag != null){
                    String currentNodeName = xmlTag.getName();
                    // if it's a <block> search for remove and reference
                    if(currentNodeName.equals("block")){
                        nodeNames = new String[]{"reference", "remove"};
                    }
                }

                MagentoXml xml = MagentoXmlFactory.getInstance(sourceElement);
                if(xml != null && xml instanceof MagentoLayoutXml)
                {
                    MagentoLayoutXml layoutXml = (MagentoLayoutXml)xml;
                    psiElements.addAll( layoutXml.findNodesInOriginalXmlByBlockName(blockName, nodeNames));
                }
            }
        }
        else if(settings.layoutEnabled && MagentoParser.isBlockAliasInTemplate(sourceElement))
        {
            String blockName = sourceElement.getText().replace("\"", "").replace("'", "");
            if(blockName != null && ! blockName.isEmpty())
            {

                String[] nodeNames = new String[]{"block"};

                // check only children block nodes of the current template block node
                Set<String> currentTemplateBlockNames = new HashSet<String>();
                Template template = new Template(sourceElement.getContainingFile().getOriginalFile().getVirtualFile());
                String area = template.getArea();
                if(area != null && ! area.isEmpty())
                {
                    // search blocks for current template
                    List<Element> blocks = template.getBlockElements(sourceElement.getProject());
                    if(blocks != null)
                    {
                        for(Element block : blocks)
                        {
                            String currentTemplateBlockName = block.getAttributeValue("name");
                            if(currentTemplateBlockName != null && ! currentTemplateBlockName.isEmpty())
                            {
                                currentTemplateBlockNames.add(currentTemplateBlockName);
                            }
                        }
                    }

                }

                MagentoXml xml = MagentoXmlFactory.getInstance(sourceElement);
                if(xml != null && xml instanceof MagentoLayoutXml)
                {
                    MagentoLayoutXml layoutXml = (MagentoLayoutXml)xml;
                    // search by alias and also by name, then filter using the currentTemplateBlockNames
                    List<XmlTag> blocks = layoutXml.findNodesInOriginalXmlByBlockAlias(blockName, nodeNames);
                    blocks.addAll(layoutXml.findNodesInOriginalXmlByBlockName(blockName, nodeNames));
                    for(XmlTag block : blocks){
                        String parentBlockName = block.getParentTag().getAttributeValue("name");
                        // add elements if we have parent blocks corresponding to the current template, or if we don't have any parent block for the current template
                        // whis will happen when the template is assigned directly inside the Block class, not inside the layout.xml
                        if(parentBlockName != null && (currentTemplateBlockNames.size() == 0 || currentTemplateBlockNames.contains(parentBlockName))){
                            psiElements.add(block);
                        }
                    }
                }
            }
        }
        else if(settings.layoutEnabled && (MagentoParser.isHandleNode(sourceElement) || MagentoParser.isUpdateHandleInLayoutXml(sourceElement)))
        {
            String handleName = null;
            if(MagentoParser.isHandleNode(sourceElement)){
                XmlTag xmlTag = XmlHelper.getParentOfType(sourceElement, XmlTag.class, false);
                handleName = xmlTag.getName();
            }
            else {
                XmlAttribute attribute = XmlHelper.getParentOfType(sourceElement, XmlAttribute.class, true);
                handleName = attribute.getValue();
            }
            if(handleName != null){
                MagentoXml xml = MagentoXmlFactory.getInstance(sourceElement);
                if(xml != null && xml instanceof MagentoLayoutXml)
                {
                    MagentoLayoutXml layoutXml = (MagentoLayoutXml)xml;
                    psiElements.addAll( layoutXml.findNodesInOriginalXmlByNodeName(handleName));
                }
            }
        }


        if(psiElements.size() > 0)
        {
            PsiElement[] _psiElements = psiElements.toArray(new PsiElement[psiElements.size()]);
            if(psiElements.size() == 1 || sourceElement.getReference() != null){
                // we are checking this because it will fail if there are more elements added by other declaration handlers and the sourceElement.getReference is null
                //       because the IDE uses it for generating the psiElementPopup (I think for the title only...)
                return _psiElements;
            }
//            if(psiElements.size() == 1)
//            {
//                IdeHelper.navigateToPsiElement(psiElements.get(0));
//            }
            String title = "Magicento";
            //NavigationUtil.getPsiElementPopup(_psiElements, new DefaultPsiElementCellRenderer(), title, processor).showInBestPositionFor(editor);

            // TODO: this should be executed only if we are here because an Action (if not we will get "Access is allowed from event dispatch thread only")
            if(EventQueue.isDispatchThread()){
                try{
                    NavigationUtil.getPsiElementPopup(_psiElements, new DefaultPsiElementCellRenderer(), title).showInBestPositionFor(editor);
                }
                catch(Exception e){
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * this is for PHPStorm 4 (jIDEA 11+) compatibility
     * Provides the custom action text
     * @return the custom text or null to use the default text
     * @param context the action data context
     */
    public String getActionText(DataContext context)
    {
        return null;
    }



}
