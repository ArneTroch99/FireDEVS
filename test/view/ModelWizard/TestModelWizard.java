package view.ModelWizard;

import static org.assertj.swing.core.matcher.JButtonMatcher.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.awt.FontMetrics;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import controller.ControllerInterface;
import facade.modeling.FCoupledModel;
import net.jcip.annotations.NotThreadSafe;
import util.classUtils.DevsModelLoader;
import util.classUtils.LoadedDevsModel;
import util.db.DatabaseConnectionConfiguration;
import util.db.DatabaseTypes;
import view.FModelView;
import view.TrackingControl;
import view.TrackingControlImpl;
import view.ViewInterface;
import view.groupableTable.clickableHeader.CheckboxHeader;
import view.modelwizard.ModelWizardConfiguration;
import view.modelwizard.db.v.DatabaseConfigurationView;
import view.modelwizard.trackingConfig.table.v.TableNames;
import view.modelwizard.trackingConfig.table.v.TrackAllTableName;
import view.modelwizard.trackingConfig.table.v.TrackingTableColumn;
import view.modelwizard.trackingConfig.table.v.TrackingTableColumns;
import view.modelwizard.trackingConfig.table.v.TrackingTableView;
import view.modelwizard.util.ModelWizardSettings;
import view.modelwizard.v.ModelWizardView;

@NotThreadSafe
class TestModelWizard
{
    private ModelWizardView cut;
    private ViewInterface mockView;
    private ControllerInterface mockController;
    private DatabaseConfigurationView mockDBConfigView;
    private FModelView modelView;
    private TrackingControl trackingControl;
    private DialogFixture f;
    
    private final String SETTINGS_FILE = "SimView_settings";
    
    private Path makePath()
    {
        return makePath(SETTINGS_FILE);
    }
    
    private Path makePath(String fileName)
    {
        return Paths.get("./", fileName);
    }
    
    private GenericTypeMatcher<JCheckBox> checkboxMatcher(String text)
    {
        return new GenericTypeMatcher<JCheckBox>(JCheckBox.class) {

            @Override
            protected boolean isMatching(JCheckBox component)
            {
                return text.equals(component.getText());
            }
            
        };
    }
    
    private GenericTypeMatcher<JTree> treeWithRootText(String text)
    {
        return new GenericTypeMatcher<JTree>(JTree.class) {

            @Override
            protected boolean isMatching(JTree component)
            {
                return component.getName().equals(text);
            }
            
        };
    }
    
    @BeforeEach
    void before() throws IOException
    {
        if (fileExists())
        {
            backUpSettings();
        }
    }
    
    @AfterEach
    void after() throws IOException, InterruptedException
    {
        deleteSettings();

        if (fileExists(SETTINGS_FILE + '2'))
        {
            restoreSettings();
        }

        cut.setVisible(false);
        cut = null;
        
        if (this.f != null)
        {
            this.f.cleanUp();
        }
    }
    
    private void makeInstance(String selectedPkg, String selectedModel)
    {
        makeInstance(selectedPkg, selectedModel, true);
    }

    private void makeInstance(String selectedPkg, String selectedModel, boolean isDBVisible)
    {
        mockView = mock(ViewInterface.class);
        mockController = mock(ControllerInterface.class);
        FontMetrics mockFontMetrics = mock(FontMetrics.class);
        
        trackingControl = new TrackingControlImpl(mockView);
        modelView = new FModelView(mockController, trackingControl);
        
        doReturn(mockFontMetrics).when(mockView).getFontMetrics(any());
        doReturn(modelView).when(mockView).getModelView();
        doReturn(mockController).when(mockView).getController();
        doReturn(mockView).when(mockController).getView();
        doReturn(trackingControl).when(mockView).getTrackingControl();
        
        if(selectedPkg != null)
        {
            doReturn(selectedPkg).when(mockView).getSelectedPackage();
        }
        
        if (selectedModel != null)
        {
            doReturn(selectedModel).when(mockView).getSelectedModel();
        }

        doReturn(12).when(mockFontMetrics).getHeight();
        cut = new ModelWizardView(mockView, new ModelWizardConfiguration());
        
        mockDBConfigView = mock(DatabaseConfigurationView.class);

        if (isDBVisible)
        {
            DatabaseConnectionConfiguration mockDBConnection = mock(DatabaseConnectionConfiguration.class);
            doReturn(DatabaseTypes.PostgreSQL).when(mockDBConnection).getDatabaseType();
            doReturn(new ArrayList<String>()).when(mockDBConnection).toList();
            doReturn(Optional.of(DatabaseTypes.PostgreSQL)).when(mockDBConfigView).getSelectedDatabaseType();
            doReturn(Optional.of(mockDBConnection)).when(mockDBConfigView).getDatabaseConnectionConfiguration();
        }
        else
        {
            doReturn(Optional.empty()).when(mockDBConfigView).getDatabaseConnectionConfiguration();
        }
        cut.makeFirstPage().setDatabaseConfigurationView(mockDBConfigView);
    }
    
    private void createSettings() throws IOException
    {
        JsonObject settings = new JsonObject();
        settings.addProperty("sourceDirectory", "test");
        settings.addProperty("binDirectory", "target/test-classes");
        settings.addProperty("selectedPackage", "SimpArcModTestArtifacts");
        settings.addProperty("selectedModel", "Fake_gpt2");
        
        JsonArray packageNames = new JsonArray(2);
        packageNames.add("SimpArcModTestArtifacts");
        packageNames.add("Package2");
        settings.add("packageNames", packageNames);
        
        FileWriter writer = new FileWriter(new File(SETTINGS_FILE));
        writer.write(settings.toString());
        
        writer.close();
    }
    
    private void createLegacySettings() throws IOException
    {
        FileOutputStream out = new FileOutputStream(ModelWizardSettings.SETTINGS_FILE_NAME);
        ObjectOutputStream s = new ObjectOutputStream(out);
        s.writeObject("target/test-classes");
        s.writeObject(Arrays.asList("SimpArcModTestArtifacts", "Packages4"));
        s.writeObject("SimpArcModTestArtifacts");
        s.writeObject("Fake_gpt2");
        s.writeObject("test");
        s.flush();
        s.close();
        out.close();
    }
    
    private void restoreSettings() throws IOException
    {
        Files.move(makePath(SETTINGS_FILE + '2'), makePath(), StandardCopyOption.ATOMIC_MOVE);
    }
    
    private void backUpSettings() throws IOException
    {
        assertTrue(Files.isReadable(makePath()));
        Files.move(makePath(), makePath(SETTINGS_FILE + '2'), StandardCopyOption.ATOMIC_MOVE);
    }
    
    private void deleteSettings() throws IOException
    {
        Files.deleteIfExists(makePath());
    }
    
    private boolean fileExists(String fileName) throws IOException
    {
        return Files.exists(Paths.get("./", fileName), LinkOption.NOFOLLOW_LINKS);
    }
    
    private boolean fileExists() throws IOException
    {
        return fileExists(SETTINGS_FILE);
    }
    
    private void validateTableInitialState(TableModel tableModel)
    {
        boolean loopEntered = false;
        for (int i = 0; i < tableModel.getRowCount(); ++i)
        {
            for (int j = 2; j < tableModel.getColumnCount(); ++j)
            {
                if (!loopEntered)
                {
                    loopEntered = true;
                }
                assertFalse((boolean) tableModel.getValueAt(i, j));
            }
        }
        assertTrue(loopEntered);
    }
    
    private void validateTableColumn(TableModel tableModel, TrackingTableColumns column, boolean value)
    {
        boolean loopEntered = false;
        for (int i = 0; i < tableModel.getRowCount(); ++i)
        {
            if (!loopEntered)
            {
                loopEntered = true;
            }
            assertEquals(value, tableModel.getValueAt(i, column.i));
        }
        assertTrue(loopEntered);
    }
    
    private void validateTablesInitialState(DialogFixture f)
    {
        TableNames.forEach((TableNames tableName) -> {
            validateTableInitialState(f.table(tableName.toString()).target().getModel());
        });
    }
    
    private void testTrackingLogCheckAll(DialogFixture f)
    {
        validateTablesInitialState(f);
        CheckboxHeader header = (CheckboxHeader) f.table(TrackAllTableName.name).target().getColumn(TrackingTableColumns.LOG.name).getHeaderRenderer();
        final int headerColumnIndex = ((TrackingTableView) f.table(TrackAllTableName.name).target()).getColumnIndexByName(TrackingTableColumns.LOG.name);
        header.click(headerColumnIndex);
        assertTrue(header.isSelected());

        validateTableColumn(f.table(TableNames.InputPorts.toString()).target().getModel(), TrackingTableColumns.LOG, true);
        validateTableColumn(f.table(TableNames.OutputPorts.toString()).target().getModel(), TrackingTableColumns.LOG, true);
        validateTableColumn(f.table(TableNames.States.toString()).target().getModel(), TrackingTableColumns.LOG, true);
        validateTableColumn(f.table(TableNames.Time.toString()).target().getModel(), TrackingTableColumns.LOG, true);
        
        JTable stateTable = f.table(TableNames.States.toString()).target();
        stateTable.setValueAt(false, 1, TrackingTableColumns.LOG.i);
        assertFalse(header.isSelected());
        
        JTable outputPortTable = f.table(TableNames.OutputPorts.toString()).target();
        outputPortTable.setValueAt(false, 0, TrackingTableColumns.LOG.i);
        assertFalse(header.isSelected());

        stateTable.setValueAt(true, 1, TrackingTableColumns.LOG.i);
        assertFalse(header.isSelected());
        
        outputPortTable.setValueAt(true, 0, TrackingTableColumns.LOG.i);
        assertTrue(header.isSelected());
        
        header.click(headerColumnIndex);
        validateTablesInitialState(f);
    }
    
    private void testTrackButtonEnablement(DialogFixture f)
    {
        final boolean isDBVisible = cut.isDBVisible().get();
        TableNames.forEach((TableNames tableName) -> {
            TrackingTableColumns.forEachTrackingColumn((TrackingTableColumn column) -> {
                testTrackButtonEnablement(f, f.table(tableName.toString()).target(), column.index());
            }, isDBVisible);
        });
    }
    
    private void testTrackButtonEnablement(DialogFixture f, JTable table, int column)
    {
        table.setValueAt(true, 0, column);
        
        f.button(withText("Track")).target().doClick();
        
        assertFalse(f.button(withText("Track")).target().isEnabled());
        
        table.setValueAt(false, 0, column);

        assertTrue(f.button(withText("Track")).target().isEnabled());
        
        f.button(withText("Track")).target().doClick();
        
        assertFalse(f.button(withText("Track")).target().isEnabled());
    }
    
    private void testStateTable(DialogFixture f, boolean isDBVisible)
    {
        JTree modelHierarchy = f.tree(treeWithRootText("ModelHierarchy")).target();
        Object root = modelHierarchy.getModel().getRoot();
        Object node = null;
        
        int numChildren = modelHierarchy.getModel().getChildCount(root);
        for (int i = 0; i < numChildren; ++i)
        {
            if (modelHierarchy.getModel().getChild(root, i).toString().equals("Fake_g"))
            {
                node = modelHierarchy.getModel().getChild(root, i);
            }
        }
        assertNotNull(node);
        TreePath p = new TreePath(node);
        modelHierarchy.setSelectionPath(p);
        
        JTable stateTable = f.table(TableNames.States.toString()).target();
        
        assertEquals(4, stateTable.getRowCount());
        
        int rowValidated = 0;
        for (int i = 0; i < 4; ++i)
        {
            if (stateTable.getValueAt(i, TrackingTableColumns.NAME.i).equals("int_arr_time"))
            {
                assertFalse(stateTable.getValueAt(i, TrackingTableColumns.STACK.i).equals(Boolean.TRUE));
                assertTrue(stateTable.getValueAt(i, TrackingTableColumns.NO_STACK.i).equals(Boolean.TRUE));
                assertTrue(stateTable.getValueAt(i, TrackingTableColumns.LOG.i).equals(Boolean.TRUE));
                if (isDBVisible)
                {
                    assertFalse(stateTable.getValueAt(i, TrackingTableColumns.DB.i).equals(Boolean.TRUE));
                }
                ++rowValidated;
            }
            else if (stateTable.getValueAt(i, TrackingTableColumns.NAME.i).equals("count"))
            {
                assertTrue(stateTable.getValueAt(i, TrackingTableColumns.STACK.i).equals(Boolean.TRUE));
                assertFalse(stateTable.getValueAt(i, TrackingTableColumns.NO_STACK.i).equals(Boolean.TRUE));
                assertFalse(stateTable.getValueAt(i, TrackingTableColumns.LOG.i).equals(Boolean.TRUE));
                if (isDBVisible)
                {
                    assertTrue(stateTable.getValueAt(i, TrackingTableColumns.DB.i).equals(Boolean.TRUE));
                }
                ++rowValidated;
            }
        }
        
        assertEquals(2, rowValidated);
        
        p = new TreePath(root);
        modelHierarchy.setSelectionPath(p);
    }
    
    @Test
    void testFirstPage()
    {
        try
        {
            assertFalse(fileExists());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }

        makeInstance("SimpArcModTestArtifacts", "Fake_gpt");

        cut.setVisible(true);
        
        f = new DialogFixture(cut);
        
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());

        assertFalse(f.button(withText("Finish")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());
        
        assertTrue(f.checkBox(checkboxMatcher("SimView")).target().isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("SimView")).target().isSelected());
        
        assertTrue(f.checkBox(checkboxMatcher("Tracking")).target().isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("Tracking")).target().isSelected());
        
        assertEquals("Select a package", f.comboBox("PackageChooser").target().getSelectedItem());
        assertNull(f.comboBox("ModelChooser").target().getSelectedItem());
        
        f.button(withText("Configure File System")).target().doClick();

        assertEquals("", f.textBox("ClassPathField").text());
        assertEquals("", f.textBox("SourcePathField").text());
        assertEquals("", f.textBox("PackagesList").text());
        
        f.textBox("ClassPathField").target().setText("target/test-classes");
        f.textBox("SourcePathField").target().setText("test");
        f.textBox("PackagesList").target().setText("SimpArcModTestArtifacts");
        
        f.button(withText("Ok")).target().doClick();
        
        f.comboBox("PackageChooser").target().setSelectedItem("SimpArcModTestArtifacts");
        f.comboBox("ModelChooser").target().setSelectedItem("Fake_gpt");
        
        assertTrue(f.button(withText("Finish")).isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("SimView")).target().isSelected());
        assertFalse(f.checkBox(checkboxMatcher("Tracking")).target().isSelected());
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());

        f.checkBox(checkboxMatcher("SimView")).target().doClick();
        
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());

        f.checkBox(checkboxMatcher("Tracking")).target().doClick();

        assertTrue(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        
        f.button(withText("Next >")).target().doClick();
        
        try
        {
            assertTrue(fileExists());
            
            ModelWizardSettings mws = ModelWizardSettings.readSettings();
            
            assertEquals("SimpArcModTestArtifacts", mws.selectedPackage);
            assertEquals("Fake_gpt", mws.selectedModel);
            assertEquals("target/test-classes", mws.binDirectory);
            assertEquals("test", mws.sourceDirectory);
            assertEquals(Arrays.asList("SimpArcModTestArtifacts"), mws.packageNames);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }
    }
    
    @Test
    void testFirstPageWithLegacySettings() throws IOException
    {
        try
        {
            assertFalse(fileExists());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }

        createLegacySettings();

        makeInstance("SimpArcModTestArtifacts", "Fake_gpt2");

        cut.setVisible(true);
        f = new DialogFixture(cut);
        
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());

        assertTrue(f.button(withText("Finish")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());
        
        assertTrue(f.checkBox(checkboxMatcher("SimView")).target().isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("SimView")).target().isSelected());
        
        assertTrue(f.checkBox(checkboxMatcher("Tracking")).target().isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("Tracking")).target().isSelected());
        
        assertEquals("SimpArcModTestArtifacts", f.comboBox("PackageChooser").target().getSelectedItem());
        assertEquals("Fake_gpt2", f.comboBox("ModelChooser").target().getSelectedItem());
        
        f.button(withText("Configure File System")).target().doClick();

        assertEquals("target/test-classes", f.textBox("ClassPathField").text());
        assertEquals("test", f.textBox("SourcePathField").text());
        assertEquals("SimpArcModTestArtifacts\nPackages4", f.textBox("PackagesList").text());
        
        f.textBox("PackagesList").target().setText("SimpArcModTestArtifacts");

        doReturn("Fake_gpt").when(mockView).getSelectedModel();
        
        f.button(withText("Ok")).target().doClick();
        
        f.comboBox("PackageChooser").target().setSelectedItem("SimpArcModTestArtifacts");
        f.comboBox("ModelChooser").target().setSelectedItem("Fake_gpt");
        
        assertTrue(f.button(withText("Finish")).isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("SimView")).target().isSelected());
        assertFalse(f.checkBox(checkboxMatcher("Tracking")).target().isSelected());
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());

        f.checkBox(checkboxMatcher("SimView")).target().doClick();
        
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());

        f.checkBox(checkboxMatcher("Tracking")).target().doClick();

        assertTrue(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        
        f.button(withText("Next >")).target().doClick();
        
        try
        {
            assertTrue(fileExists());
            
            ModelWizardSettings mws = ModelWizardSettings.readSettings();
            
            assertEquals("SimpArcModTestArtifacts", mws.selectedPackage);
            assertEquals("Fake_gpt", mws.selectedModel);
            assertEquals("target/test-classes", mws.binDirectory);
            assertEquals("test", mws.sourceDirectory);
            assertEquals(Arrays.asList("SimpArcModTestArtifacts"), mws.packageNames);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }
    }
    
    @Test
    void testFirstPageWithSavedSettings() throws IOException
    {
        try
        {
            assertFalse(fileExists());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }

        createSettings();

        makeInstance("SimpArcModTestArtifacts", "Fake_gpt2");

        cut.setVisible(true);
        f = new DialogFixture(cut);
        
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());

        assertTrue(f.button(withText("Finish")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());
        
        assertTrue(f.checkBox(checkboxMatcher("SimView")).target().isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("SimView")).target().isSelected());
        
        assertTrue(f.checkBox(checkboxMatcher("Tracking")).target().isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("Tracking")).target().isSelected());
        
        assertEquals("SimpArcModTestArtifacts", f.comboBox("PackageChooser").target().getSelectedItem());
        assertEquals("Fake_gpt2", f.comboBox("ModelChooser").target().getSelectedItem());
        
        f.button(withText("Configure File System")).target().doClick();

        assertEquals("target/test-classes", f.textBox("ClassPathField").text());
        assertEquals("test", f.textBox("SourcePathField").text());
        assertEquals("SimpArcModTestArtifacts\nPackage2", f.textBox("PackagesList").text());
        
        f.textBox("PackagesList").target().setText("SimpArcModTestArtifacts");

        doReturn("Fake_gpt").when(mockView).getSelectedModel();
        
        f.button(withText("Ok")).target().doClick();
        
        f.comboBox("PackageChooser").target().setSelectedItem("SimpArcModTestArtifacts");
        f.comboBox("ModelChooser").target().setSelectedItem("Fake_gpt");
        
        assertTrue(f.button(withText("Finish")).isEnabled());
        assertFalse(f.checkBox(checkboxMatcher("SimView")).target().isSelected());
        assertFalse(f.checkBox(checkboxMatcher("Tracking")).target().isSelected());
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());

        f.checkBox(checkboxMatcher("SimView")).target().doClick();
        
        assertFalse(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        assertTrue(f.button(withText("Cancel").andShowing()).target().isEnabled());

        f.checkBox(checkboxMatcher("Tracking")).target().doClick();

        assertTrue(f.button(withText("Next >")).target().isEnabled());
        assertFalse(f.button(withText("< Prev")).target().isEnabled());
        
        f.button(withText("Next >")).target().doClick();
        
        try
        {
            assertTrue(fileExists());
            
            ModelWizardSettings mws = ModelWizardSettings.readSettings();
            
            assertEquals("SimpArcModTestArtifacts", mws.selectedPackage);
            assertEquals("Fake_gpt", mws.selectedModel);
            assertEquals("target/test-classes", mws.binDirectory);
            assertEquals("test", mws.sourceDirectory);
            assertEquals(Arrays.asList("SimpArcModTestArtifacts"), mws.packageNames);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }
    }
    
    private void doSecondPageTest()
    {
        f.button(withText("Configure File System")).target().doClick();

        f.textBox("ClassPathField").target().setText("target/test-classes");
        f.textBox("SourcePathField").target().setText("test");
        f.textBox("PackagesList").target().setText("SimpArcModTestArtifacts");
        
        f.button(withText("Ok")).target().doClick();
        
        f.comboBox("PackageChooser").target().setSelectedItem("SimpArcModTestArtifacts");
        f.comboBox("ModelChooser").target().setSelectedItem("Fake_gpt");
        
        f.checkBox(checkboxMatcher("SimView")).target().doClick();
        f.checkBox(checkboxMatcher("Tracking")).target().doClick();

        LoadedDevsModel data = DevsModelLoader.loadModelClass("SimpArcModTestArtifacts", "Fake_gpt", "test");
        FCoupledModel rootModel = new FCoupledModel(data.instanceModel);
        trackingControl.loadSimModel(rootModel);
        doReturn(true).when(mockView).isSimView();
        modelView.loadModel(rootModel);

        f.button(withText("Next >")).target().doClick();
        
        JTree modelHierarchy = f.tree(treeWithRootText("ModelHierarchy")).target();
        TreePath p = new TreePath(modelHierarchy.getModel().getRoot());
        modelHierarchy.setSelectionPath(p);
        
        assertTrue(f.button(withText("Track")).isEnabled());
        assertTrue(f.button(withText("Reset")).isEnabled());
        
        assertFalse(f.checkBox(checkboxMatcher("Superdense")).target().isSelected());
        
        assertEquals("sec", f.textBox("X-Axis").target().getText());
        assertEquals("10", f.textBox("Increment").target().getText());
        
        testStateTable(f, cut.isDBVisible().get());
        testTrackingLogCheckAll(f);
        
        assertTrue(f.button(withText("Track")).isEnabled());
        assertTrue(f.button(withText("Reset")).isEnabled());
        
        testTrackButtonEnablement(f);
        
        cut.setVisible(false);
    }
    
    @Test
    void testSecondPage()
    {
        try
        {
            assertFalse(fileExists());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }
        
        makeInstance("SimpArcModTestArtifacts", "Fake_gpt", false);
        assertFalse(cut.makeFirstPage().isDBVisible());

        cut.setVisible(true);
        f = new DialogFixture(cut);

        doSecondPageTest();
    }
    
    @Test
    void testSecondPageDB()
    {
        try
        {
            assertFalse(fileExists());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Exception not expected!");
        }
        
        makeInstance("SimpArcModTestArtifacts", "Fake_gpt", true);
        assertTrue(cut.makeFirstPage().isDBVisible());

        cut.setVisible(true);
        f = new DialogFixture(cut);

        doSecondPageTest();
    }
}
