package name.haochenxie.lib.labelsheet;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Iterator;

import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.google.common.primitives.Longs;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.aztec.AztecWriter;
import com.google.zxing.common.BitMatrix;

public class TryPrintLabels {

  private SheetLayout aOne65 = new SheetLayout() {

    @Override
    public Iterator<Double> iterator() {
      return new Iterator<Rectangle2D.Double>() {

        private int cur = 0;

        private double topPadding = 6.6; // mm
        private double leftPadding = 4.75 - 1; // mm

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

  @Test
  public void tryPrintBarcodes() throws Exception {
    LabelPrintingQueue queue = new LabelPrintingQueue(aOne65);

    LabelPrintable label1 = new LabelPrintable() {

      @Override
      public boolean print(LabelArea area, Graphics2D g) {
        try {
          double w = area.x;
          double h = area.y;

          g.setStroke(new BasicStroke(0.2f));
          double margin = translate(2);

          g.draw(new Rectangle2D.Double(margin, margin, w-margin, h-margin));

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
