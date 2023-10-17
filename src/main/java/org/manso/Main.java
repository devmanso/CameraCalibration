package org.manso;

import org.opencv.core.*;
import org.opencv.calib3d.Calib3d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.Videoio;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static volatile boolean stopButtonClicked = false;
    private static volatile boolean exitButtonClicked = false;


    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    public static void main(String[] args) {

        int CHECKERBOARD_COLS = 4;
        int CHECKERBOARD_ROWS = 6;
        Size boardSize = new Size(CHECKERBOARD_COLS, CHECKERBOARD_ROWS);

        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 30, 0.3);

        List<Mat> objectPoints = new ArrayList<>();
        List<Mat> imagePoints = new ArrayList<>();

        MatOfPoint3f objectP3D = new MatOfPoint3f();
        objectP3D.create(1, CHECKERBOARD_COLS * CHECKERBOARD_ROWS, CvType.CV_32FC3);
        //objectP3D.create(CHECKERBOARD_ROWS, CHECKERBOARD_COLS, CvType.CV_32FC3);
        float[] objBuff = new float[3 * CHECKERBOARD_COLS * CHECKERBOARD_ROWS];
        for (int i = 0; i < CHECKERBOARD_ROWS; i++) {
            for (int j = 0; j < CHECKERBOARD_COLS; j++) {
                objBuff[(i * CHECKERBOARD_COLS + j) * 3] = j;
                objBuff[(i * CHECKERBOARD_COLS + j) * 3 + 1] = i;
                objBuff[(i * CHECKERBOARD_COLS + j) * 3 + 2] = 0;
            }
        }
        objectP3D.put(0, 0, objBuff);

        VideoCapture capture = new VideoCapture(0);
        double frameWidth;
        double frameHeight;

        //capture.set(3, 1280);
        //capture.set(4, 720);

        MatOfPoint2f corners = new MatOfPoint2f();

        Mat image = new Mat();
        int currentFrame = 0;

        // Button layout

        JButton stopButton = new JButton("Stop Calibration");
        stopButton.setBounds(10, HighGui.WINDOW_NORMAL - 40, 80, 30);
        stopButton.addActionListener( e -> stopButtonClicked = true);

        JButton exitButton = new JButton("Quit Program");

        JFrame options = HighGui.createJFrame("Camera Calibration", HighGui.WINDOW_NORMAL);
        options.setLayout(new FlowLayout());
        Container optionsPane = options.getContentPane();
        optionsPane.add(stopButton, BorderLayout.SOUTH);
        options.setSize(200, 200);
        options.setVisible(true);

        while (true) {
            //frameWidth = capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
            //frameHeight = capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
            currentFrame +=1;
            System.out.println(currentFrame);
            capture.read(image);
            //if(currentFrame == 50) {break;}
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            //Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
            boolean found = Calib3d.findChessboardCorners(gray, boardSize, corners);

            if (found) {
                System.out.println("checkerboard found");
                Mat corners2 = new Mat();
                Imgproc.cornerSubPix(gray, corners, new Size(11, 11), new Size(-1, -1), criteria);
                corners2.release();
                corners2 = corners.clone();
                imagePoints.add(corners2);
                objectPoints.add(objectP3D);
                Calib3d.drawChessboardCorners(image, boardSize, corners, found);

            }

            HighGui.imshow("Camera Calibration", image);
            if (HighGui.waitKey(1) == 27) {
                break;
            }

        }

        capture.release();
        HighGui.destroyAllWindows();

        Size imageSize = new Size(image.width(), image.height());

        //System.out.println("capture resolution");
        //System.out.println(frameWidth + " x " + frameHeight);

        System.out.println("Image Width "+ image.width());
        System.out.println("Image Height "+ image.height());

        Mat cameraMatrix = new Mat(3, 3, CvType.CV_64F);
        MatOfDouble distortionCoefficients = new MatOfDouble();
        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();

        Calib3d.calibrateCamera(objectPoints, imagePoints, imageSize, cameraMatrix, distortionCoefficients, rvecs, tvecs);

        System.out.println("Camera Matrix:");
        System.out.println(cameraMatrix.dump());

        System.out.println("\nDistortion Coefficients:");
        System.out.println(distortionCoefficients.dump());

        System.out.println("\nRotation Vectors:");
        for (Mat rvec : rvecs) {
            System.out.println(rvec.dump());
        }

        System.out.println("\nTranslation Vectors:");
        for (Mat tvec : tvecs) {
            System.out.println(tvec.dump());
        }
    }

}

