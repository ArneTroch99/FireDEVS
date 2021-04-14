package view.ModelWizard.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import view.modelwizard.util.ModelWizardSettings;

class TestModelWizardSettings
{

    @BeforeEach
    void before() throws IOException
    {
        if (fileExists(ModelWizardSettings.SETTINGS_FILE_NAME))
        {
            backUpSettings();
        }
        createSettings();
    }
    
    @AfterEach
    void after() throws IOException
    {
        deleteSettings();
        
        if (fileExists(ModelWizardSettings.SETTINGS_FILE_NAME + '2'))
        {
            restoreSettings();
        }
    }
    
    private void createSettings() throws IOException
    {
        JsonObject settings = new JsonObject();
        settings.addProperty("sourceDirectory", "src");
        settings.addProperty("binDirectory", "bin");
        settings.addProperty("selectedPackage", "SimpArcMod");
        settings.addProperty("selectedModel", "gpt2");
        
        JsonArray packageNames = new JsonArray(2);
        packageNames.add("SimpArcMod");
        packageNames.add("Package2");
        settings.add("packageNames", packageNames);
        
        FileWriter writer = new FileWriter(new File(ModelWizardSettings.SETTINGS_FILE_NAME));
        writer.write(settings.toString());
        
        writer.close();
    }
    
    private boolean fileExists(String fileName)
    {
        return Files.exists(makePath(fileName), LinkOption.NOFOLLOW_LINKS);
    }

    private void restoreSettings() throws IOException
    {
        Files.move(makePath(ModelWizardSettings.SETTINGS_FILE_NAME + '2'), makePath(), StandardCopyOption.ATOMIC_MOVE);
    }
    
    private void backUpSettings() throws IOException
    {
        Files.move(makePath(), makePath(ModelWizardSettings.SETTINGS_FILE_NAME + '2'), StandardCopyOption.ATOMIC_MOVE);
    }
    
    private void deleteSettings() throws IOException
    {
        Files.deleteIfExists(makePath());
    }
    
    private Path makePath()
    {
        return makePath(ModelWizardSettings.SETTINGS_FILE_NAME);
    }
    
    private Path makePath(String fileName)
    {
        return Paths.get("./", fileName);
    }
    
    @Test
    void testReadSettings()
    {
        ModelWizardSettings cut = ModelWizardSettings.readSettings();
        assertEquals("SimpArcMod", cut.selectedPackage);
        assertEquals("gpt2", cut.selectedModel);
        assertEquals("bin", cut.binDirectory);
        assertEquals("src", cut.sourceDirectory);
        assertEquals(Arrays.asList("SimpArcMod", "Package2"), cut.packageNames);
    }
    
    @Test
    void testWriteSettings() throws IOException
    {
        ModelWizardSettings cut = ModelWizardSettings.readSettings();
        deleteSettings();
        assertFalse(Files.exists(Paths.get(ModelWizardSettings.SETTINGS_FILE_NAME), LinkOption.NOFOLLOW_LINKS));
        
        assertEquals("gpt2", cut.selectedModel);
        cut.selectedModel = "gpt";
        cut.writeSettings();
        
        assertTrue(Files.exists(Paths.get(ModelWizardSettings.SETTINGS_FILE_NAME), LinkOption.NOFOLLOW_LINKS));
        
        cut = null;
        cut = ModelWizardSettings.readSettings();
        
        assertEquals("SimpArcMod", cut.selectedPackage);
        assertEquals("gpt", cut.selectedModel);
        assertEquals("bin", cut.binDirectory);
        assertEquals("src", cut.sourceDirectory);
        assertEquals(Arrays.asList("SimpArcMod", "Package2"), cut.packageNames);
    }
}
