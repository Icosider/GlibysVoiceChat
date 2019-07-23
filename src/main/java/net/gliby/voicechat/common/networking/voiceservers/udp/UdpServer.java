package net.gliby.voicechat.common.networking.voiceservers.udp;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.concurrent.ThreadFactory;

public class UdpServer
{
    private static Logger LOGGER;
    private static final String GROUPS_DEFAULT = null;
    private int port = 8000;
    private String groups;
    private UdpServer.State currentState;
    private final Collection<Listener> listeners;
    private final UdpServer.Event event;
    private final PropertyChangeSupport propSupport;
    private final UdpServer This;
    private ThreadFactory threadFactory;
    private Thread ioThread;
    private MulticastSocket mSocket;
    private final DatagramPacket packet;
    private Throwable lastException;
    private String hostname;

    public UdpServer(Logger logger)
    {
        this.groups = GROUPS_DEFAULT;
        this.currentState = UdpServer.State.STOPPED;
        this.listeners = new LinkedList<>();
        this.event = new UdpServer.Event(this);
        this.propSupport = new PropertyChangeSupport(this);
        this.This = this;
        this.packet = new DatagramPacket(new byte[65536], 65536);
        LOGGER = logger;
    }

    public UdpServer(Logger logger, int port, ThreadFactory factory)
    {
        this.groups = GROUPS_DEFAULT;
        this.currentState = UdpServer.State.STOPPED;
        this.listeners = new LinkedList<>();
        this.event = new UdpServer.Event(this);
        this.propSupport = new PropertyChangeSupport(this);
        this.This = this;
        this.packet = new DatagramPacket(new byte[65536], 65536);
        LOGGER = logger;
        this.port = port;
        this.threadFactory = factory;
    }

    UdpServer(Logger logger2, int port)
    {
        this.groups = GROUPS_DEFAULT;
        this.currentState = UdpServer.State.STOPPED;
        this.listeners = new LinkedList<>();
        this.event = new UdpServer.Event(this);
        this.propSupport = new PropertyChangeSupport(this);
        this.This = this;
        this.packet = new DatagramPacket(new byte[65536], 65536);
        LOGGER = logger2;
        this.port = port;
    }

    UdpServer(Logger logger2, String hostname, int port)
    {
        this.groups = GROUPS_DEFAULT;
        this.currentState = UdpServer.State.STOPPED;
        this.listeners = new LinkedList<>();
        this.event = new UdpServer.Event(this);
        this.propSupport = new PropertyChangeSupport(this);
        this.This = this;
        this.packet = new DatagramPacket(new byte[65536], 65536);
        LOGGER = logger2;
        this.port = port;
        this.hostname = hostname;
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        this.propSupport.addPropertyChangeListener(listener);
    }

    private synchronized void addPropertyChangeListener(String property, PropertyChangeListener listener)
    {
        this.propSupport.addPropertyChangeListener(property, listener);
    }

    synchronized void addUdpServerListener(UdpServer.Listener l)
    {
        this.listeners.add(l);
    }

    void clearUdpListeners()
    {
        this.listeners.clear();
    }

    private void fireExceptionNotification(Throwable t)
    {
        Throwable oldVal = this.lastException;
        this.lastException = t;
        this.firePropertyChange("lastException", oldVal, t);
    }

    public synchronized void fireProperties()
    {
        this.firePropertyChange("port", null, this.getPort());
        this.firePropertyChange("groups", null, this.getGroups());
        this.firePropertyChange("state", null, this.getState());
    }

    private synchronized void firePropertyChange(String prop, Object oldVal, Object newVal)
    {
        try
        {
            this.propSupport.firePropertyChange(prop, oldVal, newVal);
        }
        catch (Exception e)
        {
            LOGGER.log(Level.WARN, "A property change listener threw an exception: " + e.getMessage(), e);
            this.fireExceptionNotification(e);
        }
    }

    private synchronized void fireUdpServerPacketReceived() {
        UdpServer.Listener[] ll = this.listeners.toArray(new Listener[0]);

        for (Listener l : ll)
        {
            try
            {
                l.packetReceived(this.event);
            }
            catch (Exception e)
            {
                LOGGER.warn("UdpServer.Listener " + l + " threw an exception: " + e.getMessage());
                this.fireExceptionNotification(e);
            }
        }
    }

    private synchronized String getGroups()
    {
        return this.groups;
    }

    public synchronized Throwable getLastException()
    {
        return this.lastException;
    }

    public synchronized DatagramPacket getPacket()
    {
        return this.packet;
    }

    public synchronized int getPort()
    {
        return this.port;
    }

    public synchronized int getReceiveBufferSize() throws SocketException
    {
        if (this.mSocket == null)
        {
            throw new SocketException("getReceiveBufferSize() cannot be called when the server is not started.");
        }
        else {
            return this.mSocket.getReceiveBufferSize();
        }
    }

    private synchronized UdpServer.State getState()
    {
        return this.currentState;
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
    {
        this.propSupport.removePropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(String property, PropertyChangeListener listener)
    {
        this.propSupport.removePropertyChangeListener(property, listener);
    }

    public synchronized void removeUdpServerListener(UdpServer.Listener l)
    {
        this.listeners.remove(l);
    }

    private synchronized void reset()
    {
        switch (this.currentState)
        {
            case STARTED:
                this.addPropertyChangeListener("state", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        UdpServer.State newState = (UdpServer.State)evt.getNewValue();

                        if (newState == UdpServer.State.STOPPED)
                        {
                            UdpServer server = (UdpServer)evt.getSource();
                            server.removePropertyChangeListener("state", this);
                            server.start();
                        }
                    }
                });
                this.stop();
            default:
        }
    }

    private void runServer()
    {
        try
        {
            UdpServer proposed;

            try
            {
                if (this.hostname != null)
                {
                    InetAddress var23 = InetAddress.getByName(this.hostname);
                    this.mSocket = new MulticastSocket(new InetSocketAddress(var23, this.getPort()));
                }
                else {
                    this.mSocket = new MulticastSocket(this.getPort());
                }

                LOGGER.info("UDP Server established on port " + this.getPort());

                try
                {
                    this.mSocket.setReceiveBufferSize(this.packet.getData().length);
                    LOGGER.info("UDP Server receive buffer size (bytes): " + this.mSocket.getReceiveBufferSize());
                }
                catch (IOException e)
                {
                    int size = this.packet.getData().length;
                    int buffSize = this.mSocket.getReceiveBufferSize();
                    LOGGER.warn(String.format("Could not set receive buffer to %d. It is now at %d. Error: %s", size, buffSize, e.getMessage()));
                }

                String groups = this.getGroups();

                if (groups != null)
                {
                    String[] splitted = groups.split("[\\s,]+");

                    for (String p : splitted)
                    {
                        try
                        {
                            this.mSocket.joinGroup(InetAddress.getByName(p));
                            LOGGER.info("UDP Server joined multicast group " + p);
                        }
                        catch (IOException e)
                        {
                            LOGGER.warn("Could not join " + p + " as a multicast group: " + e.getMessage());
                        }
                    }
                }

                this.setState(UdpServer.State.STARTED);
                LOGGER.info("UDP Server listening...");

                while (!this.mSocket.isClosed())
                {
                    proposed = this.This;

                    synchronized (this.This)
                    {
                        if (this.currentState == UdpServer.State.STOPPING)
                        {
                            LOGGER.info("Stopping UDP Server by request.");
                            this.mSocket.close();
                        }
                    }

                    if (!this.mSocket.isClosed())
                    {
                        this.mSocket.receive(this.packet);
                        this.fireUdpServerPacketReceived();
                    }
                }
            }
            catch (Exception e)
            {
                proposed = this.This;

                synchronized (this.This)
                {
                    if (this.currentState == UdpServer.State.STOPPING)
                    {
                        this.mSocket.close();
                        LOGGER.info("Udp Server closed normally.");
                    }
                    else {
                        LOGGER.warn("If the server cannot bind: Switch to Minecraft Networking in config or setup UDP properly, that means port-forwarding.");
                        LOGGER.log(Level.WARN, "Server closed unexpectedly: " + e.getMessage(), e);
                    }
                }

                this.fireExceptionNotification(e);
            }
        }
        finally
        {
            this.setState(UdpServer.State.STOPPING);

            if (this.mSocket != null)
            {
                this.mSocket.close();
            }
            this.mSocket = null;
        }
    }

    synchronized void send(DatagramPacket packet) throws IOException
    {
        if (this.mSocket == null)
        {
            throw new IOException("No socket available to send packet; is the server running?");
        }
        else {
            this.mSocket.send(packet);
        }
    }

    public synchronized void setGroups(String group)
    {
        String oldVal = this.groups;
        this.groups = group;

        if (this.getState() == UdpServer.State.STARTED)
        {
            this.reset();
        }
        this.firePropertyChange("groups", oldVal, this.groups);
    }

    public synchronized void setPort(int port)
    {
        if (port >= 0 && port <= '\uffff')
        {
            int oldVal = this.port;
            this.port = port;

            if (this.getState() == UdpServer.State.STARTED)
            {
                this.reset();
            }
            this.firePropertyChange("port", oldVal, port);
        }
        else {
            throw new IllegalArgumentException("Cannot set port outside range 0..65535: " + port);
        }
    }

    public synchronized void setReceiveBufferSize(int size) throws SocketException
    {
        if (this.mSocket == null)
        {
            throw new SocketException("setReceiveBufferSize(..) cannot be called when the server is not started.");
        }
        else {
            this.mSocket.setReceiveBufferSize(size);
        }
    }

    protected synchronized void setState(UdpServer.State state)
    {
        UdpServer.State oldVal = this.currentState;
        this.currentState = state;
        this.firePropertyChange("state", oldVal, state);
    }

    public synchronized void start()
    {
        if (this.currentState == UdpServer.State.STOPPED)
        {
            assert this.ioThread == null : this.ioThread;

            Runnable run = () -> {
                UdpServer.this.runServer();
                UdpServer.this.ioThread = null;
                UdpServer.this.setState(State.STOPPED);
            };

            if (this.threadFactory != null)
            {
                this.ioThread = this.threadFactory.newThread(run);
            }
            else {
                this.ioThread = new Thread(run, this.getClass().getName());
            }
            this.setState(UdpServer.State.STARTING);
            this.ioThread.start();
        }
    }

    public synchronized void stop()
    {
        if (this.currentState == UdpServer.State.STARTED)
        {
            this.setState(UdpServer.State.STOPPING);

            if (this.mSocket != null)
            {
                this.mSocket.close();
            }
        }
    }

    public static class Event extends EventObject
    {
        private static final long serialVersionUID = 1L;

        Event(UdpServer src)
        {
            super(src);
        }

        public DatagramPacket getPacket()
        {
            return this.getUdpServer().getPacket();
        }

        byte[] getPacketAsBytes()
        {
            DatagramPacket packet = this.getPacket();

            if (packet == null)
            {
                return null;
            }
            else {
                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), packet.getOffset(), data, 0, data.length);
                return data;
            }
        }

        public String getPacketAsString()
        {
            DatagramPacket packet = this.getPacket();

            if (packet == null)
            {
                return null;
            }
            else {
                return new String(packet.getData(), packet.getOffset(), packet.getLength());
            }
        }

        public UdpServer.State getState()
        {
            return this.getUdpServer().getState();
        }

        UdpServer getUdpServer()
        {
            return (UdpServer)this.getSource();
        }

        public void send(DatagramPacket packet) throws IOException
        {
            this.getUdpServer().send(packet);
        }
    }

    public enum State
    {
        STARTING, STARTED, STOPPING, STOPPED
    }

    public interface Listener extends EventListener
    {
        void packetReceived(UdpServer.Event var1);
    }
}