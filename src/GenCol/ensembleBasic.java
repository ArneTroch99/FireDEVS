/*     
 *    
 *  Author     : ACIMS(Arizona Centre for Integrative Modeling & Simulation)
 *  Version    : DEVSJAVA 2.7 
 *  Date       : 08-15-02 
 */
package GenCol;

interface ensembleBasic<T>
{
    public void tellAll(String MethodNm, Class<?>[] classes, Object[] args);

    public void tellAll(String MethodNm);

    public void AskAll(ensembleInterface<T> result, String MethodNm, Class<?>[] classes, Object[] args);

    public void which(ensembleInterface<T> result, String MethodNm, Class<?>[] classes, Object[] args);

    public T whichOne(String MethodNm, Class<?>[] classes, Object[] args);
}
