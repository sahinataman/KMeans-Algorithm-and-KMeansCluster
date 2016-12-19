package kmeans; 

/**
 *
 * @author shn.ataman
 */

import java.awt.image.BufferedImage; 
import java.io.File; 
import java.util.Arrays; 
import javax.imageio.ImageIO; 

public class KMeansCluster { 
    
    BufferedImage original; 
    BufferedImage result; 
    Cluster[] clusters; 
    public static final int MODE_CONTINUOUS = 1; 
    public static final int MODE_ITERATIVE = 2; 
         
    public static void main(String[] args) { 
         
        String src ="ImageInput.jpg";  
        String dst = "OutputImage.jpg";
        int k = Integer.parseInt("3" ); //Cluster Sayisi
        String m = " [mode -i (ITERATIVE)|-c (CONTINUOS)]"; 
        int mode = 1; 
        if (m.equals("-i")) { 
            mode = MODE_ITERATIVE; 
        } else if (m.equals("-c")) { 
            mode = MODE_CONTINUOUS; 
        } 
         
        // KMeansCluster objesi oluşturuluyor 
        KMeansCluster kmeans = new KMeansCluster(); 
        // Hesaplama fonksiyonu cagırılarak kümeleme gerceklesiyor
        BufferedImage dstImage = kmeans.calculate(loadImage(src), 
                                                    k,mode); 
        // Sonuclari image üzerinde kaydetme 
        saveImage(dst, dstImage); 
    } 
     
    public KMeansCluster() {    } 
     
    public BufferedImage calculate(BufferedImage image, int k, int mode) { 
        long start = System.currentTimeMillis(); 
        int w = image.getWidth(); 
        int h = image.getHeight(); 
        // Cluster yaratiom
        clusters = createClusters(image,k); 
        // Yarattığım Cluster ın tablolarına bakıyorum
        int[] lut = new int[w*h]; 
        Arrays.fill(lut, -1); 
         
        // Tüm kümelerin piksellerini hareket ettirme
        boolean pixelChangedCluster = true; 
        // Cluster lar dengelenene kadar döngü!
        int loops = 0; 
        while (pixelChangedCluster) { 
            pixelChangedCluster = false; 
            loops++; 
            for (int y=0;y<h;y++) { 
                for (int x=0;x<w;x++) { 
                    int pixel = image.getRGB(x, y); 
                    Cluster cluster = findMinimalCluster(pixel); 
                    if (lut[w*y+x]!=cluster.getId()) { 
                        // Cluster değiştirme 
                        if (mode==MODE_CONTINUOUS) { 
                            if (lut[w*y+x]!=-1) { 
                                // Onceki Cluster kaldırma
                                clusters[lut[w*y+x]].removePixel( pixel); 
                            } 
                            // Cluster a piksel ekleme
                            cluster.addPixel(pixel); 
                        } 
                        // Döngüye devam...
                        pixelChangedCluster = true; 
                     
                        // Image güncelleme
                        lut[w*y+x] = cluster.getId(); 
                    } 
                } 
            } 
            if (mode==MODE_ITERATIVE) { 
                // Cluster ları güncelleme
                for (int i=0;i<clusters.length;i++) { 
                    clusters[i].clear(); 
                } 
                for (int y=0;y<h;y++) { 
                    for (int x=0;x<w;x++) { 
                        int clusterId = lut[w*y+x]; 
                        // Cluster lara piksel ekleme
                        clusters[clusterId].addPixel(image.getRGB(x, y)); 
                    } 
                } 
            } 
             
        } 
        // Cıktı image olusturma 
        BufferedImage result = new BufferedImage(w, h,  
                                    BufferedImage.TYPE_INT_RGB); 
        for (int y=0;y<h;y++) { 
            for (int x=0;x<w;x++) { 
                int clusterId = lut[w*y+x]; 
                result.setRGB(x, y, clusters[clusterId].getRGB()); 
            } 
        } 
        long end = System.currentTimeMillis(); 
        System.out.println("Clustered to "+k 
                            + " clusters in "+loops 
                            +" loops in "+(end-start)+" ms."); 
        return result; 
    } 
    
     public Cluster findMinimalCluster(int rgb) { 
        Cluster cluster = null; 
        int min = Integer.MAX_VALUE; 
        for (int i=0;i<clusters.length;i++) { 
            int distance = clusters[i].distance(rgb); 
            if (distance<min) { 
                min = distance; 
                cluster = clusters[i]; 
            } 
        } 
        return cluster; 
    } 
     
    public Cluster[] createClusters(BufferedImage image, int k) { 
        // Burada Cluster lar belirli adımlarla gerceklesir
        // İstersek, küme merkezlerini rastgele seçebiliriz. 
        Cluster[] result = new Cluster[k]; 
        int x = 0; int y = 0; 
        int dx = image.getWidth()/k; 
        int dy = image.getHeight()/k; 
        for (int i=0;i<k;i++) { 
            result[i] = new Cluster(i,image.getRGB(x, y)); 
            x+=dx; y+=dy; 
        } 
        return result; 
    } 
    
    public static BufferedImage loadImage(String filename) { 
        BufferedImage result = null; 
        try { 
            result = ImageIO.read(new File(filename)); 
        } catch (Exception e) { 
            System.out.println(e.toString()+" Image '" 
                                +filename+"' not found."); 
        } 
        return result; 
    } 
    
    public static void saveImage(String filename,  
            BufferedImage image) { 
        File file = new File(filename); 
        try { 
            ImageIO.write(image, "png", file); 
        } catch (Exception e) { 
            System.out.println(e.toString()+" Image '"+filename 
                                +"' saving failed."); 
        } 
    }  
} 