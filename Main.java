import java.util.Scanner;
import java.util.Vector;

public class Main{
  
  public static void main(String args[]){
    
    Archivo archivoRegistro = new Archivo();

      Scanner scanner = new Scanner(System.in);
      /*
      System.out.print("Cuantas veces desea correr la simulacion?: ");
      String s = scanner.nextLine();
      int nSimulaciones = Integer.parseInt(s);
      
      System.out.print("Cuanto tiempo quiere que dure cada simulacion?: ");
      s = scanner.nextLine();
      int relojMax = Integer.parseInt(s);
      
      System.out.print("Desea correr las simulaciones en modo lento? s/n: ");
      s = scanner.nextLine();
      boolean modoLento = (s == "s");
      

      Simulacion simulacion = new Simulacion(relojMax, modoLento, archivoRegistro);
    */
    Simulacion simulacion = new Simulacion(2000, false, archivoRegistro);
    int nSimulaciones = 10;

    Vector<Estadistica> estadisticas = new Vector<Estadistica>();
    
    for (int i = 0; i < nSimulaciones; i++) {

      System.out.println("Simulacion #"+(i+1));
      archivoRegistro.agregarSimulacion("Simulacion #"+(i+1));

      Estadistica e = simulacion.correrSimulacion();
      estadisticas.add(e);

      archivoRegistro.guardarSimulacion(i+1);


      System.out.println("Estadisticas de la simulacion #"+(i+1));
      archivoRegistro.agregarEstadistica("Estadisticas de la simulacion #"+(i+1));

      e.imprimirEstadistica(archivoRegistro);
      
      System.out.println("");
      archivoRegistro.agregarEstadistica("");
    }

    Estadistica estadisticaPromedio = estadisticas.elementAt(0).calcularPromedioEstadisticas(estadisticas);
    System.out.println("Estadisticas generales");
    archivoRegistro.agregarEstadistica("Estadisticas generales");
    estadisticaPromedio.imprimirEstadistica(archivoRegistro);

    archivoRegistro.guardarEstadisticas();
  }
  
}
