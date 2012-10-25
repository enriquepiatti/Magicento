package com.magicento.extensions;

import com.intellij.ide.fileTemplates.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.magicento.MagicentoIcons;
import com.magicento.helpers.IdeHelper;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Enrique Piatti
 */
public class MagicentoTemplateFactory implements FileTemplateGroupDescriptorFactory {

    public enum Template
    {
        ModuleXml("magicento_module.xml"),
        ConfigXml("magicento_config.xml"),
        Helper("magicento_helper.php"),
        Installer("magicento_installer.php"),
        PhpClass("magicento_class.php");

        String file;
        Template(String file) {
            this.file = file;
        }

        public String getFile() {
            return file;
        }
    }

    public FileTemplateGroupDescriptor getFileTemplatesDescriptor()
    {
        String title = "Magicento";
        final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(title, MagicentoIcons.MAGENTO_ICON_16x16);
        group.addTemplate(new FileTemplateDescriptor(Template.PhpClass.getFile(), MagicentoIcons.MAGENTO_ICON_16x16));

        return group;
    }

    public static PsiFile createFromTemplate(final PsiDirectory directory, Properties customProperties, String fileName, Template template)
    {

        String templateName = template.getFile();

        FileTemplate fileTemplate;
        if(template == Template.PhpClass) {
            fileTemplate = FileTemplateManager.getInstance().getJ2eeTemplate(templateName);
        }
        else {
            fileTemplate = FileTemplateManager.getInstance().getInternalTemplate(templateName);
        }

        Properties properties = new Properties(FileTemplateManager.getInstance().getDefaultProperties());
        if(customProperties != null){
            properties.putAll(customProperties);
        }

        String text;
        try {
            text = fileTemplate.getText(properties);
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to load template for " + templateName, e);
        }

        final PsiFileFactory factory = PsiFileFactory.getInstance(directory.getProject());
        final PsiFile file = factory.createFileFromText(fileName, text);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
            @Override
            public void run()
            {
                directory.add(file);
            }
        });

        return directory.findFile(fileName);

    }

    public static PsiFile createFromTemplate(String directoryPath, Properties customProperties, String fileName, Template template, Project project)
    {
        VirtualFile directory = LocalFileSystem.getInstance().findFileByIoFile(new File(directoryPath));
        if(directory == null){
            try {
                directory = VfsUtil.createDirectories(directoryPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(directory != null)
        {
            final PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(directory);
            if(psiDirectory != null)
            {
                if(psiDirectory.findFile(fileName) == null)
                {
                    return createFromTemplate(psiDirectory, customProperties, fileName, template);
                }
                else {
                    String message = "File " + directoryPath + "/" + fileName + " already exists";
                    IdeHelper.logError(message);
                    IdeHelper.showDialog(project, message, "Cannot create new file");
                }
            }
        }
        return null;
    }
}
