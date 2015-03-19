package name.haochenxie.lib.labelsheet;

import static org.junit.Assert.assertEquals;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.junit.Test;

public class LabelPrintingQueueTest {

  @Test
  public void testPageCountAreHandledCorrectly() throws Exception {
    SheetLayout sheetLayout = new SheetLayout() {

      @Override
      public Iterator<Rectangle2D.Double> iterator() {
        return Arrays.asList(
            new Rectangle2D.Double(0, 0, 1, 1),
            new Rectangle2D.Double(1, 0, 1, 1)).iterator();
      }

      @Override
      public int getLabelCount() {
        return 2;
      }
    };

    LabelPrintingQueue queue = new LabelPrintingQueue(sheetLayout);

    LabelPrintable labelPrintable = new LabelPrintable() {

      @Override
      public boolean print(LabelArea area, Graphics2D g) {
        double w = area.x;
        double h = area.y;

        g.draw(new Rectangle2D.Double(0, 0, w, h));

        return true;
      }
    };

    IntStream.range(0, 10)
      .forEach(x -> queue.addJob(labelPrintable));

    Printable renderedPrintable = queue.renderQueue();

    assertEquals(Printable.PAGE_EXISTS,
        renderedPrintable.print(new NoOpGraphics2D(), new PageFormat(), 0));
    assertEquals(Printable.PAGE_EXISTS,
        renderedPrintable.print(new NoOpGraphics2D(), new PageFormat(), 1));
    assertEquals(Printable.PAGE_EXISTS,
        renderedPrintable.print(new NoOpGraphics2D(), new PageFormat(), 2));
    assertEquals(Printable.PAGE_EXISTS,
        renderedPrintable.print(new NoOpGraphics2D(), new PageFormat(), 3));
    assertEquals(Printable.PAGE_EXISTS,
        renderedPrintable.print(new NoOpGraphics2D(), new PageFormat(), 4));
    assertEquals(Printable.NO_SUCH_PAGE,
        renderedPrintable.print(new NoOpGraphics2D(), new PageFormat(), 5));
  }

}
