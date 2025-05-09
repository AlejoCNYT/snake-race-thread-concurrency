package snakepackage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import enums.GridSize;
import java.io.InputStream;

public class Board extends JLabel implements Observer
		{
	public static final Object collisionLock = new Object();
	private static final long serialVersionUID = 1L;
	public static final int NR_BARRIERS = 5;
	public static final int NR_JUMP_PADS = 2;
	public static final int NR_TURBO_BOOSTS = 2;
	public static final int NR_FOOD = 5;
	static Cell[] food = new Cell[NR_FOOD];
	static Cell[] barriers = new Cell[NR_BARRIERS];
	static Cell[] jump_pads = new Cell[NR_JUMP_PADS];
	static Cell[] turbo_boosts = new Cell[NR_TURBO_BOOSTS];
	static int[] result = new int[SnakeApp.MAX_THREADS];
	Random random = new Random();
	static Cell[][] gameboard = new Cell[GridSize.GRID_WIDTH][GridSize.GRID_HEIGHT];
	private static final Object foodLock = new Object();
	static final Object barriersLock = new Object();
	private static final Object jumpPadsLock = new Object();
	private static final Object turboBoostsLock = new Object();

	@SuppressWarnings("unused")
	public Board() {
		if ((NR_BARRIERS + NR_JUMP_PADS + NR_FOOD + NR_TURBO_BOOSTS) > GridSize.GRID_HEIGHT
				* GridSize.GRID_WIDTH)
			throw new IllegalArgumentException(); 
		GenerateBoard();
		GenerateFood();
		GenerateBarriers();
		GenerateJumpPads();
		GenerateTurboBoosts();
	}

	private void GenerateTurboBoosts() {
		for (int i = 0; i != NR_TURBO_BOOSTS; i++) {
			Cell tmp = gameboard[random.nextInt(GridSize.GRID_WIDTH)][random
					.nextInt(GridSize.GRID_HEIGHT)];
			if (!tmp.hasElements()) {
				turbo_boosts[i] = tmp;
				turbo_boosts[i].setTurbo_boost(true);
			} else {
				i--;
			}
		}
	}

	private void GenerateJumpPads() {
		for (int i = 0; i != NR_JUMP_PADS; i++) {
			Cell tmp = gameboard[random.nextInt(GridSize.GRID_WIDTH)][random
					.nextInt(GridSize.GRID_HEIGHT)];
			if (!tmp.hasElements()) {
				jump_pads[i] = tmp;
				jump_pads[i].setJump_pad(true);
			} else {
				i--;
			}
		}
	}

	private void GenerateBoard() {
		for (int i = 0; i != GridSize.GRID_WIDTH; i++) {
			for (int j = 0; j != GridSize.GRID_HEIGHT; j++) {
				gameboard[i][j] = new Cell(i, j);
				//System.out.println(" ins " + gameboard[i][j]);
			}
		}

	}

	private void GenerateBarriers() {
		for (int i = 0; i != NR_BARRIERS; i++) {
			Cell tmp = gameboard[random.nextInt(GridSize.GRID_WIDTH)][random
					.nextInt(GridSize.GRID_HEIGHT)];
			if (!tmp.hasElements()) {
				barriers[i] = tmp;
				barriers[i].setBarrier(true);
			} else {
				i--;
			}
		}
	}

	private void GenerateFood() {
		for (int i = 0; i != NR_FOOD; i++) {
			Cell tmp = gameboard[random.nextInt(GridSize.GRID_WIDTH)][random
					.nextInt(GridSize.GRID_HEIGHT)];
			if (!tmp.hasElements()) {
				food[i] = tmp;
				food[i].setFood(true);
			} else {
				i--;
			}
		}
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		drawGrid(g);
		drawSnake(g);
		drawFood(g);
		drawBarriers(g);
		drawJumpPads(g);
		drawTurboBoosts(g);
	}

	private void drawTurboBoosts(Graphics g) {
		Image light = null;
                InputStream resource=ClassLoader.getSystemResourceAsStream("Img/lightning.png");
		try {
			light = ImageIO.read(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Cell c : turbo_boosts) {
			g.drawImage(light, c.getX() * GridSize.WIDTH_BOX, c.getY()
					* GridSize.HEIGH_BOX, this);
		}
	}

	private void drawJumpPads(Graphics g) {
		Image jump = null;
                
                InputStream resource=ClassLoader.getSystemResourceAsStream("Img/up.png");

                try {
			jump = ImageIO.read(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Cell c : jump_pads) {
			g.drawImage(jump, c.getX() * GridSize.WIDTH_BOX, c.getY()
					* GridSize.HEIGH_BOX, this);
		}
	}

	private void drawBarriers(Graphics g) {

		Image firewall = null;
                
                InputStream resource=ClassLoader.getSystemResourceAsStream("Img/firewall.png");

                try {
			firewall = ImageIO.read(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Cell c : barriers) {
			g.drawImage(firewall, c.getX() * GridSize.WIDTH_BOX, c.getY()
					* GridSize.HEIGH_BOX, this);
			// g.fillRect(c.getX() * Grid_Size.WIDTH_BOX, c.getY()
			// * Grid_Size.HEIGH_BOX, Grid_Size.WIDTH_BOX,
			// Grid_Size.HEIGH_BOX);
		}
	}

	private void drawFood(Graphics g) {
		Image mouse = null;
                InputStream resource=ClassLoader.getSystemResourceAsStream("Img/mouse.png");
		
		try {
			mouse = ImageIO.read(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (Cell c : food){
		g.drawImage(mouse, c.getX() * GridSize.WIDTH_BOX, c.getY()
				* GridSize.HEIGH_BOX, this);
		}
	}

	private void drawSnake(Graphics g) {
		for (int i = 0; i != SnakeApp.MAX_THREADS; i++) {
			for (Cell p : SnakeApp.getApp().snakes[i].getBody()) {
				if (p.equals(SnakeApp.getApp().snakes[i].getBody().peekFirst())) {
					g.setColor(new Color(050+(i*10), 205, 150));
					g.fillRect(p.getX() * GridSize.WIDTH_BOX, p.getY()
							* GridSize.HEIGH_BOX, GridSize.WIDTH_BOX,
							GridSize.HEIGH_BOX);
				} else {
					if (SnakeApp.getApp().snakes[i].isSelected()) {
						g.setColor(new Color(032, 178, 170));
					} else
						g.setColor(new Color(034, 139, 034));
					g.fillRect(p.getX() * GridSize.WIDTH_BOX, p.getY()
							* GridSize.HEIGH_BOX, GridSize.WIDTH_BOX,
							GridSize.HEIGH_BOX);
				}
			}
		}

	}

	private void drawGrid(Graphics g) {
		g.setColor(new Color(255, 250, 250));
		g.fillRect(0, 0, GridSize.GRID_WIDTH * GridSize.WIDTH_BOX,
				GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX);
		g.setColor(new Color(135, 135, 135));
		g.drawRect(0, 0, GridSize.GRID_WIDTH * GridSize.WIDTH_BOX,
				GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX);
		for (int i = GridSize.WIDTH_BOX; i < GridSize.GRID_WIDTH
				* GridSize.WIDTH_BOX; i += GridSize.WIDTH_BOX) {
			g.drawLine(i, 0, i, GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX);
		}
		for (int i = GridSize.HEIGH_BOX; i < GridSize.GRID_HEIGHT
				* GridSize.HEIGH_BOX; i += GridSize.HEIGH_BOX) {
			g.drawLine(0, i, GridSize.GRID_WIDTH * GridSize.WIDTH_BOX, i);
		}

	}

	public static void removeFood(Cell foodCell) {
		synchronized(foodLock) {
			for(int i=0; i<NR_FOOD; i++) {
				if(food[i].equals(foodCell)) {
					food[i] = null;
					break;
				}
			}
		}
	}

	public static void removeBarrier(Cell barrierCell) {
		synchronized(barriersLock) {
			for(int i = 0; i < NR_BARRIERS; i++) {
				if(barriers[i] != null && barriers[i].equals(barrierCell)) {
					barriers[i] = null;
					break;
				}
			}
		}
	}

	public static boolean containsBarrier(Cell cell) {
		synchronized(barriersLock) {
			for(int i = 0; i < NR_BARRIERS; i++) {
				if(barriers[i] != null && barriers[i].equals(cell)) {
					return true;
				}
			}
			return false;
		}
	}

	public static void removeJumpPad(Cell jumpPadCell) {
		synchronized(jumpPadsLock) {
			for(int i = 0; i < NR_JUMP_PADS; i++) {
				if(jump_pads[i] != null && jump_pads[i].equals(jumpPadCell)) {
					jump_pads[i] = null;
					break;
				}
			}
		}
	}

	public static Cell getAvailableJumpPad() {
		synchronized(jumpPadsLock) {
			for(int i = 0; i < NR_JUMP_PADS; i++) {
				if(jump_pads[i] != null) {
					return jump_pads[i];
				}
			}
			return null;
		}
	}

	public static void removeTurboBoost(Cell turboCell) {
		synchronized(turboBoostsLock) {
			for(int i = 0; i < NR_TURBO_BOOSTS; i++) {
				if(turbo_boosts[i] != null && turbo_boosts[i].equals(turboCell)) {
					turbo_boosts[i] = null;
					break;
				}
			}
		}
	}

	public static boolean hasTurboBoostAt(Cell cell) {
		synchronized(turboBoostsLock) {
			for(int i = 0; i < NR_TURBO_BOOSTS; i++) {
				if(turbo_boosts[i] != null && turbo_boosts[i].equals(cell)) {
					return true;
				}
			}
			return false;
		}
	}

			public static boolean tryMoveSnake(Snake snake, Cell newCell) {
				synchronized(collisionLock) {
					// Verificar si la celda está disponible
					if(newCell.isFull()) {
						return false; // Movimiento inválido
					}

					// Reservar la celda
					newCell.setFull(true);
					snake.getHead().setFull(false);

					// Actualizar cuerpo de la serpiente
					synchronized(snake.getBodyLock()) {
						snake.getBody().push(newCell);
						if(snake.getGrowth() <= 0) {
							Cell tail = snake.getBody().removeLast();
							tail.setFull(false);
						}
					}
					return true;
				}
			}

			// Método para verificar disponibilidad de celda
			public static boolean isCellAvailable(int x, int y) {
				synchronized(collisionLock) {
					if(x < 0 || x >= GridSize.GRID_WIDTH ||
							y < 0 || y >= GridSize.GRID_HEIGHT) {
						return false;
					}
					return !gameboard[x][y].isFull();
				}
			}

			// Método para crecimiento seguro
			public static void growSnake(Snake snake) {
				synchronized(snake.getBodyLock()) {
					Cell newHead = snake.calculateNextCell();
					if(newHead != null && !newHead.isFull()) {
						newHead.setFull(true);
						snake.getBody().push(newHead);
						snake.growing--;
					}
				}
			}

	@Override
	public void update(Observable arg0, Object arg1) {
		repaint();
	}

			// Método para actualización atómica de múltiples celdas
			public static void updateCells(Cell... cells) {
				synchronized(collisionLock) {
					// Ordenar celdas para prevenir deadlocks
					Arrays.sort(cells, Comparator.comparingInt(Cell::hashCode));

					// Bloquear todas las celdas en orden
					for(Cell cell : cells) {
						synchronized(cell) {
							// Realizar operaciones
						}
					}
				}
			}

			// Ejemplo de uso cuando una serpiente crece
			public static void snakeGrow(Snake snake) {
				Cell head = snake.getHead();
				Cell newHead = snake.calculateNextCell();

				updateCells(head, newHead);

				synchronized(snake.getBodyLock()) {
					snake.getBody().push(newHead);
					newHead.setFull(true);
				}
			}

}
