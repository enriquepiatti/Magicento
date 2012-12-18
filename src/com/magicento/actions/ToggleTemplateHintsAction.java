package com.magicento.actions;

import com.intellij.notification.NotificationType;
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
            "$websiteIds = Mage::getModel('core/website')->getCollection()->getAllIds();"+
            "$storeIds = Mage::getModel('core/store')->getCollection()->getAllIds();"+
            "$config = new Mage_Core_Model_Config();"+
            "$pathTemplateHints = 'dev/debug/template_hints';"+
            "$pathBlockHints = 'dev/debug/template_hints_blocks';"+
            "$current = Mage::getStoreConfig($pathTemplateHints);"+
            "$toggle = 1 - $current;"+
            "$config->saveConfig($pathTemplateHints, $toggle, 'default', 0);"+
            "$config->saveConfig($pathBlockHints, $toggle, 'default', 0);"+
            "foreach($websiteIds as $id){"+
                "$config->saveConfig($pathTemplateHints, $toggle, 'websites', $id);"+
                "$config->saveConfig($pathBlockHints, $toggle, 'websites', $id);"+
            "}"+
            "foreach($storeIds as $id){"+
                "$config->saveConfig($pathTemplateHints, $toggle, 'stores', $id);"+
                "$config->saveConfig($pathBlockHints, $toggle, 'stores', $id);"+
            "}"+
            "echo $toggle;";
            String result = magicento.executePhpWithMagento(phpCode);
            String text = result.equals("1") ? "Enabled" : "Disabled";
            // magicento.showMessage("Template hints are now " + text, "Template Hints", MagicentoIcons.MAGENTO_ICON_16x16);
            IdeHelper.showNotification("Template hints are now " + text, NotificationType.INFORMATION, getProject());
        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        return true;
    }
}
