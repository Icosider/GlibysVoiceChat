package org.xiph.speex.spi;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.AudioFileWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SpeexAudioFileWriter extends AudioFileWriter {

   public static final Type[] NO_FORMAT = new Type[0];
   public static final Type[] SPEEX_FORMAT = new Type[]{SpeexFileFormatType.SPEEX};


   public Type[] getAudioFileTypes() {
      return SPEEX_FORMAT;
   }

   public Type[] getAudioFileTypes(AudioInputStream var1) {
      return var1.getFormat().getEncoding() instanceof SpeexEncoding?SPEEX_FORMAT:NO_FORMAT;
   }

   public int write(AudioInputStream var1, Type var2, OutputStream var3) throws IOException {
      Type[] var4 = this.getAudioFileTypes(var1);
      if(var4 != null && var4.length > 0) {
         return this.write(var1, var3);
      } else {
         throw new IllegalArgumentException("cannot write given file type");
      }
   }

   public int write(AudioInputStream var1, Type var2, File var3) throws IOException {
      Type[] var4 = this.getAudioFileTypes(var1);
      if(var4 != null && var4.length > 0) {
         FileOutputStream var5 = new FileOutputStream(var3);
         return this.write(var1, var5);
      } else {
         throw new IllegalArgumentException("cannot write given file type");
      }
   }

   private int write(AudioInputStream var1, OutputStream var2) throws IOException {
      byte[] var3 = new byte[2048];

      int var4;
      int var5;
      for(var4 = 0; (var5 = var1.read(var3, 0, 2048)) > 0; var4 += var5) {
         var2.write(var3, 0, var5);
      }

      var2.flush();
      var2.close();
      return var4;
   }

}
