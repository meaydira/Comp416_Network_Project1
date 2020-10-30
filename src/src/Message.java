package src;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Message is the base class of all messages to be send or received over the socket.
 * In this class we present the full set of functions needed to send and receive data
 * over a socket. In a real world case, we'd probably want better separation of concerns
 * but this is more than satisfactory for an example.
 *
 *
 *
 *
 *
 *
 * Message has two abstract methods that must be implemented, these two methods handle
 * conversion to and from a byte buffer.
 */
public class Message {
    byte phase = 0;
    byte type = 0;
    int length = 0;

    String payload = "";
    private final static Logger LOGGER = Logger.getLogger("MESSAGE");


    public Message(byte phase, byte type , int length,String payload){
        this.phase =  phase;
        this.type =  type;
        this.length = length;
        this.payload = payload;
    }
    /**
     * Must be implemented by all sub classes to convert the bytes in the buffer
     * into the fields in this message object.
     * @param buffer the byte buffer containing the message
     */
    public Message fromBytesToMessage(ByteBuffer buffer){
        byte[] arr = new byte[buffer.remaining()];
        buffer.get(arr);
        byte phase = arr[0];
        byte type = arr[1];
        int length = arr[2];
        String fromBytes = new String(Arrays.copyOfRange(arr, 3, arr.length));
        return new Message(phase,type,length,fromBytes);
    };


    public static byte[] messageToBytes(Message message){
        byte[] bytes = new byte[message.length +6];
        bytes[0] = message.phase;
        bytes[1] = message.type;
        byte[] bytesTemp =  intToByteArray(message.length);

        for(int i = 2 ; i < 6 ; i++)
            bytes[i] =  bytesTemp[i-2];

        byte[] bytesMessage= message.payload.getBytes();


        for(int i = 6 ; i < bytes.length ; i++)
            bytes[i] =  bytesMessage[i-6];

        return bytes;
    };

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }

    /**
     * We use the simple class name (without package) as the message type.
     * This is not entirely efficient, but will suffice for this example.
     * @return the message type.
     */
    Integer messageType() {return Integer.valueOf(this.type);}


    public static void stringToMsg(ByteBuffer buffer, Integer type) {
        int len = 4;
        buffer.putShort((short) len);
        buffer.put(ByteBuffer.allocateDirect(type));
    }

    /**
     * converts a message field from the buffer into a string
     * @param buffer the message as a buffer
     * @return the string field
     */
    public static String stringFromMsg(ByteBuffer buffer) {
        int len = buffer.getShort();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes);
    }



    public static Message nextMessageFromSocket(DataInputStream is) throws IOException {


        // read phase
        //ensureBytesAvailable(is, dataBuffer, 1);
        byte phase = is.readByte();
        byte type = is.readByte();
        int length = is.readInt();
        byte[] payload = new byte[length];
        is.read(payload);


        // read the rest of the message (as denoted by length)
        //ensureBytesAvailable(is, dataBuffer, length);

        String payloadString = new String(payload, Charset.defaultCharset());
        // we now get the message type from the payload and see what type of message to create.
        // In a real world example, we may have a message factory that did this for us.
        Message msg = null;
        /*
        Message Type
        0 -> Auth_Request
        1 -> Auth_Challenge
        2 -> Auth_Fail
        3 -> Auth_Success
         */

        msg = new Message(phase,type, length, payloadString);

        // message's fromBytes is now used to recover the rest of the fields from the payload
        // msg.fromBytes(dataBuffer);

        LOGGER.info("Message read from socket: " + msg);
        return msg;

    }


    public static void sendMessage(OutputStream os, Message toSend) throws IOException {
        byte[] bytes = messageToBytes(toSend);

        os.write(bytes);


        LOGGER.info("Message written to socket: " + toSend.payload + ", length was: " +toSend.length);
    }

  /*  private static void ensureBytesAvailable(InputStream socket, ByteBuffer buffer, int required) throws IOException {
        // if there's already something in the buffer, then compact it and prepare it for writing again.
        if(buffer.position() != 0) {
            buffer.compact();
        }

        // we loop until we have enough data to decode the message
        while(buffer.position() < required) {

            // try and read, if read returns 0 or less, the socket's closed.
            int len = socket.read(buffer);
            if(!socket.isOpen() || len <= 0) {
                throw new IOException("Socket closed while reading");
            }

            LOGGER.info("Bytes now in buffer: " + buffer.remaining() + " read from socket: " + len);
        }

        // and finally, prepare the buffer for reading.
        buffer.flip();

    }  */
}