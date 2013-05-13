package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magicento.extensions.MagicentoTemplateFactory;
import com.magicento.helpers.*;
import com.magicento.ui.dialog.ChooseModuleDialog;
import org.apache.commons.lang.WordUtils;
import org.jdom.Document;
import org.jdom.Element;


import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * @author Enrique Piatti
 */
public class RewriteClassAction extends MagicentoActionAbstract {


    @Override
    public void executeAction()
    {
        final Project project = getProject();

        PsiElement psiElement = getPsiElementAtCursor();
        String className = PsiPhpHelper.getClassName(psiElement);
        if(className.endsWith("_Abstract")){
            IdeHelper.showDialog(project, "Cannot rewrite Abstract Classes", "Magicento Rewrite Class");
            return;
        }

        ChooseModuleDialog dialog = new ChooseModuleDialog(project);
        dialog.show();
        if( dialog.isOK())
        {
            String selectedModulePath = dialog.getSelectedModulePath();
            String selectedPool = dialog.getSelectedPool();
            if(selectedModulePath != null)
            {
                VirtualFile currentFile = getVirtualFile();
                String classNameParts[] = className.split("_");
                if(classNameParts.length > 3)
                {
                    String originalNamespace = classNameParts[0];
                    String originalModuleName = classNameParts[1];
                    String originalFullModuleName = originalNamespace+"_"+originalModuleName;
                    String classType = classNameParts[2];
                    String originalClassPrefix = originalFullModuleName+"_"+classType;
                    String originalFilePath = currentFile.getPath().replace("\\", "/");
                    String regex = "^(.+?/"+classNameParts[0]+"/"+classNameParts[1]+"/).+";
                    String originalModulePath = JavaHelper.extractFirstCaptureRegex(regex,originalFilePath);
                    String originalXmlPath = originalModulePath+"etc/config.xml";
                    File originalConfigXml = new File(originalXmlPath);
                    if(originalConfigXml.exists())
                    {

                        String firstPart = null;
                        String secondPartClassName = null;
                        String secondPart = null;
                        String groupType = null;

                        // search inside config.xml for the class prefix (so we can detect if this is a resource model)
                        // and also we need to know the group name on the xml to add the <rewrite> node there
                        String xpath = "/config/global/*[name()='models' or name()='helpers' or name()='blocks']/*/class[contains(.,'"+originalClassPrefix+"')]";
                        List<Element> originalClassNodes = XmlHelper.findXpath(originalConfigXml, xpath);
                        if(originalClassNodes != null && originalClassNodes.size() > 0)
                        {
                            Element correctNode = null;
                            for(Element originalClassNode : originalClassNodes){
                                String classPrefix = originalClassNode.getValue();
                                if(className.contains(classPrefix) && (correctNode == null || correctNode.getValue().length() < classPrefix.length())){
                                    correctNode = originalClassNode;
                                }
                            }

                            if(correctNode != null){
                                firstPart = correctNode.getParentElement().getName();
                                secondPartClassName = className.substring(correctNode.getValue().length()+1);
                                secondPart = WordUtils.uncapitalize(secondPartClassName.replace("_", " ")).replace(" ", "_");    // MagentoParser.getSecondPartUriFromClassName(className, correctNode.getValue());
                                groupType = correctNode.getValue().substring(originalFullModuleName.length() + 1);
                            }
                            else {
                                IdeHelper.showDialog(project, "Couldn't find class node", "Magicento Rewrite Class");
                                return;
                            }

                        }
                        // try the default nodes from Magento (not declared explicitely on the config.xml)
                        else if(originalNamespace.equals("Mage")){
                            firstPart = originalModuleName.toLowerCase();
                            secondPartClassName = className.substring(originalClassPrefix.length()+1);
                            secondPart = WordUtils.uncapitalize(secondPartClassName.replace("_", " ")).replace(" ", "_");
                            groupType = classType;
                        }
                        else {
                            IdeHelper.showDialog(project, "Couldn't find class node containing "+ originalClassPrefix + " value", "Magicento Rewrite Class");
                            return;
                        }


                        // String originalUri = firstPart+"/"+secondPart;
                        File targetConfigXml = new File(selectedModulePath+"/etc/config.xml");

                        String targetModuleName = MagentoParser.getModuleNameFromModulePath(selectedModulePath);
                        String suggestedClassName = targetModuleName+"_"+groupType+"_"+originalModuleName+"_"+secondPartClassName;
                        final String newClassName = Messages.showInputDialog(project, "New class name", "Write the name of the new class", Messages.getQuestionIcon(), suggestedClassName, null);
                        if(newClassName == null || newClassName.isEmpty() || ! newClassName.startsWith(targetModuleName+"_"+classType)){
                            IdeHelper.showDialog(project, "Wrong class name", "Magicento Rewrite Class");
                            return;
                        }


                        VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(targetConfigXml);
                        final XmlFile psiXmlFile = (XmlFile)PsiManager.getInstance(project).findFile(vFile);

                        if(psiXmlFile != null)
                        {
                            String type = classType.toLowerCase()+"s";
                            String path = "config/global/"+type+"/"+firstPart+"/rewrite";
                            XmlTag newTag = XmlHelper.createTagInFile(psiXmlFile, secondPart, newClassName, path);
                            if(newTag == null){
                                IdeHelper.showDialog(project, "Error trying to create rewrite node in "+psiXmlFile.getVirtualFile().getPath(), "Magicento Rewrite Class");
                                return;
                            }

                            String relativePath = newClassName.substring(targetModuleName.length(),newClassName.lastIndexOf("_")).replace("_", "/");
                            String directoryPath = selectedModulePath + relativePath;
                            PsiFile newClassFile = createClass(directoryPath, newClassName, className);

                            openFile(newClassFile);

                        }


                    }
                    else {
                        IdeHelper.showDialog(project, "Couldn't find: "+originalXmlPath, "Magicento Rewrite Class");
                    }

                }

            }
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        PsiElement psiElement = getPsiElementAtCursor();

        if(psiElement == null)
            return false;

        if( ! PsiPhpHelper.isPhp(psiElement) )
            return false;

        if( MagentoParser.isModel(psiElement) ||
            MagentoParser.isResourceModel(psiElement) ||
            MagentoParser.isCollection(psiElement) ||
            MagentoParser.isBlock(psiElement) ||
            MagentoParser.isHelper(psiElement)
            ) {
            return true;
        }

        return false;
    }


    protected PsiFile createClass(String directoryPath, String className, String parentClassName)
    {
        final Properties properties = new Properties();
        properties.setProperty("CLASSNAME", className);
        properties.setProperty("EXTENDS", parentClassName);

        String[] parts = className.split("_");
        final String fileName = parts[parts.length-1]+".php";

        PsiFile psiFile = MagicentoTemplateFactory.createFromTemplate(directoryPath,
                properties,
                fileName,
                MagicentoTemplateFactory.Template.PhpClass,
                getProject());

        // reformatFile(psiFile);

        return psiFile;
    }
}
