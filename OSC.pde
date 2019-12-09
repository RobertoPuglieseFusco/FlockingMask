
import oscP5.*;
import netP5.*;


OscP5 oscP5;
NetAddress myRemoteLocation;

float low, mid, high;

void setupOSC() {
 
  /* start oscP5, listening for incoming messages at port 12000 */
  oscP5 = new OscP5(this,12000);
  

 // myRemoteLocation = new NetAddress("127.0.0.1",12000);
}




void oscEvent(OscMessage theOscMessage) {
  /* check if theOscMessage has the address pattern we are looking for. */
    if(theOscMessage.checkAddrPattern("/Low")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      low = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  }
  if(theOscMessage.checkAddrPattern("/Mid")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      mid = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  } 
  if(theOscMessage.checkAddrPattern("/High")==true) {
    /* check if the typetag is the right one. */   
      /* parse theOscMessage and extract the values from the osc message arguments. */
      high = theOscMessage.get(0).floatValue(); 
      //println("received mid value " + mid);
      return;   
  } 
 // println("### received an osc message. with address pattern "+theOscMessage.addrPattern());
}
