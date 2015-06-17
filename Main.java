import java.util.Scanner;

public class Main{

 public static void main(String args[]){

  
  /** modo largo que pide la profe

  Scanner scanner = new Scanner(System.in);
  
  System.out.print("Cuantas veces desea correr la simulacion?: ");
  String s = scanner.nextLine();
  int nSimulacion = Integer.parseInt(s);

  System.out.print("Cuanto tiempo quiere que dure cada simulacion?: ");
  s = scanner.nextLine();
  int timer = Integer.parseInt(s);

  System.out.print("Cuanto tiempo quiere que dure el timer?: ");
  s = scanner.nextLine();
  int relojMax = Integer.parseInt(s);

  System.out.print("Desea correr las simulaciones en modo lento? s/n: ");
  s = scanner.nextLine();
  boolean modoLento = (s == "s");

  Simulacion simulacion = new Simulacion(timer, relojMax, modoLento);
  */

  Simulacion simulacion = new Simulacion(6, 100, false);

  for (int i = 0; i < 1; i++) {
   simulacion.correrSimulacion();
   
  }

  //for()





  //Simulacion s = new Simulacion(false);
 
 }

}
