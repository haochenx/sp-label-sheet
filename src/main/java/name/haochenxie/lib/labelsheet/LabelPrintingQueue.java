package name.haochenxie.lib.labelsheet;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * The interface class that operates the printer for executing label printing.
 */
public class LabelPrintingQueue {

  private PrinterJob printerJob;

  private SheetLayout sheetLayout;

  private Deque<LabelPrintable> queue;

  public LabelPrintingQueue(SheetLayout sheetLayout) {
    this.printerJob = PrinterJob.getPrinterJob();
    this.queue = new ArrayDeque<>();
    this.sheetLayout = sheetLayout;
  }

  public void addJob(LabelPrintable label) {
    queue.addLast(label);
  }

  public void addJob(Collection<LabelPrintable> labels) {
    queue.addAll(labels);
  }

  protected Printable renderQueue() {
    List<LabelPrintable> labels = new ArrayList<>(queue);
    queue.clear();

    List<Rectangle2D.Double> layoutCache = buildLayoutCache();

    int pageLabelCount = sheetLayout.getLabelCount();
    int pageCount = (int) Math.ceil((double) labels.size() / pageLabelCount);

    for (int pageIndex = 0; pageIndex < pageCount; ++pageIndex) {

    }

    return new Printable() {

      @Override
      public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        Graphics2D g2d = (Graphics2D) graphics;

        if (pageIndex < pageCount) {
          List<LabelPrintable> pageLabels =
              labels.subList(pageIndex * pageLabelCount,
                  Math.min((pageIndex + 1) * pageLabelCount, labels.size()));

          Iterator<LabelPrintable> labelIterator = pageLabels.iterator();
          Iterator<Rectangle2D.Double> areaIterator = layoutCache.iterator();
          int idx = 0;

          AffineTransform defaultTransform = g2d.getTransform();
          while (labelIterator.hasNext() && areaIterator.hasNext()) {
            LabelPrintable label = labelIterator.next();
            Rectangle2D.Double pageArea = areaIterator.next();

            LabelArea area = new LabelArea(pageArea.width, pageArea.height, idx++);

            g2d.setClip(null);
            g2d.setTransform(defaultTransform);
            g2d.clip(pageArea);
            g2d.translate(pageArea.x, pageArea.y);

            label.print(area, g2d);
          }

          return PAGE_EXISTS;
        } else {
          return NO_SUCH_PAGE;
        }
      }
    };
  }

  private List<Rectangle2D.Double> buildLayoutCache() {
    return Lists.newArrayList(sheetLayout);
  }

  public boolean print(boolean showPrintDialog)
      throws PrinterException {
    boolean doPrint = true;

    if (showPrintDialog) {
      doPrint = printerJob.printDialog();
    }

    if (doPrint) {
      Printable document = renderQueue();

      printerJob.setPrintable(document);
      printerJob.print();
    }

    return doPrint;
  }

}
