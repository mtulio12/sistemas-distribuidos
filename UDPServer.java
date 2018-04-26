import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;

class UDPServer {
    static Arquivo arquivo = new Arquivo();
    static Queue<DatagramPacket> requisicoes = new LinkedList<>();
    static Queue<DatagramPacket> disco = new LinkedList<>();
    static Queue<DatagramPacket> processamento = new LinkedList<>();
    static Map<BigInteger, String> map = new HashMap();
    static DatagramSocket serverSocket;
    static byte[] receiveData = new byte[1400];
    static byte[] sendData = new byte[1400];
    
    public static void main(String args[]) throws SocketException, IOException{
        Properties p = new Properties();
        FileInputStream inputStream = new FileInputStream("src/propriedades.properties");
        p.load(inputStream);
        serverSocket = new DatagramSocket(Integer.parseInt(p.getProperty("porta_server")));
        getLog();
        System.out.println("Numero de requisições recuperadas>> "+requisicoes.size());
        processamento();
        while(true){
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            
            //Thread para colocar em uma fila de requisições
            Thread t = new Thread();
            requisicoes.offer(receivePacket);
            System.out.println("Requisição adicionada, quantidade atual >> "+requisicoes.size());
            
            //Thread consumindo da fila de requisições e logando em disco
            disco.add(requisicoes.peek());
            //setLog(receivePacket);
            
            //Thread para processamento
            processamento.add(requisicoes.poll()); 
            processamento();
        }
    }
          
    //Recebe pacotes do cliente
    static DatagramPacket receive() throws IOException{
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        return receivePacket;
    }
    
    //Envia pacotes para o cliente
    static void send(DatagramPacket receivePacket, String sentence) throws IOException{
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        sendData = sentence.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);
    }

    //Pego os dados da fila de 'processamento' e envio as respostas
    static void processamento() throws IOException{
        while(true){
            DatagramPacket receivePacket = requisicoes.poll();
            if(receivePacket != null){
                System.out.println("Requisição atendida, quantidade atual>> "+requisicoes.size());
            }else{
                System.out.println("Não há mais requisições>> "+requisicoes.size());
                break;
            }
            String request = new String(receivePacket.getData(), 0, receivePacket.getLength());

            DatagramPacket sendPacket;
            String dados[] = request.split(" ");
            switch( dados[0] ){
              case "insert":
                  BigInteger chave = new BigInteger(dados[1]);
                  String valor = dados[2];
                  insert(chave, valor);
                  InetAddress IPAddress = receivePacket.getAddress();
                  int port = receivePacket.getPort();
                  sendData = "inserido com sucesso!!".getBytes();
                  sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
                  break;
              case "delete":
                  remove(new BigInteger(dados[1]));
                  IPAddress = receivePacket.getAddress();
                  port = receivePacket.getPort();
                  sendData = "Removido com sucesso!!".getBytes();
                  sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
                  break;
              case "update":
                  update(new BigInteger(dados[1]), dados[2]);
                  IPAddress = receivePacket.getAddress();
                  port = receivePacket.getPort();
                  sendData = "Atualizado com sucesso!!".getBytes();
                  sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
                  break;
              case "select":
                  String retorno = select(new BigInteger(dados[1]));
                  IPAddress = receivePacket.getAddress();
                  port = receivePacket.getPort();
                  sendData = retorno.getBytes();
                  sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
                  break;
               case "list":
                  IPAddress = receivePacket.getAddress();
                  port = receivePacket.getPort();
                  sendData = list().getBytes();
                  sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
                  break;
              default:
                  IPAddress = receivePacket.getAddress();
                  port = receivePacket.getPort();
                  sendData = "Comando não reconhecido".getBytes();
                  sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                  serverSocket.send(sendPacket);
                  break;
              }
        }
    }

    //Salva log no disco
    static void setLog(DatagramPacket receivePacket) throws IOException{
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        String sentence = new String( receivePacket.getData(), 0, receivePacket.getLength() );
        arquivo.escrever(IPAddress+" "+port+" "+sentence);
    }
    
    //Recupera log do disco
    static void getLog() throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new FileReader("src/log.txt"));
        while(true){
            String linha = bufferedReader.readLine();
            if(linha != null){
                String dados[] = linha.split(" ");
                InetAddress IPAddress = InetAddress.getByName(dados[0].replace("/", ""));
                int port = Integer.parseInt(dados[1]);
                String sentence = dados[2];
                sendData = sentence.getBytes();
                DatagramPacket dp = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                requisicoes.offer(dp);
            }else{
                break;
            }
        }
        bufferedReader.close();
    }
    
    //Insere objeto no HashMap
    static void insert(BigInteger bigInteger, String s){
        map.put(bigInteger, s);
        System.out.println("Valor inserido no HashMap>> "+map.get(bigInteger));
        System.out.println("Tamanho do HashMap>> "+map.size());
    }

    //Atualiza objeto no HashMap
    static void update(BigInteger bigInteger, String s){
        Set<BigInteger> set = map.keySet();
        for(BigInteger chave: set){
            if(chave.compareTo(bigInteger)==1){
                map.put(chave,s);
            }
        }
    }
    
    //Remove objeto no HashMap
    static void remove(BigInteger bigInteger){
        Set<BigInteger> set = map.keySet();
        for(BigInteger chave: set){
            if(chave.compareTo(bigInteger)==1){
                map.remove(chave);
            }
        }
    }

    //Seleciona objeto do HashMap
    static String select(BigInteger bigInteger){
        Set<BigInteger> set = map.keySet();
        String retorno = "";
        for(BigInteger chave: set){
            if(chave.compareTo(bigInteger)==1){
                retorno = map.get(chave) +"\n";
            }
        }
        System.out.println("Lista de valores do HashMap>>\n"+retorno);
        return retorno;
    }

    //Lista objetos do HashMap
    static String list(){
        Set<BigInteger> set = map.keySet();
        String retorno = "";
        for(BigInteger chave: set){
            if(chave != null){
                retorno += "- "+map.get(chave) +"\n";
            }
        }
        System.out.println("Lista de valores do HashMap>>\n"+retorno);
        return retorno;
    }
}