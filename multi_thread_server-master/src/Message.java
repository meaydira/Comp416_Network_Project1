

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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


    public Message(int phase, int type ,int length, String payload){
        this.phase = (byte) phase;
        this.type = (byte) type;
        this.length = length;
        this.payload = payload;
    }
    /**
     * Must be implemented by all sub classes to convert the bytes in the buffer
     * into the fields in this message object.
     * @param buffer the byte buffer containing the message
     */
    public String fromBytes(ByteBuffer buffer){
        int len = buffer.getShort();
        byte[] bytes = new byte[len];
        buffer.get(bytes);
        return new String(bytes);
    };

    /**
     * Must be implemented by all sub classes to convert the message into
     * bytes in the buffer.
     * @param buffer the byte buffer to receive the message data.
     */
    public void toBytes(ByteBuffer buffer){

    };

    /**
     * We use the simple class name (without package) as the message type.
     * This is not entirely efficient, but will suffice for this example.
     * @return the message type.
     */
    private String messageType() {return this.type}

    /**
     * Converts a string into a message field in the buffer passed in.
     * into the buffer
     * @param buffer the buffer that represents the socket
     * @param str the string to be written
     */
    public static void stringToMsg(ByteBuffer buffer, String str) {
        byte[] bytes = str.getBytes();
        int len = bytes.length;
        buffer.putShort((short) len);
        buffer.put(bytes);
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

    /**
     * Reads a single message from the socket, returning it as a sub class of Message
     * @param socket socket to read from
     * @param dataBuffer the data buffer to use
     * @return a message if it could be parsed
     * @throws IOException if the message could not be converted.
     */
    public static Message nextMessageFromSocket(SocketChannel socket, ByteBuffer dataBuffer) throws IOException {


        // read phase
        ensureBytesAvailable(socket, dataBuffer, 1);
        int phase = dataBuffer.get();

        //read message type
        ensureBytesAvailable(socket, dataBuffer, 1);
        int messageType = dataBuffer.get();

        // read the first 4 bytes to get the message length.
        ensureBytesAvailable(socket, dataBuffer, 4);
        int length = dataBuffer.getInt();

        // read the rest of the message (as denoted by length)
        ensureBytesAvailable(socket, dataBuffer, length);
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

        msg = new Message(phase,messageType, length, stringFromMsg(dataBuffer));

        // message's fromBytes is now used to recover the rest of the fields from the payload
       // msg.fromBytes(dataBuffer);

        LOGGER.info("Message read from socket: " + msg);

        return msg;

    }

    /**
     * Send any message derived from Message base class on the socket,
     * @param channel the channel on which the message is sent
     * @param toSend the message to send.
     * @throws IOException if there is a problem during writing.
     */
    public static void sendMessage(SocketChannel channel, Message toSend) throws IOException {

        // we need to put the message type into the buffer first.
        ByteBuffer bbMsg = ByteBuffer.allocate(2048);
        stringToMsg(bbMsg, toSend.messageType());

        // and then any extra fields for this type of message
        toSend.toBytes(bbMsg);
        bbMsg.flip();

        // now we need to encode the length into a different buffer.
        ByteBuffer bbOverall = ByteBuffer.allocate(10);
        bbOverall.putInt(bbMsg.remaining());
        bbOverall.flip();

        // and lastly, we write the length, followed by the message.
        long written = channel.write(new ByteBuffer[]{bbOverall, bbMsg});

        LOGGER.info("Message written to socket: " + toSend + ", length was: " + written);
    }

    /**
     * When we are reading messages from the wire, we need to ensure there are
     * enough bytes in the buffer to fully decode the message. If not we keep
     * reading until we have enough.
     * @param socket the socket to read from
     * @param buffer the buffer to store the bytes
     * @param required the amount of data required.
     * @throws IOException if the socket closes or errors out.
     */
    private static void ensureBytesAvailable(SocketChannel socket, ByteBuffer buffer, int required) throws IOException {
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

    }
}