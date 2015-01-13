package com.team3019.VisionCode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/*
 * @author: Elijah Kaufman and his team
 * 
 * Note From Author: If you use this code or the algorithm
 * please give credit to Elijah Kaufman and FRC team 3019, Firebird Robotics
 */


public class YellowToteTracker {

	public static void main(String[] args) {
		//required for openCV to work -call before any functions of oCV are used
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		//main loop of the program
		while(true){
			processImage();
		}
	}
	//opens a new connection to the Axis camera and opens a new snapshot "instance"
	public static void processImage(){
		try {
			//the url of the camera snapshot to save ##.## with your team number
			//Url url = new URL("http://10.##.##.11/axis-cgi/jpg/image.cgi");
			URL url = new URL("http://10.30.19.11/axis-cgi/jpg/image.cgi");
			URLConnection uc = url.openConnection();
			//saves the image to a file
			BufferedImage img = ImageIO.read((uc.getInputStream()));
			ImageIO.write(img, "jpg", new File("frame.jpg"));
			//time for the OpenCV fun!
			Mat frame = new Mat();
			frame = Highgui.imread("frame.jpg");
			ArrayList<MatOfPoint> contours = new ArrayList<>();
			//applies a threshhold in the form of BlueGreenRed
			Core.inRange(frame, new Scalar(0,35,0), new Scalar(15,57,77), frame);
			//find the cluster of particles
			Imgproc.findContours(frame, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			//iterating through the list of contours and removing the ones with an "area" less then 100
			for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext();) {
				MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
				if(matOfPoint.width() * matOfPoint.height() < 100){
					iterator.remove();
				}
			}
			//if theres only one contour dont do the silly math of the bounding rectangles
			if(contours.size() == 1){
					Rect rec1 = Imgproc.boundingRect(contours.get(0));
					Core.rectangle(frame, rec1.tl(), rec1.br(), new Scalar(255,255,0));
					String string = "TargetFound at X:" + (rec1.tl().x + rec1.br().x) / 2 + "Y:" + (rec1.tl().y + rec1.br().y) / 2;
					Core.putText(frame, string, new Point(200,frame.size().height-10), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255,255,0));
			}
			//there is more then one and we have to get the extremes of the ranges
			//for the top left and bottom corners of the rectangle
			else{
				ArrayList<Rect> recList = new ArrayList<Rect>();
				for(MatOfPoint mOP : contours){
					recList.add(Imgproc.boundingRect(mOP));
				}
				Point tl = recList.get(0).tl();
				Point br = recList.get(0).br();
				for(Rect rec : recList){
					if(tl.x > rec.tl().x){
						tl.x = rec.tl().x;
					}
					if(tl.y > rec.tl().y){
						tl.y = rec.tl().y;
					}
					if(br.x < rec.br().x){
						br.x = rec.br().x;
					}
					if(br.y < rec.br().y){
						br.y = rec.br().y;
					}
				}
				
				Rect bb = new Rect(tl, br);
				//outputting the data in a visible form
				Core.rectangle(frame, tl,br, new Scalar(255,255,0));
				String string = "TargetFound at X:" + (bb.tl().x + bb.br().x) / 2 + "Y:" + (bb.tl().y + bb.br().y) / 2;
				Core.putText(frame, string, new Point(200,frame.size().height-10), Core.FONT_HERSHEY_PLAIN, 1, new Scalar(255,255,0));
			}//view this file to see the vision tracking
			//windows will update the image after every save
			Highgui.imwrite("rectangle.png", frame);
		//mostly for debugging but errors happen
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}