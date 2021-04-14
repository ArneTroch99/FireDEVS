package view.ModelWizard.db.v;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.FlowLayout;
import java.util.Optional;

import javax.swing.JComboBox;
import javax.swing.JDialog;

import org.assertj.swing.fixture.DialogFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.jcip.annotations.NotThreadSafe;
import util.db.DatabaseTypes;
import view.modelwizard.db.v.DatabaseConfigurationView;

@NotThreadSafe
public class TestDatabaseConfigurationView
{
    private JDialog testDialog;
    private DatabaseConfigurationView cut;
    
    @BeforeEach
    void SetUp()
    {
        testDialog = new JDialog();
        testDialog.setLayout(new FlowLayout());
        testDialog.setSize(650, 600);
        cut = new DatabaseConfigurationView(Optional.empty());
        testDialog.add(cut);
        testDialog.setVisible(true);
    }
    
    @AfterEach
    void TearDown()
    {
        testDialog.setVisible(false);
    }
    
    @Test
    void testBase()
    {
        DialogFixture f = new DialogFixture(testDialog);
        
        JComboBox<String> databaseTypePicker = f.comboBox("DatabaseType Combobox").target();
        assertEquals("Select a database type...", databaseTypePicker.getSelectedItem());
        databaseTypePicker.setSelectedItem(DatabaseTypes.PostgreSQL.toString());
        
        assertEquals(DatabaseTypes.PostgreSQL.toString(), databaseTypePicker.getSelectedItem());
        
        assertTrue(f.textBox("Database Name:").target().isVisible());
        assertTrue(f.textBox("Host:").target().isVisible());
        assertTrue(f.textBox("Port:").target().isVisible());
        assertTrue(f.textBox("User:").target().isVisible());
        assertTrue(f.textBox("Schema:").target().isVisible());
        
        assertEquals("postgres", f.textBox("Database Name:").target().getText());
        assertEquals("localhost", f.textBox("Host:").target().getText());
        assertEquals("5432", f.textBox("Port:").target().getText());
    }
}
