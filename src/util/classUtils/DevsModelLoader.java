package util.classUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import controller.ControllerInterface;
import facade.modeling.FModel;
import model.modeling.initializer;
import model.modeling.initializerFactory;
import model.modeling.initializerFactory.DynamicInit;
import model.modeling.script.TestFrame;
import model.modeling.script.TestFrame.TestScript;
import view.ViewInterface;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

public class DevsModelLoader
{
    private static ViewableComponent constructModel(Class<?> modelClass, String initName) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        if (TestFrame.class.isAssignableFrom(modelClass))
        {
            Constructor<?> constructor = modelClass.getConstructor(String.class, Method.class);
            Method[] methods = modelClass.getMethods();
            
            // Gather test cases from the TestFrame
            
            for (Method m : methods)
            {
                TestScript ann = m.getAnnotation(TestScript.class);
                
                if (ann != null)
                {
                    if (m.getName().equals(initName))
                    {
                        return (TestFrame) constructor.newInstance("testframe", m);
                    }   
                }
            }
            
            return (TestFrame) constructor.newInstance("testframe", null);
        }
        else
        {
            Method[] methods = modelClass.getMethods();
            
            for (Method m : methods)
            {
                if (m.isAnnotationPresent(initializer.class))
                {
                    initializer ann = m.getAnnotation(initializer.class);
                    String disp_name = ann.displayName();
                    
                    if (disp_name == null || disp_name.length() == 0)
                    {
                        disp_name = m.getName();
                    }
                    
                    if (disp_name.equals(initName))
                    {
                        return (ViewableComponent) m.invoke(null);
                    }
                }
                else if (m.isAnnotationPresent(initializerFactory.class))
                {
                    Object init_list = null;
                    
                    try
                    {
                        init_list = m.invoke(null);
                    }
                    catch (
                        IllegalAccessException
                        | IllegalArgumentException
                        | InvocationTargetException e)
                    {
                        e.printStackTrace();
                        return null;
                    }
                    
                    if (init_list instanceof Iterable<?>)
                    {
                        for (Object o : (Iterable<?>)init_list)
                        {
                            if (o instanceof DynamicInit)
                            {
                                DynamicInit dyn_init = (DynamicInit) o;
                                
                                if (dyn_init.getDisplayName().equals(initName))
                                {
                                    return dyn_init.getInitFunc().build();
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return (ViewableComponent) modelClass.getConstructor().newInstance();
    }
    
    public static LoadedDevsModel loadModelClass(String selectedPkg, String selectedModel, String selectedInit)
    {
        LoadedDevsModel data = new LoadedDevsModel();
        
        if (selectedPkg != null && selectedModel != null)
        {
            try
            {
                Object instance;
                Class<?> modelClass;
                try
                {
                    modelClass = DevsClassFileReader.readClass(selectedPkg, selectedModel);
                    instance = constructModel(modelClass, selectedInit);
                }
                catch (Exception en)
                {
                    en.printStackTrace();
                    return data;
                }

                if (instance instanceof TestFrame)
                {
                    TestFrame frame = (TestFrame) instance;
                    instance = frame.createFixture("testfixture");
                }
                
                if (instance instanceof ViewableAtomic)
                {
                    data.instanceModel = new ViewableDigraph("ViewableAtomic");
                    
                    ViewableAtomic atomic = (ViewableAtomic) instance;

                    data.instanceModel.add(atomic);
                    // for each of the names of the outports of the atomic
                    
                    List<?> names = atomic.getOutportNames();
                    for (int i = 0; i < names.size(); i++)
                    {
                        String portName = (String) names.get(i);

                        // add an outport with this port name to the wrapper
                        // digraph,
                        // and couple it to the atomic's outport of this name,
                        // so that outputs from that outport will be visible
                        // when they are emitted
                        data.instanceModel.addOutport(portName);
                        data.instanceModel.addCoupling(atomic, portName, data.instanceModel, portName);
                    }

                    data.modelType = FModel.ATOMIC;
                }
                else if (instance instanceof ViewableDigraph)
                {
                    data.instanceModel = (ViewableDigraph) instance;
                    data.modelType = FModel.COUPLED;
                }
            }
            catch (Exception e)
            {
                System.err.println("An Error Occured While Loading Model: " + e);
                e.printStackTrace();
            }
        }
        return data;
    }
    
    public static void initializeSimulatorWithModel(ViewInterface v, LoadedDevsModel data)
    {
        if (v.isCASelected() )
        {
            v.getController().userGesture(ControllerInterface.LOAD_MODEL_ASYNC, null);
        }
        else
        {
            v.getController().userGesture(ControllerInterface.LOAD_MODEL_GESTURE, null);
        }
                
        v.getController().initializeSimulator(data);
    }
    
    /**
     * Singleton class; no instances.
     */
    private DevsModelLoader()
    {
        
    }
}
