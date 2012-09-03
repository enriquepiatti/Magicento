package com.magicento.ui.forms;

import com.magicento.MagicentoProjectComponent;
import com.magicento.MagicentoSettings;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.magicento.helpers.IdeHelper;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author Enrique Piatti
 */
public class MagicentoSettingsForm implements Configurable {
    private JComponent myComponent;
    private JTextField pathToMageTextField;
    private JPanel magicentoPanel;
    private JCheckBox enabledCheckBox;
    private JCheckBox phpEnabledCheckBox;
    private JTextField pathToPhpTextField;

    public MagicentoSettingsForm() {

        phpEnabledCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                pathToPhpTextField.setEditable(((JCheckBox) e.getSource()).isSelected());
            }
        });
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Magicento";
    }

    @Override
    public Icon getIcon() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getHelpTopic() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public JComponent createComponent()
    {
        // Add listener to the Default Font button
//        MyButtonListener actionListener = new MyButtonListener();
//        actionListener.myFontName = myFontName;
//        actionListener.myFontSize = myFontSize;
//        MyDefaultFontButton.addActionListener(actionListener);
        // Define a set of possible values for combo boxes.
//        UISettings settings = UISettings.getInstance();
//        myFontName.setModel(new DefaultComboBoxModel(UIUtil.getValidFontNames(false)));
//        myFontSize.setModel(new DefaultComboBoxModel(UIUtil.getStandardFontSizes()));
//        myFontName.setSelectedItem(settings.FONT_FACE);
//        myFontSize.setSelectedItem(String.valueOf(settings.FONT_SIZE));

        Project project = ProjectUtil.guessCurrentProject(null);

        //String defaultPathToMagento = MagicentoProjectComponent.getInstance(project).getDefaultPathToMagento();
        //String pathToMagento = PropertiesComponent.getInstance(project).getValue("Magicento.pathToMagento", defaultPathToMagento);

        MagicentoSettings magicentoSettings = MagicentoSettings.getInstance(project);

        pathToMageTextField.setText("");
        enabledCheckBox.setSelected(false);
        phpEnabledCheckBox.setSelected(false);
        pathToPhpTextField.setText("");

        if(magicentoSettings != null){
            if(magicentoSettings.getPathToMage() != null){
                pathToMageTextField.setText(magicentoSettings.getPathToMage());
            }
            if(magicentoSettings.pathToPhp != null){
                pathToPhpTextField.setText(magicentoSettings.pathToPhp);
            }
            enabledCheckBox.setSelected(magicentoSettings.enabled);
            phpEnabledCheckBox.setSelected(magicentoSettings.phpEnabled);
        }

        pathToPhpTextField.setEditable(phpEnabledCheckBox.isSelected());

        myComponent = (JComponent) magicentoPanel;
        return myComponent;

        // PropertiesComponent.getInstance(Project)
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException
    {
        try {

            DataManager dataManager = DataManager.getInstance();

            if(myComponent != null && dataManager != null && PlatformDataKeys.PROJECT != null){
                //Project projectGuessed = ProjectUtil.guessCurrentProject(null);
                Project project = PlatformDataKeys.PROJECT.getData(dataManager.getDataContext(myComponent));
                if(project != null){

                    String pathToMage = null;
                    try{
                        pathToMage = pathToMageTextField.getText();
                    }
                    catch (Exception e){
                        pathToMage = "";
                        IdeHelper.logError("Error trying to read the path to Mage.php from the textfield");
                        IdeHelper.logError(e.getMessage());
                    }

                    boolean enabled = enabledCheckBox.isSelected();
                    boolean phpEnabled = phpEnabledCheckBox.isSelected();

                    //PropertiesComponent.getInstance(project).setValue("Magicento.pathToMagento", pathToMagento);

                    MagicentoSettings settings = MagicentoSettings.getInstance(project);
                    if(settings != null)
                    {
                        if(pathToMage != null){
                            // clear cache path to magento was changed
                            if(settings.getPathToMage() == null || ! settings.getPathToMage().equals(pathToMage)){
                                MagicentoProjectComponent magicentoProject = MagicentoProjectComponent.getInstance(project);
                                if(magicentoProject != null){
                                    magicentoProject.clearAllCache();
                                }
                            }
                            settings.setPathToMage(pathToMage);
                        }
                        settings.setPathToPhp(pathToPhpTextField.getText());
                        settings.enabled = enabled;
                        settings.phpEnabled = phpEnabled;

                    }
                    else {
                        IdeHelper.logError("MagicentoSettings is null");
                    }

                }
                else {
                    IdeHelper.logError("Project is null");
                }
            }
            else {
                IdeHelper.logError("DataManager is null");
            }
        }
        catch(Exception e){
            IdeHelper.logError("Unknown Error trying to save magicento settings");
            IdeHelper.logError(e.getMessage());
        }


//        UISettings settings = UISettings.getInstance();
//        LafManager lafManager = LafManager.getInstance();
//        String _fontFace = (String) myFontName.getSelectedItem();
//        String _fontSize_STR = (String) myFontSize.getSelectedItem();
//        int _fontSize = Integer.parseInt(_fontSize_STR);
//
//        if (_fontSize != settings.FONT_SIZE || !settings.FONT_FACE.equals(_fontFace)) {
//            settings.FONT_SIZE = _fontSize;
//            settings.FONT_FACE = _fontFace;
//            settings.fireUISettingsChanged();
//            lafManager.updateUI();
//        }


    }

    @Override
    public void reset() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }


}
