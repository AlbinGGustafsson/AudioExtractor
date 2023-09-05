import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * A command-line program for extracting audio from video files (MP4) using FFmpeg.
 * It allows users to specify the input video file, start and end times for extraction, and the output audio file name.
 * The extracted audio is saved as an MP3 file in the "output" folder.
 */
public class AudioExtractor {
    private static final String[] ACCEPTED_INPUT_FORMATS = {
            ".mp4", ".avi", ".mkv", ".mov", ".wmv", ".flv", ".3gp", ".webm", ".ogg"
    };
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * The main method of the AudioExtractor class. Initializes an instance of the class
     * and calls the runAudioExtraction method to execute the audio extraction process.
     *
     * @param args The command-line arguments (not used).
     */
    public static void main(String[] args) {
        AudioExtractor audioExtractor = new AudioExtractor();
        audioExtractor.runAudioExtraction();
    }

    /**
     * Runs the audio extraction process, guiding the user through the steps and handling user input.
     */
    private void runAudioExtraction() {
        createFolder("input");

        File[] files = getFiles();
        List<File> filteredFiles = filterVideoFiles(files, ACCEPTED_INPUT_FORMATS);

        if (filteredFiles.isEmpty()) {
            System.out.println("Could not find any video files in the input folder. Please add video files.");
            return;
        }

        printFiles(filteredFiles);

        int selectedFileIndex = promptForFileSelection(filteredFiles);
        if (selectedFileIndex == -1) {
            System.out.println("Invalid file selection.");
            return;
        }

        String startTime = promptForUserInput("Start Time: ");
        String endTime = promptForUserInput("End Time: ");
        String outputFileName = promptForUserInput("Filename: ") + ".mp3";

        createFolder("output");

        String inputFileName = "input/" + filteredFiles.get(selectedFileIndex).getName();
        String outputFilePath = "output/" + outputFileName;

        extractMp3(parseTimeString(startTime), parseTimeString(endTime) - parseTimeString(startTime), outputFilePath, inputFileName);
    }

    /**
     * Prompts the user to select a file by entering its number.
     *
     * @param fileList A list of File objects representing available files.
     * @return The index of the selected file in the list, or -1 if the selection is invalid.
     */
    private int promptForFileSelection(List<File> fileList) {
        System.out.print("Pick a file by entering its number: ");
        int fileNumber = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character
        return (fileNumber >= 0 && fileNumber < fileList.size()) ? fileNumber : -1;
    }

    /**
     * Prompts the user for input with a specified prompt message.
     *
     * @param prompt The prompt message displayed to the user.
     * @return The user's input as a String.
     */
    private String promptForUserInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Gets an array of files from the "input" folder.
     *
     * @return An array of File objects representing the files in the "input" folder.
     */
    private File[] getFiles() {
        Path currentRelativePath = Paths.get("");
        return new File(currentRelativePath.toAbsolutePath() + "\\input").listFiles();
    }

    /**
     * Prints a list of available files to the console along with their corresponding numbers.
     *
     * @param fileList A list of File objects to be printed.
     */
    private void printFiles(List<File> fileList) {
        System.out.println("Available files:");
        AtomicInteger listFileNumber = new AtomicInteger();
        fileList.forEach(f -> System.out.printf("%d: %s %n", listFileNumber.getAndIncrement(), f.getName()));
    }

    /**
     * Filters an array of files based on a list of accepted video file extensions.
     *
     * @param files              An array of File objects to be filtered.
     * @param acceptedExtensions An array of video file extensions to accept (e.g., ".mp4", ".avi").
     * @return A list of File objects that have filenames ending with one of the accepted extensions.
     */
    private List<File> filterVideoFiles(File[] files, String[] acceptedExtensions) {
        return Arrays.stream(files)
                .filter(file -> Arrays.stream(acceptedExtensions)
                        .anyMatch(extension -> file.getName().toLowerCase().endsWith(extension.toLowerCase())))
                .toList();
    }

    /**
     * Parses a time string in the format "mm:ss" or "ss" into an integer representing the time in seconds.
     *
     * @param timeString The time string to be parsed.
     * @return An integer representing the time in seconds.
     */
    private int parseTimeString(String timeString) {

        if (timeString.contains(":")){
            String[] test = timeString.split(":");
            return Integer.parseInt(test[0]) * 60 + Integer.parseInt(test[1]);
        }
        return Integer.parseInt(timeString);
    }

    /**
     * Creates a folder with the specified name if it does not already exist.
     *
     * @param folderName The name of the folder to be created.
     */
    private void createFolder(String folderName) {
        File folder = new File(folderName);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Created '" + folderName + "' folder.");
            } else {
                System.err.println("Failed to create '" + folderName + "' folder.");
            }
        }
    }

    /**
     * Extracts audio from a video file using FFmpeg with the specified start time, duration, output file, and input file.
     *
     * @param startTime  The start time for audio extraction in seconds.
     * @param duration   The duration of the extracted audio in seconds.
     * @param output     The path to the output audio file (including the ".mp3" extension).
     * @param input      The path to the input video file.
     */
    private void extractMp3(int startTime, int duration, String output, String input) {
        try {
            // Build FFmpeg command
            String[] cmd = {
                    "ffmpeg",
                    "-ss", String.valueOf(startTime),   // Start time
                    "-i", input,
                    "-vn", "-acodec", "libmp3lame",
                    "-t", String.valueOf(duration),     // Duration
                    output
            };

            // Run FFmpeg command
            Process process = Runtime.getRuntime().exec(cmd);

            // Read FFmpeg output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for FFmpeg to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Audio extraction successful!");
            } else {
                System.out.println("Audio extraction failed!");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}