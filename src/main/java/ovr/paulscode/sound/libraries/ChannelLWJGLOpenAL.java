package ovr.paulscode.sound.libraries;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import paulscode.sound.Channel;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Objects;

public class ChannelLWJGLOpenAL extends Channel {

   public IntBuffer ALSource;
   public int ALformat;
   public int sampleRate;
   public float millisPreviouslyPlayed = 0.0F;


   public ChannelLWJGLOpenAL(int type, IntBuffer src) {
      super(type);
      this.libraryType = LibraryLWJGLOpenAL.class;
      this.ALSource = src;
   }

   public boolean attachBuffer(IntBuffer buf) {
      if(this.errorCheck(this.channelType != 0, "Sound buffers may only be attached to normal sources.")) {
         return false;
      } else {
         AL10.alSourcei(this.ALSource.get(0), 4105, buf.get(0));
         if(this.attachedSource != null && this.attachedSource.soundBuffer != null && this.attachedSource.soundBuffer.audioFormat != null) {
            this.setAudioFormat(this.attachedSource.soundBuffer.audioFormat);
         }

         return this.checkALError();
      }
   }

   public int buffersProcessed() {
      if(this.channelType != 1) {
         return 0;
      } else {
         int processed = AL10.alGetSourcei(this.ALSource.get(0), 4118);
         return this.checkALError()?0:processed;
      }
   }

   private boolean checkALError() {
      switch(AL10.alGetError()) {
      case 0:
         return false;
      case '\ua001':
         this.errorMessage("Invalid name parameter.");
         return true;
      case '\ua002':
         this.errorMessage("Invalid parameter.");
         return true;
      case '\ua003':
         return false;
      case '\ua004':
         this.errorMessage("Illegal call.");
         return true;
      case '\ua005':
         this.errorMessage("Unable to allocate memory.");
         return true;
      default:
         this.errorMessage("An unrecognized error occurred.");
         return true;
      }
   }

   public void cleanup() {
      if(this.ALSource != null) {
         try {
            AL10.alSourceStop(this.ALSource);
            AL10.alGetError();
         } catch (Exception var3) {
            ;
         }

         try {
            AL10.alDeleteSources(this.ALSource);
            AL10.alGetError();
         } catch (Exception var2) {
            ;
         }

         this.ALSource.clear();
      }

      this.ALSource = null;
      super.cleanup();
   }

   public void close() {
      try {
         AL10.alSourceStop(this.ALSource.get(0));
         AL10.alGetError();
      } catch (Exception var2) {
         ;
      }

      if(this.channelType == 1) {
         this.flush();
      }

   }

   public int feedRawAudioData(byte[] buffer) {
      if(this.errorCheck(this.channelType != 1, "Raw audio data can only be fed to streaming sources.")) {
         return -1;
      } else {
         ByteBuffer byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer(buffer.length).put(buffer).flip();
         int processed = AL10.alGetSourcei(this.ALSource.get(0), 4118);
         IntBuffer intBuffer;
         if(processed > 0) {
            intBuffer = BufferUtils.createIntBuffer(processed);
            AL10.alGenBuffers(intBuffer);
            if(this.errorCheck(this.checkALError(), "Error clearing stream buffers in method \'feedRawAudioData\'")) {
               return -1;
            }

            AL10.alSourceUnqueueBuffers(this.ALSource.get(0), intBuffer);
            if(this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method \'feedRawAudioData\'")) {
               return -1;
            }

            intBuffer.rewind();

            for(; intBuffer.hasRemaining(); this.checkALError()) {
               int i = intBuffer.get();
               if(AL10.alIsBuffer(i)) {
                  this.millisPreviouslyPlayed += this.millisInBuffer(i);
               }
            }

            AL10.alDeleteBuffers(intBuffer);
            this.checkALError();
         }

         intBuffer = BufferUtils.createIntBuffer(1);
         AL10.alGenBuffers(intBuffer);
         if(this.errorCheck(this.checkALError(), "Error generating stream buffers in method \'preLoadBuffers\'")) {
            return -1;
         } else {
            AL10.alBufferData(intBuffer.get(0), this.ALformat, byteBuffer, this.sampleRate);
            if(this.checkALError()) {
               return -1;
            } else {
               AL10.alSourceQueueBuffers(this.ALSource.get(0), intBuffer);
               if(this.checkALError()) {
                  return -1;
               } else {
                  if(this.attachedSource != null && this.attachedSource.channel == this && this.attachedSource.active() && !this.playing()) {
                     AL10.alSourcePlay(this.ALSource.get(0));
                     this.checkALError();
                  }

                  return processed;
               }
            }
         }
      }
   }

   public void flush() {
      int queued = AL10.alGetSourcei(this.ALSource.get(0), 4117);

      for(IntBuffer intBuffer = BufferUtils.createIntBuffer(1); queued > 0; --queued) {
         try {
            AL10.alSourceUnqueueBuffers(this.ALSource.get(0), intBuffer);
         } catch (Exception var4) {
            return;
         }

         if(this.checkALError()) {
            return;
         }
      }

      this.millisPreviouslyPlayed = 0.0F;
   }

   public float millisecondsPlayed() {
      float offset = (float)AL10.alGetSourcei(this.ALSource.get(0), 4134);
      float bytesPerFrame = 1.0F;
      switch(this.ALformat) {
      case 4352:
         bytesPerFrame = 1.0F;
         break;
      case 4353:
         bytesPerFrame = 2.0F;
         break;
      case 4354:
         bytesPerFrame = 2.0F;
         break;
      case 4355:
         bytesPerFrame = 4.0F;
      }

      offset = offset / bytesPerFrame / (float)this.sampleRate * 1000.0F;
      if(this.channelType == 1) {
         offset += this.millisPreviouslyPlayed;
      }

      return offset;
   }

   public float millisInBuffer(int alBufferi) {
      return (float)AL10.alGetBufferi(alBufferi, 8196) / (float)AL10.alGetBufferi(alBufferi, 8195) / ((float)AL10.alGetBufferi(alBufferi, 8194) / 8.0F) / (float)this.sampleRate * 1000.0F;
   }

   public void pause() {
      AL10.alSourcePause(this.ALSource.get(0));
      this.checkALError();
   }

   public void play() {
      AL10.alSourcePlay(this.ALSource.get(0));
      this.checkALError();
   }

   public boolean playing() {
      int state = AL10.alGetSourcei(this.ALSource.get(0), 4112);
      return !this.checkALError() && state == 4114;
   }

   public boolean preLoadBuffers(LinkedList bufferList) {
      if(this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
         return false;
      } else if(this.errorCheck(bufferList == null, "Buffer List null in method \'preLoadBuffers\'")) {
         return false;
      } else {
         boolean playing = this.playing();
         if(playing) {
            AL10.alSourceStop(this.ALSource.get(0));
            this.checkALError();
         }

         int processed = AL10.alGetSourcei(this.ALSource.get(0), 4118);
         IntBuffer streamBuffers;
         if(processed > 0) {
            streamBuffers = BufferUtils.createIntBuffer(processed);
            AL10.alGenBuffers(streamBuffers);
            if(this.errorCheck(this.checkALError(), "Error clearing stream buffers in method \'preLoadBuffers\'")) {
               return false;
            }

            AL10.alSourceUnqueueBuffers(this.ALSource.get(0), streamBuffers);
            if(this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method \'preLoadBuffers\'")) {
               return false;
            }
         }

         if(playing) {
            AL10.alSourcePlay(this.ALSource.get(0));
            this.checkALError();
         }

         streamBuffers = BufferUtils.createIntBuffer(Objects.requireNonNull(bufferList).size());
         AL10.alGenBuffers(streamBuffers);
         if(this.errorCheck(this.checkALError(), "Error generating stream buffers in method \'preLoadBuffers\'")) {
            return false;
         } else {
            ByteBuffer byteBuffer = null;

            for(int e = 0; e < bufferList.size(); ++e) {
               byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer(((byte[])bufferList.get(e)).length).put((byte[])bufferList.get(e)).flip();

               try {
                  AL10.alBufferData(streamBuffers.get(e), this.ALformat, byteBuffer, this.sampleRate);
               } catch (Exception var9) {
                  this.errorMessage("Error creating buffers in method \'preLoadBuffers\'");
                  this.printStackTrace(var9);
                  return false;
               }

               if(this.errorCheck(this.checkALError(), "Error creating buffers in method \'preLoadBuffers\'")) {
                  return false;
               }
            }

            try {
               AL10.alSourceQueueBuffers(this.ALSource.get(0), streamBuffers);
            } catch (Exception var8) {
               this.errorMessage("Error queuing buffers in method \'preLoadBuffers\'");
               this.printStackTrace(var8);
               return false;
            }

            if(this.errorCheck(this.checkALError(), "Error queuing buffers in method \'preLoadBuffers\'")) {
               return false;
            } else {
               AL10.alSourcePlay(this.ALSource.get(0));
               return !this.errorCheck(this.checkALError(), "Error playing source in method \'preLoadBuffers\'");
            }
         }
      }
   }

   public boolean queueBuffer(byte[] buffer) {
      if(this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
         return false;
      } else {
         ByteBuffer byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer(buffer.length).put(buffer).flip();
         IntBuffer intBuffer = BufferUtils.createIntBuffer(1);
         AL10.alSourceUnqueueBuffers(this.ALSource.get(0), intBuffer);
         if(this.checkALError()) {
            return false;
         } else {
            if(AL10.alIsBuffer(intBuffer.get(0))) {
               this.millisPreviouslyPlayed += this.millisInBuffer(intBuffer.get(0));
            }

            this.checkALError();
            AL10.alBufferData(intBuffer.get(0), this.ALformat, byteBuffer, this.sampleRate);
            if(this.checkALError()) {
               return false;
            } else {
               AL10.alSourceQueueBuffers(this.ALSource.get(0), intBuffer);
               return !this.checkALError();
            }
         }
      }
   }

   public void rewind() {
      if(this.channelType != 1) {
         AL10.alSourceRewind(this.ALSource.get(0));
         if(!this.checkALError()) {
            this.millisPreviouslyPlayed = 0.0F;
         }

      }
   }

   public void setAudioFormat(AudioFormat audioFormat) {
      short soundFormat1;
      if(audioFormat.getChannels() == 1) {
         if(audioFormat.getSampleSizeInBits() == 8) {
            soundFormat1 = 4352;
         } else {
            if(audioFormat.getSampleSizeInBits() != 16) {
               this.errorMessage("Illegal sample size in method \'setAudioFormat\'");
               return;
            }

            soundFormat1 = 4353;
         }
      } else {
         if(audioFormat.getChannels() != 2) {
            this.errorMessage("Audio data neither mono nor stereo in method \'setAudioFormat\'");
            return;
         }

         if(audioFormat.getSampleSizeInBits() == 8) {
            soundFormat1 = 4354;
         } else {
            if(audioFormat.getSampleSizeInBits() != 16) {
               this.errorMessage("Illegal sample size in method \'setAudioFormat\'");
               return;
            }

            soundFormat1 = 4355;
         }
      }

      this.ALformat = soundFormat1;
      this.sampleRate = (int)audioFormat.getSampleRate();
   }

   public void setFormat(int format, int rate) {
      this.ALformat = format;
      this.sampleRate = rate;
   }

   public void stop() {
      AL10.alSourceStop(this.ALSource.get(0));
      if(!this.checkALError()) {
         this.millisPreviouslyPlayed = 0.0F;
      }

   }
}
