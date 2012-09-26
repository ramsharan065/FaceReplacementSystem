package frs.gui.pages;

import frs.gui.components.FeaturePointsView;
import frs.gui.components.IOSUIImageView;
import frs.main.RFApplication;
import hu.droidzone.iosui.IOSUILabel;
import hu.droidzone.iosui.IOSUITextArea;
import hu.droidzone.iosui.IOSUIView;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

public class P20_SrcFP extends RFPage {

    FeaturePointsView fp = new FeaturePointsView("500px", "400px");
    IOSUIView rightView = new IOSUIView("220px","40px,10px,350px");
    IOSUILabel sampleImgLbl = new IOSUILabel("Sample Image:");
    IOSUIImageView sampleView = new IOSUIImageView("220px", "350px");

    public P20_SrcFP(RFApplication app) {
        super(app, "Specify Feature Points");
        fp.setImage(frs.getSrcImg());
        initComponents();

        helpText.setText("Specify the feature points on the face.");
    }

    protected void initComponents() {
        mainView = new IOSUIView("10px,500px,10px,220px,10px", "400px");
        mainView.addXY(fp, 2, 1, "f,f");
        try {
            sampleView.setImage((BufferedImage) (ImageIO.read(new File("images\\Capture.PNG"))));
        } catch (IOException ex) {
            Logger.getLogger(P20_SrcFP.class.getName()).log(Level.SEVERE, null, ex);
        }
        sampleImgLbl.setForeground(Color.WHITE);
        sampleImgLbl.setFont(new Font(Font.SANS_SERIF,Font.BOLD,15));
        rightView.addXY(sampleImgLbl,1,1);
        rightView.addXY(sampleView,1,3,"f,f");
        mainView.addXY(rightView, 4, 1, "f,f");
        addXY(mainView, 1, 1);
    }

    @Override
    public void goNext() {
        setSourceFeaturePoints();
        pc.navigateTo(new P35_SrcFace(app));
    }

    //Saves the feature points in the source image
    public void setSourceFeaturePoints() {
        Point[] featurePoints = fp.getFeaturePoints();//feature Points as they are drawn, scaled instance
        Point[] tempFeaturePoints = new Point[featurePoints.length];
        for (int i = 0; i < featurePoints.length; i++) {
            tempFeaturePoints[i] = fp.toActualImagePoint(featurePoints[i]);//map point to original image size
        }
        frs.setSrcFP(tempFeaturePoints);
    }
}
