/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

public interface EntityInterface
{ // for some reason Entity doesn't generate .class file
    public String getName();

    public Object equalName(String name);

    public ExternalRepresentation getExtRep();
}
