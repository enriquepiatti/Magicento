package com.magicento.helpers;

import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Enrique Piatti
 */
public class JavaHelper {

    public static boolean classExists(String fullClassName)
    {
        try {
            //Class.forName(fullClassName, false, null);    // this is not working I don't know why
            Class.forName(fullClassName);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static <T> T factory(String fullClassName, Class<T> clazz)
    {
        try {
            T instance = (T)Class.forName(fullClassName).newInstance();
            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public static boolean testRegex(String regex, String test)
    {
        Pattern myPattern = Pattern.compile(regex);
        Matcher myMatcher = myPattern.matcher(test);
        return myMatcher.find();
    }

    public static String extractFirstCaptureRegex(String regex, String text)
    {
        Pattern myPattern = Pattern.compile(regex);
        Matcher myMatcher = myPattern.matcher(text);
        if (myMatcher.find())
        {
            return myMatcher.group(1);
        }
        return null;
    }



    /**
     *
     * @param dirPath
     * @param regex filename regex
     * @return
     */
    public static File[] getAllFilesFromDirectory(String dirPath, final String regex)
    {
        final File dir = new File(dirPath);
//        if(! dir.exists() || ! dir.isDirectory()){        // dir.list will check this automatically
//            return null;
//        }

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File file, String name) {
                // file is the original "dir" here !
                return JavaHelper.testRegex(regex, name);
            }
        };
        //children = dir.list(filter);
        File[] children = dir.listFiles(filter);

        return children;

//        if (children == null || children.length == 0) {
//            return null;
//        }

    }

    public static String camelCase(String original, String separator)
    {
        return WordUtils.uncapitalize(WordUtils.capitalize(original.replace(separator, " ")).replace(" ", ""));
    }

}
