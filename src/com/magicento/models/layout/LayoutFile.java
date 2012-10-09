package com.magicento.models.layout;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.magicento.helpers.JavaHelper;

import java.io.File;

/**
 * @author Enrique Piatti
 */
public class LayoutFile
{
    protected File file;
    protected VirtualFile virtualFile;

    protected String area;
    protected String packageName;
    protected String theme;


    public LayoutFile(VirtualFile file)
    {
        virtualFile = file;
        //file = new File(file.getPath());
        this.file = VfsUtil.virtualToIoFile(virtualFile);
    }

    public LayoutFile(File file)
    {
        this.file = file;
        //virtualFile = VirtualFileManager.getInstance().findFileByUrl(file.getAbsolutePath());
        virtualFile = LocalFileSystem.getInstance().findFileByIoFile(file);
        //virtualFile = VfsUtil.findFileByURL(VfsUtil.convertToURL(VfsUtil.pathToUrl(file.getAbsolutePath())))
    }

    public String getArea()
    {
        if(area == null){
            String filePath = file.getAbsolutePath().replace("\\", "/");
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
                String filePath = file.getAbsolutePath().replace("\\", "/");
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
                String filePath = file.getAbsolutePath().replace("\\", "/");
                String regex = "^.*/app/design/"+area+"/"+packageName+"/([^/]+)/.*$";
                theme = JavaHelper.extractFirstCaptureRegex(regex, filePath);
            }
        }
        return theme;
    }

    public File getFile()
    {
        return file;
    }

    public VirtualFile getVirtualFile()
    {
        return virtualFile;
    }

}
