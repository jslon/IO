import java.util.Scanner;
import java.util.Vector;

public class Main{
  
  public static void main(String args[]){
    
    
    /** modo largo que pide la profe
      * 
      Scanner scanner = new Scanner(System.in);
      
      System.out.print("Cuantas veces desea correr la simulacion?: ");
      String s = scanner.nextLine();
      int nSimulaciones = Integer.parseInt(s);
      
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
    
    Simulacion simulacion = new Simulacion(12, 60, false);
    int nSimulaciones = 2;
    Vector<Estadistica> estadisticas = new Vector<Estadistica>();
    
    for (int i = 0; i < nSimulaciones; i++) {
      System.out.println("Simulacion #"+(i+1));
      Estadistica e = simulacion.correrSimulacion();
      estadisticas.add(e);

      System.out.println("Estadisticas de la simulacion #"+(i+1));
      e.imprimirEstadistica();
      System.out.println("");
    }

    Estadistica estadisticaPromedio = estadisticas.elementAt(0).calcularPromedioEstadisticas(estadisticas);
    System.out.println("Estadisticas generales");
    estadisticaPromedio.imprimirEstadistica();
  }
  
}
