package com.magicento.helpers;

import com.magicento.MagicentoSettings;
import org.apache.tools.ant.util.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class FileHelper
{

    @NotNull
    public static List<String> getParentDirectories(@NotNull File file)
    {
        List<String> directories = new ArrayList<String>();
        File directory = file.isDirectory() ? file : file.getParentFile();
        while(directory.isDirectory()){
            directories.add(0, directory.getName());
        }
        return directories;
    }

    @NotNull
    public static String[] getSubdirectories(@NotNull File file)
    {
        // FileUtils.getPathStack();
        // file.getAbsolutePath().replace("\\", "/").split("/");
        if(file.exists() && file.isDirectory()){
            return file.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (new File(dir+"/"+name).isDirectory());
                }
            });
        }
        return new String[]{};
    }

}
