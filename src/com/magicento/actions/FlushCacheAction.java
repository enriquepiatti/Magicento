package com.magicento.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.magicento.MagicentoIcons;
import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.magicento.helpers.IdeHelper;

/**
 * @author Enrique Piatti
 */
public class FlushCacheAction extends MagicentoPhpActionAbstract
{
    @Override
    public void executeAction()
    {
        MagicentoProjectComponent magicento = getMagicentoComponent();
        if(magicento != null)
        {

            String removeFPC =
                    "$options = Mage::app()->getConfig()->getNode('global/full_page_cache');" +
                    "if($options){" +
                    "    $options = $options->asArray();" +
                    "    if( ! empty($options['backend_options']['cache_dir'])){" +
                    "        $cache_dir = Mage::getBaseDir('var') . DS . $options['backend_options']['cache_dir'];" +
                    "        if(strlen($cache_dir) > 4){"+
                    "            Mage_System_Dirs::rm($cache_dir);" +
                    "        }"+
                    "    }" +
                    "}";

            MagicentoSettings settings = MagicentoSettings.getInstance(getProject());
            if(settings != null)
            {
                if(settings.useHttp){
                    removeFPC = "";
                }
                String phpCode =
                        "Mage::app()->getCacheInstance()->flush();"+
                                "Mage::app()->cleanCache();"+
                                "if(class_exists('Enterprise_PageCache_Model_Cache')){" +
                                "Enterprise_PageCache_Model_Cache::getCacheInstance()->clean(Enterprise_PageCache_Model_Processor::CACHE_TAG);"+
                                "Mage::app()->getCacheInstance()->cleanType('full_page');"+
                                "}"+
                                removeFPC+
                                "echo '1';";
                String result = magicento.executePhpWithMagento(phpCode);
                String text = result.equals("1") ? "Magento and Storage cache flushed" : "Error flushing the cache";
                // magicento.showMessage(text, "Flush Cache", MagicentoIcons.MAGENTO_ICON_16x16);
                IdeHelper.showNotification(text, NotificationType.INFORMATION, getProject());

            }
            else {
                String text = "Magicento Settings not found";
                // magicento.showMessage(text, "Flush Cache", MagicentoIcons.MAGENTO_ICON_16x16);
                IdeHelper.showNotification(text, NotificationType.ERROR, getProject());
            }

        }
    }

    @Override
    public Boolean isApplicable(AnActionEvent e) {
        return true;
    }
}
