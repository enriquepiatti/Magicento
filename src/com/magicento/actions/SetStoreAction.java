package com.magicento.actions;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.actions.LayoutCodeDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;
import com.magicento.ui.dialog.CopyTemplateDialog;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Enrique Piatti
 */
public class SetStoreAction extends MagicentoPhpActionAbstract {

    public void executeAction()
    {
        Project project = getProject();

        if(project != null)
        {
            MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
            if(settings != null)
            {
                String store = settings.store;
                List<String> allowedStores = getAllowedStores();
                String allowed = StringUtils.join(allowedStores, ", ");
                store = Messages.showInputDialog(project, "Store code\n(allowed: "+allowed+")", "Set Magento Store", Messages.getQuestionIcon(), store, null);
                settings.store = store;
            }
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        return true;
    }

    protected List<String> getAllowedStores()
    {
        List<String> stores = new ArrayList<String>();
        String result = getMagicentoComponent().executePhpWithMagento("echo implode(',',array_keys(Mage::app()->getStores(true,true)));");
        if(result != null){
            stores = Arrays.asList(result.split(","));
        }
        return stores;
    }
}
