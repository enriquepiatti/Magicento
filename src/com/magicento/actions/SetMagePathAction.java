package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.PHP;

import java.io.File;

/**
 * @author Enrique Piatti
 */
public class SetMagePathAction extends MagicentoActionAbstract
{
    /**
     * forces the setting to Mage.php, useful if the magicento form in the settings IDE is not working properly
     */
    public void executeAction()
    {

        Project project = getProject();
        if(project != null)
        {
            MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
            if(settings != null)
            {

                String pathToMage = settings.getPathToMage();

                pathToMage = Messages.showInputDialog(project, "Absolute path to Mage.php", "Path to Mage.php" , Messages.getQuestionIcon(), pathToMage ,null);

                if(pathToMage != null && ! pathToMage.isEmpty())
                {
                    File f = new File(pathToMage);
                    while( ! f.isFile() )
                    {
                        IdeHelper.showDialog(project, pathToMage + " is not correct!", "Path to Mage.php incorrect", Messages.getInformationIcon());
                        pathToMage = Messages.showInputDialog(_project, "Absolute path to Mage.php (empty or cancel for disabling magicento on this project)", "Path to Mage.php" , Messages.getQuestionIcon(), pathToMage ,null);

                        // if user removes the path, disable Magicento for this project
                        if( pathToMage == null || pathToMage.isEmpty() ) {
                            getMagicentoComponent().disableMagicento();
                            break;
                        }

                        f = new File(pathToMage);

                    }

                    settings.setPathToMage(pathToMage);
                }

            }
        }

    }

    @Override
    public Boolean isApplicable(AnActionEvent e)
    {
        return true;
    }
}
