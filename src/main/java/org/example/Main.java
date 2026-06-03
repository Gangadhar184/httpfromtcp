package org.example;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    // Special marker used to signal that the producer thread
    // has finished reading the file.
    // Java BlockingQueues cannot.
    //
    //  we place a special  value into
    // the queue to tell consumers that no more data is coming.
    private static final String EOF_MARKER = "__EOF__";

    public static void main(String[] args) throws Exception {

        // Open the file.
        //
        // Notice that we DO NOT use try-with-resources here.
        //
        // Why?
        //
        // The worker thread will continue reading from this file
        // after getLinesChannel() returns.
        //
        // If we closed it here, the worker thread would fail.
        InputStream file = new FileInputStream("message.txt");

        // Start the worker thread and receive the queue.
        //
        // Think of this exactly like:
        //
        // Java:
        // queue = getLinesChannel(file)
        BlockingQueue<String> lines = getLinesChannel(file);

        // Consume lines as they arrive.
        //
        // We keep pulling items from the queue until we
        // receive the EOF marker.
        while (true) {

            // take() blocks (waits) if the queue is empty.
            // This is similar to receiving from a Go channel.
            String line = lines.take();

            // Producer thread signals completion by sending
            // the EOF marker.
            if (EOF_MARKER.equals(line)) {
                break;
            }
            System.out.printf("read: %s%n", line);
        }
    }

    /**
     * Responsibilities:
     * - Read file in 8-byte chunks
     * - Reconstruct complete lines
     * - Send lines to a queue
     * - Close file when finished
     * - Signal completion
     */
    public static BlockingQueue<String> getLinesChannel(
            InputStream stream
    ) {

        // Create the queue.
        //
        // Think of this as our channel.
        //
        // Producer thread writes to it.
        // Consumer thread reads from it.
        BlockingQueue<String> channel =
                new LinkedBlockingQueue<>();

        // Create worker thread.
        Thread worker = new Thread(() -> {

            // try-with-resources ensures the file is closed
            // when the worker is completely finished.
            try (InputStream in = stream) {

                // Buffer used for reading.
                // Size = 8 bytes because that's what the
                // assignment requires.
                byte[] buffer = new byte[8];

                // Holds a line that may span multiple reads.
                // Example:
                // Read #1:
                // "Hello Wo"
                // currentLine = "Hello Wo"
                //
                // Read #2:
                // "rld\nJava"
                // currentLine helps us reconstruct:
                //
                // "Hello World"
                String currentLine = "";

                // Continue reading until EOF.
                while (true) {

                    // Read up to 8 bytes into buffer.
                    // Returns:
                    // 8  -> full buffer
                    // <8 -> partial read
                    // -1 -> EOF
                    int bytesRead = in.read(buffer);

                    // End of file.
                    if (bytesRead == -1) {
                        break;
                    }

                    // Convert ONLY valid bytes into a String.
                    // Never use:
                    // new String(buffer)
                    // because old data may remain in unused
                    // parts of the buffer.
                    String chunk =
                            new String(buffer, 0, bytesRead);

                    // Split chunk on newline characters.
                    // Example:
                    // chunk:
                    // "rld\nJava"
                    // parts:
                    // ["rld", "Java"]
                    // The -1 tells Java to preserve trailing
                    // empty strings.
                    String[] parts = chunk.split("\n", -1);

                    // Process every part EXCEPT the last.

                    // Every part before the last ended with
                    // a newline, therefore it represents a
                    // complete line.
                    // Example:
                    // ["rld", "Java"]
                    // "rld" completes a line.
                    // "Java" may continue later.
                    for (int i = 0; i < parts.length - 1; i++) {

                        // Build complete line.
                        // Example:
                        // currentLine:
                        // "Hello Wo"
                        // parts[i]:
                        // "rld"
                        // result:
                        // "Hello World"
                        String completeLine = currentLine + parts[i];

                        // Send line into queue.
                        channel.put(completeLine);

                        // Line is complete and sent.
                        // Reset builder for next line.
                        currentLine = "";
                    }

                    // Last part may be incomplete.
                    // Save it for future reads
                    // Example:
                    // parts:
                    // ["rld", "Java"]
                    // currentLine becomes:
                    // "Java"
                    currentLine += parts[parts.length - 1];
                }

                // EOF reached.
                // There may still be one unfinished line
                // Example:
                //
                // File:
                // Hello
                // World
                //
                // If the file does not end with '\n',
                // "World" will still be stored in currentLine.
                if (!currentLine.isEmpty()) {

                    // Send final line.
                    channel.put(currentLine);
                }

            } catch (Exception e) {

                // Print any unexpected errors.
                e.printStackTrace();

            } finally {

                // Signal that no more lines are coming.
                try {
                    channel.put(EOF_MARKER);
                } catch (InterruptedException ignored) {
                }
            }
        });

        // Start worker thread.
        //
        // Nothing inside the lambda executes until start()
        // is called.
        worker.start();

        // Return immediately.
        //
        // The worker thread continues running in the background.
        return channel;
    }
}