package com.magicento.extensions;

import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
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
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.helpers.XmlHelper;
import com.magicento.models.MagentoClassInfo;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
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
        else if(MagentoParser.isFilePath(sourceElement)){
            String filePath = MagentoParser.getFilePath(sourceElement);

            int start = filePath.lastIndexOf('/');
            String fileName = filePath.substring(start == -1 ? 0 : start+1);
            // FilenameUtils.getName()

            PsiFile[] psiFiles = FilenameIndex.getFilesByName(project, fileName, ProjectScope.getProjectScope(project));

            if(psiFiles.length > 0){
                for(PsiFile psiFile : psiFiles){
                    String fullPath = psiFile.getVirtualFile().getPath();
                    if(fullPath.endsWith(filePath)){
                        psiElements.add(psiFile);
                    }
                }
            }

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
