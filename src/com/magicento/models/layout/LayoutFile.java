package com.magicento.models.layout;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.JavaHelper;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

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
            String filePath = getFilePath();
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
                String filePath = getFilePath();
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
                String filePath = getFilePath();
                String regex = "^.*/app/design/"+area+"/"+packageName+"/([^/]+)/.*$";
                theme = JavaHelper.extractFirstCaptureRegex(regex, filePath);
            }
        }
        return theme;
    }

    public boolean isValidLayoutFile()
    {
        return getArea() != null && getPackage() != null && getTheme() != null;
    }

    public File getFile()
    {
        return file;
    }

    public VirtualFile getVirtualFile()
    {
        return virtualFile;
    }

    public String getFilePath()
    {
        return file.getAbsolutePath().replace("\\", "/");
    }

    public boolean isValidPackageAndTheme(Project project)
    {

        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        Set<String> allowedPackages = new LinkedHashSet<String>(settings.getPackages());
        Set<String> allowedThemes = new LinkedHashSet<String>(settings.getThemes());

        // String filePath = getFilePath();
        String currentArea = getArea();
        String currentPackage = getPackage();
        String currentTheme = getTheme();

        boolean isValidPackage = true;
        boolean isValidTheme = true;

        if( ! currentArea.equals("adminhtml"))
        {

            if( ! allowedPackages.isEmpty()){
                isValidPackage = false;
                for(String packageName : allowedPackages){
                    if(packageName.equals(currentPackage))
                    {
                        isValidPackage = true;
                        break;
                    }
                }
            }

            if(isValidPackage && ! allowedThemes.isEmpty()){
                isValidTheme = false;
                for(String theme : allowedThemes){
                    if(theme.equals(currentTheme))
                    {
                        isValidTheme = true;
                        break;
                    }
                }
            }

        }
        return isValidPackage && isValidTheme;
    }

}
