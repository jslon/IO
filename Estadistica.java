import java.text.NumberFormat;
import java.text.DecimalFormat;

import java.util.Vector;

public class Estadistica{
  
  private NumberFormat f; 
  private double tamPromCola, tiempoPromPerm, tiempoPromTrans, tiempoPromServ, eficiencia;
  
  public Estadistica(double tamPromCola, double tiempoPromPerm, double tiempoPromTrans, double tiempoPromServ, double eficiencia){
    f = new DecimalFormat("#0.00");
    this.tiempoPromPerm = tiempoPromPerm;
    this.tiempoPromTrans = tiempoPromTrans;
    this.tiempoPromServ = tiempoPromServ;
    this.eficiencia = eficiencia;
    this.tamPromCola = tamPromCola;
  }
  
  public void imprimirEstadistica(){
    System.out.println("Tamano promedio de la cola A: "+ f.format(this.tamPromCola));
    System.out.println("Tiempo promedio de permanencia de mensajes en el sistema: " + f.format(this.tiempoPromPerm));
    System.out.println("Tiempo promedio de transmision: "+f.format(this.tiempoPromTrans));
    System.out.println("Tiempo promedio de servicio: "+f.format(this.tiempoPromServ));
    System.out.println("Eficiencia: "+ f.format(eficiencia));
    
  }

  public Estadistica calcularPromedioEstadisticas(Vector<Estadistica> v){
    int cantSim = v.size();

    for(int i = 0; i<cantSim;i++){}

    double tamPromCola = 0.0;
    double tiempoPromPerm = 0.0;
    double tiempoPromTrans = 0.0;
    double tiempoPromServ = 0.0;
    double eficiencia = 0.0;

    for(int i = 0; i<cantSim;i++){
      Estadistica e = v.elementAt(i);
      tamPromCola     += e.tamPromCola;
      tiempoPromPerm  += e.tiempoPromPerm;
      tiempoPromTrans += e.tiempoPromTrans;
      tiempoPromServ  += e.tiempoPromServ;
      eficiencia      += e.eficiencia;
    }
    tamPromCola     /= cantSim;
    tiempoPromPerm  /= cantSim;
    tiempoPromTrans /= cantSim;
    tiempoPromServ  /= cantSim;
    eficiencia      /= cantSim;

    return new Estadistica(tamPromCola, tiempoPromPerm, tiempoPromTrans, tiempoPromServ, eficiencia);
  }

}