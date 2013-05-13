package com.magicento.ui.forms;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
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
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private JButton pathToMageButton;
    private JRadioButton useIdeaFolderRadioButton;
    private JTextField customFolderHttpTextField;
    private JButton customFolderHttpButton;
    private JCheckBox phpDisableWarningCheckBox;
    private JRadioButton useVarFolderRadioButton;
    private JRadioButton useCustomFolderRadioButton;
    private JCheckBox useVarDumpCheckBox;
    private JCheckBox layoutEnabledCheckBox;
    private JTextField packagesTextField;
    private JTextField themesTextField;
    private JTextField storeTextField;
    private JCheckBox automaticThisInTemplateCheckBox;
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
        useHTTPRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(useHTTPRadioButton.isSelected()){
                    String warningMessage = "This will create a file called eval.php accessible via HTTP, make sure you are not adding to VCS or deploying that file!";
                    IdeHelper.showDialog(project, warningMessage, "WARNING");
                }
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
        useCustomFolderRadioButton.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                _updatePhpSection();
            }
        });
        pathToMageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButtonListener(e);
            }
        });
        customFolderHttpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseButtonListener(e);
            }
        });

        layoutEnabledCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enable = layoutEnabledCheckBox.isSelected();
                themesTextField.setEditable(enable);
                packagesTextField.setEditable(enable);
            }
        });
    }


    protected void browseButtonListener(ActionEvent e)
    {
        JFileChooser chooser = new JFileChooser();
        Object source = e.getSource();
        JTextField textField = pathToMageTextField;
        if(source == customFolderHttpButton){
            textField = customFolderHttpTextField;
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        else {
            chooser.setFileFilter(new FileNameExtensionFilter("PHP Files", "php"));
        }
        String startPath = "";
        if (textField != null) {
            startPath = textField.getText();
        }
        if(startPath == null || startPath.isEmpty() || ! (new File(startPath)).exists()){
            MagicentoProjectComponent magicento = MagicentoProjectComponent.getInstance(project);
            if(magicento != null){
                startPath = magicento.getDefaultPathToMagento();
            }
        }
        if(startPath != null && ! startPath.isEmpty()){
            chooser.setCurrentDirectory(new File(startPath));
        }
        chooser.showOpenDialog(WindowManager.getInstance().suggestParentWindow(project));
        if (chooser.getSelectedFile() != null) {
            textField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
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
            String pathToMage = magicentoSettings.pathToMage;
            if(pathToMage != null){
                pathToMageTextField.setText(pathToMage);
            }
            if(magicentoSettings.pathToPhp != null){
                pathToPhpTextField.setText(magicentoSettings.pathToPhp);
            }
            enabledCheckBox.setSelected(magicentoSettings.enabled);
            phpEnabledCheckBox.setSelected(magicentoSettings.phpEnabled);
            phpDisableWarningCheckBox.setSelected( ! magicentoSettings.showPhpWarning);
            useHTTPRadioButton.setSelected(magicentoSettings.useHttp);
            if(magicentoSettings.urlToMagento != null){
                urlToLocalMagentoTextField.setText(magicentoSettings.urlToMagento);
            }
            if(magicentoSettings.useVarFolder){
                useVarFolderRadioButton.setSelected(true);
            }
            else {
                useIdeaFolderRadioButton.setSelected(true);
            }
            useVarDumpCheckBox.setSelected(magicentoSettings.useVarDump);
            layoutEnabledCheckBox.setSelected(magicentoSettings.layoutEnabled);
            if(magicentoSettings.packages != null){
                packagesTextField.setText(magicentoSettings.packages);
            }
            if(magicentoSettings.themes != null){
                themesTextField.setText(magicentoSettings.themes);
            }
            if(magicentoSettings.store != null){
                storeTextField.setText(magicentoSettings.store);
            }
            automaticThisInTemplateCheckBox.setSelected(magicentoSettings.automaticThisInTemplate);

        }
        else {
            pathToMageTextField.setText("");
            enabledCheckBox.setSelected(false);
            phpEnabledCheckBox.setSelected(false);
            phpDisableWarningCheckBox.setSelected(false);
            pathToPhpTextField.setText("");
            urlToLocalMagentoTextField.setText("");
            useIdeaFolderRadioButton.setSelected(true);
            useVarDumpCheckBox.setSelected(true);
        }

        _updatePhpSection();

        boolean enable = layoutEnabledCheckBox.isSelected();
        themesTextField.setEditable(enable);
        packagesTextField.setEditable(enable);

        // hide custom folder (not supported yet)
        useCustomFolderRadioButton.setVisible(false);
        customFolderHttpTextField.setVisible(false);
        customFolderHttpButton.setVisible(false);


        myComponent = (JComponent) magicentoPanel;


        return myComponent;

    }


    protected void _updatePhpSection()
    {
        boolean phpEnabled = phpEnabledCheckBox.isSelected();
        boolean httpEnabled = phpEnabled && useHTTPRadioButton.isSelected();
        useHTTPRadioButton.setEnabled(phpEnabled);
        usePHPInterpreterRadioButton.setEnabled(phpEnabled);
        pathToPhpTextField.setEditable(phpEnabled && ! httpEnabled);
        urlToLocalMagentoTextField.setEditable(httpEnabled);
        useIdeaFolderRadioButton.setEnabled(httpEnabled);
        useVarFolderRadioButton.setEnabled(httpEnabled);
        useCustomFolderRadioButton.setEnabled(httpEnabled);
        customFolderHttpTextField.setEditable(httpEnabled && useCustomFolderRadioButton.isSelected());
        customFolderHttpButton.setEnabled(httpEnabled && useCustomFolderRadioButton.isSelected());
    }

    @Override
    public boolean isModified()
    {
        MagicentoSettings magicentoSettings = MagicentoSettings.getInstance(project);
        if(magicentoSettings != null){
            String pathToMage = magicentoSettings.pathToMage; // use property directly to avoid validation here magicentoSettings.getPathToMage();
            if( pathToMage != null && pathToMage.equals(pathToMageTextField.getText()) &&
                magicentoSettings.enabled == enabledCheckBox.isSelected() &&
                magicentoSettings.phpEnabled == phpEnabledCheckBox.isSelected() &&
                magicentoSettings.pathToPhp != null && magicentoSettings.pathToPhp.equals(pathToPhpTextField.getText()) &&
                magicentoSettings.useHttp == useHTTPRadioButton.isSelected() &&
                magicentoSettings.urlToMagento != null && magicentoSettings.urlToMagento.equals(urlToLocalMagentoTextField.getText()) &&
                magicentoSettings.showPhpWarning == ! phpDisableWarningCheckBox.isSelected() &&
                magicentoSettings.useVarFolder == useVarFolderRadioButton.isSelected() &&
                magicentoSettings.useIdeaFolder == useIdeaFolderRadioButton.isSelected() &&
                magicentoSettings.useVarDump == useVarDumpCheckBox.isSelected() &&
                magicentoSettings.layoutEnabled == layoutEnabledCheckBox.isSelected() &&
                magicentoSettings.themes != null && magicentoSettings.themes.equals(themesTextField.getText()) &&
                magicentoSettings.packages != null && magicentoSettings.packages.equals(packagesTextField.getText()) &&
                magicentoSettings.store != null && magicentoSettings.store.equals(storeTextField.getText()) &&
                magicentoSettings.automaticThisInTemplate == automaticThisInTemplateCheckBox.isSelected()
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
                            if(settings.pathToMage == null || ! settings.pathToMage.equals(pathToMage)){
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
                        settings.showPhpWarning = ! phpDisableWarningCheckBox.isSelected();
                        settings.useIdeaFolder = useIdeaFolderRadioButton.isSelected();
                        settings.useVarFolder = useVarFolderRadioButton.isSelected();
                        settings.useVarDump = useVarDumpCheckBox.isSelected();
                        settings.layoutEnabled = layoutEnabledCheckBox.isSelected();
                        settings.themes = themesTextField.getText().trim();
                        settings.packages = packagesTextField.getText().trim();
                        settings.store = storeTextField.getText().trim();
                        settings.automaticThisInTemplate = automaticThisInTemplateCheckBox.isSelected();
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
