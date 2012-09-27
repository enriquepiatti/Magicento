package com.magicento.ui.forms;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jgoodies.forms.layout.CellConstraints;
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
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

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
    private JRadioButton usePHPInterpreterRadioButton;
    private JRadioButton useHTTPRadioButton;
    private JTextField urlToLocalMagentoTextField;
    // private TextFieldWithBrowseButton myProcessorPathField;
    private Project project;

    public MagicentoSettingsForm(@NotNull final Project currentProject) {

        project = currentProject;

        phpEnabledCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                //pathToPhpTextField.setEditable(((JCheckBox) e.getSource()).isSelected());
                _updatePhpSection();
            }
        });
        useHTTPRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                _updatePhpSection();
            }
        });
        usePHPInterpreterRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                _updatePhpSection();
            }
        });
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Magicento";
    }

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

        MagicentoSettings magicentoSettings = MagicentoSettings.getInstance(project);

        if(magicentoSettings != null)
        {
            if(magicentoSettings.getPathToMage() != null){
                pathToMageTextField.setText(magicentoSettings.getPathToMage());
            }
            if(magicentoSettings.pathToPhp != null){
                pathToPhpTextField.setText(magicentoSettings.pathToPhp);
            }
            enabledCheckBox.setSelected(magicentoSettings.enabled);
            phpEnabledCheckBox.setSelected(magicentoSettings.phpEnabled);
            useHTTPRadioButton.setSelected(magicentoSettings.useHttp);
            if(magicentoSettings.urlToMagento != null){
                urlToLocalMagentoTextField.setText(magicentoSettings.urlToMagento);
            }
        }
        else {
            pathToMageTextField.setText("");
            enabledCheckBox.setSelected(false);
            phpEnabledCheckBox.setSelected(false);
            pathToPhpTextField.setText("");
            urlToLocalMagentoTextField.setText("");
        }

        _updatePhpSection();

        myComponent = (JComponent) magicentoPanel;


        return myComponent;

    }


    protected void _updatePhpSection()
    {
        boolean phpEnabled = phpEnabledCheckBox.isSelected();
        useHTTPRadioButton.setEnabled(phpEnabled);
        usePHPInterpreterRadioButton.setEnabled(phpEnabled);
        pathToPhpTextField.setEditable(phpEnabled && usePHPInterpreterRadioButton.isSelected());
        urlToLocalMagentoTextField.setEditable(phpEnabled && useHTTPRadioButton.isSelected());
    }

    @Override
    public boolean isModified()
    {
        MagicentoSettings magicentoSettings = MagicentoSettings.getInstance(project);
        if(magicentoSettings != null){
            if( magicentoSettings.getPathToMage() != null && magicentoSettings.getPathToMage().equals(pathToMageTextField.getText()) &&
                magicentoSettings.enabled == enabledCheckBox.isSelected() &&
                magicentoSettings.phpEnabled == phpEnabledCheckBox.isSelected() &&
                magicentoSettings.pathToPhp != null && magicentoSettings.pathToPhp.equals(pathToPhpTextField.getText()) &&
                magicentoSettings.useHttp == useHTTPRadioButton.isSelected() &&
                magicentoSettings.urlToMagento != null && magicentoSettings.urlToMagento.equals(urlToLocalMagentoTextField.getText())
              ) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void apply() throws ConfigurationException
    {
        try {

            DataManager dataManager = DataManager.getInstance();

            if(myComponent != null && dataManager != null && PlatformDataKeys.PROJECT != null){
                //Project projectGuessed = ProjectUtil.guessCurrentProject(null);
//                Project project = PlatformDataKeys.PROJECT.getData(dataManager.getDataContext(myComponent));
//                if(project == null){
//                    project = ProjectUtil.guessCurrentProject(myComponent);
//                }
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
                        settings.useHttp = useHTTPRadioButton.isSelected();
                        settings.setUrlToMagento(urlToLocalMagentoTextField.getText());

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
