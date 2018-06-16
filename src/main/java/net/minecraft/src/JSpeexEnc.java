package net.minecraft.src;

import org.xiph.speex.*;

import java.io.*;

public class JSpeexEnc
{
    private int printlevel = 1;
    private int srcFormat = 1;
    private int destFormat = 2;
    protected int mode = -1;
    private int quality = 8;
    private int complexity = 3;
    private int nframes = 1;
    private int bitrate = -1;
    private int sampleRate = -1;
    private int channels = 1;
    private float vbr_quality = -1.0F;
    private boolean vbr = false;
    private boolean vad = false;
    private boolean dtx = false;
    private String srcFile;
    private String destFile;
    
    public static void main(String[] var0) throws IOException
    {
        JSpeexEnc enc = new JSpeexEnc();
        
        if (enc.parseArgs(var0))
        {
            enc.encode();
        }
    }

    private boolean parseArgs(String[] args)
    {
        if (args.length < 2)
        {
            if (args.length == 1 && (args[0].equalsIgnoreCase("-v") || args[0].equalsIgnoreCase("--version")))
            {
                version();
                return false;
            }
            else {
                usage();
                return false;
            }
        } 
        else {
            this.srcFile = args[args.length - 2];
            this.destFile = args[args.length - 1];
            
            if (this.srcFile.toLowerCase().endsWith(".wav"))
            {
                this.srcFormat = 2;
            }
            else {
                this.srcFormat = 0;
            }

            if (this.destFile.toLowerCase().endsWith(".spx"))
            {
                this.destFormat = 1;
            }
            else if (this.destFile.toLowerCase().endsWith(".wav"))
            {
                this.destFormat = 2;
            }
            else {
                this.destFormat = 0;
            }

            int arg = 0;

            while (arg < args.length - 2)
            {
                if (!args[arg].equalsIgnoreCase("-h") && !args[arg].equalsIgnoreCase("--help"))
                {
                    if (!args[arg].equalsIgnoreCase("-v") && !args[arg].equalsIgnoreCase("--version"))
                    {
                        if (args[arg].equalsIgnoreCase("--verbose"))
                        {
                            this.printlevel = 0;
                        }
                        else if (args[arg].equalsIgnoreCase("--quiet"))
                        {
                            this.printlevel = 2;
                        }
                        else if (!args[arg].equalsIgnoreCase("-n") && !args[arg].equalsIgnoreCase("-nb") && !args[arg].equalsIgnoreCase("--narrowband"))
                        {
                            if (!args[arg].equalsIgnoreCase("-w") && !args[arg].equalsIgnoreCase("-wb") && !args[arg].equalsIgnoreCase("--wideband"))
                            {
                                if (!args[arg].equalsIgnoreCase("-u") && !args[arg].equalsIgnoreCase("-uwb") && !args[arg].equalsIgnoreCase("--ultra-wideband"))
                                {
                                    if (!args[arg].equalsIgnoreCase("-q") && !args[arg].equalsIgnoreCase("--quality"))
                                    {
                                        if (args[arg].equalsIgnoreCase("--complexity"))
                                        {
                                            try
                                            {
                                                ++arg;
                                                this.complexity = Integer.parseInt(args[arg]);
                                            }
                                            catch (NumberFormatException e)
                                            {
                                                usage();
                                                return false;
                                            }
                                        }
                                        else if (args[arg].equalsIgnoreCase("--nframes"))
                                        {
                                            try
                                            {
                                                ++arg;
                                                this.nframes = Integer.parseInt(args[arg]);
                                            }
                                            catch (NumberFormatException e)
                                            {
                                                usage();
                                                return false;
                                            }
                                        }
                                        else if (args[arg].equalsIgnoreCase("--vbr"))
                                        {
                                            this.vbr = true;
                                        }
                                        else if (args[arg].equalsIgnoreCase("--vad"))
                                        {
                                            this.vad = true;
                                        }
                                        else if (args[arg].equalsIgnoreCase("--dtx"))
                                        {
                                            this.dtx = true;
                                        }
                                        else if (args[arg].equalsIgnoreCase("--rate"))
                                        {
                                            try
                                            {
                                                ++arg;
                                                this.sampleRate = Integer.parseInt(args[arg]);
                                            }
                                            catch (NumberFormatException e)
                                            {
                                                usage();
                                                return false;
                                            }
                                        }
                                        else {
                                            if (!args[arg].equalsIgnoreCase("--stereo"))
                                            {
                                                usage();
                                                return false;
                                            }
                                            this.channels = 2;
                                        }
                                    }
                                    else {
                                        try
                                        {
                                            ++arg;
                                            this.vbr_quality = Float.parseFloat(args[arg]);
                                            this.quality = (int) this.vbr_quality;
                                        }
                                        catch (NumberFormatException e)
                                        {
                                            usage();
                                            return false;
                                        }
                                    }
                                }
                                else {
                                    this.mode = 2;
                                }
                            }
                            else {
                                this.mode = 1;
                            }
                        }
                        else {
                            this.mode = 0;
                        }
                        ++arg;
                        continue;
                    }
                    version();
                    return false;
                }
                usage();
                return false;
            }
            return true;
        }
    }

    private static void usage()
    {
        version();
        System.out.println();
        System.out.println("Usage: JSpeexEnc [options] input_file output_file");
        System.out.println("Where:");
        System.out.println("  input_file can be:");
        System.out.println("    filename.wav  a PCM wav file");
        System.out.println("    filename.*    a raw PCM file (any extension other than .wav)");
        System.out.println("  output_file can be:");
        System.out.println("    filename.spx  an Ogg Speex file");
        System.out.println("    filename.wav  a Wave Speex file (beta!!!)");
        System.out.println("    filename.*    a raw Speex file");
        System.out.println("Options: -h, --help     This help");
        System.out.println("         -v, --version  Version information");
        System.out.println("         --verbose      Print detailed information");
        System.out.println("         --quiet        Print minimal information");
        System.out.println("         -n, -nb        Consider input as Narrowband (8kHz)");
        System.out.println("         -w, -wb        Consider input as Wideband (16kHz)");
        System.out.println("         -u, -uwb       Consider input as Ultra-Wideband (32kHz)");
        System.out.println("         --quality n    Encoding quality (0-10) default 8");
        System.out.println("         --complexity n Encoding complexity (0-10) default 3");
        System.out.println("         --nframes n    Number of frames per Ogg packet, default 1");
        System.out.println("         --vbr          Enable varible bit-rate (VBR)");
        System.out.println("         --vad          Enable voice activity detection (VAD)");
        System.out.println("         --dtx          Enable file based discontinuous transmission (DTX)");
        System.out.println("         if the input file is raw PCM (not a Wave file)");
        System.out.println("         --rate n       Sampling rate for raw input");
        System.out.println("         --stereo       Consider input as stereo");
        System.out.println("More information is available from: http://jspeex.sourceforge.net/");
        System.out.println("This code is a Java port of the Speex codec: http://www.speex.org/");
    }

    public static void version()
    {
        System.out.println("Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)");
        System.out.println("using Java Speex Encoder v0.9.7 ($Revision: 1.6 $)");
        System.out.println("Copyright (C) 2002-2004 Wimba S.A.");
    }

    private void encode() throws IOException
    {
        this.encode(new File(this.srcFile), new File(this.destFile));
    }

    private void encode(File args, File arg) throws IOException
    {
        byte[] var3 = new byte[2560];
        
        if (this.printlevel <= 1)
        {
            version();
        }

        if (this.printlevel <= 0)
        {
            System.out.println();
        }

        if (this.printlevel <= 0)
        {
            System.out.println("Input File: " + args);
        }

        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(args));
        if (this.srcFormat == 2)
        {
            dataInputStream.readFully(var3, 0, 12);
            
            if (!"RIFF".equals(new String(var3, 0, 4)) && !"WAVE".equals(new String(var3, 8, 4)))
            {
                System.err.println("Not a WAVE file");
                return;
            }
            dataInputStream.readFully(var3, 0, 8);
            String text = new String(var3, 0, 4);

            int data;
            for (data = readInt(var3, 4); !text.equals("data"); data = readInt(var3, 4))
            {
                dataInputStream.readFully(var3, 0, data);
                
                if (text.equals("fmt "))
                {
                    if (readShort(var3, 0) != 1)
                    {
                        System.err.println("Not a PCM file");
                        return;
                    }

                    this.channels = readShort(var3, 2);
                    this.sampleRate = readInt(var3, 4);
                    
                    if (readShort(var3, 14) != 16)
                    {
                        System.err.println("Not a 16 bit file " + readShort(var3, 18));
                        return;
                    }

                    if (this.printlevel <= 0)
                    {
                        System.out.println("File Format: PCM wave");
                        System.out.println("Sample Rate: " + this.sampleRate);
                        System.out.println("Channels: " + this.channels);
                    }
                }
                dataInputStream.readFully(var3, 0, 8);
                text = new String(var3, 0, 4);
            }

            if (this.printlevel <= 0)
            {
                System.out.println("Data size: " + data);
            }
        }
        else {
            if (this.sampleRate < 0)
            {
                switch (this.mode)
                {
                    case 0:
                        this.sampleRate = 8000;
                        break;
                    case 1:
                        this.sampleRate = 16000;
                        break;
                    case 2:
                        this.sampleRate = 32000;
                        break;
                    default:
                        this.sampleRate = 8000;
                }
            }

            if (this.printlevel <= 0)
            {
                System.out.println("File format: Raw audio");
                System.out.println("Sample rate: " + this.sampleRate);
                System.out.println("Channels: " + this.channels);
                System.out.println("Data size: " + args.length());
            }
        }

        if (this.mode < 0)
        {
            if (this.sampleRate < 100)
            {
                this.sampleRate *= 1000;
            }

            if (this.sampleRate < 12000)
            {
                this.mode = 0;
            }
            else if (this.sampleRate < 24000)
            {
                this.mode = 1;
            }
            else {
                this.mode = 2;
            }
        }

        SpeexEncoder speexEncoder = new SpeexEncoder();
        speexEncoder.init(this.mode, this.quality, this.sampleRate, this.channels);
        
        if (this.complexity > 0)
        {
            speexEncoder.getEncoder().setComplexity(this.complexity);
        }

        if (this.bitrate > 0)
        {
            speexEncoder.getEncoder().setBitRate(this.bitrate);
        }

        if (this.vbr)
        {
            speexEncoder.getEncoder().setVbr(this.vbr);
            
            if (this.vbr_quality > 0.0F)
            {
                speexEncoder.getEncoder().setVbrQuality(this.vbr_quality);
            }
        }

        if (this.vad)
        {
            speexEncoder.getEncoder().setVad(this.vad);
        }

        if (this.dtx)
        {
            speexEncoder.getEncoder().setDtx(this.dtx);
        }

        if (this.printlevel <= 0)
        {
            System.out.println("Output File: " + arg);
            System.out.println("File format: Ogg Speex");
            System.out.println("Encoder mode: " + (this.mode == 0?"Narrowband":(this.mode == 1?"Wideband":"UltraWideband")));
            System.out.println("Quality: " + (this.vbr?this.vbr_quality:(float)this.quality));
            System.out.println("Complexity: " + this.complexity);
            System.out.println("Frames per packet: " + this.nframes);
            System.out.println("Varible bitrate: " + this.vbr);
            System.out.println("Voice activity detection: " + this.vad);
            System.out.println("Discontinouous Transmission: " + this.dtx);
        }

        Object writer;
        
        if (this.destFormat == 1)
        {
            writer = new OggSpeexWriter(this.mode, this.sampleRate, this.channels, this.nframes, this.vbr);
        }
        else if (this.destFormat == 2)
        {
            this.nframes = PcmWaveWriter.WAVE_FRAME_SIZES[this.mode - 1][this.channels - 1][this.quality];
            writer = new PcmWaveWriter(this.mode, this.quality, this.sampleRate, this.channels, this.nframes, this.vbr);
        }
        else {
            writer = new RawWriter();
        }

        ((AudioFileWriter)writer).open(arg);
        ((AudioFileWriter)writer).writeHeader("Encoded with: Java Speex Command Line Encoder v0.9.7 ($Revision: 1.5 $)");
        int args3 = 2 * this.channels * speexEncoder.getFrameSize();

        try
        {
            while (true)
            {
                dataInputStream.readFully(var3, 0, this.nframes * args3);

                int frame;
                
                for (frame = 0; frame < this.nframes; ++frame)
                {
                    speexEncoder.processData(var3, frame * args3, args3);
                }

                frame = speexEncoder.getProcessedData(var3, 0);
                
                if (frame > 0)
                {
                    ((AudioFileWriter)writer).writePacket(var3, 0, frame);
                }
            }
        }
        catch (EOFException e)
        {
            ((AudioFileWriter)writer).close();
            dataInputStream.close();
        }
    }

    protected static int readInt(byte[] var0, int args)
    {
        return var0[args] & 255 | (var0[args + 1] & 255) << 8 | (var0[args + 2] & 255) << 16 | var0[args + 3] << 24;
    }

    private static int readShort(byte[] var0, int args)
    {
        return var0[args] & 255 | var0[args + 1] << 8;
    }
}