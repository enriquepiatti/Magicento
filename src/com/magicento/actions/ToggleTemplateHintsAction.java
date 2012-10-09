package com.magicento.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.magicento.MagicentoIcons;
import com.magicento.MagicentoProjectComponent;
import com.magicento.helpers.IdeHelper;

/**
 * @author Enrique Piatti
 */
public class ToggleTemplateHintsAction extends MagicentoPhpActionAbstract
{
    public void executeAction()
    {
        MagicentoProjectComponent magicento = getMagicentoComponent();
        if(magicento != null)
        {
            String phpCode =
            "$config = new Mage_Core_Model_Config();"+
            "$pathTemplateHints = 'dev/debug/template_hints';"+
            "$pathBlockHints = 'dev/debug/template_hints_blocks';"+
            "$current = Mage::getStoreConfig($pathTemplateHints);"+
            "$toggle = 1 - $current;"+
            "$config->saveConfig($pathTemplateHints, $toggle, 'websites', 1);"+
            "$config->saveConfig($pathBlockHints, $toggle, 'websites', 1);"+
            "$config->saveConfig($pathTemplateHints, $toggle, 'default', 0);"+
            "$config->saveConfig($pathBlockHints, $toggle, 'default', 0);"+
            "echo $toggle;";
            String result = magicento.executePhpWithMagento(phpCode);
            String text = result.equals("1") ? "Enabled" : "Disabled";
            magicento.showMessage("Template hints are now " + text, "Template Hints", MagicentoIcons.MAGENTO_ICON_16x16);
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        return true;
    }
}
