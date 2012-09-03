package com.magicento.helpers;

import com.magicento.MagicentoProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for doing magic things
 * @author Enrique Piatti
 */
public class Magicento {

    /**
     * get the Namespace_Module of the file
     * @param path
     * @return
     */
    public static String getNamespaceModule(String path)
    {
        String[] pools = {"core", "community", "local"};
        String pool = "(" + StringUtils.join(pools, "|") + ")";
        String regex = "/app/code/"+pool+"/([^/]+)/([^/]+)/";
        // regex = "/app/code/(core|community|local)/([^/]+)/([^/]+)/";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(path);
        if (m.find()) {
            String namespace = m.group(2);
            String module = m.group(3);
            return namespace+"_"+module;
        }
        return null;
    }

    public static String getNamespaceModule(File file)
    {
        String path = file.getPath();
        return getNamespaceModule(path);
    }

    public static String getNamespaceModule(VirtualFile file)
    {
        String path = file.getPath();
        return getNamespaceModule(path);
    }

    /**
     * @deprecated
     * @see Magento uc_words
     * simulates the uc_words function from magento
     * @param str
     * @return
     */
    public static String uc_words(String str)
    {
        return Magento.uc_words(str);
    }

    public static File getCachedConfigXml(Project project)
    {
        if(project != null){
            MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
            if(magicentoProject != null){
                return MagicentoProjectComponent.getInstance(project).getCachedConfigXml();
            }
        }
        return null;
    }


    public static String getClassNameFromFilePath(String filePath)
    {
        String[] pools = {"core", "community", "local"};
        String pool = "(" + StringUtils.join(pools, "|") + ")";
        String regex = "/app/code/"+pool+"/(.+)\\.php$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(filePath);
        if (m.find()) {
            String classPath = m.group(2);
            classPath = classPath.replace("/controllers", "").replace("\\controllers", "");
            String className = classPath.replace('/', '_').replace('\\', '_');
            return className;
        }
        return null;
    }



}
