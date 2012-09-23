package com.magicento.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diff.DiffBundle;
import com.intellij.openapi.diff.DiffManager;
import com.intellij.openapi.diff.DiffRequest;
import com.intellij.openapi.diff.SimpleDiffRequest;
import com.intellij.openapi.diff.actions.CompareFiles;
import com.intellij.openapi.diff.ex.DiffContentFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Enrique Piatti
 */
public class CompareWithOriginalAction extends CompareFiles implements IMagicentoAction
{

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        //final Project project = e.getData(PlatformDataKeys.PROJECT);
        final VirtualFile currentFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if(currentFile != null){
            return getOriginalFile(currentFile) != null;
        }
        return false;
    }


    public static final DataKey<DiffRequest> DIFF_REQUEST = DataKey.create("CompareFiles.DiffRequest");

    public void update(AnActionEvent e)
    {
//        Presentation presentation = e.getPresentation();
//        boolean canShow = isAvailable(e);
//        if (ActionPlaces.isPopupPlace(e.getPlace())) {
//            presentation.setVisible(canShow);
//        }
//        else {
//            presentation.setVisible(true);
//            presentation.setEnabled(canShow);
//        }
    }

    private static boolean isAvailable(AnActionEvent e)
    {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        DiffRequest diffRequest = e.getData(DIFF_REQUEST);
        if (diffRequest == null) {
            final VirtualFile[] virtualFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
            if (virtualFiles == null || virtualFiles.length != 2) {
                return false;
            }
            diffRequest = getDiffRequest(project, virtualFiles);
        }
        if (diffRequest == null) {
            return false;
        }
        return !diffRequest.isSafeToCallFromUpdate() || DiffManager.getInstance().getDiffTool().canShow(diffRequest);
    }

    protected DiffRequest getDiffData(DataContext dataContext)
    {
        final Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        final DiffRequest diffRequest = DIFF_REQUEST.getData(dataContext);
        if (diffRequest != null) {
            return diffRequest;
        }
        VirtualFile currentFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
        VirtualFile original = getOriginalFile(currentFile);
        if(original != null){
            final VirtualFile[] data = {currentFile, original};
            if (data == null || data.length != 2) {
                return null;
            }
            return getDiffRequest(project, data);
        }
        return null;
    }

    @Nullable
    private static DiffRequest getDiffRequest(Project project, VirtualFile[] files)
    {
        if (files == null || files.length != 2) return null;
        String title = DiffBundle.message("diff.element.qualified.name.vs.element.qualified.name.dialog.title",
                getVirtualFileContentTitle(files[0]),
                getVirtualFileContentTitle(files[1]));
        SimpleDiffRequest diffRequest = DiffContentFactory.compareVirtualFiles(project, files[0], files[1], title);
        if (diffRequest == null) return null;
        diffRequest.setContentTitles(getVirtualFileContentTitle(files [0]),
                getVirtualFileContentTitle(files [1]));
        return diffRequest;
    }


    protected VirtualFile getOriginalFile(@NotNull VirtualFile file)
    {
//        String extension = file.getExtension();
//        if(extension.equals("php")){
//            return getOriginalCoreFile(file);
//        }
//        else if(extension.equals("phtml")){
//            return getOriginalTemplateFile(file);
//        }
        String fullPath = file.getPath().replace("\\", "/");
        if(fullPath.contains("/app/code/")){
            return getOriginalCoreFile(file);
        }
        else if(fullPath.contains("/app/design/")){
            return getOriginalTemplateFile(file);
        }
        return null;
    }

    private VirtualFile getOriginalTemplateFile(VirtualFile file)
    {
        String fullPath = file.getPath().replace("\\", "/");
        String newPath = "";
        if(fullPath.contains("/app/design/adminhtml/")){
            newPath = fullPath.replaceAll("/app/design/adminhtml/.*?/.*?/", "/app/design/adminhtml/default/default/");
        }
        else if(fullPath.contains("/app/design/frontend/")){
            newPath = fullPath.replaceAll("/app/design/frontend/.*?/.*?/", "/app/design/frontend/base/default/");
        }

//        Pattern pattern = Pattern.compile("/app/design/(.*?)/.*?/.*?/");
//        Matcher matcher = pattern.matcher(fullPath);
//        if(matcher.find()){
//            String group = matcher.group(1);
//            if(group.equals("frontend")){
//                String newPath = matcher.replaceAll("/app/design/$1/base/default/");
//            }
//            else if(group.equals("adminhtml")){
//                String newPath = matcher.replaceAll("/app/design/$1/default/default/");
//            }
//        }

        if( ! newPath.isEmpty()){
            return LocalFileSystem.getInstance().findFileByIoFile(new File(newPath));
        }
        return null;
    }

    private VirtualFile getOriginalCoreFile(VirtualFile file)
    {
        String fullPath = file.getPath().replace("\\", "/");
        String newPath = fullPath.replaceAll("/app/code/.*?/", "/app/code/core/");
        return LocalFileSystem.getInstance().findFileByIoFile(new File(newPath));
    }

}


