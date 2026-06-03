package org.example;

import java.io.FileInputStream;
import java.io.IOException;

/**
 *  your program will now read messages.txt 8 bytes at a time
 *  and print that data back to stdout in 8 byte chunks.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        //open the file txt for reading
        //FileInputStream so we can read bytes from a file.
        try(FileInputStream fs = new FileInputStream("message.txt")) {
            //creating a byte array for capacity of 8 bytes[?,?,?,?,?,?,?,?]
            byte[] buffer = new byte[8];

            //holds the line currently being built
            // Example:
            // Read #1 -> "Hello Wo"
            // currentLine = "Hello Wo"
            //
            // Read #2 -> "rld\nJava"
            // currentLine helps us reconstruct:
            // "Hello World"
            String currentLine = "";

            //keep reading until end of file
            while(true) {
                //read up to 8 bytes form the file into buffer,
                //eg: if txt file contains "helloworld"
                //first read: buffer = [h,e,l,l,o,w,o,r] bytreads = 8
                //second read: buffer = [l,d,o,w,o,r,...], byteread = 2
                // only first 2bytes are new data
                int byteReads = fs.read(buffer);
                if(byteReads == -1){
                    break; //if its -1 we are end of file
                }
                //convert only the bytes that were actually read into string,
                // Parameters:
                // buffer      -> source byte array
                // 0           -> start at index 0
                // bytesRead   -> number of valid bytes
                //
                // Why not:
                // new String(buffer)
                //
                // Because the last read might not fill the buffer.
                //
                // Example:
                // buffer size = 8
                //
                // Previous read:
                // "abcdefgh"
                //
                // Last read:
                // only "xy"
                //
                // buffer becomes:
                // [x, y, c, d, e, f, g, h]
                //
                // new String(buffer)
                // -> "xycdefgh" (WRONG)
                //
                // new String(buffer, 0, 2)
                // -> "xy" (CORRECT)
                String chunk = new String(buffer,0, byteReads);

                /**
                 * split the chunk wherever a newline occurs
                 * eg chunk = "rld\nJava"
                 * parts = ["rld","Java"]
                 * the -1 tells java: keep trailing empty strings too
                 * eg: "abc\n" wihthout -1 : ["abc"] with -1: ["abc", ""]
                 * this is imp because it lets us detect lines that end exactly at a new line
                 */
                String[] parts = chunk.split("\n", -1);
                /**
                 * process every part except the last one. why?
                 * every part before the last is guaranteed to end at a new line, meanint it forms complete line
                 * eg:parts = ["rld","Java"]
                 * "rld" complets the line. "java" may continue int the next chunk
                 */
                for(int i = 0; i < parts.length - 1; i++) {
                    //pring the complete line, currentline contain data from previous red
                    //eg: currL = "Hello Wo" parts[i ] = "rld", output: Hello World
                    System.out.printf("read: %s%n", currentLine + parts[i]);
                    currentLine = "";
                }
                /**
                 * last part may be incomplete,
                 * save it so future reads can continue building the line
                 * eg: ["rld", "java"]
                 * after princint "Hello World":
                 * currentLine += "java"
                 */
                currentLine += parts[parts.length - 1];
            }
            /**
             * after eof there might still be data left
             * eg:
             * File:
             * Hello
             * world
             * if the file does not end with a new line, "world" wuold still be sitting in currentline
             * we must printit
             */
            if(!currentLine.isEmpty()) {
                System.out.printf("read: %s%n", currentLine);
            }
        }
    }
}
