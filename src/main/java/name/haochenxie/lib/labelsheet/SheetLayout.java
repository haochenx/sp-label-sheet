package name.haochenxie.lib.labelsheet;

import java.awt.geom.Rectangle2D;

/**
 * A layout should represents a list of label areas, in the form of
 * {@code Iterable<Rectangle2D.Double>}
 */
public interface SheetLayout extends Iterable<Rectangle2D.Double> {

  public int getLabelCount();

}
