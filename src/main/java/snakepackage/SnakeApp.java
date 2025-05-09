package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;

import static snakepackage.Board.result;

/**
 * @author jd-
 *
 */
public class SnakeApp
{
    private static final Object gameStateLock = new Object();
    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    static Snake[] snakes = new Snake[MAX_THREADS];
    private static final Object snakesLock = new Object();
    private static final Cell[] spawn =
            {
        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2,
        3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
        new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
        new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
        new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
        GridSize.GRID_HEIGHT - 2)
            };
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];

    public SnakeApp()
    {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();

        frame.add(board,BorderLayout.CENTER);

        JPanel actionsBPabel=new JPanel();
        actionsBPabel.setLayout(new FlowLayout());
        actionsBPabel.add(new JButton("Action "));
        frame.add(actionsBPabel,BorderLayout.SOUTH);

    }

    public static void main(String[] args)
    {
        app = new SnakeApp();
        app.init();
    }

    private void init()
    {

        for (int i = 0; i != MAX_THREADS; i++)
        {

            snakes[i] = new Snake(i + 1, spawn[i], i + 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
            thread[i].start();
        }

        frame.setVisible(true);

        while (true)
        {
            int x = 0;
            for (int i = 0; i != MAX_THREADS; i++)
            {
                if (snakes[i].isSnakeEnd() == true)
                {
                    x++;
                }
            }
            if (x == MAX_THREADS)
            {
                break;
            }
        }

        System.out.println("Thread (snake) status:");
        for (int i = 0; i != MAX_THREADS; i++)
        {
            System.out.println("["+i+"] :"+thread[i].getState());
        }

    }

    public static SnakeApp getApp()
    {
        return app;
    }

    public static Snake getSnake(int id) {
        synchronized(snakesLock) {
            return snakes[id];
        }
    }

    public static void updateSnakeStatus(int id, boolean status) {
        synchronized(snakesLock) {
            snakes[id].setSnakeEnd(status);
        }
    }

    public static boolean checkGameOver() {
        synchronized(gameStateLock) {
            // 1. Verificar si todas las serpientes han terminado
            boolean allDead = true;
            for(Snake snake : snakes) {
                if(snake != null && !snake.isSnakeEnd()) {
                    allDead = false;
                    break;
                }
            }
            if(allDead) return true;

            // 2. Verificar colisiones entre serpientes
            for(int i = 0; i < snakes.length; i++) {
                if(snakes[i] == null || snakes[i].isSnakeEnd()) continue;

                Cell head = snakes[i].getHead();
                if(head == null) continue;

                // 2.1. Colisión con otras serpientes
                for(int j = 0; j < snakes.length; j++) {
                    if(i == j || snakes[j] == null) continue;

                    LinkedList<Cell> body = snakes[j].getBody();
                    for(Cell cell : body) {
                        if(cell != null && cell.equals(head)) {
                            // Colisión con cuerpo de otra serpiente
                            snakes[i].setSnakeEnd(true);
                            if(i != j && !snakes[j].getHead().equals(head)) {
                                // Solo cuenta como muerte si no es colisión de cabezas
                                result[i] = body.size(); // Guardar puntuación
                            }
                        }
                    }
                }

                // 2.2. Colisión con bordes
                if(head.getX() < 0 || head.getX() >= GridSize.GRID_WIDTH ||
                        head.getY() < 0 || head.getY() >= GridSize.GRID_HEIGHT) {
                    snakes[i].setSnakeEnd(true);
                    result[i] = snakes[i].getBody().size();
                }

                // 2.3. Colisión con barreras
                synchronized(Board.barriersLock) {
                    for(Cell barrier : Board.barriers) {
                        if(barrier != null && barrier.equals(head)) {
                            snakes[i].setSnakeEnd(true);
                            result[i] = snakes[i].getBody().size();
                        }
                    }
                }
            }

            // 3. Verificar si queda solo una serpiente viva
            int aliveCount = 0;
            Snake lastAlive = null;
            for(Snake snake : snakes) {
                if(snake != null && !snake.isSnakeEnd()) {
                    aliveCount++;
                    lastAlive = snake;
                }
            }

            if(aliveCount <= 1) {
                if(lastAlive != null) {
                    result[lastAlive.getIdt() - 1] = lastAlive.getBody().size() * 2; // Bonus por ganar
                }
                return true;
            }

            return false;
        }
    }
}
