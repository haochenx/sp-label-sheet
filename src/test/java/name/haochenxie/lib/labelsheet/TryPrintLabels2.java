package name.haochenxie.lib.labelsheet;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Iterator;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.google.common.primitives.Longs;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.aztec.AztecWriter;
import com.google.zxing.common.BitMatrix;

public class TryPrintLabels2 {

  private SheetLayout aOne65 = new SheetLayout() {

    @Override
    public Iterator<Double> iterator() {
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
        public Double next() {
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

  private static double translate(double x) {
    return x * 2.83464567;
  }

  public static void main(String[] args) throws Exception {

    LabelPrintingQueue queue = new LabelPrintingQueue(new TryPrintLabels2().aOne65);
    queue.setCalibrationWithMM(24, 20.7);
    // queue.setCalibrationWithMM(5, 5);

    queue.printCalibrationPage(true);
    System.exit(0);

    LabelPrintable label = new LabelPrintable() {

      Stroke stroke = new BasicStroke(0.4f);
      Font font = new Font("Times New Roman", Font.PLAIN, 6);

      @Override
      public boolean print(LabelArea area, Graphics2D g) {
        g.setStroke(stroke);

        g.draw(new Rectangle2D.Double(0, 0, area.x, area.y));
        g.drawString(Integer.toString(area.getAreaIndex()), (int) translate(10), (int) translate(10));

        return true;
      }
    };

    for (int i=0; i < 65; ++i) {
      queue.addJob(label);
    }

    queue.print(true);

  }

  @Test
  public void tryPrintBarcodes() throws Exception {
    LabelPrintingQueue queue = new LabelPrintingQueue(aOne65);

    LabelPrintable label1 = new LabelPrintable() {

      @Override
      public boolean print(LabelArea area, Graphics2D g) {
        try {
          double w = area.x;
          double h = area.y;

          double margin = translate(2);

          double bcSize = h - margin * 2;

          String itemId = getRandomItemIdString();
          BitMatrix bc = generateAztecBarcode(getItemUrl(itemId), 0);
          double bcPixelSize = bcSize / bc.getHeight();

          for (int i=0; i < bc.getWidth(); ++i) {
            for (int j=0; j < bc.getHeight() ; ++j) {
              if (bc.get(i, j)) {
                g.fill(new Rectangle2D.Double(margin + i * bcPixelSize, margin + j * bcPixelSize, bcPixelSize, bcPixelSize));
              }
            }
          }

          {
            AttributedString attstr = new AttributedString(itemId);
            attstr.addAttribute(TextAttribute.FAMILY, "Courier New");
            attstr.addAttribute(TextAttribute.SIZE, 6);
            AttributedCharacterIterator paragraph = attstr.getIterator();
            int paraStart = paragraph.getBeginIndex();
            int paraEnd = paragraph.getEndIndex();
            FontRenderContext renderContext = g.getFontRenderContext();
            LineBreakMeasurer lineMeasure = new LineBreakMeasurer(paragraph, renderContext);

            float breakWidth = (float) (w - bcSize - margin - margin - margin);

            int drawBaseY = (int) margin;
            int drawBaseX = (int) (margin + bcSize + margin);

            int drawOffsetY = 0;
            lineMeasure.setPosition(paraStart);

            while (lineMeasure.getPosition() < paraEnd) {
              TextLayout layout = lineMeasure.nextLayout(breakWidth);

              drawOffsetY += layout.getAscent();

              layout.draw(g, drawBaseX, drawBaseY + drawOffsetY);

              drawOffsetY += layout.getDescent() + layout.getLeading();
            }
          }

          return true;
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    };

    int rep = aOne65.getLabelCount();

    for (int i = 0; i < rep; ++i) {
      queue.addJob(label1);
    }

    queue.print(true);

  }

  private SecureRandom SRNG = new SecureRandom();

  private String getRandomItemIdString() throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    byte[] buff = new byte[128];
    SRNG.nextBytes(buff);
    md.update(buff);
    md.update(Longs.toByteArray(System.currentTimeMillis()));

    byte[] hash = md.digest();

    return String.format("hex=%s", Hex.encodeHexString(hash));
  }

  private String getItemUrl(String itemId) {
    return String.format("https://hxie.cc/.i/%s", itemId);
  }

  public BitMatrix generateAztecBarcode(String data, int size) throws Exception {
    AztecWriter aztecWriter = new AztecWriter();
    BitMatrix bmap = aztecWriter.encode(data, BarcodeFormat.AZTEC, size, size);
    return bmap;
  }

}
