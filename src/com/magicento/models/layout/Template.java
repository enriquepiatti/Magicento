package com.magicento.models.layout;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.magicento.helpers.JavaHelper;

import java.io.File;

/**
 * @author Enrique Piatti
 */
public class Template {

    protected File templateFile;
    protected VirtualFile templateVirtualFile;
    protected String area;
    protected String packageName;
    protected String theme;

    public Template(VirtualFile file)
    {
        templateVirtualFile = file;
        //templateFile = new File(file.getPath());
        templateFile = VfsUtil.virtualToIoFile(templateVirtualFile);
    }

    public Template(File file)
    {
        templateFile = file;
        //templateVirtualFile = VirtualFileManager.getInstance().findFileByUrl(file.getAbsolutePath());
        templateVirtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        //templateVirtualFile = VfsUtil.findFileByURL(VfsUtil.convertToURL(VfsUtil.pathToUrl(file.getAbsolutePath())))
    }

    public boolean isTemplate()
    {
        return templateFile != null && templateFile.exists() && templateFile.isFile() && FileUtil.getExtension(templateFile.getAbsolutePath()).equals("phtml");
    }

    public String getArea()
    {
        if(area == null){
            String filePath = templateFile.getAbsolutePath().replace("\\", "/");
            String regex = "^.*/app/design/([^/]+)/.*$";
            area = JavaHelper.extractFirstCaptureRegex(regex, filePath);
        }
        return area;
    }

    public String getPackage()
    {
        if(packageName == null)
        {
            String area = getArea();
            if(area != null){
                String filePath = templateFile.getAbsolutePath().replace("\\", "/");
                String regex = "^.*/app/design/"+area+"/([^/]+)/.*$";
                packageName = JavaHelper.extractFirstCaptureRegex(regex, filePath);
            }
        }
        return packageName;
    }

    public String getTheme()
    {
        if(theme == null){
            String area = getArea();
            String packageName = getPackage();
            if(area != null && packageName != null){
                String filePath = templateFile.getAbsolutePath().replace("\\", "/");
                String regex = "^.*/app/design/"+area+"/"+packageName+"/([^/]+)/.*$";
                theme = JavaHelper.extractFirstCaptureRegex(regex, filePath);
            }
        }
        return theme;
    }

    public File getFile()
    {
        return templateFile;
    }

    public VirtualFile getVirtualFile()
    {
        return templateVirtualFile;
    }

    public String getRelativePath()
    {
        String area = getArea();
        String packageName = getPackage();
        String theme = getTheme();
        if(area != null && packageName != null && theme != null){
            String filePath = templateFile.getAbsolutePath().replace("\\", "/");
            String regex = "^.*/app/design/"+area+"/"+packageName+"/"+theme+"/template/(.*)$";
            return JavaHelper.extractFirstCaptureRegex(regex, filePath);
        }
        return null;
    }

}
