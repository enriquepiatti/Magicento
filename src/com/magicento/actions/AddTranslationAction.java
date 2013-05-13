package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.MagentoParser;
import com.magicento.helpers.PsiPhpHelper;
import com.magicento.models.layout.Template;
import com.magicento.ui.dialog.AddTranslationDialog;
import com.magicento.ui.dialog.CopyTemplateDialog;

import java.io.File;
import java.io.IOException;

/**
 * @author Enrique Piatti
 */
public class AddTranslationAction extends MagicentoActionAbstract {

    @Override
    public void executeAction()
    {
        final Project project = getProject();

        PsiElement currentElement = getPsiElementAtCursor();

        String originalText = currentElement.getText();
        if(originalText != null && ! originalText.isEmpty())
        {
            // remove quotes
            originalText = originalText.replaceAll("(^['\"])|(['\"]$)","");
            if(PsiPhpHelper.isElementType(currentElement, PsiPhpHelper.SINGLE_QUOTED_STRING)){
                originalText = originalText.replaceAll("\\\\'","'");
            }
            else {
                originalText = originalText.replaceAll("\\\\\"","\"");
            }
            AddTranslationDialog dialog = new AddTranslationDialog(project, originalText, getPsiElementAtCursor());
            dialog.show();
            if( dialog.isOK())
            {
                String csvFilePath = dialog.getSelectedCsvFilePath();
                String csvFileName = dialog.getSelectedCsvFileName();
                String directoryPath = dialog.getSelectedCsvFileDirectory();
                String translatedText = dialog.getTranslatedText();
                // allow change of original text
                originalText = dialog.getOriginalText();

                String textToAdd = "\""+originalText.replace("\"", "\"\"")+"\", \""+translatedText.replace("\"", "\"\"")+"\"";

                // create folder if not exists
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
                        // if file doesn't exist, create it
                        PsiFile psiFile = psiDirectory.findFile(csvFileName);
                        if(psiFile == null)
                        {
                            final PsiFileFactory factory = PsiFileFactory.getInstance(getProject());
                            final PsiFile file = factory.createFileFromText(csvFileName, PlainTextFileType.INSTANCE,textToAdd);
                            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                                @Override
                                public void run()
                                {
                                    psiDirectory.add(file);
                                }
                            });
                        }
                        else {
                            try {
                                VfsUtil.saveText(psiFile.getOriginalFile().getVirtualFile(), psiFile.getText()+"\n"+textToAdd);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        psiFile = psiDirectory.findFile(csvFileName);
                        if(psiFile != null){
                            openFile(psiFile);
                        }
                    }
                }
            }
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        setEvent(e);
        PsiElement currentElement = getPsiElementAtCursor();
        if(currentElement != null && PsiPhpHelper.isString(currentElement))
        {
            // this actiion works only if cursor is over the parameter list of ->__([HERE])
            PsiElement element = null;
            if(PsiPhpHelper.isParameterList(currentElement)){
                element = currentElement;
            }
            if(element == null){
                element = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.PARAMETER_LIST);
            }
            if(element != null)
            {
                PsiElement methodReference = PsiPhpHelper.findFirstParentOfType(currentElement, PsiPhpHelper.METHOD_REFERENCE);
                if(methodReference != null){
                    if(MagentoParser.isMethod(methodReference, "__")){
                        return true;
                    }
                }
            }
        }
        return false;
    }


}
