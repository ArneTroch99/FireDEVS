/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

interface ensembleLogic
{
    public boolean none(String MethodNm, Class<?>[] classes, Object[] args);

    public boolean all(String MethodNm, Class<?>[] classes, Object[] args);
}
