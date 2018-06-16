package org.xiph.speex.spi;

import javax.sound.sampled.AudioFileFormat.Type;

public class SpeexFileFormatType extends Type {

   public static final Type SPEEX = new SpeexFileFormatType("SPEEX", "spx");


   public SpeexFileFormatType(String var1, String var2) {
      super(var1, var2);
   }

}
