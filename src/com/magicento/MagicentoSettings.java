package com.magicento;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.magicento.helpers.IdeHelper;
import org.jetbrains.annotations.Nullable;

import java.io.File;

@State(
        name = "MagicentoSettings",    // must be equal to the class name I think
        storages = {
                @Storage(id = "default", file = "$PROJECT_FILE$"),
                @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/magicento.xml", scheme = StorageScheme.DIRECTORY_BASED)
        }
)
public class MagicentoSettings implements PersistentStateComponent<MagicentoSettings> {

    // we are using getters and setters for this property but still using public so it's saved automatically by the serializer
    public String pathToMage;
    public boolean enabled = true;
    public boolean phpEnabled = false;
    public boolean useHttp = false;
    public String pathToPhp;
    public String urlToMagento;
    public String store = "";

    protected String relativePathToMage = "/app/Mage.php";

    public static MagicentoSettings getInstance() {
        return getInstance(guessProject());
    }

    public static MagicentoSettings getInstance(Project project) {
        if(project == null){
            project = guessProject();
            if(project == null){
                IdeHelper.logError("Cannot find Magicento settings for null project");
                return null;
            }
        }
        MagicentoSettings settings = ServiceManager.getService(project, MagicentoSettings.class);
        if(settings == null){
            IdeHelper.logError("Cannot find Magicento Settings");
        }
        return settings;
    }

    @Override
    public MagicentoSettings getState() {
        return this;
    }

    @Override
    public void loadState(MagicentoSettings state)
    {
        try{
            XmlSerializerUtil.copyBean(state, this);
        }
        catch(Exception e){
            IdeHelper.logError(e.getMessage());
            IdeHelper.showDialog("Cannot read the saved settings for Magicento. Please try saving them again from File > Settings > Magicento", "Error in Magicento Settings");
        }
        if(pathToMage == null || pathToMage.isEmpty())
        {
            String defaultPathToMagento = getDefaultPathToMagento();
            if( defaultPathToMagento != null){
                setPathToMage(defaultPathToMagento + relativePathToMage);
            }
            else {
                enabled = false;
            }
        }
    }

    public String getDefaultPathToMagento(Project project)
    {
        MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
        if(magicento != null){
            return magicento.getDefaultPathToMagento();
        }
        else {
            IdeHelper.logError("Cannot find MagicentoProjectComponent instance");
        }
        return null;
    }

    public String getDefaultPathToMagento()
    {
        Project guessed = guessProject();
        if(guessed != null){
            return getDefaultPathToMagento(guessed);
        }
        else {
            IdeHelper.logError("Cannot guess the current project");
        }
        return null;
    }

    @Nullable
    private static Project guessProject() {
        //final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        //return openProjects.length == 1 ? openProjects[0] : null;
        return ProjectUtil.guessCurrentProject(null);
    }


    public void setPathToPhp(String path)
    {
        if(path != null){
            path = path.trim().replace("\\", "/");
        }
        pathToPhp = path;
    }

    /**
     * Path to the root of Magento
     * @return
     */
    public String getPathToMagento()
    {
        if(pathToMage != null && pathToMage.length() > relativePathToMage.length()){
            return pathToMage.substring( 0, pathToMage.length() - relativePathToMage.length() );
        }
        return null;
    }

    /**
     * Path to Mage.php
     * @return
     */
    public String getPathToMage()
    {
        return pathToMage;
    }

    /**
     *
     * @param path absolute path to Mage.php
     */
    public void setPathToMage(String path)
    {
        if(path != null){
            path = path.trim().replace("\\", "/");
        }
        pathToMage = path;

//        File f = new File(pathToMage);
//        if( f.isFile() ){
//            IdeHelper.logError("Cannot find /app/Mage.php using the path: " + pathToMagento);
//        }

    }

    public void setUrlToMagento(String url)
    {
        if(url != null){
            url = url.trim().replace("\\", "/");
            if( ! url.isEmpty() && ! url.toLowerCase().startsWith("http")){
                url = "http://"+url;
            }
        }
        urlToMagento = url;
    }
}


