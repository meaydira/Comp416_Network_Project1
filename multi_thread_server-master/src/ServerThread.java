import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

class ServerThread extends Thread
{
    protected BufferedReader is;
    protected PrintWriter os;
    protected Socket s;
    private String line = new String();
    private String lines = new String();

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try
        {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());

        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        System.out.println("Authenticating the client: " + s.getRemoteSocketAddress());

        if(!isAuth()){
            os.println("Authentication failed, closing the connection");
            os.flush();
            closeConnection();
        }else{
            os.println("Authentication successful");
            os.flush();
        }

        try
        {
            line = is.readLine();
            while (line.compareTo("QUIT") != 0)
            {
		lines = "Client messaged : " + line + " at  : " + Thread.currentThread().getId();
                os.println(lines);
                os.flush();
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent :  " + lines);
                line = is.readLine();
            }
        }
        catch (IOException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            closeConnection();
        }//end finally
    }


    private void closeConnection(){
        try
        {
            System.out.println("Closing the connection");
            if (is != null)
            {
                is.close();
                System.err.println(" Socket Input Stream Closed");
            }

            if (os != null)
            {
                os.close();
                System.err.println("Socket Out Closed");
            }
            if (s != null)
            {
                s.close();
                System.err.println("Socket Closed");
            }

        }
        catch (IOException ie)
        {
            System.err.println("Socket Close Error");
        }
    }


    private boolean isAuth(){

        try
        {
            ArrayList<Question> questions = getQuestions();
            int random = (int) (Math.random() % questions.size());
            Question question1 = questions.get(random);
            questions.remove(random);
            random = (int) (Math.random() % questions.size());
            Question question2 = questions.get(random);
            questions.remove(random);


            os.println(question1.getQuestion());
            os.flush();
           // line = is.readLine();

            if ((line = is.readLine()) == null) {
               System.out.println("Readline returned empty string");
                //Your code
            }

            System.out.println("Client answered question 1: " + question1.getQuestion() + " answer :  " + line);

            String response = "";
            if(line.equalsIgnoreCase(question1.getAnswer())){
                System.out.println(" Question 1 is Correct");
                response+= "Question 1 is Correct ! One more : ";

            }else{

                System.out.println("Wrong! correct answer was: " + question1.getAnswer());
                os.println("Wrong answer, closing the connection ");
                os.flush();
                return false;
            }

            response+=question2.getQuestion();
            os.println(response);
            os.flush();
            line = is.readLine();
            System.out.println("Client answered question 2: " + question2.getQuestion() + " answer :  " + line);

            if(line.equalsIgnoreCase(question2.getAnswer())){
                System.out.println(" Question 2 is Correct");
                os.println("Question 2 is Correct ! User is authenticated successfully ");
                os.flush();
                return true;

            }else{

                System.out.println("Wrong! correct answer was: " + question1.getAnswer());
                os.println("Wrong answer");
                os.flush();
                return false;
            }






        }
        catch (IOException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName();
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            closeConnection();
        }//end finally
        return true;
    }

    private ArrayList<Question> getQuestions(){
        ArrayList<Question> list = new ArrayList<>();
        list.add(new Question("What is your favourite color?", "red"));
        list.add(new Question("Last name of your best friend?", "bozyilan"));
        list.add(new Question("your group name?", "superstars"));
        list.add(new Question("Who is your favourite teacher?", "öznur özkasap"));

        return list;

    }

}
