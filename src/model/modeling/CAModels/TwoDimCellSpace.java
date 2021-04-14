/**
 * This program implements a 2-dimensional optimized cell space *
 * author: Lewis Ntaimo
 * Date: April 16, 2003
 * Revision: , 2003
 *
 */
package model.modeling.CAModels;

import java.awt.*;

import Component.GridCellplots.*;
import GenCol.*;
import model.modeling.*;
import view.simView.*;
import view.modeling.*;

public class TwoDimCellSpace
    extends ViewableDigraph {
  public int xDimCellspace; // Cell space x dimension size
  public int yDimCellspace; // Cell space x dimension size
  protected int xcoord; // A cell's x coordinate position in the cell space
  protected int ycoord; // A cell's y coordinate position in the cell space
  protected int my_xcoord; // This cell's x coordinate position in the cell space
  protected int my_ycoord; // This cell's y coordinate position in the cell space
  public int numCells;
  Cell d1;

  /**
   * Convenient constructor
   */
  public TwoDimCellSpace() {
    this("TwoDimCellSpace", 1, 1);
  }

  /**
   * Constructor
   */
  public TwoDimCellSpace(String nm, int xDimCellspace, int yDimCellspace) {
    super(nm);
    this.xDimCellspace = xDimCellspace;
    this.yDimCellspace = yDimCellspace;
    this.numCells = xDimCellspace * yDimCellspace;
    addInport("start");
    addInport("stop");
    addInport("inN");
    addInport("inNE");
    addInport("inE");
    addInport("inSE");
    addInport("inS");
    addInport("inSW");
    addInport("inW");
    addInport("inNW");
    addOutport("out");
    addOutport("outN");
    addOutport("outNE");
    addOutport("outE");
    addOutport("outSE");
    addOutport("outS");
    addOutport("outSW");
    addOutport("outW");
    addOutport("outNW");
    addOutport("outTrans");
    addOutport("outDisplay");

    addTestInput("inN", new Pair(new Integer(xcoord), new Integer(ycoord + 1)));
    addTestInput("inNE",
                 new Pair(new Integer(xcoord + 1), new Integer(ycoord + 1)));
    addTestInput("inE", new Pair(new Integer(xcoord + 1), new Integer(ycoord)));
    addTestInput("inSE",
                 new Pair(new Integer(xcoord + 1), new Integer(ycoord - 1)));
    addTestInput("inS", new Pair(new Integer(xcoord), new Integer(ycoord - 1)));
    addTestInput("inSW",
                 new Pair(new Integer(xcoord - 1), new Integer(ycoord - 1)));
    addTestInput("inW",
                 new Pair(new Integer(xcoord - 1), new Integer(ycoord - 1)));
    addTestInput("inNW",
                 new Pair(new Integer(xcoord - 1), new Integer(ycoord + 1)));

  }

/////////////////////////////////////////////////////////////////////////////////
  // These methods were taken from oneDimCellSpace.java by Dr. Zeigler      //
  // Some methods were modified to fit the 2-D cell space and that is noted //
  // wherever that is applicable.                                           //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * See parent method.
   */
  //Disabled by Chao
//  @Override
//  public boolean layoutForSimViewOverride() {
//    preferredSize = new Dimension(800, 700);
//
//    //ViewableComponentUtil.layoutCellsInGrid(numCells, "cell_", 5, this, 50, 30);
//    return true;
//    //return false;
//  }

  public Cell withId(Pair cellId) {
    return (Cell) withName("Cell: " + cellId.getKey() + ", " + cellId.getValue());
  }

  public Cell withId(int xcoord, int ycoord) {
    return withId(new Pair(new Integer(xcoord), new Integer(ycoord)));
  }
  
  public TwoDimCell getCell(int xcoord, int ycoord) {
	  return (TwoDimCell) this.withId( xcoord, ycoord);
  }

  public Cell neighborOf(Cell c, int i, int j) {
    return withId(c.neighborId(i, j));
  }

  public void hideAll() {
    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      devs d1 = (devs) it1.nextComponent();
      ( (ViewableComponent) d1).setHidden(true); //hide cells
    }
  }

  public void doAllToAllCoupling() {
    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      devs d1 = (devs) it1.nextComponent();
      componentIterator it2 = components.cIterator();
      while (it2.hasNext()) {
        devs d2 = (devs) it2.nextComponent();
        if (!d1.equals(d2)) {
          addCoupling(d1, "out", d2, "in");
        }
      }
    }
  }

  public void coupleAllTo(String sourcePt, devs d, String destinPt) {
    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      devs d1 = (devs) it1.nextComponent();
      if (!d1.equals(d)) {
        addCoupling(d1, sourcePt, d, destinPt);
      }
    }
  }

  public void coupleOneToAll(devs d, String sourcePt, String destinPt) {
    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      devs d1 = (devs) it1.nextComponent();
      if (!d1.equals(d)) {
        addCoupling(d, sourcePt, d1, destinPt);
      }
    }
  }

  public void coupleAllToExcept(String sourcePt, devs d, String destinPt
                                , devs other) {

    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      devs d1 = (devs) it1.nextComponent();
      if (!d1.equals(d) && !d1.equals(other)) {
        addCoupling(d1, sourcePt, d, destinPt);
      }
    }
  }

  /**
   *  This method was modified to do neighbor to neighbor coupling for the
   *   cells
   */
  public void doNeighborToNeighborCoupling() {
    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      Cell d1 = (Cell) it1.nextComponent();
      Pair myid = (Pair) d1.getId();
      Integer my_xint = (Integer) myid.getKey();
      Integer my_yint = (Integer) myid.getValue();
      int my_x = my_xint.intValue();
      int my_y = my_yint.intValue();
      //System.out.println();
      //System.out.println("My cell id:  " + myid.toString());
      //System.out.println();
      componentIterator it2 = components.cIterator();
      while (it2.hasNext()) {
        Cell d2 = (Cell) it2.nextComponent();
        Pair other_id = (Pair) d2.getId();
        if (!other_id.equals(myid)) {
          Integer other_xint = (Integer) other_id.getKey();
          Integer other_yint = (Integer) other_id.getValue();
          int other_x = other_xint.intValue();
          int other_y = other_yint.intValue();
          //System.out.println("Other cell id:  " + other_id.toString());
          // N Neighbor
          if (my_x == other_x && my_y == other_y - 1) {
            addCoupling(d1, "outS", d2, "inN");
          } // NE Neighbor
          else if (my_x == other_x - 1 && my_y == other_y - 1) {
            addCoupling(d1, "outSE", d2, "inNW");
          } // E Neighbor
          else if (my_x == other_x - 1 && my_y == other_y) {
            addCoupling(d1, "outE", d2, "inW");
            //System.out.println("My East Neighbor id:  " + other_id.toString());
          } // SE Neighbor
          else if (my_x == other_x - 1 && my_y == other_y + 1) {
            addCoupling(d1, "outNE", d2, "inSW");
          } // S Neighbor
          else if (my_x == other_x && my_y == other_y + 1) {
            addCoupling(d1, "outN", d2, "inS");
          } // SW Neighbor
          else if (my_x == other_x + 1 && my_y == other_y + 1) {
            addCoupling(d1, "outNW", d2, "inSE");
          } // W Neighbor
          else if (my_x == other_x + 1 && my_y == other_y) {
            addCoupling(d1, "outW", d2, "inE");
          } // NW Neighbor
          else if (my_x == other_x + 1 && my_y == other_y - 1) {
            addCoupling(d1, "outSW", d2, "inNE");
          }
        } // end if
      } // End inner while loop
    } // End outer while loop
  }

  /**
   *  This method was modified to do neighbor to neighbor coupling for the
   *   cells
   */
  public void doNeighborToNeighborCoupling(String port) {
    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      Cell d1 = (Cell) it1.nextComponent();
      Pair myid = (Pair) d1.getId();
      Integer my_xint = (Integer) myid.getKey();
      Integer my_yint = (Integer) myid.getValue();
      int my_x = my_xint.intValue();
      int my_y = my_yint.intValue();
      //System.out.println();
      //System.out.println("My cell id:  " + myid.toString());
      //System.out.println();
      componentIterator it2 = components.cIterator();
      while (it2.hasNext()) {
        Cell d2 = (Cell) it2.nextComponent();
        Pair other_id = (Pair) d2.getId();
        if (!other_id.equals(myid)) {
          Integer other_xint = (Integer) other_id.getKey();
          Integer other_yint = (Integer) other_id.getValue();
          int other_x = other_xint.intValue();
          int other_y = other_yint.intValue();
          //System.out.println("Other cell id:  " + other_id.toString());
          // N Neighbor
          if (my_x == other_x && my_y == other_y - 1) {
            addCoupling(d1, port + "outS", d2, port + "inN");
          } // NE Neighbor
          else if (my_x == other_x - 1 && my_y == other_y - 1) {
            addCoupling(d1, port + "outSE", d2, port + "inNW");
          } // E Neighbor
          else if (my_x == other_x - 1 && my_y == other_y) {
            addCoupling(d1, port + "outE", d2, port + "inW");
            //System.out.println("My East Neighbor id:  " + other_id.toString());
          } // SE Neighbor
          else if (my_x == other_x - 1 && my_y == other_y + 1) {
            addCoupling(d1, port + "outNE", d2, port + "inSW");
          } // S Neighbor
          else if (my_x == other_x && my_y == other_y + 1) {
            addCoupling(d1, port + "outN", d2, port + "inS");
          } // SW Neighbor
          else if (my_x == other_x + 1 && my_y == other_y + 1) {
            addCoupling(d1, "outNW", d2, "inSE");
          } // W Neighbor
          else if (my_x == other_x + 1 && my_y == other_y) {
            addCoupling(d1, port + "outW", d2, port + "inE");
          } // NW Neighbor
          else if (my_x == other_x + 1 && my_y == other_y - 1) {
            addCoupling(d1, port + "outSW", d2, port + "inNE");
          }
        } // end if
      } // End inner while loop
    } // End outer while loop
  }

  /**
   *  This method was modified to do neighbor to neighbor coupling for
   *  cells given the cell id and ports for coupling
   */
  public void doNeighborCoupling(Pair id, String sourcePt, String destPt) {
    componentIterator it1 = components.cIterator();
    boolean id_found = false;
    int my_x = 0;
    int my_y = 0;
    // Find cell with this id
    while (it1.hasNext()) {
      Cell d1 = (Cell) it1.nextComponent();
      Pair myid = (Pair) d1.getId();
      if (myid.equals(id)) {
        id_found = true;
        Integer my_xint = (Integer) myid.getKey();
        Integer my_yint = (Integer) myid.getValue();
        my_x = my_xint.intValue();
        my_y = my_yint.intValue();
        break;
      }
    }
    // if cell with this id is found find its neighbors and do coupling
    if (id_found) {
      componentIterator it2 = components.cIterator();
      while (it2.hasNext()) {
        Cell d2 = (Cell) it2.nextComponent();
        Pair other_id = (Pair) d2.getId();
        Integer other_xint = (Integer) other_id.getKey();
        Integer other_yint = (Integer) other_id.getValue();
        int other_x = other_xint.intValue();
        int other_y = other_yint.intValue();

        // N Neighbor
        if (my_x == other_x && my_y == other_y - 1) {
          addCoupling(d1, sourcePt, d2, destPt);
        } // NE Neighbor
        else if (my_x == other_x - 1 && my_y == other_y - 1) {
          addCoupling(d1, sourcePt, d2, destPt);
        } // E Neighbor
        else if (my_x == other_x - 1 && my_y == other_y) {
          addCoupling(d1, sourcePt, d2, destPt);
        } // SE Neighbor
        else if (my_x == other_x - 1 && my_y == other_y + 1) {
          addCoupling(d1, sourcePt, d2, destPt);
        }
        else if (my_x == other_x && my_y == other_y + 1) {
          addCoupling(d1, sourcePt, d2, destPt);
        } // SW Neighbor
        else if (my_x == other_x + 1 && my_y == other_y + 1) {
          addCoupling(d1, sourcePt, d2, destPt);
        } // W Neighbor
        else if (my_x == other_x + 1 && my_y == other_y) {
          addCoupling(d1, sourcePt, d2, destPt);
        } // NW Neighbor
        else if (my_x == other_x + 1 && my_y == other_y - 1) {
          addCoupling(d1, sourcePt, d2, destPt);
        }

      }
    } // End if

  }


  ///////////////////////////////////////////////////////////////////////////////

  /**
   * This method does neighbor to neighbor coupling for a given Cell
       * specified by its (x,y) coordinate position in the Cell space. It also couples
   * the Cell to all the default Cell space couplings.
   * @param _xcoord the x-coordinate of this Cell
   * @param _ycoord the y-coordinate of this Cell
   */
  public void doThisCellsNeighborCouplings(int _xcoord, int _ycoord) {
    componentIterator it1 = components.cIterator();
    while (it1.hasNext()) {
      d1 = (Cell) it1.nextComponent();
      my_xcoord = d1.getXcoord();
      my_ycoord = d1.getYcoord();
      if (my_xcoord == _xcoord && my_ycoord == _ycoord) { // Found this Cell!
        break;
      }
    }
    //Couple this Cell to all default Cellspace ports
    doCellToCellspaceCouplings(d1);

    // Find this Cell's neighbors and couple them to this Cell
    componentIterator it2 = components.cIterator();
    while (it2.hasNext()) {
      Cell d2 = (Cell) it2.nextComponent();
      // Check if Cell d2 has all the neighbor couplings
      //if (!d2.isAllCoupled()) { //
        xcoord = d2.getXcoord();
        ycoord = d2.getYcoord();
        // N Neighbor
        if (my_xcoord == xcoord && my_ycoord == ycoord - 1) {
          addCoupling(d1, "outS", d2, "inN");
        } // NE Neighbor
        else if (my_xcoord == xcoord - 1 && my_ycoord == ycoord - 1) {
          addCoupling(d1, "outSE", d2, "inNW");
        } // E Neighbor
        else if (my_xcoord == xcoord - 1 && my_ycoord == ycoord) {
          addCoupling(d1, "outE", d2, "inW");
        } // SE Neighbor
        else if (my_xcoord == xcoord - 1 && my_ycoord == ycoord + 1) {
          addCoupling(d1, "outNE", d2, "inSW");
        } // S Neighbor
        else if (my_xcoord == xcoord && my_ycoord == ycoord + 1) {
          addCoupling(d1, "outN", d2, "inS");
        } // SW Neighbor
        else if (my_xcoord == xcoord + 1 && my_ycoord == ycoord + 1) {
          addCoupling(d1, "outNW", d2, "inSE");
        } // W Neighbor
        else if (my_xcoord == xcoord + 1 && my_ycoord == ycoord) {
          addCoupling(d1, "outW", d2, "inE");
        } // NW Neighbor
        else if (my_xcoord == xcoord + 1 && my_ycoord == ycoord - 1) {
          addCoupling(d1, "outSW", d2, "inNE");
        }
      //} // End out if isOld
    } // End while loop

    //Set that this Cell has all the couplings done
    d1.setCouplingsDone();

  } //End method doThisCellsNeighborCoupling

  /**
   * This method performs all default couplings between a given
   * Cell and the Cellspace
   */
  public void doCellToCellspaceCouplings(Cell c) {
    addCoupling(this, "start", c, "start");
    addCoupling(this, "stop", c, "stop");
    addCoupling(this, "outCSM", c, "outCSM");
    //addCoupling(this, "inTwo", c, "inTwo");
    //addCoupling(this, "inThree", c, "inThree");
    //addCoupling(c, "outOne", this, "outOne");
    //addCoupling(c, "outTwo", this, "outTwo");
    //addCoupling(c, "outThree", this, "outThree");
  }

  /**
   * This method creates and adds a new Cell to the Cell space. Also performs
   * all appropriate couplings
   */

  public void addCell(Cell c) {
    this.add(c);
    int xcoord = c.getXcoord();
    int ycoord = c.getYcoord();
   //doCellToCellspaceCouplings(c);
    //doThisCellsNeighborCouplings(xcoord, ycoord);
  }


} // End class TwoDimCellSpace
