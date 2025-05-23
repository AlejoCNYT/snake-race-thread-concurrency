### Escuela Colombiana de Ingeniería
## Arquitecturas de Software - ARSW

#### Laboratorio - Programación concurrente, condiciones de carrera, esquemas de sincronización, colecciones sincronizadas y concurrentes.

Ejercicio inividual o en parejas.

### Part I


Parte I – Antes de terminar la clase.

Control de hilos con [wait/notify.](http://howtodoinjava.com/core-java/multi-threading/how-to-work-with-wait-notify-and-notifyall-in-java/)

1.  Descargue el proyecto
    [*PrimeFinder*](https://github.com/ARSW-ECI/wait-notify-excercise).
    Este es un programa que calcula números primos entre 0 y M
    (Control.MAXVALUE), concurrentemente, distribuyendo la búsqueda de
    los mismos entre n (Control.NTHREADS) hilos independientes.

2.  Se necesita modificar la aplicación de manera que cada t
    milisegundos de ejecución de los threads, se detengan todos los
    hilos y se muestre el número de primos encontrados hasta el momento.
    Luego, se debe esperar a que el usuario presione ENTER para reanudar
    la ejecución de los mismos. Utilice los mecanismos de sincronización
    provistos por el lenguaje (wait y notify, notifyAll).

Tenga en cuenta:

-   La construcción synchronized se utiliza para obtener acceso exclusivo a un objeto.

-   La instrucción A.wait() ejecutada en un hilo B pone en modo suspendido al hilo B (independientemente de qué objeto 'A' sea usado como 'lock'). Para reanudarlo, otro hilo activo puede reanudar a B haciendo 'notify()' al objeto usado como 'lock' (es decir, A).

-   La instrucción notify(), despierta el primer hilo que hizo wait()
    sobre el objeto.

-   La instrucción notifyAll(), despierta todos los hilos que estan
    esperando por el objeto (hicieron wait()sobre el objeto).


### Parte II

SnakeRace es una versión autónoma, multi-serpiente del famoso juego 'snake', basado en el proyecto de João Andrade -este ejercicio es un 'fork' del mismo-. En este juego:
	
- N serpientes funcionan de manera autónoma.
- No existe el concepto de colisión entre las mismas. La única forma de que mueran es estrellándose contra un muro.
- Hay ratones distribuídos a lo largo del juego. Como en el juego clásico, cada vez que una serpiente se come a un ratón, ésta crece.
- Existen unos puntos (flechas rojas) que teletransportan a las serpientes.
- Los rayos hacen que la serpiente aumente su velocidad.

![](img/sshot.png)

Ejercicio

1. Analice el código para entender cómo hace uso de hilos para crear un comportamiento autónomo de las N serpientes.

	- La lista de hilos generada `thread` pasará a contenerlos y, en cuyo init() son agregados al tablero.

2. De acuerdo con lo anterior, y con la lógica del juego, identifique y escriba claramente (archivo RESPUESTAS.txt):
    - **Posibles condiciones de carrera**.
      Acceso concurrente a estructuras compartidas como _static Cell[ ] food, static Cell[ ] barriers, static Cell[ ] jump_pads, static Cell[ ] turbo_boosts, static Cell[ ][ ] gameboard_. Las inconsistencias pueden 		generarse debido a que algunos métodos compartidos de _cell()_ no están sincronized, acualmente (de hecho, el único método que sí está siendo sincronized actualmente es el de _freeCell()_). De la misma manera, 	la clase _Snake_ tampoco está manejando posibles fallos que reflejen la posición de las serpientes. 
    - **Uso inadecuado de colecciones**, considerando su manejo concurrente (para esto, aumente la velocidad del juego y ejecútelo varias veces hasta que se genere un error).
      Particularmente, este bloque es crítico:

	```
  		for (int i = 0; i != SnakeApp.MAX_THREADS; i++)
  		{
    			for (Cell p : SnakeApp.getApp().snakes[i].getBody())
  			{
        
 			}
		}
  	```
	ya que LinkedList(Cell) puede retornar una estructura que no es segura para acceso concurrente. Además, una serpiente que actualiza su cuerpo mientras el tablero está pintando puede presentar 			`ConcurrentModificationException`. Dicho de otra manera, la sincronización o `CopyOnWriteArrayList` debería ser utilizado para no se accedidos inadecuadamente y reflejar errores en el _body_ de las _snakes_. 	De otra parte, múltiples _snakes_ podrían querer acceder a un mismo recurso y, si éstas no pueden ser accedidas concurrentemente, múltiples hilos podrían adquirir los beneficios de un mismo _power-up_.

  - Podría haber riesgo si dos hilos leen o modifican estructuras como `gameboard[x][y]` sin sincronización.
	![imagen](https://github.com/user-attachments/assets/489bc502-4d1b-44c2-9df3-8a5981947dc7)
      	![imagen](https://github.com/user-attachments/assets/b8facc64-21db-4c56-b022-41d32937c85b)

    - **Uso innecesario de esperas activas.**
      Si se realizan muchos `updates` en el `repaint()`, podria generar uso de recurso de CPU innecesario. Igualmente, al aumentar la velocidad pueden ocurrir errores relacionados a la lentitud del juego 		        `Thread.sleep()` y, generar errores como `ConcurrentModificationException`. También, en caso que las serpientes tengan una espera activa por la liberación de cada celda (`while(!cell.isFree()) {}`), genera 		_busy-waiting_. Además, el método `freeCell()` utiliza una notificación a todos lo hilos sin que se complemente con la contraparte de _wait_, lo que lo convierte en un mecanismo ineficiente.
      
3. Identifique las regiones críticas asociadas a las condiciones de carrera, y haga algo para eliminarlas. Tenga en cuenta que se debe sincronizar estríctamente LO NECESARIO. En su documento de respuestas indique, la solución realizada para cada ítem del punto 2. Igualmente tenga en cuenta que en los siguientes puntos NO se deben agregar más posibles condiciones de carrera.

	- 

4. Como se puede observar, el juego está incompleto. Haga los ajustes necesarios para que a través de botones en la interfaz se pueda Iniciar/Pausar/Reanudar el juego: iniciar el juego no se ha iniciado aún, suspender el juego si está en ejecución, reactivar el juego si está suspendido. Para esto tenga en cuenta:
    * Al pausar (suspender) el juego, en alguna parte de la interfaz (agregue los componentes que desee) se debe mostrar:
        - La serpiente viva más larga.
        - La peor serpiente: la que primero murió.
    
        Recuerde que la suspensión de las serpientes NO es instantánea, y que se debe garantizar que se muestre información consistente.
    

