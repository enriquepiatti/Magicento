package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.models.layout.Template;
import com.magicento.ui.dialog.CopyTemplateDialog;

import java.io.File;
import java.io.IOException;

/**
 * @author Enrique Piatti
 */
public class CopyTemplateAction extends MagicentoActionAbstract
{
    public void executeAction()
    {

        final Project project = getProject();

        final VirtualFile file = getVirtualFile();
        Template template = new Template(file);

        final CopyTemplateDialog dialog = new CopyTemplateDialog(project, template);
//        UIUtil.invokeAndWaitIfNeeded(new Runnable() {
//            @Override
//            public void run() {
//                dialog.show();
//            }
//        });
//        ok = dialog.isOK();
        dialog.show();
        if( dialog.isOK())
        {
            String packageName = dialog.getPackage();
            String theme = dialog.getTheme();
            MagicentoSettings settings = MagicentoSettings.getInstance(project);
            if(settings != null){
                String pathToMagento = settings.getPathToMagento();
                if(pathToMagento != null && ! pathToMagento.isEmpty())
                {
                    String newFilePath = pathToMagento+"/app/design/"+template.getArea()+"/"+packageName+"/"+theme+"/template/"+template.getRelativePath();
                    final File newFile = new File(newFilePath);
                    if(newFile.exists()){
                        if( ! IdeHelper.prompt("File " + newFilePath + " already exists\nDo you want to override it?", "File already exists")){
                            return;
                        }
                    }

                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run()
                        {
                            try {
                                final VirtualFile newParent = VfsUtil.createDirectories(newFile.getParent());
                                if(newParent != null){
                                    VirtualFile copy = VfsUtilCore.copyFile(this, file, newParent);
                                    openFile(copy);
                                }
                                else {
                                    IdeHelper.logError("Cannot create parent directories for: "+newFile.getAbsolutePath());
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }

            }

        }

    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        VirtualFile file = getVirtualFile();
        Template template = new Template(file);
        return template.isTemplate();
    }
}
