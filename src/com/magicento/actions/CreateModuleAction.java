package com.magicento.actions;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PsiNavigateUtil;
import com.magicento.MagicentoSettings;
import com.magicento.extensions.MagicentoTemplateFactory;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.Magento;
import com.magicento.helpers.Magicento;
import com.magicento.models.layout.Template;
import com.magicento.ui.dialog.CopyTemplateDialog;
import com.magicento.ui.dialog.NewModuleDialog;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Enrique Piatti
 */
public class CreateModuleAction extends MagicentoActionAbstract {

    protected String pathToMagento;
    protected String pathToModuleFolder;

    public void executeAction()
    {

        final Project project = getProject();

        MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
        pathToMagento = settings.getPathToMagento();
        if(pathToMagento == null || pathToMagento.isEmpty()){
            IdeHelper.logError("Cannot find path to magento, make sure you have setted the path to Mage.php in Settings > Magicento");
            return;
        }

        final NewModuleDialog dialog = new NewModuleDialog(project);

        dialog.show();
        if( dialog.isOK())
        {
            String codePool = dialog.getCodePool();
            String namespace = dialog.getNamespace();
            String module = dialog.getModule();
            String moduleName = namespace+"_"+module;
            String version = dialog.getVersion();
            String group = dialog.getGroup();
            String[] depends = dialog.getDepends();
            boolean includeHelper = dialog.includeHelper();
            boolean includeBlock = dialog.includeBlock();
            boolean includeModel = dialog.includeModel();
            boolean includeInstaller = dialog.includeInstaller();

            PsiFile moduleXml = createModuleXmlFile(moduleName, codePool, depends);
            if(moduleXml != null){

                setModuleFolder(moduleName, codePool);
                if(group.isEmpty()){
                    group = StringUtil.toLowerCase(moduleName);
                }
                PsiFile configXml = createConfigXmlFile(moduleName, version, group, includeBlock, includeHelper, includeModel, includeInstaller);
                // assert configXml != null;
                if(configXml != null)
                {
                    if(includeBlock){
                        createFolder(pathToModuleFolder+"/Block");
                    }
                    if(includeModel){
                        createFolder(pathToModuleFolder+"/Model");
                    }
                    if(includeHelper){
                        createHelper(moduleName);
                    }
                    if(includeInstaller){
                        createInstaller(group, version);
                    }
                    openFile(configXml);
                }
            }

        }
    }

    protected PsiFile createInstaller(String group, String version)
    {
        final String fileName = "mysql4-install-"+version+".php";

        String directoryPath = pathToModuleFolder+"/sql/"+group+"_setup";
        PsiFile psiFile = MagicentoTemplateFactory.createFromTemplate(directoryPath,
                null,
                fileName,
                MagicentoTemplateFactory.Template.Installer,
                getProject());

        // reformatFile(psiFile);

        return psiFile;
    }

    protected PsiFile createHelper(String moduleName)
    {
        final Properties properties = new Properties();
        String className = moduleName+"_Helper_Data";
        properties.setProperty("CLASSNAME", className);

        final String fileName = "Data.php";

        String directoryPath = pathToModuleFolder+"/Helper";
        PsiFile psiFile = MagicentoTemplateFactory.createFromTemplate(directoryPath,
                properties,
                fileName,
                MagicentoTemplateFactory.Template.Helper,
                getProject());


        // reformatFile(psiFile);

        return psiFile;
    }

    protected void createFolder(String folder)
    {
        try {
            VfsUtil.createDirectories(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected PsiFile createConfigXmlFile(String moduleName, String version, String group, boolean includeBlock,
                                          boolean includeHelper, boolean includeModel, boolean includeInstaller)
    {

        final Properties properties = new Properties();
        properties.setProperty("MODULENAME", moduleName);
        properties.setProperty("VERSION", version);
        properties.setProperty("GROUP", group);
        properties.setProperty("INCLUDEBLOCK", (includeBlock ? "1" : ""));
        properties.setProperty("INCLUDEMODEL", (includeModel ? "1" : ""));
        properties.setProperty("INCLUDEHELPER", (includeHelper ? "1" : ""));
        properties.setProperty("INCLUDEINSTALLER", (includeInstaller ? "1" : ""));

        final String fileName = "config.xml";

        String directoryPath = pathToModuleFolder+"/etc";
        PsiFile psiFile = MagicentoTemplateFactory.createFromTemplate(directoryPath,
                properties,
                fileName,
                MagicentoTemplateFactory.Template.ConfigXml,
                getProject());


        reformatFile(psiFile);

        return psiFile;

    }

    protected void setModuleFolder(String moduleName, String codePool)
    {
        String[] parts = moduleName.split("_");
        if(parts == null || parts.length != 2){
            IdeHelper.logError("Module name: "+ moduleName + " is not valid");
        }
        pathToModuleFolder = pathToMagento+"/app/code/"+codePool+"/"+parts[0]+"/"+parts[1];
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        return true;
    }


    protected PsiFile createModuleXmlFile(String name, String codePool, String[] depends)
    {
        String dependsText = "";
        if(depends != null && depends.length > 0)
        {
            for(String depend : depends){
                dependsText += "<"+depend+"/>\n";
            }
        }

        final Properties properties = new Properties();
        properties.setProperty("MODULENAME", name);
        properties.setProperty("CODEPOOL", codePool);
        properties.setProperty("DEPENDS", dependsText);

        final String fileName = name+".xml";

        String directoryPath = pathToMagento+"/app/etc/modules";
        PsiFile psiFile = MagicentoTemplateFactory.createFromTemplate(directoryPath,
                                                                      properties,
                                                                      fileName,
                                                                      MagicentoTemplateFactory.Template.ModuleXml,
                                                                      getProject());


        reformatFile(psiFile);

        return psiFile;

    }

    protected void reformatFile(final PsiFile psiFile)
    {
        if(psiFile != null){
            final CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(getProject());
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run()
                {
                    codeStyleManager.reformat(psiFile);
                }
            });
        }
    }

}




