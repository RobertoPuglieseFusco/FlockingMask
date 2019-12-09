// Daniel Shiffman
// Tracking the average location beyond a given depth threshold
// Thanks to Dan O'Sullivan

// https://github.com/shiffman/OpenKinect-for-Processing
// http://shiffman.net/p5/kinect/

import org.openkinect.freenect.*;
import org.openkinect.processing.*;

// The kinect stuff is happening in another class
KinectTracker tracker;
Kinect kinect;

PVector v2;
PVector com;

void drawKinect() {

  // Run the tracking analysis
  tracker.track();
  // Show the image
  if (toggleGui) {
    tracker.display();
  }

  // Let's draw the raw location
  PVector v1 = tracker.getPos();
  fill(50, 100, 250, 200);
  noStroke();
  //ellipse(v1.x, v1.y, 20, 20);

  // Let's draw the "lerped" location
  v2 = tracker.getLerpedPos();
  fill(255, 0, 50, 200);
  noStroke();
  if (toggleGui) {
    ellipse(v2.x, v2.y, 10, 10);
  }
  if (debug) {
    println("v2 " + v2);
  }

  if (XY_XZ_YZ == 1) { 
    com.set(map(v2.x, 0, 640, 0, width), map(v2.y, 0, 480, 0, height), 0);
    if (debug) {
      println("tracking: XY");
    }
  } else if (XY_XZ_YZ == 2) {
    com.set(map(v2.x, 0, 640, 0, width), map(v2.z, minThreshold, maxThreshold, 0, height), 0);
    if (debug) {
      println("tracking: XZ");
    }
  } else if (XY_XZ_YZ == 3) {
    com.set( map(v2.y, 0, 480, 0, height), map(v2.z, minThreshold, maxThreshold, 0, height), 0);
    if (debug) {
      println("tracking: YZ");
    }
  }

  fill(100, 250, 50, 200);
  noStroke();
  if (toggleGui) {
    ellipse(com.x, com.y, 20, 20);

    int t = tracker.getmaxThreshold();
    fill(255);
    text("framerate: " + int(frameRate), 10, 500);
  }
  if (debug) {
    println("com " + com);
  }
}
