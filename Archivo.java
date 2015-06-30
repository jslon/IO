import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class Archivo{

	private List<String> estadisticas, simulaciones;
	private String nombreEstadisticas, hora;

	public Archivo(){
		this.hora = String.valueOf(System.currentTimeMillis());

		File file = new File("registros/"+this.hora);
		if(!file.exists()){
			file.mkdir();
		}

		this.nombreEstadisticas = "registros/"+this.hora+"/estadisticas.txt";
		this.estadisticas = new ArrayList<String>();
		this.simulaciones = new ArrayList<String>();

	}

	public void agregarEstadistica(String p){
		estadisticas.add(p);
	}

	public void agregarSimulacion(String p){
		simulaciones.add(p);
	}

	public void guardarSimulacion(int s){
		String sim = "registros/"+this.hora+"/simulacion"+String.valueOf(s)+".txt";
		this.guardar(simulaciones, sim);
		simulaciones.clear();
	}

	public void guardarEstadisticas(){
		this.guardar(estadisticas, nombreEstadisticas);
	}

	public void guardar(List<String> l, String nombreArch){

		Writer fileWriter = null;
		BufferedWriter bufferedWriter = null;

		try {
			File file = new File(nombreArch);
			if(!file.exists()){
				file.createNewFile();
			}

			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);

			for (String line : l) {
				line += System.getProperty("line.separator");
				bufferedWriter.write(line);
			}

		} catch (IOException e) {
			System.err.println("Error writing the file : ");
			e.printStackTrace();
		} finally {

			if (bufferedWriter != null && fileWriter != null) {
				try {
					bufferedWriter.close();
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}