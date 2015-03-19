package name.haochenxie.lib.labelsheet;

import java.awt.Graphics2D;

public interface LabelPrintable {

  /**
   * @param area
   * @param g
   * @return a boolean indicating whether this label could fit in the {@code area}
   */
  public boolean print(LabelArea area, Graphics2D g);

}
