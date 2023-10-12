package org.manso;

import org.opencv.core.*;
import org.opencv.calib3d.Calib3d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.highgui.HighGui;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        int CHECKERBOARD_COLS = 8;
        int CHECKERBOARD_ROWS = 6;
        Size boardSize = new Size(CHECKERBOARD_COLS, CHECKERBOARD_ROWS);

        // modify epsillon maybe?
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 12, 0.5);

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

        MatOfPoint2f corners = new MatOfPoint2f();

        Mat image = new Mat();
        int currentFrame = 0;
        while (true) {
            currentFrame +=1;
            //System.out.println(currentFrame);
            capture.read(image);

            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

            boolean found = Calib3d.findChessboardCorners(gray, boardSize, corners);

            if (found) {
                Mat corners2 = new Mat();
                Imgproc.cornerSubPix(gray, corners, new Size(11, 11), new Size(-1, -1), criteria);
                corners2.release();
                corners2 = corners.clone();
                imagePoints.add(corners2);
                objectPoints.add(objectP3D);
                Calib3d.drawChessboardCorners(image, boardSize, corners, found);
            }

            // Draw a square in the middle of the video feed
//            int squareSize = 50;
//            int centerX = image.width() / 2;
//            int centerY = image.height() / 2;
//            Imgproc.rectangle(image, new Point(centerX - squareSize / 2, centerY - squareSize / 2),
//                    new Point(centerX + squareSize / 2, centerY + squareSize / 2), new Scalar(0, 255, 0),2);

            HighGui.imshow("Camera Calibration", image);
            if (HighGui.waitKey(1) == 'q') {
                break;
            }

        }

        capture.release();
        HighGui.destroyAllWindows();

        Size imageSize = new Size(image.width(), image.height());

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

