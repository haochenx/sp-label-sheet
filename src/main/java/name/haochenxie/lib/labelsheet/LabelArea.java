package name.haochenxie.lib.labelsheet;

import java.awt.geom.Point2D;

public class LabelArea extends Point2D.Double {

  private static final long serialVersionUID = -696391983303495926L;

  private int areaIndex;

  public LabelArea(double width, double height, int areaIndex) {
    super(width, height);
    this.areaIndex = areaIndex;
  }

  /**
   * @return the index of this label area in a page, in {@code int}
   */
  public int getAreaIndex() {
    return areaIndex;
  }


}
