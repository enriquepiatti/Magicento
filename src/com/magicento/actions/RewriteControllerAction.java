package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.magicento.extensions.MagicentoTemplateFactory;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.helpers.XmlHelper;
import com.magicento.ui.dialog.RewriteControllerDialog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Enrique Piatti
 */
public class RewriteControllerAction extends MagicentoActionAbstract {

    @Override
    public void executeAction()
    {
        PsiElement currentElement = getPsiElementAtCursor();
        RewriteControllerDialog dialog = new RewriteControllerDialog(getProject(), currentElement);
        dialog.show();
        if(dialog.isOK()){
            String routerName = dialog.getRouterName();
            if(routerName == null){
                IdeHelper.showDialog(getProject(), "Couldn't find the original router definition", "Magicento Rewrite Controller");
                return;
            }
            String originalClassName = PsiPhpHelper.getClassName(currentElement);
            String originalModule = MagentoParser.getNamespaceModuleFromClassName(originalClassName);
            // String originalExtends = PsiPhpHelper.getParentClassName(currentElement);
            String area = dialog.getArea();
            String before = dialog.getBefore();
            // String codePool = dialog.getSelectedPool();
            String targetModule = dialog.getSelectedModule();
            String targetModulePath = dialog.getSelectedModulePath();
            String subfolder = dialog.getSubfolder();
            String originalControllerPrefix = dialog.getRouterModule();
            String controllerName = originalClassName.substring(originalControllerPrefix.length()+1);
            String secondPartName = (subfolder.isEmpty() ? "" : subfolder+"_") + controllerName;
            String suggestedClassName = targetModule+"_"+secondPartName;
            String parentClassName = dialog.getParentClassName();
            String originalFilePath = getVirtualFile().getPath().replace("\\","/");
            String require = parentClassName.equals(originalClassName) ? originalFilePath.substring(originalFilePath.indexOf(originalModule.replace("_", "/"))) : "";
            String targetPath = targetModulePath+"/controllers/"+secondPartName.replace("_", "/")+".php";
            File targetControllerFile = new File(targetPath);
            if(targetControllerFile.exists()){
                IdeHelper.showDialog(getProject(), "Error: the file "+targetPath+" already exists", "Magicento Rewrite Controller");
                return;
            }
            File targetConfigXml = new File(targetModulePath+"/etc/config.xml");

            VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(targetConfigXml);
            final XmlFile psiXmlFile = (XmlFile) PsiManager.getInstance(getProject()).findFile(vFile);

            if(psiXmlFile != null)
            {
                String path = "config/"+area+"/routers/"+routerName+"/args/modules";
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("before", before);
                XmlTag newTag = XmlHelper.createTagInFile(psiXmlFile, targetModule.toLowerCase(), targetModule+(subfolder.isEmpty() ? "" : "_"+subfolder), path, attributes);
                if(newTag == null){
                    IdeHelper.showDialog(getProject(), "Error trying to create routers node in "+psiXmlFile.getVirtualFile().getPath(), "Magicento Rewrite Controller");
                    return;
                }

                PsiFile newClassFile = createControllerFile(targetPath, suggestedClassName, parentClassName, require);
                openFile(newClassFile);

            }

        }

    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        setEvent(e);
        PsiElement psiElement = getPsiElementAtCursor();

        if(psiElement == null || ! isPhp()){
            return false;
        }

        return MagentoParser.isController(psiElement);
    }


    protected PsiFile createControllerFile(String fullPath, String className, String parentClassName, String require)
    {
        final Properties properties = new Properties();
        properties.setProperty("CLASSNAME", className);
        properties.setProperty("EXTENDS", parentClassName);
        properties.setProperty("REQUIRE", require);

        String[] parts = fullPath.split("/");
        final String fileName = parts[parts.length-1];
        String directoryPath = fullPath.substring(0, fullPath.length()-fileName.length()-1);

        PsiFile psiFile = MagicentoTemplateFactory.createFromTemplate(directoryPath,
                properties,
                fileName,
                MagicentoTemplateFactory.Template.Controller,
                getProject());

        // reformatFile(psiFile);

        return psiFile;
    }

}
