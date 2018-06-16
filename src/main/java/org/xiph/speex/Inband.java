package org.xiph.speex;

import java.io.StreamCorruptedException;

public class Inband {

   private Stereo stereo;


   public Inband(Stereo var1) {
      this.stereo = var1;
   }

   public void speexInbandRequest(Bits var1) throws StreamCorruptedException {
      int var2 = var1.unpack(4);
      switch(var2) {
      case 0:
         var1.advance(1);
         break;
      case 1:
         var1.advance(1);
         break;
      case 2:
         var1.advance(4);
         break;
      case 3:
         var1.advance(4);
         break;
      case 4:
         var1.advance(4);
         break;
      case 5:
         var1.advance(4);
         break;
      case 6:
         var1.advance(4);
         break;
      case 7:
         var1.advance(4);
         break;
      case 8:
         var1.advance(8);
         break;
      case 9:
         this.stereo.init(var1);
         break;
      case 10:
         var1.advance(16);
         break;
      case 11:
         var1.advance(16);
         break;
      case 12:
         var1.advance(32);
         break;
      case 13:
         var1.advance(32);
         break;
      case 14:
         var1.advance(64);
         break;
      case 15:
         var1.advance(64);
      }

   }

   public void userInbandRequest(Bits var1) throws StreamCorruptedException {
      int var2 = var1.unpack(4);
      var1.advance(5 + 8 * var2);
   }
}
