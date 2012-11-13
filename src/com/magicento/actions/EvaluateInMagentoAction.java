package com.magicento.actions;

import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * @author Enrique Piatti
 */
public class EvaluateInMagentoAction extends MagicentoPhpActionAbstract {

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        //setEvent(e);
        return true; // getSelectedText() != null;
    }

    public void executeAction()
    {
        String selectedText = getSelectedText();
        String inputText = Messages.showInputDialog(getProject(), "Code to evaluate", "Evaluate in Magento" , Messages.getQuestionIcon(), selectedText, null);
        if( inputText != null && inputText != "" ) {
            String php = inputText;
            MagicentoSettings settings = getMagicentoSettings();
            if(settings != null)
            {
                if(settings.useVarDump){
                    php = "var_dump("+php+");";

                }
                String output = getMagicentoComponent().executePhpWithMagento(php);
                if(settings.useVarDump && settings.useHttp){
                    // strip HTML tags because if xdebug has xdebug.overload_var_dump on (and html_errors is on too) it will beautify the var_dump output
                    output = output.replaceAll("\\<.*?>","");
                }
                IdeHelper.showDialog(getProject(), output, "Result of code evaluated in Magento");
            }
        }
    }

}
