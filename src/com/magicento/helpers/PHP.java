package com.magicento.helpers;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ScriptRunnerUtil;
import com.magicento.MagicentoSettings;

/**
 * Class for executing PHP code
 * @author Enrique Piatti
 */
public class PHP {

    public static String execute(String phpCode)
    {
        String pathToPhp = "php";
        MagicentoSettings settings = MagicentoSettings.getInstance();
        if(settings != null){
            if(settings.pathToPhp != null && ! settings.pathToPhp.isEmpty()){
                pathToPhp = settings.pathToPhp;
            }
        }
        return execute(pathToPhp, phpCode);
    }

    public static String execute(String pathToPhp, String phpCode)
    {
        if(pathToPhp != null && ! pathToPhp.isEmpty() && phpCode != null && ! phpCode.isEmpty()){
            // TODO: this won't work if the server in on a VM, we need SSH in that case, and we need to implement the connection by ourselves with jsch for example
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

}
