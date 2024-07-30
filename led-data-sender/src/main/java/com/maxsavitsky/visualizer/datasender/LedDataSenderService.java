package com.maxsavitsky.visualizer.datasender;

import com.lazydash.audio.visualizer.spectrum.core.CoreConfig;
import com.lazydash.audio.visualizer.spectrum.core.model.FrequencyBar;
import com.lazydash.audio.visualizer.spectrum.core.service.FrequencyBarsFFTService;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LedDataSenderService {

    private final Executor executor = Executors.newSingleThreadExecutor();

    public static final int DEFAULT_FPS = 30;

    private final int FPS;

    private boolean isPreviousDataWasEmpty = false;

    private boolean isConnecting = false;

    private final FrequencyBarsFFTService frequencyBarsFFTService;
    private Socket socket;
    private boolean isSocketSetManually = false;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public LedDataSenderService(FrequencyBarsFFTService frequencyBarsFFTService){
        this(frequencyBarsFFTService, DEFAULT_FPS);
    }

    public LedDataSenderService(FrequencyBarsFFTService frequencyBarsFFTService, int fps) {
        this.frequencyBarsFFTService = frequencyBarsFFTService;
        this.FPS = fps;
    }

    public void setSocket(Socket socket){
        this.socket = socket;
        isSocketSetManually = true;
    }

    public void start(){
        executor.execute(()->{
            try {
                if(!isSocketSetManually)
                    socket = createSocket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            scheduledExecutorService.scheduleAtFixedRate(this::send, 1000, 1000 / FPS, TimeUnit.MILLISECONDS);
        });
    }

    public void stop(){
        if(!isSocketSetManually && socket != null) {
            try {
                sendBytes(new byte[]{0, 1, (byte) 254});
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }
        scheduledExecutorService.shutdownNow();
    }

    private void send(){
        List<FrequencyBar> list = frequencyBarsFFTService.getFrequencyBarList();
        if(list.isEmpty() || isConnecting)
            return;
        if(!isSocketSetManually && (!socket.isConnected() || socket.isClosed())) {
            try {
                socket = createSocket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        int sz = Math.min(CoreConfig.ledCount, list.size());
        byte[] bytes = new byte[5 + sz];
        int len = bytes.length - 2;
        //LOGGER.info(String.valueOf(len));
        bytes[0] = (byte) (len >> 8);
        bytes[1] = (byte) (len & 0xFF);
        bytes[2] = (byte) CoreConfig.barsColorMode; // color mode
        // args
        bytes[3] = (byte) (255 * (CoreConfig.barsColorMode == 0 ? 0 : list.get(0).getColorHue() / 360)); // hue
        bytes[4] = (byte) (255 * (CoreConfig.barsColorMode == 0 ? 1.0 : list.get(0).getColorSaturation())); // saturation
        boolean areAllEmpty = true;
        for(int i = 0; i < sz; i++){
            var bar = list.get(i);
            byte value = 0;
            if (CoreConfig.barsColorMode == 0) {
                var hueVal = bar.getColorHue() / 360.0;
                value = (byte) (hueVal * 255);
            } else if (CoreConfig.barsColorMode == 1) {
                value = (byte) (bar.getHeightRatio() * 255);
            }
            bytes[5 + i] = value;
            areAllEmpty &= bar.getHeightRatio() <= 0.02;
        }
        if (areAllEmpty && isPreviousDataWasEmpty) return; // just skip
        isPreviousDataWasEmpty = areAllEmpty;
        //LOGGER.info("Elapsed time {}", System.currentTimeMillis() - startTime);

        sendBytes(bytes);
    }

    private void sendBytes(byte[] bytes) {
        long startTime = System.currentTimeMillis();
        //System.out.println(bytes.length);
        try {
            OutputStream os = socket.getOutputStream();
            os.write(bytes);
        }catch (IOException e){
            if(isSocketSetManually)
                throw new RuntimeException(e);
            else {
                try {
                    socket = createSocket();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        //LOGGER.info("Elapsed time {}", System.currentTimeMillis() - startTime);
    }

    private Socket createSocket() throws IOException {
        isConnecting = true;
        Socket s = new Socket();
        s.setKeepAlive(true);
        //s.setTcpNoDelay(true);
        s.connect(new InetSocketAddress("192.168.100.90", 81));
        isConnecting = false;
        return s;
    }
}
