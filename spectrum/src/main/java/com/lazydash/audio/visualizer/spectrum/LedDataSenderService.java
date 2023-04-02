package com.lazydash.audio.visualizer.spectrum;

import com.lazydash.audio.visualizer.spectrum.core.model.FrequencyBar;
import com.lazydash.audio.visualizer.spectrum.core.service.FrequencyBarsFFTService;
import com.lazydash.audio.visualizer.spectrum.system.config.AppConfig;
import com.lazydash.audio.visualizer.spectrum.ui.code.spectral.SpectralAnimator;
import javafx.animation.AnimationTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LedDataSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LedDataSenderService.class);
    private final Executor executor = Executors.newSingleThreadExecutor();

    private static final int FPS = SpectralAnimator.FPS;

    private boolean isPreviousDataWasEmpty = false;

    private boolean isConnecting = false;

    private final FrequencyBarsFFTService frequencyBarsFFTService;
    private Socket socket;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public LedDataSenderService(FrequencyBarsFFTService frequencyBarsFFTService) {
        this.frequencyBarsFFTService = frequencyBarsFFTService;
    }

    public void start(){
        executor.execute(()->{
            try {
                socket = createSocket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            scheduledExecutorService.scheduleAtFixedRate(this::send, 1000, 1000 / FPS, TimeUnit.MILLISECONDS);
        });
    }

    public void stop(){
        if(socket != null) {
            try {
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
        if(!socket.isConnected() || socket.isClosed()) {
            try {
                socket = createSocket();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        int sz = Math.min(150, list.size());
        byte[] bytes = new byte[10 + sz];
        int len = bytes.length - 3;
        //LOGGER.info(String.valueOf(len));
        bytes[0] = 2;
        bytes[1] = (byte) (len >> 8);
        bytes[2] = (byte) (len & 0xFF);
        bytes[3] = 7; // command index
        bytes[4] = 0; // effect number (not for us)
        bytes[5] = 10; // speed (not for us)
        bytes[6] = 0; // brightness
        // args
        int argsCount = 1 + sz;
        bytes[7] = (byte) (0);
        bytes[8] = (byte) (argsCount & 0xFF);
        bytes[9] = (byte) ((AppConfig.saturation / 100.0) * 255); // saturation
        boolean areAllEmpty = true;
        for(int i = 0; i < sz; i++){
            var bar = list.get(i);
            var hueVal = bar.getColor().getHue() / 360.0;
            bytes[10 + i] = (byte) (hueVal * 255);
            areAllEmpty &= (1 - hueVal) <= 0.02;
        }
        if (areAllEmpty && isPreviousDataWasEmpty) return; // just skip
        isPreviousDataWasEmpty = areAllEmpty;

        sendBytes(bytes);
    }

    private void sendBytes(byte[] bytes) {
        try {
            OutputStream os = socket.getOutputStream();
            os.write(bytes);
            os.flush();
        }catch (IOException e){
            LOGGER.error("Error writing to socket", e);
            try{
                socket = createSocket();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private Socket createSocket() throws IOException {
        isConnecting = true;
        Socket s = new Socket("192.168.100.80", 80);
        s.setKeepAlive(true);
        isConnecting = false;
        return s;
    }
}
