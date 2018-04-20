
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Arquivo {
        public void escrever(String linha) throws IOException{
//            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/log.txt"));
//            bufferedWriter.append(linha+"\n");
//            bufferedWriter.close();
              FileOutputStream file = new FileOutputStream(new File("C:/Users/pbessa/Documents/NetBeansProjects/sistemas-distribuidos/src/log.txt"),true);
              linha = linha + "\n";
              byte[] contentInBytes = linha.getBytes();
              file.write(contentInBytes);
              file.flush();
              file.close();
        }
        
        public void ler() throws FileNotFoundException, IOException{
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/log.txt"));
            String linha = "";
            while(true){
                if(linha != null){
                    linha = bufferedReader.readLine();
                    System.out.println(linha);
                }else{
                    break;
                }
            }
            bufferedReader.close();
        }
}
