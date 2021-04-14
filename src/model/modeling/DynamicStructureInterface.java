/*
Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
*/

package model.modeling;


interface DynamicStructureInterface {
public void  addCoupling(String src, String p1, String dest, String p2);
public void  removeCoupling(String src, String p1, String dest, String p2);
public void  addModel(IODevs iod);
public void  removeModel(String modelName);
public void  addInport(String modelName, String port);
public void  addOutport(String modelName, String port);
public void  removeInport(String modelName, String port);
public void  removeOutport(String modelName, String port);

}

