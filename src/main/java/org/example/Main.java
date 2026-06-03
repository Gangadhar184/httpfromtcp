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
            //keep reading until end of file
            while(true) {
                //read up to 8 bytes form the file into buffer,
                //eg: if txt file contains "helloworld"
                //first read: buffer = [h,e,l,l,o,w,o,r] bytreads = 8
                //second read: buffer = [l,d,o,w,o,r,...], byteread = 2
                // only first 2bytes are new data
                int byteReads = fs.read(buffer);
                if(byteReads == -1) break; //if its -1 we are end of file
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
                System.out.printf("read: %s%n", chunk);
            }
        }
    }
}
