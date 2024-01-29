package game.gui.utils;

import game.logic.Utils;
import game.logic.exception.AudioLoadingException;
import game.logic.exception.GameException;
import game.logic.exception.PacmanException;
import game.logic.player.PlayersManager;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

public class UIUtils {
    private final static int TIMELAPSE_TO_REMOVE_LAST_SOUND;
    public final static int SCREEN_WIDTH;
    public final static int SCREEN_HEIGHT;
    private static Clip currentContinuousSound;
    private static Clip chomp;
    private static Clip invisible;
    private static final Deque<Clip> sounds;
    private static boolean isMutated;

    // block of static initialization
    static {
        currentContinuousSound = null;
        try {
            var audioInputStream = UIUtils.class.getResourceAsStream("/audio/chomp.wav");
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(Objects.requireNonNull(audioInputStream));
            chomp = AudioSystem.getClip();
            chomp.open(audioInput);
            chomp.loop(Clip.LOOP_CONTINUOUSLY);
            chomp.stop();
        } catch (UnsupportedAudioFileException | LineUnavailableException |
                 IOException | NullPointerException e) {
            // TODO: process via message window in first board opening, that says that some sounds are unavailable
            chomp = null;
            throw new GameException(e);
        }
        sounds = new LinkedList<>();
        TIMELAPSE_TO_REMOVE_LAST_SOUND = 5000;          // seconds
        isMutated = false;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        SCREEN_HEIGHT = screenSize.height;
        SCREEN_WIDTH = screenSize.width;
        System.out.printf("""
                Screen width: %d
                Screen height: %d
                """, SCREEN_WIDTH, SCREEN_HEIGHT);
    }

    public static void startChomp() {
        if (!isMutated() && chomp != null) {
            chomp.loop(Clip.LOOP_CONTINUOUSLY);
            chomp.start();
        }
    }

    public static void stopChomp() {
        if (chomp != null && chomp.isRunning()) {
            chomp.stop();
        }
        if (invisible != null && invisible.isRunning()) {
            invisible.stop();
            invisible.close();
            invisible = null;
        }
        sounds.forEach(DataLine::stop);
    }

    public static boolean isMutated() {
        return isMutated;
    }

    public static void processException(Frame frame, PacmanException exception) {

        // TODO: display alert of proper type
        JOptionPane.showMessageDialog(frame, exception.getMessage());
    }

    /*
     * There can be one sound over another,
     * but it is not allowed to continuous sound to be over another continuous sound
     */
    public static void playSound(String audioFileLocation, boolean isContinuous) throws AudioLoadingException {
        try {
            var audioInputStream = UIUtils.class.getResourceAsStream(audioFileLocation);
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(Objects.requireNonNull(audioInputStream));
            Clip sound = AudioSystem.getClip();
            sound.open(audioInput);
            if (isContinuous) {
                // stop previous continuous sound
                if (currentContinuousSound != null) {
                    currentContinuousSound.stop();
                    currentContinuousSound.close();
                }
                currentContinuousSound = sound;

                sound.loop(Clip.LOOP_CONTINUOUSLY);
            } else if (!isMutated()) {
                // TODO: add sound to list, and set up timeout to remove it
                synchronized (sounds) {
                    sounds.addLast(sound);
                    removeLastSoundAfterTimeout();
                }
            }
            if (!isMutated) {
                sound.start();
            } else {
                sound.stop();
            }
        } catch (UnsupportedAudioFileException | IOException |
                 LineUnavailableException | NullPointerException e) {
            // TODO: provide parameters
            throw new AudioLoadingException("cannot find an audio file or read it's content");
        }
    }

    private static void removeLastSoundAfterTimeout() {
        new Thread(() -> {
            Utils.sleep(TIMELAPSE_TO_REMOVE_LAST_SOUND);
            synchronized (sounds) {
                try {
                    var sound = sounds.removeFirst();
                    sound.stop();
                    sound.close();
                    System.out.println("Last sound removed from the queue");
                } catch (Exception e) {
                    throw new RuntimeException("there were no sounds to remove from sounds list");
                }
            }
        }).start();
    }

    public static void mute() {
        if (currentContinuousSound != null) {
            currentContinuousSound.stop();
        }
        if (chomp != null && chomp.isRunning()) {
            chomp.stop();
        }
        sounds.forEach(DataLine::stop);         // function reference
        sounds.forEach(Line::close);
        sounds.clear();
        isMutated = true;
    }

    public static void unmute() {
        if (currentContinuousSound != null) {
            currentContinuousSound.loop(Clip.LOOP_CONTINUOUSLY);
            currentContinuousSound.start();
        }
        if (invisible != null) {
            invisible.loop(Clip.LOOP_CONTINUOUSLY);
            invisible.start();
        }
        sounds.forEach(DataLine::start);        // function reference
        isMutated = false;
    }

    public static void setFrameDimension(JFrame frame, int height, int width) {
        frame.setBounds((SCREEN_WIDTH - width) / 2,
                (SCREEN_HEIGHT - height) / 2,
                width, height);
    }

    public static BufferedImage toBufferedImage(Image image)
    {
        if (image instanceof BufferedImage)
            return (BufferedImage) image;

        BufferedImage bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.drawImage(image, 0, 0, null);
        graphics2D.dispose();

        // Return the buffered image
        return bi;
    }

    public static ImageIcon rotateImage(BufferedImage image, int angle) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage toStore = new BufferedImage(h, w, image.getType());
        Graphics2D g2 = toStore.createGraphics();
        double x = (h - w)/2.0;
        double y = (w - h)/2.0;
        AffineTransform at = AffineTransform.getTranslateInstance(x, y);
        at.rotate(Math.toRadians(angle), w/2.0, h/2.0);
        g2.drawRenderedImage(image, at);
        g2.dispose();
        return new ImageIcon(toStore);
    }

    public static ImageIcon loadIcon(String iconFilePath) {
        try {
            return new ImageIcon(ImageIO.read(Objects.requireNonNull(
                    UIUtils.class.getResourceAsStream(iconFilePath)
            )));
        } catch (IOException e) {
            // TODO: replace with ImageProcessingException
            throw new RuntimeException(e);
        }
    }

    public static void exit() {
        PlayersManager.getInstance().savePlayers();
        synchronized (sounds) {
            sounds.forEach(s -> {
                s.stop();
                s.close();
            });
            if (currentContinuousSound != null) {
                currentContinuousSound.stop();
                currentContinuousSound.close();
            }
            System.out.println("Good bye!");
            System.exit(0);
        }
    }

    public static void stopContinuousSound() {
        if (currentContinuousSound != null) {
            if (currentContinuousSound.isRunning()) {
                currentContinuousSound.stop();
            }
            currentContinuousSound = null;
        }
    }

    public static void startInvisibleSound(String s) {
        try {
            var audioInputStream = UIUtils.class.getResourceAsStream(s);
            AudioInputStream audioInput = AudioSystem.getAudioInputStream(Objects.requireNonNull(audioInputStream));
            invisible = AudioSystem.getClip();
            invisible.open(audioInput);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            throw new GameException(e);
        }
        if (invisible != null && !isMutated) {
            invisible.loop(Clip.LOOP_CONTINUOUSLY);
            invisible.start();
        }
    }

    public static void stopInvisibleSound() {
        if (invisible != null) {
            invisible.stop();
            invisible.close();
            invisible = null;
        }
    }
}
