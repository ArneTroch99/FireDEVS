/**
 * This class in an interface to any Cell implementation and was adapted from
 * the 'cell' interface class in DEVSJAVA. Four methods have been added to for
     * enabling cell space optimization by knowing the cells (x,y) position, which is
 * the cell's unique id implemented as a Pair.
 *
 * Adaptation Author: Lewis Ntaimo
 *            Date: April 16, 2003
 */

package model.modeling.CAModels;

import model.modeling.IODevs;
import GenCol.Pair;

public interface Cell
    extends IODevs {

  /**
   * This method returns the cell id as a pair(int xpos, int ypos)
   * @return
   */
  public Pair getId();

  /**
   * This sets old to true: this implies that this cell has all the neighbor
   * couplings done
   */
  public void setCouplingsDone();

  /**
   * This method returns this cells x coordinate
   * @return xcoord
   */
  public int getXcoord();

  /**
   * This method returns this cells y coordinate
   * @return ycoord
   */
  public int getYcoord();

  public Pair neighborId(int i, int j);

  /**
   * This method returns a cells status
   * @return true if cell is old, else return false
   */
  public boolean isAllCoupled();

  

}
