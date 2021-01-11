package cn.foxluo.likepicture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;

import com.bumptech.glide.Glide;

import java.io.File;

public class ImageHelper {
    private static int size = 32;
    private static int smallerSize = 8;
    private static int average=0;
    private static Context context;

    public static String getHashCode(PhotoBean photo) {
        if (c == null) {
            initCoefficients();
        }
        try {

            photo = getHash(photo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photo.getHashCode();
    }

    private static PhotoBean getHash(PhotoBean photo) {

        /*
        1.缩小尺寸
        */
        //网络图片不加载hash
        if (photo.getPath().contains("http://")||photo.getPath().contains("https://")){
            return photo;
        }
        Bitmap img = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photo.getPath()), size, size);

        /*
        2.简化色彩
        */
        try {
            img = ImageHelper.convertGreyImg(img);
        }catch (Exception e){
            e.printStackTrace();
            return photo;
        }

        double[][] vals = new double[size][size];
        int[] pixels = new int[size * size];
        img.getPixels(pixels, 0, size, 0, 0, size, size);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                vals[x][y] = img.getPixel(x, y) & 0xff;
            }
        }
        img.recycle();
        img=null;
        /*
        3.计算DCT.
        */

        double[][] dctVals = applyDCT(vals);

        double total = 0;
        /*
        4.缩小DCT
        */
        for (int x = 0; x < smallerSize; x++) {
            for (int y = 0; y < smallerSize; y++) {
                total += dctVals[x][y];
            }
        }
        /*
        5. 计算平均值
        */
        total -= dctVals[0][0];

        double avg = total / (double) ((smallerSize * smallerSize) - 1);
        photo.setAvg(average);
        /*
        6.转化&构造hash
        */
        String hash = "";

        for (int x = 0; x < smallerSize; x++) {
            for (int y = 0; y < smallerSize; y++) {
                hash += (dctVals[x][y] > avg ? "1" : "0");
            }
        }
        photo.setHashCode(hash);
        return photo;
    }

    /**
     * 离散余弦变换
     *
     * @param f
     * @return f->DCT
     */
    private static double[][] applyDCT(double[][] f) {
        int N = size;
        double[][] F = new double[N][N];
        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0.0;
                for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        sum += Math.cos(((2 * i + 1) / (2.0 * N)) * u * Math.PI) * Math.cos(((2 * j + 1) / (2.0 * N)) * v * Math.PI) * (f[i][j]);
                    }
                }
                sum *= ((c[u] * c[v]) / 4.0);
                F[u][v] = sum;
            }
        }
        return F;
    }

    private static double[] c;

    private static void initCoefficients() {
        c = new double[size];

        for (int i = 1; i < size; i++) {
            c[i] = 1;
        }
        c[0] = 1 / Math.sqrt(2.0);
    }

    private static Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixels = new int[width * height];
        average=0;
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int original = pixels[width * i + j];
                int red = ((original & 0x00FF0000) >> 16);
                int green = ((original & 0x0000FF00) >> 8);
                int blue = (original & 0x000000FF);

                int grey = (int) ((float) red * 0.299 + (float) green * 0.587 + (float) blue * 0.114);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                average+=grey;
                pixels[width * i + j] = grey;
            }
        }
        img = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        img.setPixels(pixels, 0, width, 0, 0, width, height);
        average=average/(width*height);
        return img;
    }
}
