package com.magicento;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.magicento.helpers.IdeHelper;
import com.magicento.helpers.JavaHelper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

@State(
        name = "MagicentoApplicationComponent",
        storages = {
                @Storage(
                        file = StoragePathMacros.APP_CONFIG + "/magicento.xml"
                )}
)
public class MagicentoApplicationComponent implements PersistentStateComponent<MagicentoApplicationComponent>, ApplicationComponent {


    public boolean TUTORIAL_MESSAGE_WAS_SHOWN = false;
    protected String TUTORIAL_MESSAGE = "IMPORTANT!!!\n" +
            "please check this brief tutorial for solving problems with Magicento and take advantage of all the features\n" +
            "http://www.magicento.com/#features";


    public MagicentoApplicationComponent()
    {
    }


    public static MagicentoApplicationComponent getInstance() {
        return ApplicationManager.getApplication().getComponent(MagicentoApplicationComponent.class);
    }

    /**
     * Use this method if you are not sure is application initialized or not
     * @return MagicentoApplicationComponent instance or default values
     */
    public static MagicentoApplicationComponent getShadowInstance() {
        Application application = ApplicationManager.getApplication();
        return application != null ? getInstance() : new MagicentoApplicationComponent();
    }


    public MagicentoApplicationComponent getState() {
        return this;
    }

    public void loadState(MagicentoApplicationComponent object)
    {
        XmlSerializerUtil.copyBean(object, this);
    }



    @NonNls
    @NotNull
    public String getComponentName() {
        return "MagicentoApplicationComponent";
    }

    public void initComponent() {

        if ( ! TUTORIAL_MESSAGE_WAS_SHOWN)
        {
            JavaHelper.delay(1500, new Callable() {
                @Override
                public Object call() throws Exception {
                    IdeHelper.showDialog(null, TUTORIAL_MESSAGE, "Magicento");
                    TUTORIAL_MESSAGE_WAS_SHOWN = true;
                    return null;
                }
            });
        }
    }

    public void disposeComponent() {
    }
}
