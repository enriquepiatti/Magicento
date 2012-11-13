package com.magicento.helpers;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ScriptRunnerUtil;
import com.intellij.openapi.project.Project;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import org.apache.commons.httpclient.HttpClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class for executing PHP code
 * @author Enrique Piatti
 */
public class PHP {


    public static String execute(String phpCode, Project project)
    {
        String pathToPhp = "php";
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(settings != null)
        {
            if(settings.useHttp && settings.urlToMagento != null && ! settings.urlToMagento.isEmpty()){
                return executeWithHttp(settings.urlToMagento, phpCode, project);
            }

            if(settings.pathToPhp != null && ! settings.pathToPhp.isEmpty()){
                pathToPhp = settings.pathToPhp;
            }

        }
        return executeWithCommandLine(pathToPhp, phpCode);
    }

    public static String execute(String phpCode)
    {
        return execute(phpCode, null);
    }

    public static String executeWithCommandLine(String pathToPhp, String phpCode)
    {
        if(pathToPhp != null && ! pathToPhp.isEmpty() && phpCode != null && ! phpCode.isEmpty()){

            GeneralCommandLine commandLine = new GeneralCommandLine(pathToPhp, "-r", phpCode);
            try {
                String output = ScriptRunnerUtil.getProcessOutput(commandLine);
                return output;
                //Messages.showMessageDialog(output,"titulo" , Messages.getInformationIcon());
                //ActionManager.createActionPopupMenu and ActionManager.createActionToolbar. To get a Swing component from such an object, simply call the getComponent() method.
            } catch (ExecutionException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return null;
    }


    public static String executeWithHttp(String domainUrl, String phpCode, Project project)
    {
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
        MagicentoSettings settings = MagicentoSettings.getInstance(project);
        if(magicento != null && settings != null){

            String fileName = "eval.php";

            File tempFile = null;
            if(settings.useVarFolder){
                String relativePath = "/var/magicento/";
                String path = settings.getPathToMagento() + relativePath + fileName;
                tempFile = new File(path);
                domainUrl += relativePath + fileName;
            }
            else {
                tempFile = magicento.getCachedFile(fileName);
                domainUrl += "/.idea/magicento/eval.php";
            }

            phpCode = "<?php "+phpCode;
            magicento.saveCacheFile(tempFile, phpCode);

            URL url = null;
            try {
                String result = "";
                url = new URL(domainUrl);
                URLConnection yc = url.openConnection();
                BufferedReader in = new BufferedReader( new InputStreamReader( yc.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    result += inputLine;
                }
                in.close();
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
