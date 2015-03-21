package name.haochenxie.lib.labelsheet;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
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
import java.util.stream.IntStream;

import com.google.common.collect.Lists;

/**
 * The interface class that operates the printer for executing label printing.
 */
public class LabelPrintingQueue {

  private PrinterJob printerJob;

  private SheetLayout sheetLayout;

  private Deque<LabelPrintable> queue;

  private double calibrationX = 0;
  private double calibrationY = 0;

  public LabelPrintingQueue(SheetLayout sheetLayout) {
    this.printerJob = PrinterJob.getPrinterJob();
    this.queue = new ArrayDeque<>();
    this.sheetLayout = sheetLayout;
  }

  public void setCalibrationWithMM(double mmTop20mmLine, double mmLeft20mmLine) {
    calibrationX = translate(20 - mmLeft20mmLine);
    calibrationY = translate(20 - mmTop20mmLine);
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

          g2d.translate(calibrationX, calibrationY);

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

  public boolean printLayoutAlignmentConfirmationPage(boolean showPrintDialog)
      throws PrinterException {
    LabelPrintable labelPrintable = new LabelPrintable() {

      private Stroke stroke = new BasicStroke(0.4f);
      private Font font = new Font("Times New Roman", Font.PLAIN, 6);

      @Override
      public boolean print(LabelArea area, Graphics2D g) {
        g.setStroke(stroke);
        g.setFont(font);

        g.draw(new Rectangle2D.Double(0, 0, area.x, area.y));
        g.drawString(Integer.toString(area.getAreaIndex()), (int) translate(10), (int) translate(10));

        return true;
      }
    };

    for (int i=0; i < sheetLayout.getLabelCount(); ++i) {
      this.addJob(labelPrintable);
    }

    return this.print(showPrintDialog);
  }

  public boolean printCalibrationPage(boolean showPrintDialog) throws PrinterException {
    boolean doPrint = true;
    PrinterJob printerJob = PrinterJob.getPrinterJob();

    if (showPrintDialog) {
      doPrint = printerJob.printDialog();
    }

    if (doPrint) {
      Printable document = new Printable() {

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
          switch (pageIndex) {
            case 0:
              Graphics2D g = (Graphics2D) graphics;

              g.translate(calibrationX, calibrationY);

              double imgX = pageFormat.getImageableX();
              double imgY = pageFormat.getImageableY();
              double imgW = pageFormat.getImageableWidth();
              double imgH = pageFormat.getImageableHeight();

              float baseStrokeWidth = 0.4f;

              Stroke thick = new BasicStroke(baseStrokeWidth);
              // Stroke thin = new BasicStroke(baseStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] {baseStrokeWidth, 2 * baseStrokeWidth, 3 * baseStrokeWidth, 2 * baseStrokeWidth}, 0.0f);

              g.setStroke(thick);

              Font font = new Font("Courier New", Font.PLAIN, 8);
              g.setFont(font);

              g.draw(new Line2D.Double(imgX, translate(20), imgX + imgW, translate(20)));
              g.drawString("20 mm", (float) (imgX + imgW / 2), (float) translate(20 - 1));

              g.draw(new Line2D.Double(imgX, translate(25), imgX + imgW, translate(25)));
              g.drawString("25 mm", (float) (imgX + imgW / 2), (float) translate(25 - 1));

              AffineTransform fontAT = new AffineTransform();
              fontAT.rotate(270 * Math.PI/180);

              g.setFont(font.deriveFont(fontAT));

              g.draw(new Line2D.Double(translate(20), imgY, translate(20), imgY + imgH));
              g.drawString("20 mm", (float) translate(20-1), (float) (imgY + imgH / 2));
              g.draw(new Line2D.Double(translate(25), imgY, translate(25), imgY + imgH));
              g.drawString("25 mm", (float) translate(25-1), (float) (imgY + imgH / 2));

              return PAGE_EXISTS;
            default:
              return NO_SUCH_PAGE;
          }
        }
      };

      printerJob.setPrintable(document);
      printerJob.print();
    }

    return doPrint;
  }

  public static void main(String[] args) throws Exception {
    SheetLayout aOne65 = new SheetLayout() {

      @Override
      public Iterator<Rectangle2D.Double> iterator() {
        return new Iterator<Rectangle2D.Double>() {

          private int cur = 0;

          private double topPadding = 10.92; // mm
          private double leftPadding = 4.75; // mm

          private double cellWidth = 38.1; // mm
          private double cellHeight = 21.2; // mm

          private double columnSpace = 2.5; // mm

          @Override
          public boolean hasNext() {
            return cur < 65;
          }

          @Override
          public Rectangle2D.Double next() {
            int column = cur / 13;
            int row = cur % 13;

            ++cur;
            return new Rectangle2D.Double(
                translate(column * (columnSpace + cellWidth) + leftPadding), translate(row
                    * cellHeight + topPadding), translate(cellWidth), translate(cellHeight));
          }
        };

      }

      @Override
      public int getLabelCount() {
        return 65;
      }
    };

    LabelPrintingQueue queue = new LabelPrintingQueue(aOne65);
    queue.setCalibrationWithMM(24, 20.7);
    queue.printCalibrationPage(true);

    System.exit(0);

    IntStream.range(0, aOne65.getLabelCount())
      .forEach(x -> queue.addJob(new LabelPrintable() {

        private BasicStroke stroke = new BasicStroke(0.4f);

        @Override
        public boolean print(LabelArea area, Graphics2D g) {
          double margin = translate(2);

          g.setStroke(stroke);
          Rectangle2D.Double rect = new Rectangle2D.Double(margin, margin, area.x-margin, area.y-margin);
          g.draw(rect);

          return true;
        }
      }));

    queue.print(true);
  }

  public static double translate(double mm) {
    return mm * 2.83464567;
  }

}
