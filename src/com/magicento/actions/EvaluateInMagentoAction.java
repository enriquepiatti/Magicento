package com.magicento.actions;

import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

/**
 * @author Enrique Piatti
 */
public class EvaluateInMagentoAction extends MagicentoActionAbstract {

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        //setEvent(e);
        return true; // getSelectedText() != null;
    }

    public void actionPerformed(AnActionEvent e) {
        setEvent(e);

        MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
        if(settings != null && settings.enabled)
        {
            String className = null;
            if(settings.phpEnabled){
                String selectedText = getSelectedText();
                String inputText = Messages.showInputDialog(getProject(), "Code to evaluate", "Evaluate in Magento" , Messages.getQuestionIcon(), selectedText, null);
                if( inputText != null && inputText != "" ) {
                    String php = "var_dump("+inputText+");";
                    String output = getMagicentoComponent().executePhpWithMagento(php);
                    IdeHelper.showDialog(output, "Result of code evaluated in Magento");
                }
            }
            else {
                IdeHelper.showDialog("You need to enable PHP in magicento settings", "Feature available with PHP enabled only");
            }
        }


    }

}
