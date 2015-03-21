package name.haochenxie.lib.labelsheet;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import org.junit.Ignore;
import org.junit.Test;

public class TryPrintCalibrationPage {

  private PrinterJob printerJob = PrinterJob.getPrinterJob();

  @Test
  public void tryPrintHello() throws PrinterException {
    Printable doc = new Printable() {

      @Override
      public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        switch (pageIndex) {
          case 0:
            Graphics2D g = (Graphics2D) graphics;

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
    printerJob.setPrintable(doc);


    boolean printed = printerJob.printDialog();
    if (printed) {
      printerJob.print();
    }
  }

  private static double translate(double mm) {
    return mm * 2.83464567;
  }

}
