package net.minecraft.src;

import org.xiph.speex.*;

import java.io.*;
import java.util.Objects;
import java.util.Random;

public class JSpeexDec
{
    private int printlevel = 1;
    private int srcFormat = 1;
    private int destFormat = 2;
    private static Random random = new Random();
    private SpeexDecoder speexDecoder;
    private boolean enhanced = true;
    private int mode = 0;
    private int quality = 8;
    private int nframes = 1;
    private int sampleRate = -1;
    private boolean vbr = false;
    private int channels = 1;
    private int loss = 0;
    private String srcFile;
    private String destFile;
    
    public static void main(String[] args) throws IOException
    {
        JSpeexDec dec = new JSpeexDec();
        
        if(dec.parseArgs(args))
        {
            dec.decode();
        }
    }

    private boolean parseArgs(String[] args) {
        if (args.length < 2)
        {
            if (args.length == 1 && (args[0].equals("-v") || args[0].equals("--version"))) {
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
            
            if (this.srcFile.toLowerCase().endsWith(".spx"))
            {
                this.srcFormat = 1;
            }
            else if (this.srcFile.toLowerCase().endsWith(".wav")) {
                this.srcFormat = 2;
            }
            else {
                this.srcFormat = 0;
            }

            if (this.destFile.toLowerCase().endsWith(".wav"))
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
                        else if (args[arg].equalsIgnoreCase("--enh"))
                        {
                            this.enhanced = true;
                        }
                        else if (args[arg].equalsIgnoreCase("--no-enh"))
                        {
                            this.enhanced = false;
                        }
                        else if (args[arg].equalsIgnoreCase("--packet-loss"))
                        {
                            try
                            {
                                ++arg;
                                this.loss = Integer.parseInt(args[arg]);
                            }
                            catch (NumberFormatException e)
                            {
                                usage();
                                return false;
                            }
                        }
                        else if (!args[arg].equalsIgnoreCase("-n") && !args[arg].equalsIgnoreCase("-nb") && !args[arg].equalsIgnoreCase("--narrowband"))
                        {
                            if (!args[arg].equalsIgnoreCase("-w") && !args[arg].equalsIgnoreCase("-wb") && !args[arg].equalsIgnoreCase("--wideband"))
                            {
                                if (!args[arg].equalsIgnoreCase("-u") && !args[arg].equalsIgnoreCase("-uwb") && !args[arg].equalsIgnoreCase("--ultra-wideband"))
                                {
                                    if (!args[arg].equalsIgnoreCase("-q") && !args[arg].equalsIgnoreCase("--quality"))
                                    {
                                        if (args[arg].equalsIgnoreCase("--nframes"))
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
                                            float vbr_quality = Float.parseFloat(args[arg]);
                                            this.quality = (int) vbr_quality;
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
            return true;
        }
    }

    private static void usage()
    {
        version();
        System.out.println("Usage: JSpeexDec [options] input_file output_file");
        System.out.println("Where:");
        System.out.println("  input_file can be:");
        System.out.println("    filename.spx  an Ogg Speex file");
        System.out.println("    filename.wav  a Wave Speex file (beta!!!)");
        System.out.println("    filename.*    a raw Speex file");
        System.out.println("  output_file can be:");
        System.out.println("    filename.wav  a PCM wav file");
        System.out.println("    filename.*    a raw PCM file (any extension other than .wav)");
        System.out.println("Options: -h, --help     This help");
        System.out.println("         -v, --version    Version information");
        System.out.println("         --verbose        Print detailed information");
        System.out.println("         --quiet          Print minimal information");
        System.out.println("         --enh            Enable perceptual enhancement (default)");
        System.out.println("         --no-enh         Disable perceptual enhancement");
        System.out.println("         --packet-loss n  Simulate n % random packet loss");
        System.out.println("         if the input file is raw Speex (not Ogg Speex)");
        System.out.println("         -n, -nb          Narrowband (8kHz)");
        System.out.println("         -w, -wb          Wideband (16kHz)");
        System.out.println("         -u, -uwb         Ultra-Wideband (32kHz)");
        System.out.println("         --quality n      Encoding quality (0-10) default 8");
        System.out.println("         --nframes n      Number of frames per Ogg packet, default 1");
        System.out.println("         --vbr            Enable varible bit-rate (VBR)");
        System.out.println("         --stereo         Consider input as stereo");
        System.out.println("More information is available from: http://jspeex.sourceforge.net/");
        System.out.println("This code is a Java port of the Speex codec: http://www.speex.org/");
    }

    public static void version()
    {
        System.out.println("Java Speex Command Line Decoder v0.9.7 ($Revision: 1.4 $)");
        System.out.println("using Java Speex Decoder v0.9.7 ($Revision: 1.4 $)");
        System.out.println("Copyright (C) 2002-2004 Wimba S.A.");
    }

    private void decode() throws IOException
    {
        this.decode(new File(this.srcFile), new File(this.destFile));
    }

    private void decode(File args, File arg) throws IOException
    {
        byte[] var3 = new byte[2048];
        byte[] var4 = new byte[65536];
        byte[] var5 = new byte[176400];
        int args7 = 0;
        int args9 = 0;
        
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

        this.speexDecoder = new SpeexDecoder();
        DataInputStream arg0 = new DataInputStream(new FileInputStream(args));
        Object arg1 = null;

        try
        {
            int arg2;
            int arg3;
            
            do {
                int arg4;
                int arg9;
                
                while (this.srcFormat != 1)
                {
                    if (args9 == 0)
                    {
                        if (this.srcFormat == 2)
                        {
                            arg0.readFully(var3, 0, 12);
                            
                            if (!"RIFF".equals(new String(var3, 0, 4)) && !"WAVE".equals(new String(var3, 8, 4)))
                            {
                                System.err.println("Not a WAVE file");
                                return;
                            }

                            arg0.readFully(var3, 0, 8);
                            String var30 = new String(var3, 0, 4);

                            int data;
                            for (data = readInt(var3, 4); !var30.equals("data"); data = readInt(var3, 4))
                            {
                                arg0.readFully(var3, 0, data);
                                
                                if (var30.equals("fmt "))
                                {
                                    if (readShort(var3, 0) != -24311)
                                    {
                                        System.err.println("Not a Wave Speex file");
                                        return;
                                    }

                                    this.channels = readShort(var3, 2);
                                    this.sampleRate = readInt(var3, 4);
                                    args7 = readShort(var3, 12);
                                    
                                    if (readShort(var3, 16) < 82)
                                    {
                                        System.err.println("Possibly corrupt Speex Wave file.");
                                        return;
                                    }

                                    this.readSpeexHeader(var3, 20, 80);

                                    if (this.printlevel <= 0)
                                    {
                                        System.out.println("File Format: Wave Speex");
                                        System.out.println("Sample Rate: " + this.sampleRate);
                                        System.out.println("Channels: " + this.channels);
                                        System.out.println("Encoder mode: " + (this.mode == 0 ? "Narrowband" : (this.mode == 1 ? "Wideband" : "UltraWideband")));
                                        System.out.println("Frames per packet: " + this.nframes);
                                    }
                                }

                                arg0.readFully(var3, 0, 8);
                                var30 = new String(var3, 0, 4);
                            }

                            if (this.printlevel <= 0)
                            {
                                System.out.println("Data size: " + data);
                            }
                        }
                        else {
                            if (this.printlevel <= 0)
                            {
                                System.out.println("File Format: Raw Speex");
                                System.out.println("Sample Rate: " + this.sampleRate);
                                System.out.println("Channels: " + this.channels);
                                System.out.println("Encoder mode: " + (this.mode == 0?"Narrowband":(this.mode == 1?"Wideband":"UltraWideband")));
                                System.out.println("Frames per packet: " + this.nframes);
                            }

                            this.speexDecoder.init(this.mode, this.sampleRate, this.channels, this.enhanced);
                            
                            if (!this.vbr)
                            {
                                switch (this.mode)
                                {
                                    case 0:
                                        args7 = NbCodec.NB_FRAME_SIZE[NbEncoder.NB_QUALITY_MAP[this.quality]];
                                        break;
                                    case 1:
                                        args7 = NbCodec.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[this.quality]];
                                        args7 += SbCodec.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[this.quality]];
                                        break;
                                    case 2:
                                        args7 = NbCodec.NB_FRAME_SIZE[SbEncoder.NB_QUALITY_MAP[this.quality]];
                                        args7 += SbCodec.SB_FRAME_SIZE[SbEncoder.WB_QUALITY_MAP[this.quality]];
                                        args7 += SbCodec.SB_FRAME_SIZE[SbEncoder.UWB_QUALITY_MAP[this.quality]];
                                        break;
                                    default:
                                        throw new IOException("Illegal mode encoundered.");
                                }

                                args7 = args7 + 7 >> 3;
                            }
                            else {
                                args7 = 0;
                            }
                        }

                        if (this.destFormat == 2)
                        {
                            arg1 = new PcmWaveWriter(this.sampleRate, this.channels);
                            
                            if (this.printlevel <= 0)
                            {
                                System.out.println();
                                System.out.println("Output File: " + arg);
                                System.out.println("File Format: PCM Wave");
                                System.out.println("Perceptual Enhancement: " + this.enhanced);
                            }
                        } 
                        else {
                            arg1 = new RawWriter();
                            
                            if (this.printlevel <= 0)
                            {
                                System.out.println();
                                System.out.println("Output File: " + arg);
                                System.out.println("File Format: Raw Audio");
                                System.out.println("Perceptual Enhancement: " + this.enhanced);
                            }
                        }
                        ((AudioFileWriter)arg1).open(arg);
                        ((AudioFileWriter)arg1).writeHeader(null);
                        ++args9;
                    } 
                    else {
                        arg0.readFully(var4, 0, args7);
                        if (this.loss > 0 && random.nextInt(100) < this.loss)
                        {
                            this.speexDecoder.processData(null, 0, args7);

                            for (arg4 = 1; arg4 < this.nframes; ++arg4)
                            {
                                this.speexDecoder.processData(true);
                            }
                        }
                        else {
                            this.speexDecoder.processData(var4, 0, args7);

                            for (arg4 = 1; arg4 < this.nframes; ++arg4)
                            {
                                this.speexDecoder.processData(false);
                            }
                        }

                        if ((arg9 = this.speexDecoder.getProcessedData(var5, 0)) > 0)
                        {
                            ((AudioFileWriter)arg1).writePacket(var5, 0, arg9);
                        }

                        ++args9;
                    }
                }

                arg0.readFully(var3, 0, 27);
                arg2 = readInt(var3, 22);
                var3[22] = 0;
                var3[23] = 0;
                var3[24] = 0;
                var3[25] = 0;
                arg3 = OggCrc.checksum(0, var3, 0, 27);
                
                if (!"OggS".equals(new String(var3, 0, 4)))
                {
                    System.err.println("missing ogg id!");
                    return;
                }
                int arg7 = var3[26] & 255;
                arg0.readFully(var3, 27, arg7);
                arg3 = OggCrc.checksum(arg3, var3, 27, arg7);

                for (int arg8 = 0; arg8 < arg7; ++arg8)
                {
                    args7 = var3[27 + arg8] & 255;
                    
                    if (args7 == 255)
                    {
                        System.err.println("sorry, don\'t handle 255 sizes!");
                        return;
                    }

                    arg0.readFully(var4, 0, args7);
                    arg3 = OggCrc.checksum(arg3, var4, 0, args7);
                    
                    if (args9 == 0)
                    {
                        if (this.readSpeexHeader(var4, 0, args7))
                        {
                            if (this.printlevel <= 0)
                            {
                                System.out.println("File Format: Ogg Speex");
                                System.out.println("Sample Rate: " + this.sampleRate);
                                System.out.println("Channels: " + this.channels);
                                System.out.println("Encoder mode: " + (this.mode == 0?"Narrowband":(this.mode == 1?"Wideband":"UltraWideband")));
                                System.out.println("Frames per packet: " + this.nframes);
                            }

                            if (this.destFormat == 2)
                            {
                                arg1 = new PcmWaveWriter(this.speexDecoder.getSampleRate(), this.speexDecoder.getChannels());
                                
                                if (this.printlevel <= 0)
                                {
                                    System.out.println();
                                    System.out.println("Output File: " + arg);
                                    System.out.println("File Format: PCM Wave");
                                    System.out.println("Perceptual Enhancement: " + this.enhanced);
                                }
                            }
                            else {
                                arg1 = new RawWriter();
                                
                                if (this.printlevel <= 0)
                                {
                                    System.out.println();
                                    System.out.println("Output File: " + arg);
                                    System.out.println("File Format: Raw Audio");
                                    System.out.println("Perceptual Enhancement: " + this.enhanced);
                                }
                            }
                            ((AudioFileWriter)arg1).open(arg);
                            ((AudioFileWriter)arg1).writeHeader(null);
                            ++args9;
                        }
                    }
                    else if (args9 == 1)
                    {
                        ++args9;
                    }
                    else {
                        if (this.loss > 0 && random.nextInt(100) < this.loss)
                        {
                            this.speexDecoder.processData(null, 0, args7);

                            for (arg4 = 1; arg4 < this.nframes; ++arg4)
                            {
                                this.speexDecoder.processData(true);
                            }
                        }
                        else {
                            this.speexDecoder.processData(var4, 0, args7);

                            for (arg4 = 1; arg4 < this.nframes; ++arg4)
                            {
                                this.speexDecoder.processData(false);
                            }
                        }

                        if ((arg9 = this.speexDecoder.getProcessedData(var5, 0)) > 0)
                        {
                            ((AudioFileWriter)arg1).writePacket(var5, 0, arg9);
                        }
                        ++args9;
                    }
                }
            } 
            while(arg3 == arg2);

            throw new IOException("Ogg CheckSums do not match");
        }
        catch (EOFException e)
        {
            ((AudioFileWriter)Objects.requireNonNull(arg1)).close();
        }
    }

    private boolean readSpeexHeader(byte[] args, int arg, int var3)
    {
        if (var3 != 80)
        {
            System.out.println("Oooops");
            return false;
        }
        else if (!"Speex   ".equals(new String(args, arg, 8)))
        {
            return false;
        }
        else {
            this.mode = args[40 + arg] & 255;
            this.sampleRate = readInt(args, arg + 36);
            this.channels = readInt(args, arg + 48);
            this.nframes = readInt(args, arg + 64);
            return this.speexDecoder.init(this.mode, this.sampleRate, this.channels, this.enhanced);
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