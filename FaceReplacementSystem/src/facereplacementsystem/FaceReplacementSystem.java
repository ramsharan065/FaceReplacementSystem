/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facereplacementsystem;

import CoreAlgorithms.ImageWarper;
import CoreAlgorithms.Shifter;
import CoreAlgorithms.SkinColorDetector;
import CoreAlgorithms.SkinColorDetectorUsingThreshold;
import CurveFitting.SquarePolynomial_002;
import CurveFitting.SquarePolynomial_003;
import DataStructures.FeaturePoint;
import H_Matrix.ImageMat;
import Helpers.DeepCopier;
import Helpers.MeanColorShifter;
import Helpers.SkinMatrixProvider;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 *
 * @author Robik Singh Shrestha
 */
/*
 * This class is the main class of the project. All the intermediate results and
 * final results are saved in this class.
 */
public class FaceReplacementSystem {

    // <editor-fold defaultstate="collapsed" desc="The first step is to set the source and target images">
    //1st step is to set the source and target images
    protected BufferedImage sourceImage;
    protected BufferedImage targetImage;
    // The source image and the target image must be set

    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    public void setTargetImage(BufferedImage targetImage) {
        this.targetImage = targetImage;
    }

    public BufferedImage getSourceImage() {
        return sourceImage;
    }

    public BufferedImage getTargetImage() {
        return targetImage;
    }
// </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="The second step is to set the feature points of the source and target faces">
    //Next step is to define feature points for both source and the target
    protected Point[] sourceFeaturePoints, targetFeaturePoints;

    public Point[] getSourceFeaturePoints() {
        return sourceFeaturePoints;
    }

    public Point[] getTargetFeaturePoints() {
        return targetFeaturePoints;
    }

    public void setSourceFeaturePoints(Point[] featurePoints) {
        this.sourceFeaturePoints = featurePoints;
        this.setSourceFaceRectangle(getRectangleUsingFeaturePoints(featurePoints));
    }
    protected Point[] actualTargetFeaturePoints;

    public void setTargetFeaturePoints(Point[] featurePoints) {
        this.actualTargetFeaturePoints = DeepCopier.getPoints(featurePoints);
        this.targetFeaturePoints = DeepCopier.getPoints(featurePoints);
        this.setTargetFaceRectangle(getRectangleUsingFeaturePoints(featurePoints));
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="The third step is to set the rectangles around the faces of both source and target">
    //Next step is to provide the coordinates of the corners of the rectangles drawn around the faces of source and target
    protected Rectangle sourceFaceRectangle, targetFaceRectangle;
    protected BufferedImage sourceRectangleImage, targetRectangleImage;

    public void setSourceFaceRectangle(Rectangle sourceFaceRectangle) {
        this.sourceFaceRectangle = sourceFaceRectangle;
        this.sourceRectangleImage = sourceImage.getSubimage(sourceFaceRectangle.x, sourceFaceRectangle.y, sourceFaceRectangle.width, sourceFaceRectangle.height);
    }

    public void setTargetFaceRectangle(Rectangle targetFaceRectangle) {
        this.targetFaceRectangle = targetFaceRectangle;
        this.targetRectangleImage = targetImage.getSubimage(targetFaceRectangle.x, targetFaceRectangle.y, targetFaceRectangle.width, targetFaceRectangle.height);
    }

    public BufferedImage getTargetRectangleImage() {
        return targetRectangleImage;
    }

    public BufferedImage getSourceRectangleImage() {
        return sourceRectangleImage;
    }

    public Rectangle getSourceFaceRectangle() {
        return sourceFaceRectangle;
    }

    public Rectangle getTargetFaceRectangle() {
        return targetFaceRectangle;
    }

    public Rectangle getRectangleUsingFeaturePoints(Point[] featurePoints) {
        Rectangle rectangleUsingFeaturePoints;
        int xmin, xmax, ymin, ymax;
        xmin = xmax = featurePoints[0].x;
        ymin = ymax = featurePoints[0].y;
        for (int i = 1; i < featurePoints.length; i++) {
            xmin = featurePoints[i].x < xmin ? featurePoints[i].x : xmin;
            ymin = featurePoints[i].y < ymin ? featurePoints[i].y : ymin;
            xmax = featurePoints[i].x > xmax ? featurePoints[i].x : xmax;
            ymax = featurePoints[i].y > ymax ? featurePoints[i].y : ymax;
        }
        rectangleUsingFeaturePoints = new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
        return rectangleUsingFeaturePoints;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Next step is to draw the chin curve">
    List<Point> sourceLeftEdge = null;
    List<Point> sourceRightEdge = null;
    List<Point> targetLeftEdge = null;
    List<Point> targetRightEdge = null;

    public List<Point> getSourceLeftEdge() {
        return sourceLeftEdge;
    }

    public List<Point> getSourceRightEdge() {
        return sourceRightEdge;
    }

    public List<Point> getTargetLeftEdge() {
        return targetLeftEdge;
    }

    public List<Point> getTargetRightEdge() {
        return targetRightEdge;
    }

    public void FindSourceCurves() {
        SquarePolynomial_002 sq = new SquarePolynomial_002();
        Point[] pLL = {sourceFeaturePoints[FeaturePoint.CHIN], sourceFeaturePoints[FeaturePoint.LEFT_CHIN],
            sourceFeaturePoints[FeaturePoint.LEFT_CHEEK]};
        //setting the example points, at least 3 required
        sq.setPoints(pLL);
        Point[] pl = {sourceFeaturePoints[FeaturePoint.LEFT_CHEEK], sourceFeaturePoints[FeaturePoint.CHIN]};
        sourceLeftEdge = sq.getPoints(pl);//getting the fitted Points

        SquarePolynomial_002 sq2 = new SquarePolynomial_002();
        Point[] p11 = {sourceFeaturePoints[FeaturePoint.CHIN], sourceFeaturePoints[FeaturePoint.RIGHT_CHIN],
            sourceFeaturePoints[FeaturePoint.RIGHT_CHEEK]};
        sq2.setPoints(p11);
        Point[] example = {sourceFeaturePoints[FeaturePoint.CHIN], sourceFeaturePoints[FeaturePoint.RIGHT_CHEEK]};
        sourceRightEdge = sq2.getPoints(example);
    }

    public void FindTargetCurves() {
        SquarePolynomial_003 sq = new SquarePolynomial_003();
        Point[] pLL = {targetFeaturePoints[FeaturePoint.CHIN], targetFeaturePoints[FeaturePoint.LEFT_CHIN],
            targetFeaturePoints[FeaturePoint.LEFT_CHEEK]};
        //setting the example points, at least 3 required
        sq.setPoints(pLL);
        Point[] pl = {targetFeaturePoints[FeaturePoint.LEFT_CHEEK], targetFeaturePoints[FeaturePoint.CHIN]};
        targetLeftEdge = sq.getPoints(pl);//getting the fitted Points

        SquarePolynomial_003 sq2 = new SquarePolynomial_003();
        Point[] p11 = {targetFeaturePoints[FeaturePoint.CHIN], targetFeaturePoints[FeaturePoint.RIGHT_CHEEK],
            targetFeaturePoints[FeaturePoint.RIGHT_CHIN]};
        sq2.setPoints(p11);
        Point[] example = {targetFeaturePoints[FeaturePoint.CHIN], targetFeaturePoints[FeaturePoint.RIGHT_CHEEK]};
        targetRightEdge = sq2.getPoints(example);
    }
    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="The fourth step is to detect the skin in source and target">
    //Next step is to detect the skin pixels in source image. They will be used to extract the source face.
    protected BufferedImage sourceSkinImage, targetSkinImage;
    protected int[][] sourceSkinMatrix, targetSkinMatrix;

    //Detect the skin region of the source
    public void detectSourceSkin() {
        SkinColorDetector sourceSkinDetector = new SkinColorDetectorUsingThreshold(sourceRectangleImage);
        sourceSkinDetector.detectSkin();
        sourceSkinMatrix = sourceSkinDetector.getSkinMatrix();
        sourceSkinImage = sourceSkinDetector.getSkinImage();
    }

    public void detectTargetSkin() {
        SkinColorDetector targetSkinDetector = new SkinColorDetectorUsingThreshold(targetRectangleImage);
        targetSkinDetector.detectSkin();
        targetSkinMatrix = targetSkinDetector.getSkinMatrix();
        targetSkinImage = targetSkinDetector.getSkinImage();
    }

    public void detectSkin() {
        detectSourceSkin();
        detectTargetSkin();
    }

    public BufferedImage getSourceSkinImage() {
        return this.sourceSkinImage;
    }

    public BufferedImage getTargetSkinImage() {
        return this.targetSkinImage;
    }

    public int[][] getSourceSkinMatrix() {
        return sourceSkinMatrix;
    }

    public int[][] getTargetSkinMatrix() {
        return targetSkinMatrix;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Next step is to extract the boundary around the face">
    int[][] sourceBoundaryFilledFaceMatrix = null;
    //ImageMat im=new ImageMat();
    /*
     * ImageMat is the function for matrix operation. Static functions of ImageMat are used.
     * But since it is notified that it is bad programming practice to use static functions ,
     *  it could be made non static.
     * 
     * int [][] boundaryOfAnImage=ImageMat.findBoundary(int[][] binaryMatrix, int w, int h);
     * int[][] horizontalHoleFilledMatrix=ImageMat.holeFillAccordingToBoundary(matrix,w,h);
     * then invert the matrix ImageMat.invert(matrix, width, height);
     * then again hole fill then again invert and hole fill.
     */
    
    /*int matrix=getRectangleMatrix();//i dont know which one is the matrix.
     * int shrinkCount=5;
        matrix=ImageMat.shrinkMatrix(matrix, width, height, shrinkCount);//shrunk matrix
        matrix=ImageMat.growMatrix(matrix, width, height, shrinkCount);//grown matrix
        
        int [][] fineMatrix=ImageMat.holeFillAccordingToBoundary(matrix,width,height);
        int [][] inverseMatrix=ImageMat.invertMatrix(fineMatrix, width, height);
        int [][] finerMatrix=ImageMat.holeFillAccordingToBoundary(inverseMatrix,height,width);
        int [][] erectMatrix=ImageMat.invertMatrix(finerMatrix, height, width);
        * sourceBoundaryFilledFaceMatrix=erectMatrix;
     */
    public void extractBoundary() {
    }
    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="The fifth step is to warp the source face according to the fp of target face and interpolate the warped face">
    //Next step is to warp the image. The source image should be warped according to the feature points of the target image.
    protected ImageWarper imageWarper;
    //Next step is to apply interpolation
    protected BufferedImage warpedImage;
    //Next step is to apply color consistency to image
    protected MeanColorShifter meanColorShifter;
    //origin of the warped image
    protected Point warpedOrigin;
    //warp + interpolate
    protected int[][] warpedSkinMatrix;

    public void warp(int originIndex) {
        Point[] sfp = new Point[this.sourceFeaturePoints.length];
        Point[] tfp = new Point[this.targetFeaturePoints.length];
        //Translate the feature points so that it is relative to the face rectangle
        for (int i = 0; i < this.sourceFeaturePoints.length; i++) {
            sfp[i] = new Point(this.sourceFeaturePoints[i].x - this.sourceFaceRectangle.x, this.sourceFeaturePoints[i].y - this.sourceFaceRectangle.y);
            tfp[i] = new Point(this.targetFeaturePoints[i].x - this.targetFaceRectangle.x, this.targetFeaturePoints[i].y - this.targetFaceRectangle.y);
        }
        
        imageWarper = new ImageWarper(sourceSkinImage, sfp, tfp, originIndex);
        warpedImage = imageWarper.runGet();

        warpedSkinMatrix = SkinMatrixProvider.getSkinMatrix(warpedImage);
        this.warpedOrigin = imageWarper.getMappedOrigin();
        warpedImage.getGraphics().setColor(Color.RED);
        warpedImage.getGraphics().fillOval(warpedOrigin.x - 5, warpedOrigin.y - 5, 10, 10);

    }

    public BufferedImage getWarpedImage() {
        return warpedImage;
    }

    public Point getWarpedOrigin() {
        return warpedOrigin;
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="The sixth step is to apply color consistency to the image">
    //Next step is to adjust the consistency of the image
    protected BufferedImage colorConsistentImage;

    public void applyColorConsistency() {
        //Adjust the color of the warped image according to the color of the target image
        meanColorShifter = new MeanColorShifter(warpedImage, targetSkinImage);
        colorConsistentImage = meanColorShifter.runGet();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="The next step is to shift the position of the replacement point">
    protected Point replacementPoint;

    public Point getReplacementPoint() {
        return replacementPoint;
    }

    //It finds out the best point at which the image is to be replaced
    public void shiftReplacementPoint() {
        Shifter shifter = new Shifter();
        shifter.setSource(warpedSkinMatrix);
        shifter.setSourcePastePoint(warpedOrigin);
        shifter.setTarget(targetSkinMatrix);
        Point targetOriginPoint = targetFeaturePoints[FeaturePoint.CHIN];
        targetOriginPoint.x -= targetFaceRectangle.x;
        targetOriginPoint.y -= targetFaceRectangle.y;
        shifter.setTargetPastePoint(targetOriginPoint);
        replacementPoint = shifter.getPastingPoint(15, 15);
        replacementPoint.x += targetFaceRectangle.x;
        replacementPoint.y += targetFaceRectangle.y;
    }
    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="The next step is to replace face">
    protected BufferedImage replacedFaceImage;

//    public void replaceFace() {
//        warp(FeaturePoint.CHIN);
//        applyColorConsistency();
//        replacedFaceImage = DeepCopier.getBufferedImage(targetImage, BufferedImage.TYPE_INT_ARGB);
//        shiftReplacementPoint();
//        //Point replacementPoint = new Point(bestPointToReplace.x - warpedOrigin.x, bestPointToReplace.y - warpedOrigin.y);
//        for (int x = 0; x < colorConsistentImage.getWidth(); x++) {
//            for (int y = 0; y < colorConsistentImage.getHeight(); y++) {
//                Color c = new Color(colorConsistentImage.getRGB(x, y), true);
//                if (c.getAlpha() < 255) {
//                    continue;
//                }
//                try {
//                    replacedFaceImage.setRGB(x - warpedOrigin.x + replacementPoint.x, y - warpedOrigin.y + replacementPoint.y, c.getRGB());
//                } catch (Exception e) {
//                    continue;
//                }
//            }
//        }
//    }
    public void replaceFace() {
        warp(FeaturePoint.CHIN);
        applyColorConsistency();
        replacedFaceImage = DeepCopier.getBufferedImage(targetImage, BufferedImage.TYPE_INT_ARGB);
        //shiftReplacementPoint();
        //Point replacementPoint = new Point(bestPointToReplace.x - warpedOrigin.x, bestPointToReplace.y - warpedOrigin.y);
        for (int x = 0; x < colorConsistentImage.getWidth(); x++) {
            for (int y = 0; y < colorConsistentImage.getHeight(); y++) {
                Color c = new Color(colorConsistentImage.getRGB(x, y), true);
                if (c.getAlpha() < 255) {
                    continue;
                }
                try {
                    replacedFaceImage.setRGB(x - warpedOrigin.x + targetFeaturePoints[FeaturePoint.CHIN].x, y - warpedOrigin.y + targetFeaturePoints[FeaturePoint.CHIN].y, c.getRGB());
                    //replacedFaceImage.setRGB(x - warpedOrigin.x + replacementPoint.x, y - warpedOrigin.y + replacementPoint.y, c.getRGB());
                } catch (Exception e) {
                    continue;
                }
            }
        }
        replacedFaceImage.getGraphics().fillOval(targetFeaturePoints[FeaturePoint.CHIN].x - 5, targetFeaturePoints[FeaturePoint.CHIN].y - 5, 10, 10);
        //replacedFaceImage.getGraphics().fillOval(actualTargetFeaturePoints[FeaturePoint.CHIN].x - 5, actualTargetFeaturePoints[FeaturePoint.CHIN].y - 5, 10, 10);

    }

    public BufferedImage getReplacedFaceImage() {
        return replacedFaceImage;
    }
    //</editor-fold>
}
